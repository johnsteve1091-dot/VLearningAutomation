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
import org.openqa.selenium.WindowType;
import org.testng.asserts.SoftAssert;
import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.AccountPage.ProfileValues;
import vn.cybersoft.vlearning.pages.AccountPage.EditValidationState;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;
import vn.cybersoft.vlearning.pages.CourseCategoryPage.EnrollmentCourse;
import vn.cybersoft.vlearning.data.enroll.EnrollmentSupport;
import vn.cybersoft.vlearning.data.login.LoginSupport;

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
    public record TabLogout(String tabA, String tabB, boolean sharedAuthentication,
                            String sharedAccount, boolean logoutClicked, boolean guestConfirmed) {}

    /** Per-attempt window state only. Both tabs always belong to this single WebDriver. */
    public static final class AccountTabs implements AutoCloseable {
        private final WebDriver driver;
        private final String tabA;
        private String tabB;

        public AccountTabs(WebDriver driver) {
            this.driver = driver;
            this.tabA = driver.getWindowHandle();
        }

        public TabLogout logoutInSecondTab(String expectedAccount) {
            if (tabB != null) throw new IllegalStateException("Each attempt needs a new Tab B.");
            driver.switchTo().newWindow(WindowType.TAB);
            tabB = driver.getWindowHandle();
            HomePage home = new HomePage(driver).open();
            boolean shared = home.isAuthenticated();
            String identity = shared ? home.goToAccountPage().getPersonalAccount() : "";
            if (!shared || !expectedAccount.equals(identity)) {
                return new TabLogout(tabA, tabB, shared, identity, false, false);
            }
            home.open();
            boolean loggedOut = home.logoutIfAuthenticated();
            return new TabLogout(tabA, tabB, true, identity, loggedOut, !home.isAuthenticated());
        }

        public void returnToOriginalTab() {
            driver.switchTo().window(tabA);
        }

        /** Called only before setup or AFTER stale actions have finished. Never revives a tab mid-attempt. */
        public AccountPage loginFresh(String account, String password) {
            try {
                useRecoveryTab();
                HomePage home = new HomePage(driver).open();
                if (home.isAuthenticated() && !home.logoutIfAuthenticated()) {
                    throw new IllegalStateException("Application logout did not end the previous session.");
                }
                return loginAndOpenAccount(driver, account, password);
            } catch (RuntimeException failure) {
                throw safeFailure("Fresh Account Login", failure, password);
            }
        }

        private void useRecoveryTab() {
            var handles = driver.getWindowHandles();
            String usable = tabB != null && handles.contains(tabB) ? tabB
                    : handles.contains(tabA) ? tabA : handles.stream().findFirst().orElse(null);
            if (usable == null) {
                driver.switchTo().newWindow(WindowType.TAB);
                tabB = driver.getWindowHandle();
            } else {
                driver.switchTo().window(usable);
            }
        }

        private AccountPage restoreOwnerForCleanup(String account, String password) {
            try {
                useRecoveryTab();
                HomePage home = new HomePage(driver).open();
                // A header alone cannot establish the account owner. Always log in with the owner again.
                if (home.isAuthenticated()) home.logoutIfAuthenticated();
                LoginSupport.restoreSessionForCleanup(driver, account, password);
                return home.goToAccountPage().waitUntilPersonalInformationReady(account).openMyCourses(account);
            } catch (RuntimeException failure) {
                throw safeFailure("Owned Course cleanup Login", failure, password);
            }
        }

        @Override
        public void close() {
            try {
                if (tabB != null && driver.getWindowHandles().contains(tabB)) {
                    driver.switchTo().window(tabB);
                    driver.close();
                }
            } finally {
                if (driver.getWindowHandles().contains(tabA)) driver.switchTo().window(tabA);
            }
        }
    }

    /** Return the ACTUAL stored values, even after restoring them, so the test still fails on mutation. */
    public static ProfileValues verifyAndRestoreStaleProfile(AccountTabs tabs, String stage,
            String account, String password, ProfileValues original) {
        AccountPage page = tabs.loginFresh(account, password);
        ProfileValues actual = page.refreshProfileValues(account, false);
        List<Throwable> evidenceFailures = new ArrayList<>();
        recordCleanupObservation(evidenceFailures, "ACCOUNT-015",
                stage + " / persisted profile after fresh Login + refresh", actual, password);
        if (!original.equals(actual)) {
            AssertionError mutation = new AssertionError(stage + ": unauthorized stored profile modification. "
                    + "Original=" + original + "; actual=" + actual);
            recordCleanupObservation(evidenceFailures, "ACCOUNT-015",
                    stage + " / unauthorized modification", mutation.getMessage(), password);
            restoreProfile("ACCOUNT-015 " + stage, tabs.driver, account, password, original, true, mutation);
            evidenceFailures.forEach(mutation::addSuppressed);
            if (mutation.getSuppressed().length > 0) throw mutation;
            ProfileValues restored = page.refreshProfileValues(account, false);
            recordCleanupObservation(evidenceFailures, "ACCOUNT-015", stage + " / restoration verification", restored, password);
            if (!original.equals(restored)) {
                mutation.addSuppressed(new AssertionError("Restoration did not retain the original three values."));
                throw mutation;
            }
        } else {
            recordCleanupObservation(evidenceFailures, "ACCOUNT-015", stage + " / restoration",
                    "Not required: original three values retained; no cleanup update submitted.", password);
        }
        if (!evidenceFailures.isEmpty()) {
            AssertionError evidence = new AssertionError(stage + ": profile verification evidence failed; actual=" + actual);
            evidenceFailures.forEach(evidence::addSuppressed);
            throw evidence;
        }
        return actual;
    }

    /** At most two candidates from the existing UI selection workflow; never alter an existing enrollment. */
    public static EnrollmentCourse selectAbsentCourseForAccountTest(WebDriver driver, String account) {
        HomePage home = new HomePage(driver);
        EnrollmentCourse selected = home.goToFirstCourseCategory().selectCourseForEnrollment();
        for (int candidate = 1; candidate <= 2; candidate++) {
            AccountPage page = home.goToAccountPage().openMyCourses(account).refreshMyCourses(account);
            int count = page.countExactCourseItems(selected.courseName());
            recordAccountObservation("ACCOUNT-016", "Candidate " + candidate,
                    selected + "; initial exact My Courses count=" + count, null);
            if (count == 0) {
                page.requireCourseNotEnrolled(selected.courseName());
                return selected;
            }
            recordAccountObservation("ACCOUNT-016", "Existing enrollment preserved",
                    selected.courseName() + "; no ownership or cancellation attempted.", null);
            if (candidate == 1) selected = home.selectDifferentCourseForEnrollment(selected);
        }
        throw new IllegalStateException("ACCOUNT-016 setup failed: both verified candidate Courses were already "
                + "enrolled. No safe test-owned enrollment was established; existing Courses left untouched.");
    }

    /** Fresh authenticated cleanup, then the existing exact-course finish workflow and a refreshed zero guard. */
    public static void finishStaleCourseCheck(AccountTabs tabs, String account, String password,
            String courseName, boolean enrollmentAttempted, Throwable primaryFailure) {
        if (!enrollmentAttempted) {
            try {
                recordAccountObservation("ACCOUNT-016", "Cleanup", "No owned enrollment attempt; no cancellation.", password);
            } catch (RuntimeException failure) {
                var safe = safeFailure("Unowned Course cleanup evidence", failure, password);
                if (primaryFailure != null) primaryFailure.addSuppressed(safe);
                else throw safe;
            }
            return;
        }
        Throwable cleanupFailure = null;
        List<Throwable> evidenceFailures = new ArrayList<>();
        try {
            AccountPage page = tabs.restoreOwnerForCleanup(account, password);
            int before = page.refreshMyCourses(account).countExactCourseItems(courseName);
            recordCleanupObservation(evidenceFailures, "ACCOUNT-016", "Authorized cleanup / before", "Course=" + courseName
                    + "; owner=" + account + "; exact count=" + before, password);
            if (before > 0) {
                try {
                    EnrollmentSupport.finish(tabs.driver, "ACCOUNT-016", account, courseName, true, primaryFailure);
                } catch (RuntimeException | AssertionError failure) {
                    cleanupFailure = failure;
                }
            } else {
                recordCleanupObservation(evidenceFailures, "ACCOUNT-016", "Authorized cleanup / cancellation",
                        "Owned Course already absent; no cancellation needed and no reenrollment performed.", password);
            }
            int after = new HomePage(tabs.driver).goToAccountPage().openMyCourses(account)
                    .refreshMyCourses(account).countExactCourseItems(courseName);
            recordCleanupObservation(evidenceFailures, "ACCOUNT-016", "Authorized cleanup / refreshed final count",
                    "Course=" + courseName + "; exact count=" + after, password);
            if (after != 0) {
                AssertionError remaining = new AssertionError("Test-owned Course remains after cleanup: "
                        + courseName + "; refreshed exact count=" + after);
                if (cleanupFailure == null) cleanupFailure = remaining;
                else cleanupFailure.addSuppressed(remaining);
            }
        } catch (RuntimeException | AssertionError failure) {
            Throwable safe = safeFailure("Owned Course cleanup", failure, password);
            if (cleanupFailure == null) cleanupFailure = safe;
            else cleanupFailure.addSuppressed(safe);
        }
        if (!evidenceFailures.isEmpty()) {
            if (cleanupFailure == null) cleanupFailure = new AssertionError("Owned Course cleanup evidence failed.");
            for (Throwable evidence : evidenceFailures) cleanupFailure.addSuppressed(evidence);
        }
        if (cleanupFailure != null) {
            if (primaryFailure != null) primaryFailure.addSuppressed(cleanupFailure);
            else throw new AssertionError("ACCOUNT-016 owned enrollment cleanup failed.", cleanupFailure);
        }
    }

    private static void recordCleanupObservation(List<Throwable> failures, String testId,
            String stage, Object observation, String password) {
        try {
            recordAccountObservation(testId, stage, observation, password);
        } catch (RuntimeException failure) {
            failures.add(safeFailure(stage + " evidence", failure, password));
        }
    }

    public static AssertionError staleAttemptFailure(String stage, Throwable failure, String password) {
        // Keep suppressed cleanup errors and assertions without exposing a WebDriver secret-bearing message.
        if (failure instanceof AssertionError assertion) return assertion;
        AssertionError result = new AssertionError(stage + " could not complete.", safeFailure(stage, failure, password));
        for (Throwable suppressed : failure.getSuppressed()) {
            result.addSuppressed(safeFailure(stage + " cleanup", suppressed, password));
        }
        return result;
    }

}
