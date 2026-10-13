package vn.cybersoft.vlearning.data.account;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.asserts.SoftAssert;
import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.AccountPage.ProfileValues;
import vn.cybersoft.vlearning.pages.AccountPage.EditValidationState;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;

/** Shared Account setup and restoration; business assertions remain in AccountTests. */
public final class AccountSupport {

    private static final Logger LOGGER = LogManager.getLogger(AccountSupport.class);

    private AccountSupport() {}

    public static AccountPage loginAndOpenAccount(WebDriver driver, String account, String password) {
        try {
            HomePage home = new HomePage(driver);
            LoginPage login = home.goToLoginPage();
            login.enterAccount(account);
            login.enterPassword(password);
            login.clickLogin();
            if (!login.waitUntilLoggedIn()) throw new IllegalStateException("Account Login did not complete.");
            home.waitUntilAuthenticated();
            return home.goToAccountPage().waitUntilPersonalInformationReady(account);
        } catch (RuntimeException loginFailure) {
            throw safeFailure("Account setup", loginFailure, password);
        }
    }

    public static void recordProfile(String testId, String stage, ProfileValues values) {
        LOGGER.info("{} | {} | fullName={} | email={} | phone={}",
                testId, stage, values.fullName(), values.email(), values.phone());
        Allure.addAttachment(testId + " " + stage, "text/plain", values.toString(), ".txt");
    }

    /** Keep the existing ACCOUNT-006 entry point and behavior. */
    public static void restoreProfile(WebDriver driver, String account, String password,
                                      ProfileValues original, boolean updateAttempted, Throwable primaryFailure) {
        restoreProfile("ACCOUNT-006", driver, account, password, original, updateAttempted, primaryFailure);
    }

    public static void restoreProfile(String testId, WebDriver driver, String account, String password,
                                      ProfileValues original, boolean updateAttempted, Throwable primaryFailure) {
        List<Throwable> failures = new ArrayList<>();
        StringBuilder report = new StringBuilder(testId + " restoration\n");
        if (primaryFailure != null && driver instanceof TakesScreenshot screenshotDriver) {
            try {
                Allure.addAttachment(testId + " failure before restoration", "image/png",
                        new ByteArrayInputStream(screenshotDriver.getScreenshotAs(OutputType.BYTES)), ".png");
            } catch (RuntimeException screenshotFailure) {
                // Evidence failure must never prevent the attempt to restore real account data.
                report.append("Failure screenshot unavailable: ")
                        .append(screenshotFailure.getClass().getSimpleName()).append('\n');
            }
        }
        if (!updateAttempted) {
            report.append("Not required: no profile update was attempted.\n");
        } else {
            try {
                AccountPage page = new AccountPage(driver);
                page.waitUntilUpdatePopupClosed();
                page.closeEditProfileIfOpen();
                HomePage home = new HomePage(driver);
                if (!home.isAuthenticated()) {
                    page = loginAndOpenAccount(driver, account, password);
                } else {
                    // Finish popup/modal transitions before clicking the avatar again.
                    page.waitUntilProfileInteractable();
                    page = home.goToAccountPage().waitUntilPersonalInformationReady(account);
                }
                ProfileValues current = page.refreshProfileValues(account, false);
                if (!original.equals(current)) {
                    page.openEditProfile();
                    page.enterProfileUpdateValues(original, password);
                    page.submitEditProfile();
                    String title = page.waitForUpdatePopupTitle();
                    report.append("Restoration popup: ").append(title).append('\n');
                    current = page.refreshProfileValues(account, false);
                    report.append("Profile after restoration refresh: ").append(current).append('\n');
                    if (!AccountInput.UPDATE_SUCCESS_TITLE.equals(title)) {
                        throw new IllegalStateException("Restoration did not show the exact update-success title.");
                    }
                } else {
                    report.append("Original profile already present after refresh; no additional update needed.\n");
                }
                if (!original.equals(current)) {
                    throw new IllegalStateException("Restoration refresh did not match all three original profile values.");
                }
                report.append("VERIFIED: original Full Name, Email and Phone are displayed after refresh.\n");
                LOGGER.info("{} | Restoration verified for all three original profile values", testId);
            } catch (RuntimeException | AssertionError restorationFailure) {
                IllegalStateException safe = safeFailure(testId + " restoration", restorationFailure, password);
                failures.add(safe);
                report.append("FAILED: ").append(safe.getMessage()).append('\n');
                LOGGER.error("{} | Restoration failed; see the separate Allure restoration attachment", testId);
            }
        }
        report.append("Original profile: ").append(original).append('\n')
                .append("Business scenario: ").append(primaryFailure == null ? "passed" : "failed").append('\n');
        try {
            Allure.addAttachment(testId + " restoration result", "text/plain", report.toString(), ".txt");
        } catch (RuntimeException evidenceFailure) {
            failures.add(safeFailure("Restoration evidence", evidenceFailure, password));
        }
        if (primaryFailure != null) {
            failures.forEach(primaryFailure::addSuppressed);
        } else if (!failures.isEmpty()) {
            AssertionError cleanupFailure = new AssertionError(
                    testId + " business assertions passed, but restoration or its evidence failed.");
            failures.forEach(cleanupFailure::addSuppressed);
            throw cleanupFailure;
        }
    }

    public static void recordValidation(String testId, String stage, EditValidationState state, String password) {
        String evidence = state.toString().replace(password, "[REDACTED]");
        LOGGER.info("{} | {} | {}", testId, stage, evidence);
        Allure.addAttachment(testId + " " + stage, "text/plain", evidence, ".txt");
    }

    /** Preserve accumulated business mismatches if a later prerequisite prevents continuation. */
    public static AssertionError validationFailure(SoftAssert softly, Throwable interruption, String password) {
        try {
            softly.assertAll();
        } catch (AssertionError discrepancies) {
            if (!String.valueOf(interruption.getMessage()).equals(discrepancies.getMessage())) {
                discrepancies.addSuppressed(safeFailure("Later validation step", interruption, password));
            }
            return discrepancies;
        }
        if (interruption instanceof AssertionError businessFailure) return businessFailure;
        return new AssertionError("Account validation could not complete.",
                safeFailure("Validation step", interruption, password));
    }

    public static void recordAccountObservation(String testId, String stage, Object observation, String password) {
        String evidence = String.valueOf(observation);
        if (password != null && !password.isEmpty()) evidence = evidence.replace(password, "[REDACTED]");
        LOGGER.info("{} | {} | {}", testId, stage, evidence);
        Allure.addAttachment(testId + " " + stage, "text/plain", evidence, ".txt");
    }

    /** No blind restoration: inspect persisted data and reuse restoration only after a detected mutation. */
    public static void finishUnsavedProfileCheck(String testId, WebDriver driver, String account, String password,
                                                 ProfileValues original, Throwable primaryFailure) {
        Throwable cleanupFailure = null;
        try {
            AccountPage page = new AccountPage(driver);
            if (page.isEditProfileDisplayedNow()) {
                var closed = page.closeEditProfileWithCloseButton();
                recordAccountObservation(testId, "Cleanup Đóng observation", closed, password);
                if (closed.modalDisplayed() || !closed.settled()) {
                    throw new IllegalStateException("Đóng did not close the modal; cleanup will not substitute another action.");
                }
                if (!closed.popupTitles().isEmpty()) {
                    cleanupFailure = new AssertionError("An update popup appeared during non-submitting Đóng cleanup.");
                }
            }
            ProfileValues current = page.refreshProfileAfterUnsavedClose(account);
            recordProfile(testId, "Final cleanup profile verification", current);
            if (!original.equals(current)) {
                AssertionError mutation = new AssertionError(
                        "Stored profile changed during an unsaved-edit scenario; UI restoration is required.");
                if (cleanupFailure == null) cleanupFailure = mutation;
                else cleanupFailure.addSuppressed(mutation);
                restoreProfile(testId, driver, account, password, original, true,
                        primaryFailure == null ? cleanupFailure : primaryFailure);
            } else {
                recordAccountObservation(testId, "Cleanup result",
                        "Original three values verified after refresh; no restoration update submitted.", password);
            }
        } catch (RuntimeException | AssertionError failure) {
            Throwable safe = safeFailure("Unsaved-profile verification/cleanup", failure, password);
            if (cleanupFailure == null) cleanupFailure = safe;
            else cleanupFailure.addSuppressed(safe);
        }
        if (cleanupFailure != null) {
            if (primaryFailure != null) primaryFailure.addSuppressed(cleanupFailure);
            else throw new AssertionError("Unsaved-profile integrity verification or cleanup failed.", cleanupFailure);
        }
    }

    private static IllegalStateException safeFailure(String phase, Throwable failure, String password) {
        String detail = String.valueOf(failure.getMessage()).replace(password, "[REDACTED]");
        IllegalStateException safe = new IllegalStateException(phase + " failed ("
                + failure.getClass().getSimpleName() + "): " + detail);
        safe.setStackTrace(failure.getStackTrace());
        return safe;
    }
}
