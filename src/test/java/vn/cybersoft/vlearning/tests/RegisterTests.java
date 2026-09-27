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
import vn.cybersoft.vlearning.data.register.RegisterMessage;
import vn.cybersoft.vlearning.data.register.RegisterSupport;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;
import vn.cybersoft.vlearning.pages.RegisterPage;

@Epic("V Learning")
@Feature("Register")
public class RegisterTests extends BaseTest {

    @Test(
            testName = "REG-002 - Registration succeeds with valid unique data",
            description = "REG-002 - Registration succeeds with valid unique data",
            priority = 1
    )
    @Story("Successful registration")
    @Description(
            "Register with valid unique Account and Email, verify the success popup, "
                    + "then save the created pair for manual reuse."
    )
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

        Assert.assertTrue(
                registerPage.isSuccessfulRegistrationPopupDisplayed(),
                "Successful registration popup should appear for valid unique data."
        );

        RegisterSupport.saveCreatedRegistration(account, email);
    }

    @Test(
            testName = "REG-001 - Register form displays all required controls",
            description = "REG-001 - Register form displays all required controls",
            priority = 2
    )
    @Story("Register form UI")
    @Description(
            "Verify the Register title, Account, Full Name, Password, Email, Phone, "
                    + "Group dropdown and Register button are visible."
    )
    public void registerFormShouldDisplayAllRequiredControls() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        SoftAssert softly = new SoftAssert();

        softly.assertTrue(
                registerPage.isRegisterTitleDisplayed(),
                "Register title should be displayed."
        );

        softly.assertTrue(
                registerPage.isAccountInputDisplayed(),
                "Tài khoản input should be displayed."
        );

        softly.assertTrue(
                registerPage.isFullNameInputDisplayed(),
                "Họ tên input should be displayed."
        );

        softly.assertTrue(
                registerPage.isPasswordInputDisplayed(),
                "Mật khẩu input should be displayed."
        );

        softly.assertTrue(
                registerPage.isEmailInputDisplayed(),
                "Email input should be displayed."
        );

        softly.assertTrue(
                registerPage.isPhoneInputDisplayed(),
                "Số điện thoại input should be displayed."
        );

        softly.assertTrue(
                registerPage.isGroupDropdownDisplayed(),
                "Mã nhóm dropdown should be displayed."
        );

        softly.assertTrue(
                registerPage.isRegisterButtonDisplayed(),
                "Đăng ký button should be displayed."
        );

        softly.assertAll();
    }

    @Test(
            testName = "REG-003 - Empty Register submission displays all required warnings",
            description = "REG-003 - Empty Register submission displays all required warnings",
            priority = 3
    )
    @Story("Required-field validation")
    @Description(
            "Submit the Register form with all required fields empty and verify every "
                    + "expected required-field validation message."
    )
    public void emptySubmissionShouldDisplayRequiredValidationMessages() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        SoftAssert softly = new SoftAssert();

        registerPage.clickRegister();

        softly.assertEquals(
                registerPage.getAccountErrorMessage(),
                RegisterMessage.Empty.ACCOUNT,
                "Tài khoản required warning."
        );

        softly.assertEquals(
                registerPage.getFullNameErrorMessage(),
                RegisterMessage.Empty.FULL_NAME,
                "Họ tên required warning."
        );

        softly.assertEquals(
                registerPage.getPasswordErrorMessage(),
                RegisterMessage.Empty.PASSWORD,
                "Password required warning."
        );

        softly.assertEquals(
                registerPage.getEmailErrorMessage(),
                RegisterMessage.Empty.EMAIL,
                "Email required warning."
        );

        softly.assertEquals(
                registerPage.getPhoneErrorMessage(),
                RegisterMessage.Empty.PHONE,
                "Số điện thoại required warning."
        );

        softly.assertAll();
    }

    @Test(
            testName = "REG-SMOKE-001 - Combined invalid Register submission displays validation warnings",
            description = "REG-SMOKE-001 - Combined invalid Register submission displays validation warnings",
            priority = 4
    )
    @Story("Format validation")
    @Description(
            "Submit deliberately invalid Account, Full Name, Password, Email and Phone values "
                    + "and verify the expected format and password-requirement validation messages."
    )
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

        softly.assertEquals(
                registerPage.getAccountErrorMessage(),
                RegisterMessage.Format.ACCOUNT_TOO_SHORT,
                "Tài khoản format warning."
        );

        softly.assertEquals(
                registerPage.getFullNameErrorMessage(),
                RegisterMessage.Format.FULL_NAME_INVALID,
                "Họ tên format warning."
        );

        String passwordError = registerPage.getPasswordErrorMessage();
        String normalizedPasswordError =
                passwordError.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");

        // Check the requirement concepts without inventing a complete corrected sentence.
        softly.assertTrue(
                RegisterMessage.PasswordRequirement.MINIMUM_LENGTH_PATTERN
                        .matcher(normalizedPasswordError)
                        .matches(),
                "Password warning must state a minimum of 8 characters. Actual: "
                        + passwordError
        );

        softly.assertTrue(
                RegisterMessage.PasswordRequirement.UPPERCASE_WORDING.stream()
                        .anyMatch(normalizedPasswordError::contains),
                "Password warning must mention uppercase letters. Actual: "
                        + passwordError
        );

        softly.assertTrue(
                RegisterMessage.PasswordRequirement.LOWERCASE_WORDING.stream()
                        .anyMatch(normalizedPasswordError::contains),
                "Password warning must mention lowercase letters. Actual: "
                        + passwordError
        );

        softly.assertTrue(
                normalizedPasswordError.contains(
                        RegisterMessage.PasswordRequirement.NUMBER_WORDING
                ),
                "Password warning must mention a number. Actual: "
                        + passwordError
        );

        softly.assertTrue(
                RegisterMessage.PasswordRequirement.SPECIAL_CHARACTER_WORDING.stream()
                        .anyMatch(normalizedPasswordError::contains),
                "Password warning must mention a special character. Actual: "
                        + passwordError
        );

        softly.assertEquals(
                registerPage.getEmailErrorMessage(),
                RegisterMessage.Format.EMAIL_INVALID,
                "Email format warning."
        );

        softly.assertEquals(
                registerPage.getPhoneErrorMessage(),
                RegisterMessage.Format.PHONE_INVALID,
                "Số điện thoại format warning."
        );

        softly.assertAll();
    }

    @Test(
            testName = "REG-004 - Account length validation",
            description = "REG-004 - Account length validation",
            priority = 5
    )
    @Story("Account boundary validation")
    @Description(
            "Verify that a 1-character Account is rejected, "
                    + "an exactly 16-character Account displays no Account warning, "
                    + "and a 17-character Account is rejected."
    )
    public void accountLengthShouldValidateMinimumAndMaximumBoundaries() {

        // =========================================================
        // CASE 1: 1 CHARACTER -> WARNING MUST APPEAR
        // =========================================================

        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        registerPage.enterAccount(RegisterInput.Wrong.ACCOUNT_TOO_SHORT);
        registerPage.enterFullName(RegisterInput.Wrong.FULL_NAME_WITH_NUMBER);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());
        registerPage.enterPhone(RegisterInput.Correct.PHONE);

        registerPage.clickRegister();

        Assert.assertEquals(
                registerPage.getAccountErrorMessage(),
                RegisterMessage.Format.ACCOUNT_TOO_SHORT,
                "A 1-character Account should display the too-short warning."
        );

        // =========================================================
        // RESET PAGE
        // Fresh React/form/validation state for CASE 2
        // =========================================================

        driver.navigate().refresh();

        loginPage = new LoginPage(driver);
        registerPage = loginPage.goToRegisterPage();

        // =========================================================
        // CASE 2: 16 CHARACTERS -> NO ACCOUNT WARNING
        // =========================================================

        registerPage.enterAccount(RegisterInput.Correct.ACCOUNT_MAX_LENGTH);

        // Intentionally invalid so registration cannot actually succeed.
        registerPage.enterFullName(RegisterInput.Wrong.FULL_NAME_WITH_NUMBER);

        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());
        registerPage.enterPhone(RegisterInput.Correct.PHONE);

        registerPage.clickRegister();

        // Confirm form validation actually happened.
        Assert.assertEquals(
                registerPage.getFullNameErrorMessage(),
                RegisterMessage.Format.FULL_NAME_INVALID,
                "The Full Name blocker should confirm that form validation occurred."
        );

        // THIS is the actual 16-character requirement.
        Assert.assertFalse(
                registerPage.isAccountErrorDisplayedNow(),
                "A 16-character Account should NOT display an Account validation warning."
        );

        // =========================================================
        // RESET PAGE
        // Fresh React/form/validation state for CASE 3
        // =========================================================

        driver.navigate().refresh();

        loginPage = new LoginPage(driver);
        registerPage = loginPage.goToRegisterPage();

        // =========================================================
        // CASE 3: 17 CHARACTERS -> WARNING MUST APPEAR
        // =========================================================

        registerPage.enterAccount(RegisterInput.Wrong.ACCOUNT_TOO_LONG);
        registerPage.enterFullName(RegisterInput.Wrong.FULL_NAME_WITH_NUMBER);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());
        registerPage.enterPhone(RegisterInput.Correct.PHONE);

        registerPage.clickRegister();

        Assert.assertEquals(
                registerPage.getAccountErrorMessage(),
                RegisterMessage.Format.ACCOUNT_TOO_LONG,
                "A 17-character Account should display the maximum-length warning."
        );
    }

    @Test(
            testName = "REG-005 - Registration rejects duplicate Account",
            description = "REG-005 - Registration rejects duplicate Account",
            priority = 6
    )
    @Story("Duplicate registration data")
    @Description(
            "Use the configured existing database Account with a freshly generated Email "
                    + "and otherwise-valid data; verify the duplicate-account popup."
    )
    public void registrationShouldFailWhenAccountAlreadyExists() {
        String existingAccount = RegisterInput.Existing.ACCOUNT;

        Assert.assertTrue(
                existingAccount != null
                        && !existingAccount.isBlank()
                        && !existingAccount.equals(
                        RegisterInput.Existing.ACCOUNT_PLACEHOLDER
                ),
                "Missing existing account: set RegisterInput.Existing.ACCOUNT "
                        + "to a known existing database account before REG-005."
        );

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

        Assert.assertTrue(
                registerPage.isExistingAccountPopupDisplayed(),
                "Existing account popup should appear for an existing account "
                        + "with a new unique email."
        );
    }

    @Test(
            testName = "REG-006 - Full Name rejects invalid characters",
            description = "REG-006 - Full Name rejects invalid characters",
            priority = 7
    )
    @Story("Full Name validation")
    @Description(
            "Submit a Full Name containing a number, then one containing a special character, "
                    + "and verify the expected warning with valid unrelated fields."
    )
    public void fullNameShouldRejectNumbersAndSpecialCharacters() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        SoftAssert softly = new SoftAssert();

        registerPage.enterAccount(RegisterSupport.generateUniqueAccount());
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());
        registerPage.enterPhone(RegisterInput.Correct.PHONE);

        registerPage.enterFullName(RegisterInput.Wrong.FULL_NAME_WITH_NUMBER);
        registerPage.clickRegister();

        softly.assertEquals(
                registerPage.getFullNameErrorMessage(),
                RegisterMessage.Format.FULL_NAME_INVALID,
                "Full Name should reject numbers."
        );

        registerPage.enterFullName(
                RegisterInput.Wrong.FULL_NAME_WITH_SPECIAL_CHARACTER
        );
        registerPage.clickRegister();

        softly.assertEquals(
                registerPage.getFullNameErrorMessage(),
                RegisterMessage.Format.FULL_NAME_INVALID,
                "Full Name should reject special characters."
        );

        softly.assertAll();
    }

    @Test(
            testName = "REG-007 - Password validation rules",
            description = "REG-007 - Password validation rules",
            priority = 8
    )
    @Story("Password validation")
    @Description(
            "Verify in one testcase that Password validation rejects fewer than 8 characters "
                    + "and missing uppercase, lowercase, number or special character, "
                    + "using valid unrelated fields."
    )
    public void passwordShouldRejectRuleViolations() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        registerPage.enterAccount(RegisterSupport.generateUniqueAccount());
        registerPage.enterFullName(RegisterInput.Correct.FULL_NAME);
        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());
        registerPage.enterPhone(RegisterInput.Correct.PHONE);

        String[][] invalidPasswords = {
                {"fewer than 8 characters", RegisterInput.Wrong.PASSWORD_TOO_SHORT},
                {"missing uppercase", RegisterInput.Wrong.PASSWORD_MISSING_UPPERCASE},
                {"missing lowercase", RegisterInput.Wrong.PASSWORD_MISSING_LOWERCASE},
                {"missing number", RegisterInput.Wrong.PASSWORD_MISSING_NUMBER},
                {"missing special character", RegisterInput.Wrong.PASSWORD_MISSING_SPECIAL_CHARACTER}
        };

        for (String[] passwordCase : invalidPasswords) {
            registerPage.enterPassword(passwordCase[1]);
            registerPage.clickRegister();

            // Stop if a rule is not enforced: the application may have accepted registration.
            Assert.assertFalse(
                    registerPage.getPasswordErrorMessage().isBlank(),
                    "Password validation should reject "
                            + passwordCase[0]
                            + "."
            );
        }
    }

    @Test(
            testName = "REG-008 - Valid Password is accepted",
            description = "REG-008 - Valid Password is accepted",
            priority = 9
    )
    @Story("Password validation")
    @Description(
            "Submit a valid Password with an invalid Account blocker; wait for the Account "
                    + "warning and verify that no Password error is visible."
    )
    public void validPasswordShouldHaveNoValidationError() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        registerPage.enterAccount(RegisterInput.Wrong.ACCOUNT_TOO_SHORT);
        registerPage.enterFullName(RegisterInput.Correct.FULL_NAME);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());
        registerPage.enterPhone(RegisterInput.Correct.PHONE);
        registerPage.clickRegister();

        Assert.assertEquals(
                registerPage.getAccountErrorMessage(),
                RegisterMessage.Format.ACCOUNT_TOO_SHORT,
                "The Account blocker must confirm validation before checking "
                        + "Password error absence."
        );

        Assert.assertFalse(
                registerPage.isPasswordErrorDisplayedNow(),
                "The valid Password should have no field validation error."
        );
    }

    @Test(
            testName = "REG-009 - Email validation",
            description = "REG-009 - Email validation",
            priority = 10
    )
    @Story("Email validation")
    @Description(
            "Verify invalid Email is rejected, then valid unique Email has no field error, "
                    + "using an invalid Account to prevent registration."
    )
    public void emailShouldValidateInvalidAndValidFormats() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        SoftAssert softly = new SoftAssert();

        registerPage.enterAccount(RegisterInput.Wrong.ACCOUNT_TOO_SHORT);
        registerPage.enterFullName(RegisterInput.Correct.FULL_NAME);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterPhone(RegisterInput.Correct.PHONE);

        registerPage.enterEmail(RegisterInput.Wrong.EMAIL_INVALID);
        registerPage.clickRegister();

        softly.assertEquals(
                registerPage.getEmailErrorMessage(),
                RegisterMessage.Format.EMAIL_INVALID,
                "An invalid Email format should be rejected."
        );

        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());
        registerPage.clickRegister();

        Assert.assertEquals(
                registerPage.getAccountErrorMessage(),
                RegisterMessage.Format.ACCOUNT_TOO_SHORT,
                "The Account blocker must confirm validation before checking "
                        + "Email error absence."
        );

        softly.assertFalse(
                registerPage.isEmailErrorDisplayedNow(),
                "A valid Email should have no field validation error."
        );

        softly.assertAll();
    }

    @Test(
            testName = "REG-010 - Registration rejects duplicate Email",
            description = "REG-010 - Registration rejects duplicate Email",
            priority = 11
    )
    @Story("Duplicate registration data")
    @Description(
            "Use the configured existing database Email with a freshly generated Account "
                    + "and otherwise-valid data; verify the duplicate-email popup."
    )
    public void registrationShouldFailWhenEmailAlreadyExists() {
        String existingEmail = RegisterInput.Existing.EMAIL;

        Assert.assertTrue(
                existingEmail != null
                        && !existingEmail.isBlank()
                        && !existingEmail.equals(RegisterInput.Existing.EMAIL_PLACEHOLDER),
                "Missing existing email: set RegisterInput.Existing.EMAIL "
                        + "to a known existing database email before REG-010."
        );

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

        Assert.assertTrue(
                registerPage.isExistingEmailPopupDisplayed(),
                "The 'Email đã tồn tại!' popup should appear when registering "
                        + "with an existing Email and a new unique Account."
        );
    }

    @Test(
            testName = "REG-011 - Phone validation",
            description = "REG-011 - Phone validation",
            priority = 12
    )
    @Story("Phone validation")
    @Description(
            "Verify all invalid Phone variations with unique valid Account and Email, "
                    + "then verify a valid 10-digit Phone with an invalid Account blocker."
    )
    public void phoneShouldValidateLengthAndCharacters() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        SoftAssert softly = new SoftAssert();

        registerPage.enterAccount(RegisterSupport.generateUniqueAccount());
        registerPage.enterFullName(RegisterInput.Correct.FULL_NAME);
        registerPage.enterPassword(RegisterInput.Correct.PASSWORD);
        registerPage.enterEmail(RegisterSupport.generateUniqueEmail());

        String[][] invalidPhones = {
                {"fewer than 10 digits", RegisterInput.Wrong.PHONE_TOO_SHORT},
                {"more than 10 digits", RegisterInput.Wrong.PHONE_TOO_LONG},
                {"a letter", RegisterInput.Wrong.PHONE_WITH_LETTER},
                {"a special character", RegisterInput.Wrong.PHONE_WITH_SPECIAL_CHARACTER}
        };

        for (String[] phoneCase : invalidPhones) {
            registerPage.enterPhone(phoneCase[1]);
            registerPage.clickRegister();

            String phoneError = registerPage.getPhoneErrorMessage();

            Assert.assertFalse(
                    phoneError.isBlank(),
                    "Phone validation must reject "
                            + phoneCase[0]
                            + " before another submission."
            );

            softly.assertEquals(
                    phoneError,
                    RegisterMessage.Format.PHONE_INVALID,
                    "Phone validation should reject "
                            + phoneCase[0]
                            + "."
            );
        }

        // Block registration only for the valid Phone acceptance check.
        registerPage.enterAccount(RegisterInput.Wrong.ACCOUNT_TOO_SHORT);
        registerPage.enterPhone(RegisterInput.Correct.PHONE);
        registerPage.clickRegister();

        Assert.assertEquals(
                registerPage.getAccountErrorMessage(),
                RegisterMessage.Format.ACCOUNT_TOO_SHORT,
                "The Account blocker must confirm validation before checking "
                        + "Phone error absence."
        );

        softly.assertFalse(
                registerPage.isPhoneErrorDisplayedNow(),
                "A 10-digit Phone should have no field validation error."
        );

        softly.assertAll();
    }

    @Test(
            testName = "REG-012 - Course Group can be selected",
            description = "REG-012 - Course Group can be selected",
            priority = 13
    )
    @Story("Course Group controls")
    @Description(
            "Select the configured GP01 option from the existing Group dropdown "
                    + "and verify its selected text without submitting registration."
    )
    public void courseGroupShouldBeSelectable() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        registerPage.selectGroup(RegisterInput.Correct.GROUP);

        Assert.assertEquals(
                registerPage.getSelectedGroup(),
                RegisterInput.Correct.GROUP,
                "The selected Group should match the requested option."
        );
    }

    @Test(
            testName = "REG-013 - Course Group rejects arbitrary manual input",
            description = "REG-013 - Course Group rejects arbitrary manual input",
            priority = 14
    )
    @Story("Course Group controls")
    @Description(
            "Verify Group is a native select and ABC123 is not an available option; "
                    + "do not submit registration or invent a validation message."
    )
    public void courseGroupShouldRejectArbitraryInput() {
        HomePage homePage = new HomePage(driver);
        LoginPage loginPage = homePage.goToLoginPage();
        RegisterPage registerPage = loginPage.goToRegisterPage();

        Assert.assertTrue(
                registerPage.isGroupDropdownNativeSelect(),
                "Group should be a native select rather than a free-form text field."
        );

        Assert.assertFalse(
                registerPage.hasGroupOption(RegisterInput.Wrong.GROUP_ARBITRARY),
                "The arbitrary Group must not be an available dropdown option."
        );
    }
}