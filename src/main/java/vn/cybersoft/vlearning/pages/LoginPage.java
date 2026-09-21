package vn.cybersoft.vlearning.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.cybersoft.vlearning.core.BasePage;

public class LoginPage extends BasePage {

    private static final By ACCOUNT_INPUT =
            By.xpath("//form[.//h1[normalize-space()='Đăng nhập']]//input[@name='taiKhoan']");

    private static final By PASSWORD_INPUT =
            By.xpath("//form[.//h1[normalize-space()='Đăng nhập']]//input[@name='matKhau']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAccountInputDisplayed() {
        return isDisplayed(ACCOUNT_INPUT);
    }

    public boolean isPasswordInputDisplayed() {
        return isDisplayed(PASSWORD_INPUT);
    }
}