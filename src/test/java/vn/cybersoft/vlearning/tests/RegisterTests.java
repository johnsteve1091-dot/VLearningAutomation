package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.Locale;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.register.RegisterInput;
import vn.cybersoft.vlearning.data.register.RegisterSupport;
import vn.cybersoft.vlearning.data.register.RegisterMessage;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;
import vn.cybersoft.vlearning.pages.RegisterPage;



@Epic("V Learning")
@Feature("Register")
public class RegisterTests extends BaseTest {

    @Test(testName = "REG-001 - Register form displays all required controls", description = "REG-001 - Register form displays all required controls")
    @Story("Register form UI")
    @Description("Verify that the Register form displays the expected required controls including Account, Full Name, Password, Email, Phone and Register button.")
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

    @Test(testName = "REG-002 - Empty Register submission displays required-field warnings", description = "REG-002 - Empty Register submission displays required-field warnings")
    @Story("Required-field validation")
    @Description("Submit the Register form with all required fields empty and verify every expected required-field validation message.")
    public void emptySubmissionShouldDisplayRequiredValidationMessages() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        SoftAssert softly = new SoftAssert();

        registerPage.clickRegister();

        softly.assertEquals(registerPage.getAccountErrorMessage(),
                RegisterMessage.Empty.ACCOUNT,
                "Tài khoản required warning.");
        softly.assertEquals(registerPage.getFullNameErrorMessage(),
                RegisterMessage.Empty.FULL_NAME,
                "Họ tên required warning.");
        softly.assertEquals(
                registerPage.getPasswordErrorMessage(),
                RegisterMessage.Empty.PASSWORD,
                "Password required warning."
        );
        softly.assertEquals(registerPage.getEmailErrorMessage(),
                RegisterMessage.Empty.EMAIL,
                "Email required warning.");
        softly.assertEquals(registerPage.getPhoneErrorMessage(),
                RegisterMessage.Empty.PHONE,
                "Số điện thoại required warning.");
        softly.assertAll();
    }

    @Test(testName = "REG-003 - Invalid Register submission displays format warnings", description = "REG-003 - Invalid Register submission displays format warnings")
    @Story("Format validation")
    @Description("Submit deliberately invalid Account, Full Name, Password, Email and Phone values and verify the expected format and password-requirement validation messages.")
    public void invalidSubmissionShouldDisplayFormatValidationMessages() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        SoftAssert softly = new SoftAssert();

        registerPage.enterAccount(RegisterInput.Wrong.ACCOUNT_TOO_SHORT);
        registerPage.enterFullName(RegisterInput.Wrong.FULL_NAME_WITH_NUMBER);
        registerPage.enterPassword(RegisterInput.Wrong.PASSWORD_INVALID);
        registerPage.enterEmail(RegisterInput.Wrong.EMAIL_INVALID);
        registerPage.enterPhone(RegisterInput.Wrong.PHONE_INVALID);
        registerPage.clickRegister();

        softly.assertEquals(registerPage.getAccountErrorMessage(), RegisterMessage.Format.ACCOUNT_TOO_SHORT,
                "Tài khoản format warning.");
        softly.assertEquals(registerPage.getFullNameErrorMessage(), RegisterMessage.Format.FULL_NAME_INVALID,
                "Họ tên format warning.");

        String passwordError = registerPage.getPasswordErrorMessage();
        String normalizedPasswordError = passwordError.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        // Check the requirement concepts without inventing a complete corrected sentence.
        softly.assertTrue(RegisterMessage.PasswordRequirement.MINIMUM_LENGTH_PATTERN
                        .matcher(normalizedPasswordError).matches(),
                "Password warning must state a minimum of 8 characters. Actual: " + passwordError);
        softly.assertTrue(RegisterMessage.PasswordRequirement.UPPERCASE_WORDING.stream()
                        .anyMatch(normalizedPasswordError::contains),
                "Password warning must mention uppercase letters. Actual: " + passwordError);
        softly.assertTrue(RegisterMessage.PasswordRequirement.LOWERCASE_WORDING.stream()
                        .anyMatch(normalizedPasswordError::contains),
                "Password warning must mention lowercase letters. Actual: " + passwordError);
        softly.assertTrue(normalizedPasswordError.contains(RegisterMessage.PasswordRequirement.NUMBER_WORDING),
                "Password warning must mention a number. Actual: " + passwordError);
        softly.assertTrue(RegisterMessage.PasswordRequirement.SPECIAL_CHARACTER_WORDING.stream()
                        .anyMatch(normalizedPasswordError::contains),
                "Password warning must mention a special character. Actual: " + passwordError);

        softly.assertEquals(registerPage.getEmailErrorMessage(), RegisterMessage.Format.EMAIL_INVALID,
                "Email format warning.");
        softly.assertEquals(registerPage.getPhoneErrorMessage(), RegisterMessage.Format.PHONE_INVALID,
                "Số điện thoại format warning.");

        softly.assertAll();
    }

    @Test(testName = "REG-005 - Registration succeeds with valid unique data", description = "REG-005 - Registration succeeds with valid unique data", priority = 4)
    @Story("Successful registration")
    @Description("Register using valid dynamically generated unique Account and Email values and verify the successful-registration popup.")
    public void registrationShouldSucceedWithValidUniqueData() {
        // A failed rerun must not leave an older pair marked as this run's success.
        RegisterSupport.clearCreatedRegistration();

        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        String account = RegisterSupport.generateUniqueAccount();
        String email = RegisterSupport.generateUniqueEmail();

        registerPage.enterAccount(account);
        registerPage.enterFullName(RegisterInput.Correct.FULL_NAME);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(email);
        registerPage.enterPhone(RegisterInput.Correct.PHONE);
        registerPage.clickRegister();

        Assert.assertTrue(registerPage.isSuccessfulRegistrationPopupDisplayed(),
                "Successful registration popup should appear for valid unique data.");
        RegisterSupport.saveCreatedRegistration(account, email);
    }

    @Test(testName = "REG-006 - Registration fails when Account already exists", description = "REG-006 - Registration fails when Account already exists", priority = 5)
    @Story("Duplicate registration data")
    @Description("Submit otherwise-valid registration data using an existing Account and verify the duplicate-account popup.")
    public void registrationShouldFailWhenAccountAlreadyExists() {
        String existingAccount = RegisterSupport.hasCreatedAccount()
                ? RegisterSupport.getCreatedAccount() : RegisterInput.Existing.ACCOUNT;
        Assert.assertTrue(existingAccount != null && !existingAccount.isBlank()
                        && !existingAccount.equals(RegisterInput.Existing.ACCOUNT_PLACEHOLDER),
                "Missing existing account: set RegisterInput.Existing.ACCOUNT "
                        + "to a known existing database account, or run "
                        + "registrationShouldSucceedWithValidUniqueData successfully first.");

        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        String uniqueEmail = RegisterSupport.generateUniqueEmail();

        registerPage.enterAccount(existingAccount);
        registerPage.enterFullName(RegisterInput.Correct.FULL_NAME);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(uniqueEmail);
        registerPage.enterPhone(RegisterInput.Correct.PHONE);
        registerPage.clickRegister();

        Assert.assertTrue(registerPage.isExistingAccountPopupDisplayed(),
                "Existing account popup should appear for an existing account "
                        + "with a new unique email.");
    }

    @Test(testName = "REG-007 - Registration fails when Email already exists", description = "REG-007 - Registration fails when Email already exists", priority = 6)
    @Story("Duplicate registration data")
    @Description("Submit otherwise-valid registration data using an existing Email and verify the duplicate-email popup.")
    public void registrationShouldFailWhenEmailAlreadyExists() {
        String existingEmail = RegisterSupport.hasCreatedEmail()
                ? RegisterSupport.getCreatedEmail() : RegisterInput.Existing.EMAIL;
        Assert.assertTrue(existingEmail != null && !existingEmail.isBlank()
                        && !existingEmail.equals(RegisterInput.Existing.EMAIL_PLACEHOLDER),
                "Missing existing email: set RegisterInput.Existing.EMAIL "
                        + "to a known existing database email, or run "
                        + "registrationShouldSucceedWithValidUniqueData successfully first.");

        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();
        String uniqueAccount = RegisterSupport.generateUniqueAccount();

        registerPage.enterAccount(uniqueAccount);
        registerPage.enterFullName(RegisterInput.Correct.FULL_NAME);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(existingEmail);
        registerPage.enterPhone(RegisterInput.Correct.PHONE);
        registerPage.clickRegister();

        Assert.assertTrue(registerPage.isExistingEmailPopupDisplayed(),
                "Existing email popup should appear for an existing email "
                        + "with a new unique account.");
    }

    @Test(testName = "REG-004 - Account longer than 16 characters displays maximum-length warning", description = "REG-004 - Account longer than 16 characters displays maximum-length warning", priority = 3)
    @Story("Account boundary validation")
    @Description("Submit an Account value containing exactly 17 characters and verify that the maximum 16-character validation warning is displayed.")
    public void accountLongerThan16CharactersShouldDisplayMaximumLengthWarning() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        String tooLongAccount = RegisterInput.Wrong.ACCOUNT_TOO_LONG;

        Assert.assertEquals(
                tooLongAccount.length(),
                17,
                "Boundary test data must contain exactly 17 account characters."
        );

        registerPage.enterAccount(tooLongAccount);
        registerPage.enterFullName(RegisterInput.Correct.FULL_NAME);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());
        registerPage.enterPhone(RegisterInput.Correct.PHONE);

        registerPage.clickRegister();

        Assert.assertEquals(
                registerPage.getAccountErrorMessage(),
                RegisterMessage.Format.ACCOUNT_TOO_LONG,
                "Tài khoản maximum-length warning."
        );
    }

}
