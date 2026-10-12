package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.enroll.EnrollInput;
import vn.cybersoft.vlearning.data.register.RegisterInput;
import vn.cybersoft.vlearning.data.register.RegisterSupport;
import vn.cybersoft.vlearning.pages.AccountPage;
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
}
