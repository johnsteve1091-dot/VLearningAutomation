package vn.cybersoft.vlearning.data.login;

import java.util.UUID;

import org.openqa.selenium.WebDriver;

import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;

public final class LoginSupport {

    private LoginSupport() {
    }

    public static String generateInvalidAccount() {
        // This synthetic account also exceeds the current 16-character registration limit.
        return "invaliduser" + UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateInvalidPassword() {
        return "Invalid!" + UUID.randomUUID();
    }

    /** TC4/TC5 recovery only; EnrollmentSupport remains cancellation-only. */
    public static void restoreSessionForCleanup(WebDriver driver, String account, String password) {
        HomePage homePage = new HomePage(driver);
        if (homePage.isAuthenticated()) return;
        LoginPage loginPage = homePage.goToLoginPage();
        loginPage.enterAccount(account);
        loginPage.enterPassword(password);
        loginPage.clickLogin();
        if (!loginPage.waitUntilLoggedIn()) {
            throw new IllegalStateException("Could not restore Login for owned-Course cleanup.");
        }
        homePage.waitUntilAuthenticated();
    }
}
