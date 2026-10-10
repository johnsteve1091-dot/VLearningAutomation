package vn.cybersoft.vlearning.data.enroll;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.CourseDetailPage;
import vn.cybersoft.vlearning.pages.HomePage;

/** Failure-safe exact-course cancellation; BaseTest owns final browser/session cleanup. */
public final class EnrollmentSupport {

    private static final Logger LOGGER = LogManager.getLogger(EnrollmentSupport.class);

    private EnrollmentSupport() {
    }

    public static void finish(WebDriver driver, String account, String courseName,
                              boolean enrollmentAttempted, Throwable primaryFailure) {
        finish(driver, "ENROLL-001", account, courseName, enrollmentAttempted, primaryFailure);
    }

    public static void finish(WebDriver driver, String testCaseId, String account,
                              String courseName, boolean enrollmentAttempted, Throwable primaryFailure) {
        finish(driver, testCaseId, account,
                enrollmentAttempted ? List.of(courseName) : List.of(), primaryFailure);
    }

    /** Names enter this collection only after the absence guard, immediately before an attempt. */
    public static void finish(WebDriver driver, String testCaseId, String account,
                              List<String> ownedCourseNames, Throwable primaryFailure) {
        List<Throwable> failures = new ArrayList<>();
        StringBuilder cancellation = new StringBuilder();

        // BaseTest captures after this finally block. Preserve the failing screen
        // before cancellation changes the UI.
        if (primaryFailure != null && driver instanceof TakesScreenshot screenshotDriver) {
            try {
                Allure.addAttachment(testCaseId + " failure before cleanup", "image/png",
                        new ByteArrayInputStream(screenshotDriver.getScreenshotAs(OutputType.BYTES)), ".png");
            } catch (RuntimeException evidenceFailure) {
                failures.add(evidenceFailure);
                LOGGER.warn("{} | Could not capture the failure before cleanup", testCaseId, evidenceFailure);
            }
        }

        if (ownedCourseNames.isEmpty()) {
            cancellation.append("Not required: no enrollment was attempted.\n");
        }
        for (String courseName : new LinkedHashSet<>(ownedCourseNames)) {
            String result;
            try {
                // Attempt every owned course independently, including after another cleanup failure.
                new CourseDetailPage(driver).waitUntilEnrollmentPopupClosed();
                AccountPage accountPage = new HomePage(driver).goToAccountPage().openMyCourses(account);
                result = accountPage.cancelCourseIfPresent(courseName)
                        ? "Cancelled; exact-title item removal verified."
                        : "Absent; no cancellation was necessary.";
            } catch (RuntimeException | AssertionError cleanupFailure) {
                failures.add(cleanupFailure);
                result = "FAILED: " + cleanupFailure;
                LOGGER.warn("{} | Owned-Course cleanup failed | course=\"{}\"", testCaseId, courseName, cleanupFailure);
            }
            LOGGER.info("{} CLEANUP | course=\"{}\" | cancellation={}", testCaseId, courseName, result);
            cancellation.append("Course: ").append(courseName).append("\nCancellation: ").append(result).append('\n');
        }

        try {
            Allure.addAttachment(testCaseId + " cleanup", "text/plain",
                    cancellation + "Scenario: " + (primaryFailure == null ? "passed" : "failed") + "\n", ".txt");
        } catch (RuntimeException evidenceFailure) {
            failures.add(evidenceFailure);
        }

        if (primaryFailure != null) {
            failures.forEach(primaryFailure::addSuppressed);
        } else if (!failures.isEmpty()) {
            AssertionError hygieneFailure = new AssertionError(testCaseId + " scenario passed, but cleanup/evidence failed.");
            failures.forEach(hygieneFailure::addSuppressed);
            throw hygieneFailure;
        }
    }
}
