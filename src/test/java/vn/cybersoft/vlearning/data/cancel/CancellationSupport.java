package vn.cybersoft.vlearning.data.cancel;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import vn.cybersoft.vlearning.data.account.AccountSupport;
import vn.cybersoft.vlearning.data.enroll.EnrollInput;
import vn.cybersoft.vlearning.data.enroll.EnrollmentSupport;
import vn.cybersoft.vlearning.data.login.LoginSupport;
import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.CourseCategoryPage.EnrollmentCourse;
import vn.cybersoft.vlearning.pages.CourseDetailPage;
import vn.cybersoft.vlearning.pages.HomePage;

/** Per-test setup, exact enrollment ownership and cleanup; scenario assertions stay in the tests. */
public final class CancellationSupport {
    private static final Logger LOGGER = LogManager.getLogger(CancellationSupport.class);
    private final WebDriver driver;
    private final String testId;
    private final List<EnrollmentCourse> selected = new ArrayList<>();
    private final Map<String, EnrollmentCourse> owned = new LinkedHashMap<>();
    private Map<String, Integer> original;

    public CancellationSupport(WebDriver driver, String testId) {
        this.driver = driver;
        this.testId = testId;
    }

    public void loginAndCaptureBaseline() {
        if (original != null) throw new IllegalStateException("The pre-test baseline must only be captured once.");
        AccountPage page = AccountSupport.loginAndOpenAccount(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD)
                .openMyCourses(EnrollInput.ACCOUNT).refreshMyCourses(EnrollInput.ACCOUNT);
        original = page.captureMyCourseCounts(EnrollInput.ACCOUNT);
        record("Original My Courses (read-only)", new TreeMap<>(original));
    }

    /** Exclude every pre-existing/current title and previously chosen href; never cancel to make room. */
    public EnrollmentCourse selectAbsentCourse() {
        requireBaseline();
        Map<String, Integer> current = openMyCourses().refreshMyCourses(EnrollInput.ACCOUNT)
                .captureMyCourseCounts(EnrollInput.ACCOUNT);
        Set<String> unavailableNames = new LinkedHashSet<>(original.keySet());
        unavailableNames.addAll(current.keySet());
        HomePage home = new HomePage(driver);
        List<String> categories = home.getCourseCategoryUrls();
        List<String> unavailableCategories = new ArrayList<>();
        for (String url : categories) {
            try {
                var candidate = home.goToCourseCategory(url)
                        .selectCourseForEnrollmentExcluding(unavailableNames, List.copyOf(selected));
                if (candidate.isEmpty()) continue;
                EnrollmentCourse course = candidate.get();
                AccountPage page = home.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT)
                        .refreshMyCourses(EnrollInput.ACCOUNT);
                page.requireCourseNotEnrolled(course.courseName());
                selected.add(course);
                record("Selected absent Course", course + "; exact My Courses count=0");
                return course;
            } catch (TimeoutException unavailable) {
                // Read-only discovery only. No enrollment or cancellation is retried here.
                unavailableCategories.add(url);
            }
        }
        throw new IllegalStateException(testId + " setup: no distinct, absent Course could be selected from "
                + "the discovered frontend categories. Pre-existing enrollments were untouched. Unready categories="
                + unavailableCategories);
    }

    /** Each attempt rechecks absence; ownership survives cancellation and the second enrollment attempt. */
    public String enroll(EnrollmentCourse course) {
        requireBaseline();
        if (!selected.contains(course) || original.containsKey(course.courseName())) {
            throw new IllegalStateException("Refusing enrollment ownership for an unselected or pre-existing Course.");
        }
        EnrollmentCourse previous = owned.get(course.courseName());
        if (previous != null && !previous.equals(course)) {
            throw new IllegalStateException("One owned title cannot identify different captured Courses.");
        }
        AccountPage page = openMyCourses().refreshMyCourses(EnrollInput.ACCOUNT);
        page.requireCourseNotEnrolled(course.courseName());
        CourseDetailPage detail = new HomePage(driver).goToCourseCategory(course.categoryUrl())
                .openCourseForEnrollment(course);
        // Immediately before EACH click, including re-enrollment. A timed-out click may still have persisted.
        owned.put(course.courseName(), course);
        detail.clickEnroll();
        String title = detail.waitForEnrollmentPopupTitle();
        record("Enrollment response", course + "; popup=" + title);
        return title;
    }

    public AccountPage openMyCourses() {
        new CourseDetailPage(driver).waitUntilEnrollmentPopupClosed();
        return new HomePage(driver).goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
    }

    public Map<String, Integer> originalCourses() {
        requireBaseline();
        return original;
    }

    public Map<String, Integer> expectedCourses(EnrollmentCourse... presentOwnedCourses) {
        requireBaseline();
        Map<String, Integer> expected = new LinkedHashMap<>(original);
        for (EnrollmentCourse course : presentOwnedCourses) {
            if (!course.equals(owned.get(course.courseName())) || expected.putIfAbsent(course.courseName(), 1) != null) {
                throw new IllegalStateException("Expected Course must have unique test-owned identity.");
            }
        }
        return Map.copyOf(expected);
    }

    public Map<String, Integer> captureCourses(AccountPage page, String stage) {
        Map<String, Integer> actual = page.captureMyCourseCounts(EnrollInput.ACCOUNT);
        record(stage, new TreeMap<>(actual));
        return actual;
    }

    /** No enrollment is attempted here; only verified owned titles are delegated to existing cancellation cleanup. */
    public void finish(Throwable primaryFailure) {
        List<Throwable> failures = new ArrayList<>();
        if (primaryFailure != null && driver instanceof TakesScreenshot screenshots) {
            try {
                Allure.addAttachment(testId + " failure before recovery/cleanup", "image/png",
                        new ByteArrayInputStream(screenshots.getScreenshotAs(OutputType.BYTES)), ".png");
            } catch (RuntimeException evidenceFailure) {
                failures.add(safeCleanupFailure(evidenceFailure));
            }
        }
        if (owned.isEmpty()) {
            recordCleanup(failures, "Cleanup", "No enrollment attempt; no cancellation performed.");
        } else {
            try {
                // Navigation recovers even a blank page. openMyCourses verifies the actual account owner.
                HomePage home = new HomePage(driver).open();
                LoginSupport.restoreSessionForCleanup(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
                AccountPage page = home.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT)
                        .refreshMyCourses(EnrollInput.ACCOUNT);
                Map<String, Integer> before = null;
                try {
                    before = page.captureMyCourseCounts(EnrollInput.ACCOUNT);
                    recordCleanup(failures, "Before authorized cleanup", new TreeMap<>(before));
                } catch (RuntimeException | AssertionError snapshotFailure) {
                    // An unrelated malformed row must not prevent exact-title cleanup of other owned Courses.
                    failures.add(safeCleanupFailure(snapshotFailure));
                }
                List<String> remaining = new ArrayList<>();
                for (String name : owned.keySet()) {
                    if (original.containsKey(name)) {
                        failures.add(new AssertionError("Refusing cleanup of pre-existing Course: " + name));
                    } else if (before == null || before.getOrDefault(name, 0) > 0) {
                        remaining.add(name);
                    } else {
                        recordCleanup(failures, "Owned Course already absent", name + "; no cancellation needed.");
                    }
                }
                try {
                    // Existing finish attempts every owned title independently and rejects ambiguous matches.
                    EnrollmentSupport.finish(driver, testId, EnrollInput.ACCOUNT, remaining, primaryFailure);
                } catch (RuntimeException | AssertionError cleanupFailure) {
                    failures.add(safeCleanupFailure(cleanupFailure));
                }
                Map<String, Integer> after = new HomePage(driver).goToAccountPage().openMyCourses(EnrollInput.ACCOUNT)
                        .refreshMyCourses(EnrollInput.ACCOUNT).captureMyCourseCounts(EnrollInput.ACCOUNT);
                recordCleanup(failures, "After cleanup and refresh", new TreeMap<>(after));
                for (String name : owned.keySet()) {
                    if (after.getOrDefault(name, 0) != 0) {
                        failures.add(new AssertionError("Owned Course remains after refreshed cleanup: "
                                + name + "; exact count=" + after.get(name)));
                    }
                }
                if (!original.equals(after)) {
                    failures.add(new AssertionError("Original per-Course identity/count baseline was not restored. "
                            + "Expected=" + new TreeMap<>(original) + "; actual=" + new TreeMap<>(after)
                            + ". Pre-existing Courses will not be altered to conceal this mismatch."));
                }
            } catch (RuntimeException | AssertionError cleanupFailure) {
                failures.add(safeCleanupFailure(cleanupFailure));
            }
        }
        recordCleanup(failures, "Cleanup ownership", owned.values());
        if (primaryFailure != null) {
            failures.forEach(primaryFailure::addSuppressed);
        } else if (!failures.isEmpty()) {
            AssertionError cleanupFailure = new AssertionError(testId + " scenario passed, but cleanup/integrity/evidence failed.");
            failures.forEach(cleanupFailure::addSuppressed);
            throw cleanupFailure;
        }
    }

    private void requireBaseline() {
        if (original == null) throw new IllegalStateException("Capture the authenticated pre-test Course baseline first.");
    }

    private void record(String stage, Object observation) {
        String text = String.valueOf(observation).replace(EnrollInput.PASSWORD, "[REDACTED]");
        LOGGER.info("{} | {} | {}", testId, stage, text);
        Allure.addAttachment(testId + " " + stage, "text/plain", text, ".txt");
    }

    private void recordCleanup(List<Throwable> failures, String stage, Object observation) {
        try {
            record(stage, observation);
        } catch (RuntimeException evidenceFailure) {
            // Evidence must never stop removal of the remaining owned Courses.
            failures.add(safeCleanupFailure(evidenceFailure));
        }
    }

    private Throwable safeCleanupFailure(Throwable failure) {
        if (failure instanceof AssertionError) return failure;
        IllegalStateException safe = new IllegalStateException(testId + " cleanup: "
                + failure.getClass().getSimpleName() + ": "
                + String.valueOf(failure.getMessage()).replace(EnrollInput.PASSWORD, "[REDACTED]"));
        safe.setStackTrace(failure.getStackTrace());
        for (Throwable suppressed : failure.getSuppressed()) safe.addSuppressed(safeCleanupFailure(suppressed));
        return safe;
    }
}
