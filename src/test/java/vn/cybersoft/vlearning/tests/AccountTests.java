package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.account.AccountInput;
import vn.cybersoft.vlearning.data.account.AccountSupport;
import vn.cybersoft.vlearning.data.enroll.EnrollInput;
import vn.cybersoft.vlearning.data.register.RegisterInput;
import vn.cybersoft.vlearning.data.register.RegisterMessage;
import vn.cybersoft.vlearning.data.register.RegisterSupport;
import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.AccountPage.StaleProfileAction;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;
import vn.cybersoft.vlearning.pages.RegisterPage;

@Epic("V Learning")
@Feature("Account")
public class AccountTests extends BaseTest {

    private static final Logger LOGGER = LogManager.getLogger(AccountTests.class);

    @Test(groups = {"account-tc1"}, testName = "ACCOUNT-001 - Open Account after login",
            description = "ACCOUNT-001 - Authenticated avatar navigation opens the user's Account page")
    @Story("Authenticated Account navigation")
    @Description("Login with the existing account, click the header avatar and verify the Account route, "
            + "visible root, active Personal Information panel and authenticated username.")
    public void accountPageShouldOpenAfterLogin() {
        LOGGER.info("ACCOUNT-001 | Starting Account navigation");
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        loginPage.enterAccount(EnrollInput.ACCOUNT);
        loginPage.enterPassword(EnrollInput.PASSWORD);
        loginPage.clickLogin();
        if (!loginPage.waitUntilLoggedIn()) {
            throw new IllegalStateException("ACCOUNT-001 setup failed: Login did not complete.");
        }
        homePage.waitUntilAuthenticated();
        AccountPage accountPage = homePage.goToAccountPage();

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(accountPage.waitUntilAtAccountPage(), "Avatar navigation must reach /thongtincanhan.");
        softly.assertTrue(accountPage.isAccountRootDisplayed(), "Account page root must be visible.");
        softly.assertTrue(accountPage.isPersonalInformationActive(), "Personal Information must be active and visible.");
        String actualAccount = accountPage.getPersonalAccount();
        Allure.addAttachment("ACCOUNT-001 Account navigation", "text/plain",
                "URL: " + driver.getCurrentUrl() + "\nUsername: " + actualAccount, ".txt");
        softly.assertEquals(actualAccount, EnrollInput.ACCOUNT, "Displayed Account must be the authenticated user.");
        softly.assertAll();
        LOGGER.info("ACCOUNT-001 | Account navigation verified | username={}", actualAccount);
    }

    @Test(groups = {"account-tc2"}, testName = "ACCOUNT-002 - Newly registered Account profile",
            description = "ACCOUNT-002 - Explicit new-user login shows the original six registration values")
    @Story("Registration and Account data consistency")
    @Description("Register one unique account and email using existing fixtures, explicitly log in with that "
            + "new account and compare all six labeled Personal Information values with the original inputs.")
    public void newlyRegisteredAccountShouldShowCorrectProfile() {
        String account = RegisterSupport.generateUniqueAccount();
        String email = RegisterSupport.generateUniqueEmail();
        var expected = new AccountPage.PersonalInformation(account, email, RegisterInput.Correct.FULL_NAME,
                RegisterInput.Correct.PHONE, RegisterInput.Correct.GROUP, "Học viên");
        LOGGER.info("ACCOUNT-002 | Registration inputs | account={} | email={}", account, email);
        Allure.addAttachment("ACCOUNT-002 expected registration profile", "text/plain", expected.toString(), ".txt");

        HomePage homePage = new HomePage(driver);
        RegisterPage registerPage = homePage.goToLoginPage().goToRegisterPage();
        registerPage.enterAccount(account);
        registerPage.enterFullName(expected.fullName());
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(email);
        registerPage.enterPhone(expected.phone());
        if (!registerPage.isGroupDropdownNativeSelect() || !registerPage.hasGroupOption(expected.group())) {
            throw new IllegalStateException("ACCOUNT-002 setup failed: expected registration Group is unavailable: "
                    + expected.group());
        }
        registerPage.selectGroup(expected.group());
        String selectedGroup = registerPage.getSelectedGroup();
        if (!expected.group().equals(selectedGroup)) {
            throw new IllegalStateException("ACCOUNT-002 setup failed: selected Group=" + selectedGroup
                    + "; expected=" + expected.group());
        }
        registerPage.clickRegister();
        Assert.assertTrue(registerPage.isSuccessfulRegistrationPopupDisplayed(),
                "The new Account must register successfully before its profile can be verified.");
        registerPage.waitUntilRegistrationPopupClosed();

        LoginPage loginPage;
        if (homePage.isAuthenticated()) {
            homePage.logoutIfAuthenticated();
            loginPage = homePage.goToLoginPage();
        } else {
            loginPage = registerPage.goToLoginPage();
        }
        loginPage.enterAccount(account);
        loginPage.enterPassword(RegisterInput.Correct.PASSWORD);
        loginPage.clickLogin();
        if (!loginPage.waitUntilLoggedIn()) {
            throw new IllegalStateException("ACCOUNT-002 setup failed: explicit new-user Login did not complete.");
        }
        homePage.waitUntilAuthenticated();
        var actual = homePage.goToAccountPage().readPersonalInformation(account);
        Allure.addAttachment("ACCOUNT-002 actual Account profile", "text/plain", actual.toString(), ".txt");

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(actual.account(), expected.account(), "Tài khoản must match the generated Account.");
        softly.assertEquals(actual.email(), expected.email(), "Email must match the generated Email.");
        softly.assertEquals(actual.fullName(), expected.fullName(), "Họ và tên must match registration.");
        softly.assertEquals(actual.phone(), expected.phone(), "Số điện thoại must retain every digit, including leading zero.");
        softly.assertEquals(actual.group(), expected.group(), "Nhóm must match the selected registration Group.");
        softly.assertEquals(actual.learnerType(), expected.learnerType(), "Đối tượng must be Học viên.");
        softly.assertAll();
        LOGGER.info("ACCOUNT-002 | Explicit new-user Login and six profile values verified | account={} | email={}",
                account, email);
    }

    @Test(groups = {"account-tc3"}, testName = "ACCOUNT-003 - Open Edit Profile modal",
            description = "ACCOUNT-003 - Cập nhật opens the correct visible and interactable Edit Profile controls")
    @Story("Edit Profile modal controls")
    @Description("Open Account after login, click the available Cập nhật button and verify the modal heading, "
            + "four visible enabled inputs and their observed types, plus Hoàn thành and Đóng. Enter no data.")
    public void updateProfileModalShouldOpen() {
        LOGGER.info("ACCOUNT-003 | Starting Edit Profile modal check");
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        loginPage.enterAccount(EnrollInput.ACCOUNT);
        loginPage.enterPassword(EnrollInput.PASSWORD);
        loginPage.clickLogin();
        if (!loginPage.waitUntilLoggedIn()) {
            throw new IllegalStateException("ACCOUNT-003 setup failed: Login did not complete.");
        }
        homePage.waitUntilAuthenticated();
        AccountPage accountPage = homePage.goToAccountPage().waitUntilPersonalInformationReady(EnrollInput.ACCOUNT);
        Assert.assertTrue(accountPage.isUpdateProfileAvailable(), "Cập nhật must be visible and enabled.");
        var controls = accountPage.openEditProfile().readEditProfileControls();
        Allure.addAttachment("ACCOUNT-003 Edit Profile controls", "text/plain", controls.toString(), ".txt");

        SoftAssert softly = new SoftAssert();
        softly.assertTrue(controls.displayed(), "The Edit Profile modal must be visible.");
        softly.assertEquals(controls.heading(), "Chỉnh sửa thông tin cá nhân", "Edit Profile modal heading.");
        softly.assertTrue(controls.fullName().displayed() && controls.fullName().enabled(), "Full Name must be visible and enabled.");
        softly.assertEquals(controls.fullName().declaredType(), "text", "Full Name input type.");
        softly.assertTrue(controls.password().displayed() && controls.password().enabled(), "Password must be visible and enabled.");
        softly.assertEquals(controls.password().declaredType(), "password", "Password input type.");
        softly.assertTrue(controls.email().displayed() && controls.email().enabled(), "Email must be visible and enabled.");
        softly.assertEquals(controls.email().declaredType(), "email", "Email input type.");
        softly.assertTrue(controls.phone().displayed() && controls.phone().enabled(), "Phone must be visible and enabled.");
        softly.assertEquals(controls.phone().declaredType(), "phone", "Phone input's observed markup type.");
        softly.assertTrue(controls.completeDisplayed(), "Hoàn thành must be displayed.");
        softly.assertTrue(controls.closeDisplayed(), "Đóng must be displayed.");
        softly.assertAll();
        LOGGER.info("ACCOUNT-003 | Edit Profile heading and required controls verified");
    }

    @Test(groups = {"account-tc4"}, testName = "ACCOUNT-004 - Edit Profile inputs accept values",
            description = "ACCOUNT-004 - Four inputs retain valid values and dismiss without submitting")
    @Story("Edit Profile input capability")
    @Description("Enter existing valid registration fixtures and one unique email in Edit Profile. Compare input "
            + "values, verify masked password retention and close through the verified dismiss button without submitting.")
    public void updateProfileInputsShouldAcceptValues() {
        String email = RegisterSupport.generateUniqueEmail();
        LOGGER.info("ACCOUNT-004 | Starting Edit Profile input check");
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        loginPage.enterAccount(EnrollInput.ACCOUNT);
        loginPage.enterPassword(EnrollInput.PASSWORD);
        loginPage.clickLogin();
        if (!loginPage.waitUntilLoggedIn()) {
            throw new IllegalStateException("ACCOUNT-004 setup failed: Login did not complete.");
        }
        homePage.waitUntilAuthenticated();
        AccountPage accountPage = homePage.goToAccountPage().waitUntilPersonalInformationReady(EnrollInput.ACCOUNT);
        accountPage.openEditProfile();
        accountPage.enterEditProfileValues(RegisterInput.Correct.FULL_NAME, RegisterInput.Correct.PASSWORD,
                email, RegisterInput.Correct.PHONE);
        var actual = accountPage.readEditProfileValues(RegisterInput.Correct.PASSWORD);
        Allure.addAttachment("ACCOUNT-004 Edit Profile input values", "text/plain", actual.toString(), ".txt");

        SoftAssert softly = new SoftAssert();
        softly.assertEquals(actual.fullName(), RegisterInput.Correct.FULL_NAME, "Full Name input must retain the entered value.");
        softly.assertTrue(actual.passwordMatches(), "Password input must retain the entered value.");
        softly.assertEquals(actual.email(), email, "Email input must retain the generated value.");
        softly.assertEquals(actual.phone(), RegisterInput.Correct.PHONE, "Phone input must retain every entered digit.");
        softly.assertTrue(actual.passwordMasked(), "Password input must remain masked.");
        softly.assertTrue(accountPage.closeEditProfile(), "Edit Profile modal must close without submitting.");
        softly.assertAll();
        LOGGER.info("ACCOUNT-004 | Four input values verified; password masked; modal dismissed without submit");
    }

    @Test(groups = {"account-tc5"}, testName = "ACCOUNT-005 - Submit existing profile information",
            description = "ACCOUNT-005 - Updating with existing information succeeds and retains three profile values")
    @Story("Successful profile update with existing information")
    @Description("Login with EnrollInput, capture current Full Name, Email and Phone, submit those same values "
            + "with the existing password unchanged, verify the exact update-success popup, refresh and compare "
            + "the three displayed values. No replacement data or profile-restoration step is introduced.")
    public void updateProfileShouldSucceedWithExistingInformation() {
        LOGGER.info("ACCOUNT-005 | Starting same-information profile update");
        AccountPage page = AccountSupport.loginAndOpenAccount(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
        var original = page.readProfileValues(EnrollInput.ACCOUNT);
        AccountSupport.recordProfile("ACCOUNT-005", "Original profile", original);
        page.openEditProfile();
        page.enterProfileUpdateValues(original, EnrollInput.PASSWORD);
        page.submitEditProfile();
        String title = page.waitForUpdatePopupTitle();
        LOGGER.info("ACCOUNT-005 | Update popup={}", title);
        Allure.addAttachment("ACCOUNT-005 update response", "text/plain", title, ".txt");
        Assert.assertEquals(title, AccountInput.UPDATE_SUCCESS_TITLE, "Exact successful-update popup title.");

        var updated = page.refreshProfileValues(EnrollInput.ACCOUNT);
        AccountSupport.recordProfile("ACCOUNT-005", "Profile after refresh", updated);
        SoftAssert softly = new SoftAssert();
        softly.assertEquals(updated.fullName(), original.fullName(), "Full Name must remain unchanged.");
        softly.assertEquals(updated.email(), original.email(), "Email must remain unchanged.");
        softly.assertEquals(updated.phone(), original.phone(), "Phone must remain unchanged, including leading zeros.");
        softly.assertAll();
        LOGGER.info("ACCOUNT-005 | Update succeeded; all three original profile values retained");
    }

    @Test(groups = {"account-tc6"}, testName = "ACCOUNT-006 - Changed profile information persists",
            description = "ACCOUNT-006 - Three different valid profile values persist after refresh and are restored")
    @Story("Successful profile update and persistence")
    @Description("Login with the dedicated Account fixture, capture only Full Name, Email and Phone, prepare "
            + "three different valid values using Register fixtures and its existing email generator, and submit "
            + "once with the original password unchanged. Verify success, refresh, compare each field with both "
            + "submitted and original values, then restore and verify the original three fields even after failure.")
    public void updateProfileShouldPersistChangedInformation() {
        LOGGER.info("ACCOUNT-006 | Starting changed-information profile update");
        AccountPage page = AccountSupport.loginAndOpenAccount(driver,
                AccountInput.UPDATE_ACCOUNT, AccountInput.UPDATE_PASSWORD);
        var original = page.readProfileValues(AccountInput.UPDATE_ACCOUNT);
        var submitted = AccountInput.differentValidProfile(original);
        AccountSupport.recordProfile("ACCOUNT-006", "Original profile", original);
        AccountSupport.recordProfile("ACCOUNT-006", "Submitted profile", submitted);
        boolean updateAttempted = false;
        Throwable primaryFailure = null;
        try {
            page.openEditProfile();
            page.enterProfileUpdateValues(submitted, AccountInput.UPDATE_PASSWORD);
            updateAttempted = true;
            page.submitEditProfile();
            String title = page.waitForUpdatePopupTitle();
            LOGGER.info("ACCOUNT-006 | Update popup={}", title);
            Allure.addAttachment("ACCOUNT-006 update response", "text/plain", title, ".txt");
            Assert.assertEquals(title, AccountInput.UPDATE_SUCCESS_TITLE, "Exact successful-update popup title.");

            var updated = page.refreshProfileValues(AccountInput.UPDATE_ACCOUNT);
            AccountSupport.recordProfile("ACCOUNT-006", "Profile after refresh", updated);
            SoftAssert softly = new SoftAssert();
            softly.assertEquals(updated.fullName(), submitted.fullName(), "Full Name must equal the submitted value.");
            softly.assertNotEquals(updated.fullName(), original.fullName(), "Full Name must differ from the original.");
            softly.assertEquals(updated.email(), submitted.email(), "Email must equal the submitted value.");
            softly.assertNotEquals(updated.email(), original.email(), "Email must differ from the original.");
            softly.assertEquals(updated.phone(), submitted.phone(), "Phone must equal the submitted value.");
            softly.assertNotEquals(updated.phone(), original.phone(), "Phone must differ from the original.");
            softly.assertAll();
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            AccountSupport.restoreProfile(driver, AccountInput.UPDATE_ACCOUNT, AccountInput.UPDATE_PASSWORD,
                    original, updateAttempted, primaryFailure);
        }
        LOGGER.info("ACCOUNT-006 | Three changed profile values verified and original profile restored");
    }

    @Test(groups = {"account-tc7"}, testName = "ACCOUNT-007 - Reject empty profile update",
            description = "ACCOUNT-007 - Reject four empty fields without changing the stored profile")
    @Story("Rejected profile updates")
    @Description("Login with EnrollInput, capture the original three personal fields, submit four empty fields "
            + "once, observe settled validation with the modal open and no success, close and refresh, "
            + "then compare the three stored values. Exact warning wording is outside this test.")
    public void emptyProfileUpdateShouldBeRejected() {
        LOGGER.info("ACCOUNT-007 | Starting rejection scenario: four empty fields");
        AccountPage page = AccountSupport.loginAndOpenAccount(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
        var original = page.readProfileValues(EnrollInput.ACCOUNT);
        AccountSupport.recordProfile("ACCOUNT-007", "Original profile", original);
        SoftAssert softly = new SoftAssert();
        boolean updateAttempted = false;
        Throwable primaryFailure = null;
        try {
            page.openEditProfile();
            Assert.assertTrue(page.readEditProfileControls().displayed(), "Edit Profile must be visible.");
            page.clearEditProfileFields();
            var entered = page.readEditProfileValues("");
            Assert.assertEquals(entered.fullName(), "", "Full Name must be empty before submission.");
            Assert.assertEquals(entered.email(), "", "Email must be empty before submission.");
            Assert.assertEquals(entered.phone(), "", "Phone must be empty before submission.");
            Assert.assertTrue(entered.passwordMatches(), "Password must be empty before submission.");
            Assert.assertTrue(entered.passwordMasked(), "Password must remain masked.");
            Allure.addAttachment("ACCOUNT-007 submitted validation scenario", "text/plain",
                    "four empty fields\n" + entered, ".txt");
            updateAttempted = true;
            page.submitEditProfile();
            var state = page.waitForEditRejection();
            AccountSupport.recordValidation("ACCOUNT-007", "Rejection response", state, EnrollInput.PASSWORD);
            softly.assertTrue(state.settled(), "An invalid submission must reach a settled rejection state.");
            softly.assertTrue(state.warnings().hasAny(), "An observable field validation response is required.");
            softly.assertTrue(state.modalDisplayed(), "The rejected form must remain open.");
            softly.assertFalse(state.popupTitles().contains(AccountInput.UPDATE_SUCCESS_TITLE),
                    "No update-success popup may appear during rejection observation.");
            softly.assertTrue(state.popupTitles().isEmpty(), "The invalid form must not produce an update popup.");

            page.waitUntilUpdatePopupClosed();
            page.closeEditProfileIfOpen();
            var refreshed = page.refreshProfileValues(EnrollInput.ACCOUNT, false);
            AccountSupport.recordProfile("ACCOUNT-007", "Profile after refresh", refreshed);
            Allure.addAttachment("ACCOUNT-007 original-profile equality", "text/plain",
                    "All three values unchanged: " + original.equals(refreshed), ".txt");
            softly.assertEquals(refreshed.fullName(), original.fullName(), "Rejected Full Name must remain unchanged.");
            softly.assertEquals(refreshed.email(), original.email(), "Rejected Email must remain unchanged.");
            softly.assertEquals(refreshed.phone(), original.phone(), "Rejected Phone must remain unchanged.");
            softly.assertAll();
        } catch (RuntimeException | AssertionError failure) {
            AssertionError preserved = AccountSupport.validationFailure(softly, failure, EnrollInput.PASSWORD);
            primaryFailure = preserved;
            throw preserved;
        } finally {
            AccountSupport.restoreProfile("ACCOUNT-007", driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD,
                    original, updateAttempted, primaryFailure);
        }
    }

    @Test(groups = {"account-tc8"}, testName = "ACCOUNT-008 - Reject each individually empty profile field",
            description = "ACCOUNT-008 - Four independent submissions, with exactly one empty field each")
    @Story("Required-field validation with otherwise valid profile data")
    @Description("For each of Name, Password, Email and Phone: populate the other three fields with valid "
            + "original account data, leave only that field empty, submit Hoàn thành, verify its exact "
            + "required warning, reject the update, then close, refresh and verify unchanged profile data. "
            + "Preserve known product-defect failures while checking all four fields.")
    public void requiredProfileWarningsShouldClearIncrementally() {
        LOGGER.info("ACCOUNT-008 | Starting four independent single-empty-field checks");
        AccountPage page = AccountSupport.loginAndOpenAccount(driver,
                EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
        var original = page.readProfileValues(EnrollInput.ACCOUNT);
        AccountSupport.recordProfile("ACCOUNT-008", "Original profile", original);

        // Keep the existing method name so current TestNG commands still work.
        String[] fields = {"Họ và tên", "Mật khẩu", "Email", "Số điện thoại"};
        SoftAssert softly = new SoftAssert();
        boolean updateAttempted = false;
        Throwable primaryFailure = null;
        try {
            for (int emptyField = 0; emptyField < fields.length; emptyField++) {
                String stage = "Only " + fields[emptyField] + " empty";
                LOGGER.info("ACCOUNT-008 | {}", stage);

                // Start each case with a FRESH modal and fresh validation state.
                page.openEditProfile();
                Assert.assertTrue(page.readEditProfileControls().displayed(),
                        stage + ": Edit Profile modal must open.");
                page.clearEditProfileFields();

                // Exactly THREE valid fields, exactly ONE empty field.
                if (emptyField != 0) page.enterEditFullName(original.fullName());
                if (emptyField != 1) page.enterEditPassword(EnrollInput.PASSWORD);
                if (emptyField != 2) page.enterEditEmail(original.email());
                if (emptyField != 3) page.enterEditPhone(original.phone());

                var entered = page.readEditProfileValues(
                        emptyField == 1 ? "" : EnrollInput.PASSWORD);
                Assert.assertEquals(entered.fullName(), emptyField == 0 ? "" : original.fullName(),
                        stage + ": Full Name input setup.");
                Assert.assertTrue(entered.passwordMatches() && entered.passwordMasked(),
                        stage + ": Password must equal its expected value and stay masked.");
                Assert.assertEquals(entered.email(), emptyField == 2 ? "" : original.email(),
                        stage + ": Email input setup.");
                Assert.assertEquals(entered.phone(), emptyField == 3 ? "" : original.phone(),
                        stage + ": Phone input setup.");

                Allure.addAttachment("ACCOUNT-008 submitted scenario " + (emptyField + 1),
                        "text/plain", stage + "\nExpected: exactly one empty input; no raw password included.", ".txt");

                updateAttempted = true; // A defective application might still accept the invalid update.
                page.submitEditProfile();

                boolean nameEmpty = emptyField == 0;
                boolean passwordEmpty = emptyField == 1;
                boolean emailEmpty = emptyField == 2;
                boolean phoneEmpty = emptyField == 3;
                var state = page.waitForEditValidation(nameEmpty, passwordEmpty, emailEmpty, phoneEmpty);
                AccountSupport.recordValidation("ACCOUNT-008", stage, state, EnrollInput.PASSWORD);
                var warnings = state.warnings();

                softly.assertTrue(state.settled(), stage + ": validation response must settle.");
                softly.assertEquals(warnings.fullName(), nameEmpty ? RegisterMessage.Empty.FULL_NAME : "",
                        stage + ": Full Name warning (only empty input may have a warning).");
                softly.assertEquals(warnings.password(), passwordEmpty ? RegisterMessage.Empty.PASSWORD : "",
                        stage + ": Password warning (do not accept the known wrong 'Tài khoản' text).");
                softly.assertEquals(warnings.email(), emailEmpty ? RegisterMessage.Empty.EMAIL : "",
                        stage + ": Email warning (only empty input may have a warning).");
                softly.assertEquals(warnings.phone(), phoneEmpty ? RegisterMessage.Empty.PHONE : "",
                        stage + ": Phone warning (only empty input may have a warning).");
                softly.assertTrue(state.modalDisplayed(), stage + ": rejected update must leave the modal open.");
                softly.assertFalse(state.popupTitles().contains(AccountInput.UPDATE_SUCCESS_TITLE),
                        stage + ": the invalid update must not succeed.");

                // Hard safety guard: do not proceed if account mutation might have occurred.
                Assert.assertTrue(state.settled() && state.modalDisplayed()
                                && warnings.hasAny() && state.popupTitles().isEmpty(),
                        stage + ": unsafe to continue; expected settled validation in an open modal, "
                                + "at least one warning, and no update popup. See Allure evidence.");

                Assert.assertTrue(page.closeEditProfile(), stage + ": modal should close without submitting.");
                var refreshed = page.refreshProfileValues(EnrollInput.ACCOUNT, false);
                AccountSupport.recordProfile("ACCOUNT-008", stage + " / profile after refresh", refreshed);
                softly.assertEquals(refreshed.fullName(), original.fullName(),
                        stage + ": stored Full Name must remain unchanged.");
                softly.assertEquals(refreshed.email(), original.email(),
                        stage + ": stored Email must remain unchanged.");
                softly.assertEquals(refreshed.phone(), original.phone(),
                        stage + ": stored Phone must remain unchanged.");
            }
            softly.assertAll();
        } catch (RuntimeException | AssertionError failure) {
            AssertionError preserved = AccountSupport.validationFailure(
                    softly, failure, EnrollInput.PASSWORD);
            primaryFailure = preserved;
            throw preserved;
        } finally {
            AccountSupport.restoreProfile("ACCOUNT-008", driver,
                    EnrollInput.ACCOUNT, EnrollInput.PASSWORD,
                    original, updateAttempted, primaryFailure);
        }
        LOGGER.info("ACCOUNT-008 | Four single-empty-field checks complete; original profile preserved");
    }

    @Test(groups = {"account-tc9"}, testName = "ACCOUNT-009 - Reject invalid-format profile update",
            description = "ACCOUNT-009 - Reject the four existing invalid-format Register fixtures without changing the stored profile")
    @Story("Rejected profile updates")
    @Description("Login with EnrollInput, capture the original three personal fields, submit the four existing invalid-format Register fixtures "
            + "once, observe settled validation with the modal open and no success, close and refresh, "
            + "then compare the three stored values. Exact warning wording is outside this test.")
    public void invalidProfileUpdateShouldBeRejected() {
        LOGGER.info("ACCOUNT-009 | Starting rejection scenario: the four existing invalid-format Register fixtures");
        AccountPage page = AccountSupport.loginAndOpenAccount(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
        var original = page.readProfileValues(EnrollInput.ACCOUNT);
        AccountSupport.recordProfile("ACCOUNT-009", "Original profile", original);
        SoftAssert softly = new SoftAssert();
        boolean updateAttempted = false;
        Throwable primaryFailure = null;
        try {
            page.openEditProfile();
            Assert.assertTrue(page.readEditProfileControls().displayed(), "Edit Profile must be visible.");
            page.enterProfileUpdateValues(new AccountPage.ProfileValues(
                    RegisterInput.Wrong.FULL_NAME_WITH_NUMBER, RegisterInput.Wrong.EMAIL_INVALID,
                    RegisterInput.Wrong.PHONE_INVALID), RegisterInput.Wrong.PASSWORD_INVALID);
            var entered = page.readEditProfileValues(RegisterInput.Wrong.PASSWORD_INVALID);
            Assert.assertEquals(entered.fullName(), RegisterInput.Wrong.FULL_NAME_WITH_NUMBER,
                    "The original invalid Full Name fixture must be entered.");
            Assert.assertEquals(entered.email(), RegisterInput.Wrong.EMAIL_INVALID,
                    "The original invalid Email fixture must be entered.");
            Assert.assertEquals(entered.phone(), RegisterInput.Wrong.PHONE_INVALID,
                    "The original invalid Phone fixture must be entered.");
            Assert.assertTrue(entered.passwordMatches(), "The original invalid Password fixture must be entered.");
            Assert.assertTrue(entered.passwordMasked(), "Password must remain masked.");
            Allure.addAttachment("ACCOUNT-009 submitted validation scenario", "text/plain",
                    "the four existing invalid-format Register fixtures\n" + entered, ".txt");
            updateAttempted = true;
            page.submitEditProfile();
            var state = page.waitForEditRejection();
            AccountSupport.recordValidation("ACCOUNT-009", "Rejection response", state, EnrollInput.PASSWORD);
            softly.assertTrue(state.settled(), "An invalid submission must reach a settled rejection state.");
            softly.assertTrue(state.warnings().hasAny(), "An observable field validation response is required.");
            softly.assertTrue(state.modalDisplayed(), "The rejected form must remain open.");
            softly.assertFalse(state.popupTitles().contains(AccountInput.UPDATE_SUCCESS_TITLE),
                    "No update-success popup may appear during rejection observation.");
            softly.assertTrue(state.popupTitles().isEmpty(), "The invalid form must not produce an update popup.");

            page.waitUntilUpdatePopupClosed();
            page.closeEditProfileIfOpen();
            var refreshed = page.refreshProfileValues(EnrollInput.ACCOUNT, false);
            AccountSupport.recordProfile("ACCOUNT-009", "Profile after refresh", refreshed);
            Allure.addAttachment("ACCOUNT-009 original-profile equality", "text/plain",
                    "All three values unchanged: " + original.equals(refreshed), ".txt");
            softly.assertEquals(refreshed.fullName(), original.fullName(), "Rejected Full Name must remain unchanged.");
            softly.assertEquals(refreshed.email(), original.email(), "Rejected Email must remain unchanged.");
            softly.assertEquals(refreshed.phone(), original.phone(), "Rejected Phone must remain unchanged.");
            softly.assertAll();
        } catch (RuntimeException | AssertionError failure) {
            AssertionError preserved = AccountSupport.validationFailure(softly, failure, EnrollInput.PASSWORD);
            primaryFailure = preserved;
            throw preserved;
        } finally {
            AccountSupport.restoreProfile("ACCOUNT-009", driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD,
                    original, updateAttempted, primaryFailure);
        }
    }

    @Test(groups = {"account-tc10"}, testName = "ACCOUNT-010 - Reject each individually invalid profile field",
            description = "ACCOUNT-010 - Four isolated invalid-format submissions, with exactly one invalid field each")
    @Story("Format validation with otherwise valid profile data")
    @Description("For each of Name, Password, Email and Phone: enter valid original account data in the other "
            + "three fields, enter one existing invalid Register fixture, submit Hoàn thành, check the "
            + "field-specific format warning, then close, refresh and verify that the stored profile is unchanged. "
            + "Preserve product-defect failures while checking all four fields.")
    public void formatWarningsShouldClearIncrementally() {
        LOGGER.info("ACCOUNT-010 | Starting four independent single-invalid-field checks");
        AccountPage page = AccountSupport.loginAndOpenAccount(driver,
                EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
        var original = page.readProfileValues(EnrollInput.ACCOUNT);
        AccountSupport.recordProfile("ACCOUNT-010", "Original profile", original);

        // Keep the existing name so the current TestNG command continues to work.
        String[] fields = {"Họ và tên", "Mật khẩu", "Email", "Số điện thoại"};
        SoftAssert softly = new SoftAssert();
        boolean updateAttempted = false;
        Throwable primaryFailure = null;
        try {
            for (int invalidField = 0; invalidField < fields.length; invalidField++) {
                String stage = "Only " + fields[invalidField] + " invalid";
                LOGGER.info("ACCOUNT-010 | {}", stage);

                // A new modal and a clean validation state for EACH scenario.
                page.openEditProfile();
                Assert.assertTrue(page.readEditProfileControls().displayed(),
                        stage + ": Edit Profile modal must open.");
                page.clearEditProfileFields();

                String enteredName = invalidField == 0
                        ? RegisterInput.Wrong.FULL_NAME_WITH_NUMBER : original.fullName();
                String enteredPassword = invalidField == 1
                        ? RegisterInput.Wrong.PASSWORD_INVALID : EnrollInput.PASSWORD;
                String enteredEmail = invalidField == 2
                        ? RegisterInput.Wrong.EMAIL_INVALID : original.email();
                String enteredPhone = invalidField == 3
                        ? RegisterInput.Wrong.PHONE_INVALID : original.phone();

                page.enterEditFullName(enteredName);
                page.enterEditPassword(enteredPassword);
                page.enterEditEmail(enteredEmail);
                page.enterEditPhone(enteredPhone);

                var entered = page.readEditProfileValues(enteredPassword);
                Assert.assertEquals(entered.fullName(), enteredName,
                        stage + ": Full Name input setup.");
                Assert.assertTrue(entered.passwordMatches() && entered.passwordMasked(),
                        stage + ": Password must match supplied input and remain masked.");
                Assert.assertEquals(entered.email(), enteredEmail,
                        stage + ": Email input setup.");
                Assert.assertEquals(entered.phone(), enteredPhone,
                        stage + ": Phone input setup.");
                Allure.addAttachment("ACCOUNT-010 invalid scenario " + (invalidField + 1),
                        "text/plain", stage + "\nOther three fields use original valid values. No password included.", ".txt");

                // Invalid inputs MUST be rejected. Mark ownership before the click in case
                // a real application defect permits persistence despite validation.
                updateAttempted = true;
                page.submitEditProfile();

                boolean nameInvalid = invalidField == 0;
                boolean passwordInvalid = invalidField == 1;
                boolean emailInvalid = invalidField == 2;
                boolean phoneInvalid = invalidField == 3;
                var state = page.waitForEditValidation(
                        nameInvalid, passwordInvalid, emailInvalid, phoneInvalid);
                AccountSupport.recordValidation("ACCOUNT-010", stage, state, EnrollInput.PASSWORD);
                var warnings = state.warnings();

                softly.assertTrue(state.settled(), stage + ": validation response must settle.");
                softly.assertEquals(warnings.fullName(),
                        nameInvalid ? RegisterMessage.Format.FULL_NAME_INVALID : "",
                        stage + ": only an invalid Full Name may show the Name warning.");
                softly.assertEquals(warnings.email(),
                        emailInvalid ? RegisterMessage.Format.EMAIL_INVALID : "",
                        stage + ": only an invalid Email may show the Email warning.");
                softly.assertEquals(warnings.phone(),
                        phoneInvalid ? RegisterMessage.Format.PHONE_INVALID : "",
                        stage + ": preserve the documented Phone spelling in the expectation.");

                if (passwordInvalid) {
                    String actualPasswordWarning = warnings.password();
                    String normalized = actualPasswordWarning.toLowerCase(java.util.Locale.ROOT)
                            .replaceAll("\\s+", " ");
                    softly.assertTrue(RegisterMessage.PasswordRequirement.MINIMUM_LENGTH_PATTERN
                                    .matcher(normalized).matches(),
                            stage + ": Password warning must require 8 characters. Actual: "
                                    + actualPasswordWarning);
                    softly.assertTrue(RegisterMessage.PasswordRequirement.UPPERCASE_WORDING.stream()
                                    .anyMatch(normalized::contains),
                            stage + ": Password warning must mention uppercase. Actual: "
                                    + actualPasswordWarning);
                    softly.assertTrue(RegisterMessage.PasswordRequirement.LOWERCASE_WORDING.stream()
                                    .anyMatch(normalized::contains),
                            stage + ": Password warning must mention lowercase. Actual: "
                                    + actualPasswordWarning);
                    softly.assertTrue(normalized.contains(RegisterMessage.PasswordRequirement.NUMBER_WORDING),
                            stage + ": Password warning must mention numbers. Actual: "
                                    + actualPasswordWarning);
                    softly.assertTrue(RegisterMessage.PasswordRequirement.SPECIAL_CHARACTER_WORDING.stream()
                                    .anyMatch(normalized::contains),
                            stage + ": Password warning must mention special characters. Actual: "
                                    + actualPasswordWarning);
                } else {
                    softly.assertEquals(warnings.password(), "",
                            stage + ": the valid original Password must have no warning.");
                }

                softly.assertTrue(state.modalDisplayed(),
                        stage + ": an invalid update must leave the modal open.");
                softly.assertFalse(state.popupTitles().contains(AccountInput.UPDATE_SUCCESS_TITLE),
                        stage + ": invalid data must not cause a successful update.");

                // Stop if the application may have accepted the update. Do not hide
                // dangerous behavior behind a later refresh or another submission.
                Assert.assertTrue(state.settled() && state.modalDisplayed()
                                && warnings.hasAny() && state.popupTitles().isEmpty(),
                        stage + ": unsafe to continue; expected an open, rejected modal "
                                + "with validation warnings and no update popup.");

                // Crucial: THERE IS NO SUCCESS POPUP to wait for after rejection.
                // Close the rejected modal first, THEN refresh. Repeat independently.
                Assert.assertTrue(page.closeEditProfile(),
                        stage + ": rejected modal must close without submitting.");
                var refreshed = page.refreshProfileValues(EnrollInput.ACCOUNT, false);
                AccountSupport.recordProfile("ACCOUNT-010", stage + " / profile after refresh", refreshed);
                softly.assertEquals(refreshed.fullName(), original.fullName(),
                        stage + ": stored Full Name must remain unchanged.");
                softly.assertEquals(refreshed.email(), original.email(),
                        stage + ": stored Email must remain unchanged.");
                softly.assertEquals(refreshed.phone(), original.phone(),
                        stage + ": stored Phone must remain unchanged.");
            }
            softly.assertAll();
        } catch (RuntimeException | AssertionError failure) {
            AssertionError preserved = AccountSupport.validationFailure(
                    softly, failure, EnrollInput.PASSWORD);
            primaryFailure = preserved;
            throw preserved;
        } finally {
            AccountSupport.restoreProfile("ACCOUNT-010", driver,
                    EnrollInput.ACCOUNT, EnrollInput.PASSWORD,
                    original, updateAttempted, primaryFailure);
        }
        LOGGER.info("ACCOUNT-010 | Four single-invalid-field checks complete; original profile preserved");
    }

    @Test(groups = {"account-tc11"}, testName = "ACCOUNT-011 - Discard all unsaved profile edits",
            description = "ACCOUNT-011 - Đóng discards four unsaved inputs and leaves stored profile data unchanged")
    @Story("Unsaved profile changes")
    @Description("Enter different valid Name, Email and Phone plus the existing password. Click only the footer "
            + "Đóng control, reopen and inspect all four values, then close and refresh to verify the original profile.")
    public void unsavedProfileChangesShouldBeDiscarded() {
        LOGGER.info("ACCOUNT-011 | Starting all-field unsaved-edit check");
        AccountPage page = AccountSupport.loginAndOpenAccount(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
        var original = page.readProfileValues(EnrollInput.ACCOUNT);
        var replacement = AccountInput.differentValidProfile(original);
        AccountSupport.recordProfile("ACCOUNT-011", "Original profile", original);
        AccountSupport.recordProfile("ACCOUNT-011", "Replacement inputs - never submitted", replacement);
        SoftAssert softly = new SoftAssert();
        Throwable primaryFailure = null;
        try {
            page.openEditProfile();
            Assert.assertTrue(page.readEditProfileControls().displayed(), "Edit Profile must open.");
            page.enterProfileUpdateValues(replacement, EnrollInput.PASSWORD);
            var entered = page.readEditProfileValues(EnrollInput.PASSWORD);
            AccountSupport.recordAccountObservation("ACCOUNT-011", "Entered inputs", entered, EnrollInput.PASSWORD);
            Assert.assertEquals(entered.fullName(), replacement.fullName(), "Replacement Full Name must be entered.");
            Assert.assertEquals(entered.email(), replacement.email(), "Replacement Email must be entered.");
            Assert.assertEquals(entered.phone(), replacement.phone(), "Replacement Phone must be entered.");
            Assert.assertTrue(entered.passwordMatches() && entered.passwordMasked(),
                    "The existing password must be entered and masked.");

            var closed = page.closeEditProfileWithCloseButton();
            AccountSupport.recordAccountObservation("ACCOUNT-011", "First Đóng result", closed, EnrollInput.PASSWORD);
            softly.assertTrue(closed.settled(), "Đóng must reach a settled, non-interactive modal state.");
            softly.assertFalse(closed.modalDisplayed(), "Edit Profile must be hidden after Đóng.");
            softly.assertTrue(closed.updateInteractable(), "Cập nhật must be usable after the close transition.");
            softly.assertTrue(page.waitUntilAtAccountPage(), "Closing must keep the browser on Account.");
            softly.assertFalse(closed.popupTitles().contains(AccountInput.UPDATE_SUCCESS_TITLE),
                    "Closing without submission must not show update success.");
            Assert.assertTrue(closed.settled() && !closed.modalDisplayed() && closed.popupTitles().isEmpty(),
                    "Cannot safely reopen: Đóng must close normally without any update response.");

            page.openEditProfile();
            var reopened = page.captureEditProfileSnapshot(EnrollInput.PASSWORD);
            var values = reopened.values();
            AccountSupport.recordAccountObservation("ACCOUNT-011", "Inputs after reopening", reopened, EnrollInput.PASSWORD);

            softly.assertEquals(values.fullName(), replacement.fullName(),
                    "Unsaved Full Name must be retained after reopening.");

            softly.assertEquals(values.email(), replacement.email(),
                    "Unsaved Email must be retained after reopening.");

            softly.assertEquals(values.phone(), replacement.phone(),
                    "Unsaved Phone must be retained after reopening.");

            softly.assertTrue(values.passwordMatches(),
                    "Unsaved Password must be retained after reopening.");

            softly.assertTrue(values.passwordMasked(),
                    "Retained Password must remain masked.");


            var finalClose = page.closeEditProfileWithCloseButton();
            AccountSupport.recordAccountObservation("ACCOUNT-011", "Final Đóng result", finalClose, EnrollInput.PASSWORD);
            softly.assertTrue(finalClose.popupTitles().isEmpty(), "The final Đóng must not produce an update response.");
            Assert.assertTrue(finalClose.settled() && !finalClose.modalDisplayed(), "Đóng must close before refresh.");
            var refreshed = page.refreshProfileAfterUnsavedClose(EnrollInput.ACCOUNT);
            AccountSupport.recordProfile("ACCOUNT-011", "Stored profile after refresh", refreshed);
            softly.assertEquals(refreshed.fullName(), original.fullName(), "Stored Full Name must be unchanged.");
            softly.assertEquals(refreshed.email(), original.email(), "Stored Email must be unchanged.");
            softly.assertEquals(refreshed.phone(), original.phone(), "Stored Phone, including leading zeros, must be unchanged.");
            softly.assertAll();
        } catch (RuntimeException | AssertionError failure) {
            AssertionError preserved = AccountSupport.validationFailure(softly, failure, EnrollInput.PASSWORD);
            primaryFailure = preserved;
            throw preserved;
        } finally {
            AccountSupport.finishUnsavedProfileCheck("ACCOUNT-011", driver, EnrollInput.ACCOUNT,
                    EnrollInput.PASSWORD, original, primaryFailure);
        }
    }

    @Test(
            groups = {"account-tc12"},
            testName = "ACCOUNT-012 - Retain each individual unsaved profile edit",
            description = "ACCOUNT-012 - Each isolated draft survives Đóng/reopen but is never saved to the account"
    )
    @Story("Individual unsaved profile draft retention")
    @Description("Independently edit Name, Password, Email and Phone, leaving the other three inputs untouched. "
            + "After each Đóng, reopen and verify that the edited draft value is retained. "
            + "Close and refresh to confirm the stored profile is unchanged and the next attempt starts fresh. "
            + "Never click Hoàn thành.")
    public void individualUnsavedProfileChangesShouldBeRetained() {
        LOGGER.info("ACCOUNT-012 | Starting four isolated unsaved-edit retention checks");

        AccountPage page = AccountSupport.loginAndOpenAccount(
                driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
        var original = page.readProfileValues(EnrollInput.ACCOUNT);
        var replacement = AccountInput.differentValidProfile(original);

        AccountSupport.recordProfile("ACCOUNT-012", "Original profile", original);
        AccountSupport.recordProfile("ACCOUNT-012", "Replacement inputs - never submitted", replacement);

        SoftAssert softly = new SoftAssert();
        Throwable primaryFailure = null;
        AccountPage.EditProfileSnapshot firstBaseline = null;
        String[] fields = {"Full Name", "Password", "Email", "Phone"};

        try {
            for (int selected = 0; selected < fields.length; selected++) {
                String stage = "Attempt " + (selected + 1) + " - " + fields[selected] + " only";
                LOGGER.info("ACCOUNT-012 | {}", stage);

                // Each attempt starts from the original stored profile after the previous refresh.
                AccountSupport.recordProfile("ACCOUNT-012", stage + " / original profile", original);
                page.openEditProfile();
                Assert.assertTrue(page.readEditProfileControls().displayed(),
                        stage + ": Edit Profile modal must be visible.");

                var before = page.captureEditProfileSnapshot(EnrollInput.PASSWORD);
                var baseline = before.values();
                AccountSupport.recordAccountObservation(
                        "ACCOUNT-012", stage + " / fresh inputs", before, EnrollInput.PASSWORD);

                // A page refresh must have cleared the draft from the prior attempt.
                Assert.assertTrue(baseline.fullName().isEmpty()
                                || baseline.fullName().equals(original.fullName()),
                        stage + ": refreshed Name must be empty or its stored original value.");
                Assert.assertTrue(baseline.email().isEmpty()
                                || baseline.email().equals(original.email()),
                        stage + ": refreshed Email must be empty or its stored original value.");
                Assert.assertTrue(baseline.phone().isEmpty()
                                || baseline.phone().equals(original.phone()),
                        stage + ": refreshed Phone must be empty or its stored original value.");
                Assert.assertTrue(baseline.passwordMasked(),
                        stage + ": Password input must be masked.");

                if (firstBaseline == null) {
                    firstBaseline = before;
                } else {
                    Assert.assertTrue(page.isPasswordUnchangedSince(firstBaseline),
                            stage + ": refreshed Password input must match the initial baseline.");
                }
                if (selected == 1) {
                    Assert.assertFalse(baseline.passwordMatches(),
                            stage + ": Password must differ from the intended entry before typing.");
                }

                // Exactly ONE field is edited in this attempt.
                switch (selected) {
                    case 0 -> page.enterEditFullName(replacement.fullName());
                    case 1 -> page.enterEditPassword(EnrollInput.PASSWORD);
                    case 2 -> page.enterEditEmail(replacement.email());
                    case 3 -> page.enterEditPhone(replacement.phone());
                    default -> throw new IllegalStateException("Unknown ACCOUNT-012 field attempt.");
                }

                var entered = page.readEditProfileValues(EnrollInput.PASSWORD);
                AccountSupport.recordAccountObservation(
                        "ACCOUNT-012", stage + " / entered inputs", entered, EnrollInput.PASSWORD);

                Assert.assertEquals(entered.fullName(),
                        selected == 0 ? replacement.fullName() : baseline.fullName(),
                        stage + ": only Name attempt may modify Name.");
                Assert.assertEquals(entered.email(),
                        selected == 2 ? replacement.email() : baseline.email(),
                        stage + ": only Email attempt may modify Email.");
                Assert.assertEquals(entered.phone(),
                        selected == 3 ? replacement.phone() : baseline.phone(),
                        stage + ": only Phone attempt may modify Phone.");

                if (selected == 1) {
                    Assert.assertTrue(entered.passwordMatches(),
                            stage + ": existing Password must be entered.");
                } else {
                    Assert.assertTrue(page.isPasswordUnchangedSince(before),
                            stage + ": Password input must remain untouched.");
                }
                Assert.assertTrue(entered.passwordMasked(),
                        stage + ": Password input must remain masked.");

                // Đóng hides the modal; it does NOT discard draft input state.
                var closed = page.closeEditProfileWithCloseButton();
                AccountSupport.recordAccountObservation(
                        "ACCOUNT-012", stage + " / Đóng result", closed, EnrollInput.PASSWORD);

                softly.assertTrue(closed.settled(), stage + ": Đóng must settle.");
                softly.assertFalse(closed.modalDisplayed(), stage + ": modal must be hidden.");
                softly.assertTrue(closed.updateInteractable(),
                        stage + ": Cập nhật must be usable after Đóng.");
                softly.assertTrue(page.waitUntilAtAccountPage(),
                        stage + ": must remain on Account page.");
                softly.assertTrue(closed.popupTitles().isEmpty(),
                        stage + ": Đóng must not produce an update popup.");
                Assert.assertTrue(closed.settled() && !closed.modalDisplayed()
                                && closed.popupTitles().isEmpty(),
                        stage + ": unsafe to reopen after unexpected close or update response.");

                // Reopen WITHOUT refresh. The draft must still be present.
                page.openEditProfile();
                var reopened = page.captureEditProfileSnapshot(EnrollInput.PASSWORD);
                var values = reopened.values();
                AccountSupport.recordAccountObservation(
                        "ACCOUNT-012", stage + " / reopened inputs", reopened, EnrollInput.PASSWORD);

                softly.assertEquals(values.fullName(), entered.fullName(),
                        stage + ": draft Name input must persist through Đóng/reopen.");
                softly.assertEquals(values.email(), entered.email(),
                        stage + ": draft Email input must persist through Đóng/reopen.");
                softly.assertEquals(values.phone(), entered.phone(),
                        stage + ": draft Phone input must persist through Đóng/reopen.");

                if (selected == 1) {
                    softly.assertTrue(values.passwordMatches(),
                            stage + ": draft Password must persist through Đóng/reopen.");
                } else {
                    softly.assertTrue(page.isPasswordUnchangedSince(before),
                            stage + ": untouched Password must remain unchanged.");
                }
                softly.assertTrue(values.passwordMasked(),
                        stage + ": reopened Password must remain masked.");

                // Close before refresh. Refresh resets the draft, NOT the saved profile.
                var finalClose = page.closeEditProfileWithCloseButton();
                AccountSupport.recordAccountObservation(
                        "ACCOUNT-012", stage + " / final Đóng result", finalClose, EnrollInput.PASSWORD);
                softly.assertTrue(finalClose.popupTitles().isEmpty(),
                        stage + ": final Đóng must not submit an update.");
                Assert.assertTrue(finalClose.settled() && !finalClose.modalDisplayed(),
                        stage + ": modal must close before refresh.");

                var refreshed = page.refreshProfileAfterUnsavedClose(EnrollInput.ACCOUNT);
                AccountSupport.recordProfile(
                        "ACCOUNT-012", stage + " / stored profile after refresh", refreshed);
                softly.assertEquals(refreshed.fullName(), original.fullName(),
                        stage + ": stored Name must be unchanged after refresh.");
                softly.assertEquals(refreshed.email(), original.email(),
                        stage + ": stored Email must be unchanged after refresh.");
                softly.assertEquals(refreshed.phone(), original.phone(),
                        stage + ": stored Phone must be unchanged after refresh.");

                // Stop if the server-backed profile actually changed; cleanup will run.
                Assert.assertEquals(refreshed, original,
                        stage + ": stored profile mutated unexpectedly; cannot continue safely.");
            }
            softly.assertAll();
        } catch (RuntimeException | AssertionError failure) {
            AssertionError preserved = AccountSupport.validationFailure(
                    softly, failure, EnrollInput.PASSWORD);
            primaryFailure = preserved;
            throw preserved;
        } finally {
            AccountSupport.finishUnsavedProfileCheck(
                    "ACCOUNT-012", driver, EnrollInput.ACCOUNT,
                    EnrollInput.PASSWORD, original, primaryFailure);
        }
    }


    @Test(
            groups = {"account-tc13"},
            testName = "ACCOUNT-013 - Guest Account page is blank",
            description = "Verify the Account page is blank when accessed as guest"
    )
    @Story("Guest Account access")
    @Description("Open Account page as guest, check blank page, finish.")
    public void guestShouldNotAccessPersonalInformation() {
        LOGGER.info("ACCOUNT-013 | Checking guest Account page");

        AccountPage page = new AccountPage(driver);
        page.openAccountAsGuest();

        Assert.assertTrue(
                page.isGuestAccountPageBlank(),
                "Guest Account page must be blank."
        );

        LOGGER.info("ACCOUNT-013 | Blank page verified");
    }


    @Test(groups = {"account-tc14"}, testName = "ACCOUNT-014 - Switch account sessions",
            description = "ACCOUNT-014 - Login A, logout, Login B and retain B's identity/profile after refresh")
    @Story("Account session isolation")
    @Description("Use the two existing accounts. Assert labeled account identity, capture each profile, "
            + "use application logout, and compare B's captured profile after refresh. Never edit either account.")
    public void accountSwitchShouldDisplayCorrectUser() {
        AccountPage page = AccountSupport.loginAndOpenAccount(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
        HomePage home = new HomePage(driver);
        Assert.assertTrue(home.isAuthenticated(), "User A Login must succeed.");
        var userA = page.readPersonalInformation(EnrollInput.ACCOUNT);
        AccountSupport.recordAccountObservation("ACCOUNT-014", "User A identity and profile", userA, EnrollInput.PASSWORD);
        Assert.assertEquals(userA.account(), EnrollInput.ACCOUNT, "Account A must display A's labeled username.");

        boolean loggedOut = home.logoutIfAuthenticated();
        boolean guest = !home.isAuthenticated();
        AccountSupport.recordAccountObservation("ACCOUNT-014", "Application logout",
                "Logout clicked=" + loggedOut + "; guest confirmed=" + guest, null);
        Assert.assertTrue(loggedOut && guest, "User A's application session must end before Login B.");

        page = AccountSupport.loginAndOpenAccount(driver, AccountInput.UPDATE_ACCOUNT, AccountInput.UPDATE_PASSWORD);
        Assert.assertTrue(home.isAuthenticated(), "User B Login must succeed.");
        var userB = page.readPersonalInformation(AccountInput.UPDATE_ACCOUNT);
        AccountSupport.recordAccountObservation("ACCOUNT-014", "User B identity and profile", userB, AccountInput.UPDATE_PASSWORD);
        Assert.assertEquals(userB.account(), AccountInput.UPDATE_ACCOUNT, "Account B must display B's labeled username.");
        Assert.assertNotEquals(userB.account(), userA.account(), "Account B must not display A as the current account.");

        var refreshed = page.refreshPersonalInformation(AccountInput.UPDATE_ACCOUNT);
        AccountSupport.recordAccountObservation("ACCOUNT-014", "User B after refresh", refreshed, AccountInput.UPDATE_PASSWORD);
        Assert.assertEquals(refreshed.account(), AccountInput.UPDATE_ACCOUNT, "Refresh must retain User B's identity.");
        Assert.assertEquals(refreshed, userB, "All six displayed User B profile values must remain consistent.");
    }

    @Test(groups = {"account-tc15"}, testName = "ACCOUNT-015 - Stale tab cannot persist profile changes",
            description = "ACCOUNT-015 - Independently exercise four inputs and three controls after logout in Tab B")
    @Story("Profile integrity after multi-tab logout")
    @Description("Seven independent two-tab arrangements in one browser. Observe blank UI before/after each "
            + "selected action, submit only complete valid drafts, freshly log in and compare three stored values. "
            + "Restore detected mutations without removing the security failure.")
    public void loggedOutTabShouldNotUpdateProfile() {
        AccountPage initial = AccountSupport.loginAndOpenAccount(driver,
                AccountInput.UPDATE_ACCOUNT, AccountInput.UPDATE_PASSWORD);
        var original = initial.readProfileValues(AccountInput.UPDATE_ACCOUNT);
        var replacement = AccountInput.differentValidProfile(original);
        AccountSupport.recordProfile("ACCOUNT-015", "Original three values", original);
        SoftAssert softly = new SoftAssert();

        for (StaleProfileAction action : StaleProfileAction.values()) {
            String stage = "Attempt " + (action.ordinal() + 1) + " - " + action;
            try (var tabs = new AccountSupport.AccountTabs(driver)) {
                try {
                    AccountPage page = tabs.loginFresh(AccountInput.UPDATE_ACCOUNT, AccountInput.UPDATE_PASSWORD);
                    var starting = page.refreshProfileValues(AccountInput.UPDATE_ACCOUNT, false);
                    AccountSupport.recordProfile("ACCOUNT-015", stage + " / authenticated starting profile", starting);
                    Assert.assertEquals(starting, original, stage + ": original profile required before an independent attempt.");

                    var prepared = page.prepareProfileBeforeLogout(action, original, replacement, AccountInput.UPDATE_PASSWORD);
                    AccountSupport.recordAccountObservation("ACCOUNT-015", stage + " / preparation", prepared, AccountInput.UPDATE_PASSWORD);
                    Assert.assertTrue(prepared.ready(), stage + ": other fields must be valid before logout; selected field untouched.");
                    if (action == StaleProfileAction.COMPLETE || action == StaleProfileAction.CLOSE) {
                        Assert.assertTrue(page.isCompleteStaleDraftReady(prepared.intended(), AccountInput.UPDATE_PASSWORD),
                                stage + ": complete valid draft and existing masked password required before logout.");
                    }

                    var logout = tabs.logoutInSecondTab(AccountInput.UPDATE_ACCOUNT);
                    AccountSupport.recordAccountObservation("ACCOUNT-015", stage + " / Tab B logout", logout, null);
                    Assert.assertTrue(logout.sharedAuthentication(), stage + ": Tab B must share authentication.");
                    Assert.assertEquals(logout.sharedAccount(), AccountInput.UPDATE_ACCOUNT, stage + ": shared identity.");
                    Assert.assertTrue(logout.logoutClicked() && logout.guestConfirmed(), stage + ": actual logout must complete.");

                    tabs.returnToOriginalTab();
                    var observed = page.actOnStaleProfileOnce(action, prepared.intended(), AccountInput.UPDATE_PASSWORD);
                    AccountSupport.recordAccountObservation("ACCOUNT-015", stage + " / selected action", observed, AccountInput.UPDATE_PASSWORD);
                    softly.assertTrue(observed.completed() || observed.before().blank() || observed.after().blank()
                                    || observed.after().loginDisplayed() || !observed.before().targetAvailable()
                                    || !observed.after().targetAvailable(),
                            stage + ": an available action must execute or show observable blocking; " + observed.problem());

                    // Only the four input stages may follow up with a valid submission.
                    // Blank observations end stale-page interaction immediately.
                    if (action.isField() && !observed.after().blank() && observed.after().formUsable()) {
                        boolean valid = page.isCompleteStaleDraftReady(prepared.intended(), AccountInput.UPDATE_PASSWORD);
                        AccountSupport.recordAccountObservation("ACCOUNT-015", stage + " / fully valid draft before submission",
                                valid, null);
                        softly.assertTrue(valid, stage + ": incomplete/invalid form is not evidence of logout protection.");
                        if (valid) {
                            var submission = page.actOnStaleProfileOnce(StaleProfileAction.COMPLETE,
                                    prepared.intended(), AccountInput.UPDATE_PASSWORD);
                            AccountSupport.recordAccountObservation("ACCOUNT-015", stage + " / valid-form submission",
                                    submission, AccountInput.UPDATE_PASSWORD);
                            softly.assertTrue(submission.completed() || submission.before().blank() || submission.after().blank()
                                            || submission.after().loginDisplayed() || !submission.before().targetAvailable()
                                            || !submission.after().targetAvailable(),
                                    stage + ": valid-form submit could not execute or demonstrate blocking; " + submission.problem());
                        }
                    }
                } catch (RuntimeException | AssertionError failure) {
                    var preserved = AccountSupport.staleAttemptFailure(stage, failure, AccountInput.UPDATE_PASSWORD);
                    AccountSupport.recordAccountObservation("ACCOUNT-015", stage + " / interrupted", preserved, AccountInput.UPDATE_PASSWORD);
                    softly.fail(stage + ": setup/action failed; remaining stages still run.", preserved);
                } finally {
                    // This runs even after blank rendering, input/click errors or failed prerequisites.
                    try {
                        var stored = AccountSupport.verifyAndRestoreStaleProfile(tabs, stage,
                                AccountInput.UPDATE_ACCOUNT, AccountInput.UPDATE_PASSWORD, original);
                        softly.assertEquals(stored.fullName(), original.fullName(), stage + ": unauthorized stored Full Name change.");
                        softly.assertEquals(stored.email(), original.email(), stage + ": unauthorized stored Email change.");
                        softly.assertEquals(stored.phone(), original.phone(), stage + ": unauthorized stored Phone change (leading zeros preserved).");
                    } catch (RuntimeException | AssertionError failure) {
                        softly.fail(stage + ": persisted verification/restoration failed.",
                                AccountSupport.staleAttemptFailure(stage, failure, AccountInput.UPDATE_PASSWORD));
                    }
                }
            } catch (RuntimeException | AssertionError windowFailure) {
                softly.fail(stage + ": window setup/cleanup failed.",
                        AccountSupport.staleAttemptFailure(stage, windowFailure, AccountInput.UPDATE_PASSWORD));
            }
        }
        softly.assertAll();
    }

    @Test(groups = {"account-tc16"}, testName = "ACCOUNT-016 - Stale tab cannot cancel a Course",
            description = "ACCOUNT-016 - A test-owned Course remains after Tab B logout and Tab A cancellation")
    @Story("Enrollment integrity after multi-tab logout")
    @Description("Select an absent exact Course, enroll it, logout in a second tab, click its cancellation once, "
            + "freshly authenticate and assert one enrollment remains. Finally cancel only this test's Course "
            + "under its original account and verify zero after refresh, preserving any security failure.")
    public void loggedOutTabShouldNotCancelEnrolledCourse() {
        try (var tabs = new AccountSupport.AccountTabs(driver)) {
            String courseName = null;
            boolean enrollmentAttempted = false;
            Throwable primaryFailure = null;
            try {
                tabs.loginFresh(EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
                var selected = AccountSupport.selectAbsentCourseForAccountTest(driver, EnrollInput.ACCOUNT);
                courseName = selected.courseName();
                HomePage home = new HomePage(driver);
                // Recheck absence immediately before returning to the exact captured Course Detail.
                home.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT).requireCourseNotEnrolled(courseName);
                var detail = home.goToCourseCategory(selected.categoryUrl()).openCourseForEnrollment(selected);
                enrollmentAttempted = true;
                detail.clickEnroll();
                String title = detail.waitForEnrollmentPopupTitle();
                AccountSupport.recordAccountObservation("ACCOUNT-016", "Enrollment popup", title, null);
                Assert.assertEquals(title, EnrollInput.SUCCESS_TITLE, "Owned Course must enroll successfully.");
                detail.waitUntilEnrollmentPopupClosed();

                AccountPage page = home.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
                Assert.assertTrue(page.waitUntilCoursePresent(courseName), "Exact owned Course must be displayed.");
                int before = page.countExactCourseItems(courseName);
                AccountSupport.recordAccountObservation("ACCOUNT-016", "My Courses before logout",
                        "Course=" + courseName + "; exact count=" + before, null);
                Assert.assertEquals(before, 1, "Exactly one test-owned enrollment required.");
                Assert.assertTrue(page.isExactCourseCancellationAvailable(courseName), "Exact Course cancellation must be available before logout.");

                var logout = tabs.logoutInSecondTab(EnrollInput.ACCOUNT);
                AccountSupport.recordAccountObservation("ACCOUNT-016", "Tab B logout", logout, null);
                Assert.assertTrue(logout.sharedAuthentication(), "Tab B must share authentication.");
                Assert.assertEquals(logout.sharedAccount(), EnrollInput.ACCOUNT, "Tab B must identify the Course owner.");
                Assert.assertTrue(logout.logoutClicked() && logout.guestConfirmed(), "Application logout must complete.");
                tabs.returnToOriginalTab();
                var observation = page.cancelExactCourseOnceAndObserve(courseName);
                AccountSupport.recordAccountObservation("ACCOUNT-016", "Stale Tab A cancellation", observation, null);

                // The fresh-login count is the security result, regardless of blank/popup UI.
                page = tabs.loginFresh(EnrollInput.ACCOUNT, EnrollInput.PASSWORD).openMyCourses(EnrollInput.ACCOUNT)
                        .refreshMyCourses(EnrollInput.ACCOUNT);
                int after = page.countExactCourseItems(courseName);
                AccountSupport.recordAccountObservation("ACCOUNT-016", "My Courses after fresh Login + refresh",
                        "Course=" + courseName + "; exact count=" + after, null);
                Assert.assertEquals(after, 1, "Unauthorized cancellation removed/changed the test-owned enrollment: " + courseName);
                Assert.assertTrue(observation.completed() || observation.before().blank() || observation.after().blank()
                                || observation.after().loginDisplayed() || !observation.before().targetAvailable()
                                || !observation.after().targetAvailable(),
                        "Available exact cancellation must execute or show observable blocking: " + observation.problem());
            } catch (RuntimeException | AssertionError failure) {
                AssertionError preserved = AccountSupport.staleAttemptFailure("ACCOUNT-016", failure, EnrollInput.PASSWORD);
                primaryFailure = preserved;
                throw preserved;
            } finally {
                AccountSupport.finishStaleCourseCheck(tabs, EnrollInput.ACCOUNT, EnrollInput.PASSWORD,
                        courseName, enrollmentAttempted, primaryFailure);
            }
        }
    }

}
