package vn.cybersoft.vlearning.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.cybersoft.vlearning.core.BasePage;

public class LoginPage extends BasePage {

    private static final By ACCOUNT_INPUT =
            By.xpath("//form[.//h1[normalize-space()='Đăng nhập']]//input[@name='taiKhoan']");

    private static final By PASSWORD_INPUT =
            By.xpath("//form[.//h1[normalize-space()='Đăng nhập']]//input[@name='matKhau']");

    private static final By REGISTER_BUTTON =
            By.xpath("//div[contains(@class,'overlay-panel') and contains(@class,'overlay-right')]//button[@id='signUp' and normalize-space()='Đăng ký']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAccountInputDisplayed() {
        return isDisplayed(ACCOUNT_INPUT);
    }

    public boolean isPasswordInputDisplayed() {
        return isDisplayed(PASSWORD_INPUT);
    }

    public RegisterPage goToRegisterPage() {
        click(REGISTER_BUTTON);
        return new RegisterPage(driver);
    }
}
