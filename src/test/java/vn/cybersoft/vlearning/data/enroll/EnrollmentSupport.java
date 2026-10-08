package vn.cybersoft.vlearning.data.enroll;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.CourseDetailPage;
import vn.cybersoft.vlearning.pages.HomePage;

/** Failure-safe ENROLL-001 hygiene; Page Objects own every browser operation. */
public final class EnrollmentSupport {

    private static final Logger LOGGER = LogManager.getLogger(EnrollmentSupport.class);

    private EnrollmentSupport() {
    }

    public static void finish(WebDriver driver, String account, String courseName,
                              boolean enrollmentAttempted, Throwable primaryFailure) {
        List<Throwable> failures = new ArrayList<>();
        String cancellation = "Not required: enrollment was not attempted.";
        String logout = "Not attempted.";

        // BaseTest captures after this finally block. Preserve the original failing
        // screen now, before cancellation and logout change the UI.
        if (primaryFailure != null && driver instanceof TakesScreenshot screenshotDriver) {
            try {
                Allure.addAttachment("ENROLL-001 failure before cleanup", "image/png",
                        new ByteArrayInputStream(screenshotDriver.getScreenshotAs(OutputType.BYTES)), ".png");
            } catch (RuntimeException evidenceFailure) {
                primaryFailure.addSuppressed(evidenceFailure);
                LOGGER.warn("ENROLL-001 | Could not capture the failure before cleanup", evidenceFailure);
            }
        }

        try {
            if (enrollmentAttempted) {
                // The pre-enrollment absence guard establishes ownership even if
                // the request succeeds but the success-popup assertion later fails.
                new CourseDetailPage(driver).waitUntilEnrollmentPopupClosed();
                AccountPage accountPage = new HomePage(driver).goToAccountPage().openMyCourses(account);
                cancellation = accountPage.cancelCourseIfPresent(courseName)
                        ? "Selected Course cancelled; exact-title item removal verified."
                        : "Selected Course is absent; no cancellation was necessary.";
            }
        } catch (RuntimeException | AssertionError cleanupFailure) {
            failures.add(cleanupFailure);
            cancellation = "FAILED: " + cleanupFailure;
            LOGGER.warn("ENROLL-001 | Selected-Course cleanup failed", cleanupFailure);
        }

        // Always attempt application logout, including after cancellation failure.
        try {
            new CourseDetailPage(driver).waitUntilEnrollmentPopupClosed();
            logout = new HomePage(driver).logoutIfAuthenticated()
                    ? "Application logout completed; Login visible and account link absent."
                    : "Already unauthenticated; Login visible.";
        } catch (RuntimeException | AssertionError logoutFailure) {
            failures.add(logoutFailure);
            logout = "FAILED: " + logoutFailure;
            LOGGER.warn("ENROLL-001 | Application logout failed", logoutFailure);
        }

        LOGGER.info("ENROLL-001 CLEANUP | course=\"{}\" | cancellation={} | logout={}",
                courseName, cancellation, logout);
        try {
            Allure.addAttachment("ENROLL-001 cleanup and logout", "text/plain",
                    "Selected Course: " + (courseName == null ? "<not selected>" : courseName)
                            + "\nEnrollment attempted: " + enrollmentAttempted
                            + "\nCancellation: " + cancellation + "\nLogout: " + logout
                            + "\nBusiness assertions: " + (primaryFailure == null ? "passed" : "failed") + "\n", ".txt");
        } catch (RuntimeException evidenceFailure) {
            failures.add(evidenceFailure);
        }

        if (primaryFailure != null) {
            failures.forEach(primaryFailure::addSuppressed);
        } else if (!failures.isEmpty()) {
            AssertionError hygieneFailure = new AssertionError("ENROLL-001 business assertions passed, but cleanup/logout/evidence failed.");
            failures.forEach(hygieneFailure::addSuppressed);
            throw hygieneFailure;
        }
    }
}
