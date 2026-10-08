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

/** Failure-safe ENROLL-001 cleanup; Page Objects own every browser operation. */
public final class EnrollmentSupport {

    private static final Logger LOGGER =
            LogManager.getLogger(EnrollmentSupport.class);

    private EnrollmentSupport() {
    }

    public static void finish(
            WebDriver driver,
            String account,
            String courseName,
            boolean enrollmentAttempted,
            Throwable primaryFailure
    ) {
        List<Throwable> failures = new ArrayList<>();

        String cancellation =
                "Not required: enrollment was not attempted.";

        /*
         * BaseTest captures after this finally block.
         * Preserve the original failing screen before cleanup changes the UI.
         */
        if (primaryFailure != null
                && driver instanceof TakesScreenshot screenshotDriver) {

            try {
                Allure.addAttachment(
                        "ENROLL-001 failure before cleanup",
                        "image/png",
                        new ByteArrayInputStream(
                                screenshotDriver.getScreenshotAs(OutputType.BYTES)
                        ),
                        ".png"
                );
            } catch (RuntimeException evidenceFailure) {
                primaryFailure.addSuppressed(evidenceFailure);

                LOGGER.warn(
                        "ENROLL-001 | Could not capture failure before cleanup",
                        evidenceFailure
                );
            }
        }

        try {
            if (enrollmentAttempted) {

                /*
                 * If enrollment succeeded but a later assertion failed,
                 * allow the SweetAlert to disappear before navigating away.
                 */
                new CourseDetailPage(driver)
                        .waitUntilEnrollmentPopupClosed();

                AccountPage accountPage =
                        new HomePage(driver)
                                .goToAccountPage()
                                .openMyCourses(account);

                cancellation =
                        accountPage.cancelCourseIfPresent(courseName)
                                ? "Selected Course cancelled; exact-title item removal verified."
                                : "Selected Course is absent; no cancellation was necessary.";
            }

        } catch (RuntimeException | AssertionError cleanupFailure) {
            failures.add(cleanupFailure);

            cancellation =
                    "FAILED: " + cleanupFailure;

            LOGGER.warn(
                    "ENROLL-001 | Selected-Course cleanup failed",
                    cleanupFailure
            );
        }

        LOGGER.info(
                "ENROLL-001 CLEANUP | course=\"{}\" | cancellation={}",
                courseName,
                cancellation
        );

        try {
            Allure.addAttachment(
                    "ENROLL-001 cleanup",
                    "text/plain",
                    "Selected Course: "
                            + (courseName == null
                            ? "<not selected>"
                            : courseName)
                            + "\nEnrollment attempted: "
                            + enrollmentAttempted
                            + "\nCancellation: "
                            + cancellation
                            + "\nBusiness assertions: "
                            + (primaryFailure == null
                            ? "passed"
                            : "failed")
                            + "\n",
                    ".txt"
            );

        } catch (RuntimeException evidenceFailure) {
            failures.add(evidenceFailure);
        }

        if (primaryFailure != null) {
            failures.forEach(primaryFailure::addSuppressed);

        } else if (!failures.isEmpty()) {
            AssertionError hygieneFailure =
                    new AssertionError(
                            "ENROLL-001 business assertions passed, "
                                    + "but cleanup/evidence failed."
                    );

            failures.forEach(hygieneFailure::addSuppressed);

            throw hygieneFailure;
        }
    }
}