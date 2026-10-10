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
import vn.cybersoft.vlearning.pages.LoginPage;

/** Failure-safe exact-course cancellation; BaseTest owns final browser/session cleanup. */
public final class EnrollmentSupport {

    private static final Logger LOGGER = LogManager.getLogger(EnrollmentSupport.class);

    private EnrollmentSupport() {
    }

    public static void finish(WebDriver driver, String account, String courseName,
                              boolean enrollmentAttempted, Throwable primaryFailure) {
        finish(driver, "ENROLL-001", account, null, courseName, enrollmentAttempted, primaryFailure);
    }

    public static void finish(WebDriver driver, String testCaseId, String account, String password,
                              String courseName, boolean enrollmentAttempted, Throwable primaryFailure) {
        List<Throwable> failures = new ArrayList<>();
        String cancellation = "Not required: enrollment was not attempted.";

        // BaseTest captures after this finally block. Preserve the original failing
        // screen now, before cancellation changes the UI.
        if (primaryFailure != null && driver instanceof TakesScreenshot screenshotDriver) {
            try {
                Allure.addAttachment(testCaseId + " failure before cleanup", "image/png",
                        new ByteArrayInputStream(screenshotDriver.getScreenshotAs(OutputType.BYTES)), ".png");
            } catch (RuntimeException evidenceFailure) {
                primaryFailure.addSuppressed(evidenceFailure);
                LOGGER.warn("{} | Could not capture the failure before cleanup", testCaseId, evidenceFailure);
            }
        }

        try {
            if (enrollmentAttempted) {
                // The pre-enrollment absence guard establishes ownership even if
                // the request succeeds but a setup guard or business assertion later fails.
                new CourseDetailPage(driver).waitUntilEnrollmentPopupClosed();
                HomePage homePage = new HomePage(driver);
                if (password != null && !homePage.isAuthenticated()) {
                    LoginPage loginPage = homePage.goToLoginPage();
                    loginPage.enterAccount(account);
                    loginPage.enterPassword(password);
                    loginPage.clickLogin();
                    if (!loginPage.waitUntilLoggedIn()) {
                        throw new IllegalStateException(testCaseId + " cleanup could not restore Login.");
                    }
                    homePage.waitUntilAuthenticated();
                    LOGGER.info("{} CLEANUP | Authentication restored for selected-Course cancellation", testCaseId);
                }
                AccountPage accountPage = homePage.goToAccountPage().openMyCourses(account);
                cancellation = accountPage.cancelCourseIfPresent(courseName)
                        ? "Selected Course cancelled; exact-title item removal verified."
                        : "Selected Course is absent; no cancellation was necessary.";
            }
        } catch (RuntimeException | AssertionError cleanupFailure) {
            failures.add(cleanupFailure);
            cancellation = "FAILED: " + cleanupFailure;
            LOGGER.warn("{} | Selected-Course cleanup failed", testCaseId, cleanupFailure);
        }

        LOGGER.info("{} CLEANUP | course=\"{}\" | cancellation={}", testCaseId, courseName, cancellation);
        try {
            Allure.addAttachment(testCaseId + " cleanup", "text/plain",
                    "Selected Course: " + (courseName == null ? "<not selected>" : courseName)
                            + "\nEnrollment attempted: " + enrollmentAttempted
                            + "\nCancellation: " + cancellation
                            + "\nScenario: " + (primaryFailure == null ? "passed" : "failed") + "\n", ".txt");
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
