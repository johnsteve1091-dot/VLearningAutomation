package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.testng.Assert;
import org.testng.annotations.Test;
import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;

@Epic("V Learning")
@Feature("Login")
public class LoginTests extends BaseTest {


    @Test(testName = "LOG-001 - Account input is displayed on Login page", description = "LOG-001 - Account input is displayed on Login page")
    @Story("Login form UI")
    @Description("Open the Homepage, navigate to Login and verify that the Account input is displayed.")
    public void accountInputShouldBeDisplayedOnLoginPage() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        Assert.assertTrue(loginPage.isAccountInputDisplayed(),
                "Account input should be displayed on the LoginPage.");
    }

    @Test(testName = "LOG-002 - Password input is displayed on Login page", description = "LOG-002 - Password input is displayed on Login page")
    @Story("Login form UI")
    @Description("Open the Homepage, navigate to Login and verify that the Password input is displayed.")
    public void passwordInputShouldBeDisplayedOnLoginPage() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();

        Assert.assertTrue(loginPage.isPasswordInputDisplayed(),
                "Password input should be displayed on the LoginPage.");
    }


}
