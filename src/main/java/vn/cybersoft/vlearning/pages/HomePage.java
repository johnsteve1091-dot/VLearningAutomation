package vn.cybersoft.vlearning.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import vn.cybersoft.vlearning.core.BasePage;

public class HomePage extends BasePage {

    private static final By LOGIN_BUTTON =
            By.xpath("//a[@href='/login' and normalize-space()='Đăng nhập']");
    private static final By COURSE_LIST_LINK =
            By.xpath("//a[@href='/khoahoc' and normalize-space()='Khóa học']");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public LoginPage goToLoginPage() {
        click(LOGIN_BUTTON);
        return new LoginPage(driver);
    }

    public CourseListPage goToCourseListPage() {
        click(COURSE_LIST_LINK);
        return new CourseListPage(driver);
    }
}
