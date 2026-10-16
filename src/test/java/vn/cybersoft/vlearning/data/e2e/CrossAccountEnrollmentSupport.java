package vn.cybersoft.vlearning.data.e2e;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import vn.cybersoft.vlearning.data.account.AccountSupport;
import vn.cybersoft.vlearning.data.enroll.EnrollmentSupport;
import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.CourseCategoryPage.EnrollmentCourse;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;

/** Two immutable baselines and separately owned Account + Course attempts, shared by E2E-002/004. */
public final class CrossAccountEnrollmentSupport {
    private static final String TEST_ID = "E2E-002";
    private final WebDriver driver;
    private final String testId;
    private final Map<String, Student> students = new LinkedHashMap<>();
    private EnrollmentCourse selected;
    private String verifiedAccount;
    private boolean failureEvidenceAttempted;

    private static final class Student {
        private final String password;
        private Map<String, Integer> original;
        private EnrollmentCourse owned;

        private Student(String password) {
            this.password = password;
        }
    }

    public CrossAccountEnrollmentSupport(WebDriver driver, String accountA, String passwordA,
                                         String accountB, String passwordB) {
        this(driver, TEST_ID, accountA, passwordA, accountB, passwordB);
    }

    public CrossAccountEnrollmentSupport(WebDriver driver, String testId, String accountA, String passwordA,
                                         String accountB, String passwordB) {
        if (accountA.equals(accountB)) throw new IllegalArgumentException(testId + " requires two different accounts.");
        this.driver = driver;
        this.testId = testId;
        students.put(accountA, new Student(passwordA));
        students.put(accountB, new Student(passwordB));
    }

    /** Normal application logout/Login; a loaded identity, not an authenticated header, verifies the owner. */
    public AccountPage loginAs(String account) {
        return loginAs(account, null);
    }

    private AccountPage loginAs(String account, List<Throwable> cleanupFailures) {
        Student student = student(account);
        HomePage home = new HomePage(driver);
        boolean wasVerified = verifiedAccount != null;
        verifiedAccount = null;
        if (home.isAuthenticated()) {
            if (!home.logoutIfAuthenticated() || home.isAuthenticated()) {
                throw new AssertionError("Application logout did not end the previous session; refusing Login/mutation.");
            }
            if (cleanupFailures == null) record("Application logout", "Previous session ended before Login as " + account);
            else recordCleanup(cleanupFailures, "Application logout", "Previous session ended before Login as " + account);
        } else if (wasVerified && cleanupFailures == null) {
            throw new AssertionError("The previously verified session ended before the required application logout.");
        }
        AccountPage page = AccountSupport.loginAndOpenAccount(driver, account, student.password);
        String actual = page.getPersonalAccount();
        if (cleanupFailures == null) record("Loaded Account identity", "Expected=" + account + "; actual=" + actual);
        else recordCleanup(cleanupFailures, "Loaded Account identity", "Expected=" + account + "; actual=" + actual);
        if (!account.equals(actual)) throw new AssertionError("Loaded Account does not match the intended owner.");
        verifiedAccount = account;
        return page;
    }

    public Map<String, Integer> captureBaseline(String account, AccountPage page) {
        Student student = student(account);
        if (student.original != null || !account.equals(verifiedAccount)) {
            throw new IllegalStateException("Capture each original baseline once, under its verified owner.");
        }
        student.original = Map.copyOf(page.captureMyCourseCounts(account));
        record(account + " original refreshed My Courses", new TreeMap<>(student.original));
        return student.original;
    }

    /** Read-only candidate search. A newly discovered existing enrollment is excluded, never borrowed. */
    public EnrollmentCourse selectCourseAbsentFromBothAccounts() {
        if (selected != null || students.values().stream().anyMatch(student -> student.original == null)) {
            throw new IllegalStateException("Capture both baselines before selecting one shared Course.");
        }
        Set<String> excluded = new LinkedHashSet<>();
        students.values().forEach(student -> excluded.addAll(student.original.keySet()));
        List<EnrollmentCourse> examined = new ArrayList<>();
        List<String> unready = new ArrayList<>();
        HomePage home = new HomePage(driver);
        for (String category : home.getCourseCategoryUrls()) {
            while (true) {
                EnrollmentCourse candidate;
                try {
                    var available = home.goToCourseCategory(category)
                            .selectCourseForEnrollmentExcluding(excluded, List.copyOf(examined));
                    if (available.isEmpty()) break;
                    candidate = available.get();
                } catch (TimeoutException categoryNotReady) {
                    unready.add(category);
                    break; // Only read-only discovery advances to another observed frontend category.
                }
                examined.add(candidate);
                boolean absentFromBoth = true;
                for (String account : students.keySet()) {
                    AccountPage page = loginAs(account).openMyCourses(account).refreshMyCourses(account);
                    Map<String, Integer> current = captureCourses(account, page, "Candidate absence check: " + candidate);
                    excluded.addAll(current.keySet());
                    if (current.getOrDefault(candidate.courseName(), 0) != 0) absentFromBoth = false;
                }
                if (absentFromBoth) {
                    selected = candidate;
                    record("Captured shared frontend Course X", selected);
                    return selected;
                }
                record("Candidate excluded without mutation", candidate);
            }
        }
        throw new IllegalStateException(testId + " setup: no frontend Course absent from both accounts was available. "
                + "Original enrollments were untouched. Unready categories=" + unready);
    }

    /** Called immediately before each owner's single click; retained even if the response is lost. */
    public void markEnrollmentAttempt(String account, EnrollmentCourse course) {
        Student student = student(account);
        if (!account.equals(verifiedAccount) || selected == null || !selected.equals(course)
                || students.values().stream().anyMatch(value -> value.original == null)
                || student.original.containsKey(course.courseName()) || student.owned != null) {
            throw new IllegalStateException("Refusing an unverified, original, unselected or repeated enrollment ownership.");
        }
        student.owned = course;
    }

    public Map<String, Integer> expectedEnrolledCourses(String account) {
        Student student = student(account);
        if (student.owned == null) throw new IllegalStateException("This account has no owned enrollment attempt.");
        Map<String, Integer> expected = new LinkedHashMap<>(student.original);
        expected.put(student.owned.courseName(), 1);
        return Map.copyOf(expected);
    }

    public Map<String, Integer> captureCourses(String account, AccountPage page, String stage) {
        student(account);
        Map<String, Integer> current = page.captureMyCourseCounts(account);
        record(account + " / " + stage, new TreeMap<>(current));
        return current;
    }

    public void record(String stage, Object observation) {
        AccountSupport.recordAccountObservation(testId, redact(stage), redact(String.valueOf(observation)), null);
    }

    /** Call before an independent integrity check changes the failing account/page. */
    public void preserveFailureEvidence(Throwable primaryFailure) {
        if (!failureEvidenceAttempted && primaryFailure != null && driver instanceof TakesScreenshot screenshots) {
            failureEvidenceAttempted = true;
            try {
                LoginPage login = new LoginPage(driver);
                if (login.isLoginFormDisplayedNow() && !login.isPasswordMasked()) {
                    record("Failure screenshot omitted", "Login password field is visible and unmasked.");
                } else {
                    Allure.addAttachment(testId + " failure before session recovery", "image/png",
                            new ByteArrayInputStream(screenshots.getScreenshotAs(OutputType.BYTES)), ".png");
                }
            } catch (RuntimeException | AssertionError evidenceFailure) {
                primaryFailure.addSuppressed(failure("Failure evidence", evidenceFailure));
            }
        }
    }

    public void finish(Throwable primaryFailure) {
        List<Throwable> failures = new ArrayList<>();
        // Capture the business failure before recovery changes either account or page.
        preserveFailureEvidence(primaryFailure);
        for (var entry : students.entrySet()) {
            String account = entry.getKey();
            Student student = entry.getValue();
            if (student.owned == null) {
                recordCleanup(failures, account + " cleanup", "No owned enrollment attempt; no cancellation.");
                continue;
            }
            try {
                if (student.original == null || student.original.containsKey(student.owned.courseName())) {
                    throw new IllegalStateException("Refusing cleanup of an original or untracked Course.");
                }
                new HomePage(driver).open();
                // Always end any live session and explicitly log in as this owner, including when it was already active.
                AccountPage page = loginAs(account, failures).openMyCourses(account).refreshMyCourses(account);
                int count = page.countExactCourseItems(student.owned.courseName());
                recordCleanup(failures, account + " owned cleanup", student.owned + "; exact count before cleanup=" + count);
                try {
                    if (count > 1) throw new IllegalStateException("Ambiguous owned Course; refusing arbitrary cancellation.");
                    if (count == 1) {
                        // Owner was freshly verified. Reuse cancellation, collect its failure here, and preserve the outer primary.
                        EnrollmentSupport.finish(driver, testId + " owned cleanup / " + account, account,
                                student.owned.courseName(), true, null);
                    } else {
                        recordCleanup(failures, account + " owned cleanup", "Already absent; no cancellation repeated.");
                    }
                } catch (RuntimeException | AssertionError cancellationFailure) {
                    AssertionError safe = failure(account + " owned cancellation", cancellationFailure);
                    failures.add(safe);
                    recordCleanup(failures, account + " cancellation failed", safe);
                }
                Map<String, Integer> after = new HomePage(driver).goToAccountPage().openMyCourses(account)
                        .refreshMyCourses(account).captureMyCourseCounts(account);
                recordCleanup(failures, account + " final refreshed baseline", new TreeMap<>(after));
                if (!student.original.equals(after)) {
                    throw new AssertionError("Original Course baseline not restored for " + account
                            + ". Expected=" + new TreeMap<>(student.original) + "; actual=" + new TreeMap<>(after)
                            + ". Original enrollments will not be altered to hide this mismatch.");
                }
                recordCleanup(failures, account + " cleanup verified", "Owned Course absent; original identities/counts restored.");
            } catch (RuntimeException | AssertionError cleanupFailure) {
                AssertionError safe = failure(account + " cleanup/recovery", cleanupFailure);
                failures.add(safe);
                recordCleanup(failures, account + " cleanup failed", safe);
                // Continue with the other owner's independent logout/Login/identity check even after failure here.
            }
        }
        if (primaryFailure != null) {
            failures.forEach(primaryFailure::addSuppressed);
        } else if (!failures.isEmpty()) {
            AssertionError result = new AssertionError(testId + " scenario passed, but cleanup/integrity/evidence failed.");
            failures.forEach(result::addSuppressed);
            throw result;
        }
    }

    /** E2E-004: reuse owned cleanup, then independently check the account that must never be mutated. */
    public void finishAndVerifyReadOnlyAccount(String readOnlyAccount, Throwable primaryFailure) {
        Student readOnly = student(readOnlyAccount);
        if (readOnly.owned != null) {
            throw new IllegalStateException("The read-only account must have no owned enrollment or cancellation.");
        }
        List<Throwable> failures = new ArrayList<>();
        try {
            finish(primaryFailure);
        } catch (RuntimeException | AssertionError ownedCleanupFailure) {
            failures.add(failure("Owned cleanup", ownedCleanupFailure));
        }
        // Always attempt this independent check, including after A's recovery/cancellation failed.
        if (readOnly.original != null) {
            try {
                new HomePage(driver).open();
                AccountPage page = loginAs(readOnlyAccount, failures).openMyCourses(readOnlyAccount)
                        .refreshMyCourses(readOnlyAccount);
                Map<String, Integer> after = page.captureMyCourseCounts(readOnlyAccount);
                recordCleanup(failures, readOnlyAccount + " final read-only refreshed baseline", new TreeMap<>(after));
                if (!readOnly.original.equals(after)) {
                    throw new AssertionError("Read-only Account Course data changed: " + readOnlyAccount
                            + ". Expected=" + new TreeMap<>(readOnly.original) + "; actual=" + new TreeMap<>(after)
                            + ". No enrollment/cancellation will be used to conceal the mismatch.");
                }
            } catch (RuntimeException | AssertionError readOnlyFailure) {
                failures.add(failure("Independent read-only Account verification", readOnlyFailure));
            }
        } else {
            recordCleanup(failures, readOnlyAccount + " final integrity", "Original baseline was not captured; no mutation authorized.");
        }
        if (primaryFailure != null) {
            failures.forEach(primaryFailure::addSuppressed);
        } else if (!failures.isEmpty()) {
            AssertionError result = new AssertionError(testId + " scenario passed, but cleanup/integrity/evidence failed.");
            failures.forEach(result::addSuppressed);
            throw result;
        }
    }

    private void recordCleanup(List<Throwable> failures, String stage, Object observation) {
        try {
            record(stage, observation);
        } catch (RuntimeException | AssertionError evidenceFailure) {
            failures.add(failure(stage + " evidence", evidenceFailure));
        }
    }

    private Student student(String account) {
        Student student = students.get(account);
        if (student == null) throw new IllegalArgumentException("Unknown " + testId + " account.");
        return student;
    }

    private String redact(String text) {
        for (Student student : students.values()) {
            if (student.password != null && !student.password.isEmpty()) text = text.replace(student.password, "[REDACTED]");
        }
        return text;
    }

    /** Keep the primary message/stack and nested diagnostics without retaining secret-bearing raw causes. */
    public AssertionError failure(String stage, Throwable cause) {
        return safeFailure(stage, cause, Collections.newSetFromMap(new IdentityHashMap<>()));
    }

    private AssertionError safeFailure(String stage, Throwable cause, Set<Throwable> visited) {
        AssertionError safe = new AssertionError(redact(stage + ": " + cause.getClass().getSimpleName() + ": " + cause.getMessage()));
        safe.setStackTrace(cause.getStackTrace());
        if (visited.add(cause)) {
            if (cause.getCause() != null) safe.initCause(safeFailure("Cause", cause.getCause(), visited));
            for (Throwable suppressed : cause.getSuppressed()) safe.addSuppressed(safeFailure("Suppressed", suppressed, visited));
        }
        return safe;
    }
}
