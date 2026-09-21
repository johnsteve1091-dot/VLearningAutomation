package vn.cybersoft.vlearning.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.cybersoft.vlearning.core.BasePage;

public class HomePage extends BasePage {

    private static final By LOGIN_BUTTON =
            By.xpath("//a[@href='/login' and normalize-space()='Đăng nhập']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public LoginPage goToLoginPage() {
        click(LOGIN_BUTTON);
        return new LoginPage(driver);
    }
}
