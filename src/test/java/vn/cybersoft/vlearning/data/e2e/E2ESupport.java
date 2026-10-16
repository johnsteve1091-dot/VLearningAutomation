package vn.cybersoft.vlearning.data.e2e;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import vn.cybersoft.vlearning.data.account.AccountSupport;
import vn.cybersoft.vlearning.data.enroll.EnrollmentSupport;
import vn.cybersoft.vlearning.data.login.LoginSupport;
import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.CourseCategoryPage.EnrollmentCourse;
import vn.cybersoft.vlearning.pages.HomePage;

/** One generated student's read-only baseline, owned enrollment and failure-safe cleanup. */
public final class E2ESupport {
    private static final String TEST_ID = "E2E-001";
    private final WebDriver driver;
    private final String account;
    private final String password;
    private Map<String, Integer> original;
    private EnrollmentCourse selected;
    private boolean enrollmentAttempted;

    public E2ESupport(WebDriver driver, String account, String password) {
        this.driver = driver;
        this.account = account;
        this.password = password;
    }

    public Map<String, Integer> captureBaseline(AccountPage page) {
        if (original != null) throw new IllegalStateException("Capture the original Course baseline only once.");
        original = Map.copyOf(page.captureMyCourseCounts(account));
        record("Original My Courses baseline", new TreeMap<>(original));
        return original;
    }

    public EnrollmentCourse selectAbsentCourse() {
        if (original == null || selected != null) {
            throw new IllegalStateException("Capture the baseline before selecting this test's one Course.");
        }
        HomePage home = new HomePage(driver);
        List<String> unready = new ArrayList<>();
        for (String categoryUrl : home.getCourseCategoryUrls()) {
            try {
                var candidate = home.goToCourseCategory(categoryUrl)
                        .selectCourseForEnrollmentExcluding(original.keySet(), List.of());
                if (candidate.isPresent()) {
                    selected = candidate.get();
                    record("Selected frontend Course", selected);
                    return selected;
                }
            } catch (TimeoutException categoryNotReady) {
                // Read-only discovery of another actual category; no enrollment/cancellation retry.
                unready.add(categoryUrl);
            }
        }
        throw new IllegalStateException("E2E-001 could not select an absent Course from the frontend. "
                + "Original enrollments were untouched; unready categories=" + unready);
    }

    /** Call after the exact absence guard and detail readiness, immediately before clickEnroll. */
    public void markEnrollmentAttempt(EnrollmentCourse course) {
        if (original == null || selected == null || !selected.equals(course)
                || original.containsKey(course.courseName()) || enrollmentAttempted) {
            throw new IllegalStateException("Refusing ownership of an unselected, original or repeated Course attempt.");
        }
        enrollmentAttempted = true;
    }

    public Map<String, Integer> expectedEnrolledCourses() {
        if (!enrollmentAttempted) throw new IllegalStateException("No owned enrollment attempt.");
        Map<String, Integer> expected = new LinkedHashMap<>(original);
        expected.put(selected.courseName(), 1);
        return Map.copyOf(expected);
    }

    public Map<String, Integer> captureCourses(AccountPage page, String stage) {
        Map<String, Integer> actual = page.captureMyCourseCounts(account);
        record(stage, new TreeMap<>(actual));
        return actual;
    }

    public void record(String stage, Object observation) {
        AccountSupport.recordAccountObservation(TEST_ID, stage, observation, password);
    }

    public void finish(Throwable primaryFailure) {
        List<Throwable> failures = new ArrayList<>();
        // Preserve the failing UI before opening Homepage or restoring the student's session.
        if (primaryFailure != null && driver instanceof TakesScreenshot screenshots) {
            try {
                Allure.addAttachment(TEST_ID + " failure before recovery/cleanup", "image/png",
                        new ByteArrayInputStream(screenshots.getScreenshotAs(OutputType.BYTES)), ".png");
            } catch (RuntimeException | AssertionError evidenceFailure) {
                failures.add(safeFailure("Failure screenshot", evidenceFailure));
            }
        }
        if (enrollmentAttempted) {
            try {
                if (original.containsKey(selected.courseName())) {
                    throw new IllegalStateException("Refusing cleanup of a pre-existing Course.");
                }
                AccountPage page = restoreStudentForCleanup();
                Map<String, Integer> before = null;
                try {
                    before = page.captureMyCourseCounts(account);
                    recordCleanup(failures, "Before owned Course cleanup", new TreeMap<>(before));
                } catch (RuntimeException | AssertionError snapshotFailure) {
                    // An unrelated malformed row must not prevent existing exact-title cleanup.
                    failures.add(safeFailure("Pre-cleanup snapshot", snapshotFailure));
                }
                if (before == null || before.getOrDefault(selected.courseName(), 0) != 0) {
                    try {
                        EnrollmentSupport.finish(driver, TEST_ID, account,
                                selected.courseName(), true, primaryFailure);
                    } catch (RuntimeException | AssertionError cleanupFailure) {
                        failures.add(safeFailure("Owned Course cleanup", cleanupFailure));
                    }
                } else {
                    recordCleanup(failures, "Owned Course cleanup", "Already absent; no cancellation needed.");
                }
                Map<String, Integer> after = new HomePage(driver).goToAccountPage().openMyCourses(account)
                        .refreshMyCourses(account).captureMyCourseCounts(account);
                recordCleanup(failures, "Final refreshed cleanup snapshot", new TreeMap<>(after));
                if (after.getOrDefault(selected.courseName(), 0) != 0 || !original.equals(after)) {
                    throw new AssertionError("E2E-001 cleanup integrity failed. Expected original="
                            + new TreeMap<>(original) + "; actual=" + new TreeMap<>(after)
                            + ". Original enrollments will not be altered to conceal this mismatch.");
                }
                recordCleanup(failures, "Final cleanup integrity", "Owned Course absent; original identities/counts intact.");
            } catch (RuntimeException | AssertionError cleanupFailure) {
                failures.add(safeFailure("Cleanup/recovery", cleanupFailure));
            }
        } else {
            recordCleanup(failures, "Course cleanup", "No enrollment attempt; no cancellation performed.");
        }
        recordCleanup(failures, "Registration side effect", "Account=" + account
                + "; registration may persist. No account deletion was attempted; no verified deletion flow exists.");
        if (primaryFailure != null) {
            failures.forEach(primaryFailure::addSuppressed);
        } else if (!failures.isEmpty()) {
            AssertionError cleanupFailure = new AssertionError("E2E-001 scenario passed, but cleanup/integrity/evidence failed.");
            failures.forEach(cleanupFailure::addSuppressed);
            throw cleanupFailure;
        }
    }

    private AccountPage restoreStudentForCleanup() {
        HomePage home = new HomePage(driver).open();
        if (home.isAuthenticated()) {
            // LoginSupport accepts any authenticated header: inspect the real owner before reusing it.
            String activeAccount = home.goToAccountPage().getPersonalAccount();
            if (!account.equals(activeAccount) && !home.logoutIfAuthenticated()) {
                throw new IllegalStateException("Could not end the other account's session; refusing cancellation.");
            }
        }
        LoginSupport.restoreSessionForCleanup(driver, account, password);
        AccountPage page = home.goToAccountPage().waitUntilPersonalInformationReady(account);
        if (!account.equals(page.getPersonalAccount())) {
            throw new IllegalStateException("Cleanup owner is not the generated student; refusing cancellation.");
        }
        return page.openMyCourses(account).refreshMyCourses(account);
    }

    private void recordCleanup(List<Throwable> failures, String stage, Object observation) {
        try {
            record(stage, observation);
        } catch (RuntimeException | AssertionError evidenceFailure) {
            failures.add(safeFailure(stage + " evidence", evidenceFailure));
        }
    }

    private Throwable safeFailure(String stage, Throwable failure) {
        return AccountSupport.staleAttemptFailure(TEST_ID + " " + stage, failure, password);
    }
}
