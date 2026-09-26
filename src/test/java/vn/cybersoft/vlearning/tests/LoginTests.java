package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.login.LoginSupport;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;

@Epic("V Learning")
@Feature("Login")
public class LoginTests extends BaseTest {

    private static final String INVALID_CREDENTIALS_TITLE = "Tài khoản hoặc mật khẩu không đúng!";

    @Test(testName = "LOG-001 - Login form displays all required controls",
            description = "LOG-001 - Login form displays all required controls")
    @Story("Login form UI")
    @Description("Open the Login page and verify that Account, Password, Login button and Forgot Password link are displayed.")
    public void loginFormShouldDisplayAllRequiredControls() {
        LoginPage loginPage = new HomePage(driver).goToLoginPage();
        SoftAssert softly = new SoftAssert();

        softly.assertTrue(loginPage.isAccountInputDisplayed(), "Account input should be displayed.");
        softly.assertTrue(loginPage.isPasswordInputDisplayed(), "Password input should be displayed.");
        softly.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button should be displayed.");
        softly.assertTrue(loginPage.isForgotPasswordLinkDisplayed(), "Forgot Password link should be displayed.");
        softly.assertAll();
    }

    @Test(testName = "LOG-002 - Password is masked",
            description = "LOG-002 - Password is masked")
    @Story("Password security")
    @Description("Enter a dummy password and verify that the Login password field uses password input masking.")
    public void passwordShouldBeMasked() {
        LoginPage loginPage = new HomePage(driver).goToLoginPage();
        loginPage.enterPassword("DummyPassword123!");

        Assert.assertTrue(loginPage.isPasswordMasked(), "Password input should have type=\"password\".");
    }


    @Test(testName = "LOG-003 - Empty Login submission displays invalid-credentials popup",
            description = "LOG-003 - Empty Login submission displays invalid-credentials popup")
    @Story("Invalid Login")
    @Description("Submit the Login form with Account and Password empty and verify that the invalid-credentials SweetAlert appears.")
    public void emptySubmissionShouldDisplayInvalidCredentialsPopup() {
        LoginPage loginPage = new HomePage(driver).goToLoginPage();
        loginPage.enterAccount("");
        loginPage.enterPassword("");
        loginPage.clickLogin();

        Assert.assertTrue(loginPage.isInvalidCredentialsPopupDisplayed(),
                "Invalid-credentials popup should appear for an empty Login submission.");
        Assert.assertEquals(loginPage.getInvalidCredentialsPopupTitle(), INVALID_CREDENTIALS_TITLE,
                "Invalid-credentials popup title.");
    }

    @Test(testName = "LOG-004 - Random invalid credentials display invalid-credentials popup",
            description = "LOG-004 - Random invalid credentials display invalid-credentials popup")
    @Story("Invalid Login")
    @Description("Submit randomized non-valid Login credentials and verify that the invalid-credentials SweetAlert appears.")
    public void randomInvalidCredentialsShouldDisplayInvalidCredentialsPopup() {
        LoginPage loginPage = new HomePage(driver).goToLoginPage();
        loginPage.enterAccount(LoginSupport.generateInvalidAccount());
        loginPage.enterPassword(LoginSupport.generateInvalidPassword());
        loginPage.clickLogin();

        Assert.assertTrue(loginPage.isInvalidCredentialsPopupDisplayed(),
                "Invalid-credentials popup should appear for randomized invalid credentials.");
        Assert.assertEquals(loginPage.getInvalidCredentialsPopupTitle(), INVALID_CREDENTIALS_TITLE,
                "Invalid-credentials popup title.");
    }
}
