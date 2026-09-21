package vn.cybersoft.vlearning.tests;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.Test;
import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class LoginTests extends BaseTest {


    @Test
    public void accountInputShouldBeDisplayedOnLoginPage() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        Assert.assertTrue(loginPage.isAccountInputDisplayed(),
                "Account input should be displayed on the LoginPage.");
    }

    @Test
    public void passwordInputShouldBeDisplayedOnLoginPage() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();

        Assert.assertTrue(loginPage.isPasswordInputDisplayed(),
                "Password input should be displayed on the LoginPage.");
    }


}
