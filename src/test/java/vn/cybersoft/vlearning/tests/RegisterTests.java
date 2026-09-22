package vn.cybersoft.vlearning.tests;

import java.util.Locale;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;
import vn.cybersoft.vlearning.pages.RegisterPage;

public class RegisterTests extends BaseTest {

    @Test
    public void registerFormShouldDisplayAllRequiredControls() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        SoftAssert softly = new SoftAssert();

        softly.assertTrue(registerPage.isAccountInputDisplayed(), "Tài khoản input should be displayed.");
        softly.assertTrue(registerPage.isFullNameInputDisplayed(), "Họ tên input should be displayed.");
        softly.assertTrue(registerPage.isPasswordInputDisplayed(), "Mật khẩu input should be displayed.");
        softly.assertTrue(registerPage.isEmailInputDisplayed(), "Email input should be displayed.");
        softly.assertTrue(registerPage.isPhoneInputDisplayed(), "Số điện thoại input should be displayed.");
//        softly.assertTrue(registerPage.isGroupDropdownDisplayed(), "Mã nhóm dropdown should be displayed.");
        softly.assertTrue(registerPage.isRegisterButtonDisplayed(), "Đăng ký button should be displayed.");

        softly.assertAll();
    }

    @Test
    public void emptySubmissionShouldDisplayRequiredValidationMessages() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        SoftAssert softly = new SoftAssert();

        registerPage.clickRegister();

        softly.assertEquals(registerPage.getAccountErrorMessage(),
                "Tài khoản không được để trống",
                "Tài khoản required warning.");
        softly.assertEquals(registerPage.getFullNameErrorMessage(),
                "Tên không được để trống",
                "Họ tên required warning.");
        softly.assertEquals(
                registerPage.getPasswordErrorMessage(),
                "Mật khẩu không được để trống",
                "Password required warning."
        );
        softly.assertEquals(registerPage.getEmailErrorMessage(),
                "Email không được để trống",
                "Email required warning.");
        softly.assertEquals(registerPage.getPhoneErrorMessage(),
                "Số điện thoại không được để trống",
                "Số điện thoại required warning.");
        softly.assertAll();
    }

    @Test
    public void invalidSubmissionShouldDisplayFormatValidationMessages() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        SoftAssert softly = new SoftAssert();

        registerPage.enterAccount("a");
        registerPage.enterFullName("a1");
        registerPage.enterPassword(".");
        registerPage.enterEmail("a@gm");
        registerPage.enterPhone("a");
        registerPage.clickRegister();

        softly.assertEquals(registerPage.getAccountErrorMessage(), "Tài khoản quá ít kí tự",
                "Tài khoản format warning.");
        softly.assertEquals(registerPage.getFullNameErrorMessage(), "Chỉ nhập kí tự chữ",
                "Họ tên format warning.");

        String passwordError = registerPage.getPasswordErrorMessage();
        String normalizedPasswordError = passwordError.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        // Check the requirement concepts without inventing a complete corrected sentence.
        softly.assertTrue(normalizedPasswordError.matches("(?s).*(?:ít nhất|tối thiểu)\\s+8\\b.*"),
                "Password warning must state a minimum of 8 characters. Actual: " + passwordError);
        softly.assertTrue(normalizedPasswordError.contains("chữ hoa")
                        || normalizedPasswordError.contains("chữ in hoa")
                        || normalizedPasswordError.contains("viết hoa"),
                "Password warning must mention uppercase letters. Actual: " + passwordError);
        softly.assertTrue(normalizedPasswordError.contains("chữ thường")
                        || normalizedPasswordError.contains("chữ in thường")
                        || normalizedPasswordError.contains("viết thường"),
                "Password warning must mention lowercase letters. Actual: " + passwordError);
        softly.assertTrue(normalizedPasswordError.contains("số"),
                "Password warning must mention a number. Actual: " + passwordError);
        softly.assertTrue(normalizedPasswordError.contains("kí tự đặc biệt")
                        || normalizedPasswordError.contains("ký tự đặc biệt"),
                "Password warning must mention a special character. Actual: " + passwordError);

        softly.assertEquals(registerPage.getEmailErrorMessage(), "Email không hợp lệ",
                "Email format warning.");
        softly.assertEquals(registerPage.getPhoneErrorMessage(), "Số điện thoại chưa đúng định dạng",
                "Số điện thoại format warning.");

        softly.assertAll();
    }
}
