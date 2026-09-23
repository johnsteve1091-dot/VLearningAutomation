package vn.cybersoft.vlearning.tests;

import java.util.Locale;
import java.util.UUID;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;
import vn.cybersoft.vlearning.pages.RegisterPage;

public class RegisterTests extends BaseTest {

    // SET THESE to known existing database values before running duplicate tests alone.
    private static final String FALLBACK_EXISTING_ACCOUNT =
            "REPLACE_WITH_EXISTING_ACCOUNT";
    private static final String FALLBACK_EXISTING_EMAIL =
            "REPLACE_WITH_EXISTING_EMAIL";

    private static final String VALID_FULL_NAME = "Tester Group";
    private static final String VALID_PASSWORD = "AutoTest@123";
    private static final String VALID_PHONE = "0901234567";

    // Populated only after the success popup has been verified in this JVM.
    private static String successfullyRegisteredAccount;
    private static String successfullyRegisteredEmail;

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

    @Test(priority = 4)
    public void registrationShouldSucceedWithValidUniqueData() {
        // A failed rerun must not leave an older pair marked as this run's success.
        successfullyRegisteredAccount = null;
        successfullyRegisteredEmail = null;

        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        String account = generateUniqueAccount();
        String email = generateUniqueEmail();

        registerPage.enterAccount(account);
        registerPage.enterFullName(VALID_FULL_NAME);
        registerPage.enterPassword(VALID_PASSWORD);
        registerPage.enterEmail(email);
        registerPage.enterPhone(VALID_PHONE);
        registerPage.clickRegister();

        Assert.assertTrue(registerPage.isSuccessfulRegistrationPopupDisplayed(),
                "The Đăng kí thành công popup should appear for valid unique data.");
        successfullyRegisteredAccount = account;
        successfullyRegisteredEmail = email;
    }

    @Test(priority = 5)
    public void registrationShouldFailWhenAccountAlreadyExists() {
        String existingAccount = successfullyRegisteredAccount != null
                && !successfullyRegisteredAccount.isBlank()
                ? successfullyRegisteredAccount : FALLBACK_EXISTING_ACCOUNT;
        Assert.assertTrue(existingAccount != null && !existingAccount.isBlank()
                        && !existingAccount.equals("REPLACE_WITH_EXISTING_ACCOUNT"),
                "Missing existing account: set FALLBACK_EXISTING_ACCOUNT in RegisterTests "
                        + "to a known existing database account, or run "
                        + "registrationShouldSucceedWithValidUniqueData successfully first.");

        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        String uniqueEmail = generateUniqueEmail();

        registerPage.enterAccount(existingAccount);
        registerPage.enterFullName(VALID_FULL_NAME);
        registerPage.enterPassword(VALID_PASSWORD);
        registerPage.enterEmail(uniqueEmail);
        registerPage.enterPhone(VALID_PHONE);
        registerPage.clickRegister();

        Assert.assertTrue(registerPage.isExistingAccountPopupDisplayed(),
                "The Tài khoản đã tồn tại! popup should appear for an existing account "
                        + "with a new unique email.");
    }

    @Test(priority = 6)
    public void registrationShouldFailWhenEmailAlreadyExists() {
        String existingEmail = successfullyRegisteredEmail != null
                && !successfullyRegisteredEmail.isBlank()
                ? successfullyRegisteredEmail : FALLBACK_EXISTING_EMAIL;
        Assert.assertTrue(existingEmail != null && !existingEmail.isBlank()
                        && !existingEmail.equals("REPLACE_WITH_EXISTING_EMAIL"),
                "Missing existing email: set FALLBACK_EXISTING_EMAIL in RegisterTests "
                        + "to a known existing database email, or run "
                        + "registrationShouldSucceedWithValidUniqueData successfully first.");

        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        String uniqueAccount = generateUniqueAccount();

        registerPage.enterAccount(uniqueAccount);
        registerPage.enterFullName(VALID_FULL_NAME);
        registerPage.enterPassword(VALID_PASSWORD);
        registerPage.enterEmail(existingEmail);
        registerPage.enterPhone(VALID_PHONE);
        registerPage.clickRegister();

        Assert.assertTrue(registerPage.isExistingEmailPopupDisplayed(),
                "The Email đã tồn tại! popup should appear for an existing email "
                        + "with a new unique account.");
    }

    @Test(priority = 3)
    public void accountLongerThan16CharactersShouldDisplayMaximumLengthWarning() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        String tooLongAccount = "a".repeat(17);

        Assert.assertEquals(
                tooLongAccount.length(),
                17,
                "Boundary test data must contain exactly 17 account characters."
        );

        registerPage.enterAccount(tooLongAccount);
        registerPage.enterFullName(VALID_FULL_NAME);
        registerPage.enterPassword(VALID_PASSWORD);
        registerPage.enterEmail(generateUniqueEmail());
        registerPage.enterPhone(VALID_PHONE);

        registerPage.clickRegister();

        Assert.assertEquals(
                registerPage.getAccountErrorMessage(),
                "Tài khoản quá 16 kí tự",
                "Tài khoản maximum-length warning."
        );
    }

    private static String generateUniqueAccount() {
        return "a" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 14);
    }

    private static String generateUniqueEmail() {
        return "auto" + UUID.randomUUID().toString().replace("-", "") + "@gmail.com";
    }
}
