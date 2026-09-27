package vn.cybersoft.vlearning.pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import vn.cybersoft.vlearning.core.BasePage;

public class RegisterPage extends BasePage {

    private static final By REGISTER_TITLE =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//h2[normalize-space()='ĐĂNG KÝ']");

    private static final By ACCOUNT_INPUT =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='taiKhoan']");
    private static final By FULL_NAME_INPUT =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='hoTen']");
    private static final By PASSWORD_INPUT =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='matKhau']");
    private static final By EMAIL_INPUT =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='email']");
    private static final By PHONE_INPUT =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='soDT']");
    private static final By GROUP_DROPDOWN =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//select[@name='maNhom']");
    private static final By REGISTER_BUTTON =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//button[@type='submit' and normalize-space()='Đăng ký']");

    private static final By ACCOUNT_ERROR =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='taiKhoan']/following-sibling::div[1][contains(@class,'errorMessage')]");

    private static final By FULL_NAME_ERROR =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='hoTen']/following-sibling::div[1][contains(@class,'errorMessage')]");

    private static final By PASSWORD_ERROR =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='matKhau']/following-sibling::div[1][contains(@class,'errorMessage')]");

    private static final By EMAIL_ERROR =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='email']/following-sibling::div[1][contains(@class,'errorMessage')]");

    private static final By PHONE_ERROR =
            By.xpath("//form[.//h2[normalize-space()='ĐĂNG KÝ']]//input[@name='soDT']/following-sibling::div[1][contains(@class,'errorMessage')]");

    private static final By SUCCESS_POPUP =
            By.xpath("//div[contains(@class,'swal-modal')]//div[contains(@class,'swal-title') and normalize-space()='Đăng kí thành công']");
    private static final By EXISTING_ACCOUNT_POPUP =
            By.xpath("//div[contains(@class,'swal-modal')]//div[contains(@class,'swal-title') and normalize-space()='Tài khoản đã tồn tại!']");
    private static final By EXISTING_EMAIL_POPUP =
            By.xpath("//div[contains(@class,'swal-modal') and @role='dialog'][.//div[contains(@class,'swal-title') and normalize-space()='Email đã tồn tại!']]");

    public RegisterPage(WebDriver driver) {
        super(driver);
    }

    public boolean isRegisterTitleDisplayed() {
        return isControlDisplayed(REGISTER_TITLE);
    }

    public boolean isAccountInputDisplayed() {
        return isControlDisplayed(ACCOUNT_INPUT);
    }

    public boolean isFullNameInputDisplayed() {
        return isControlDisplayed(FULL_NAME_INPUT);
    }

    public boolean isPasswordInputDisplayed() {
        return isControlDisplayed(PASSWORD_INPUT);
    }

    public boolean isEmailInputDisplayed() {
        return isControlDisplayed(EMAIL_INPUT);
    }

    public boolean isPhoneInputDisplayed() {
        return isControlDisplayed(PHONE_INPUT);
    }

    public boolean isGroupDropdownDisplayed() {
        return isControlDisplayed(GROUP_DROPDOWN);
    }

    public boolean isRegisterButtonDisplayed() {
        return isControlDisplayed(REGISTER_BUTTON);
    }

    public void enterAccount(String account) {
        enterText(ACCOUNT_INPUT, account);
    }

    public void enterFullName(String fullName) {
        enterText(FULL_NAME_INPUT, fullName);
    }

    public void enterPassword(String password) {
        enterText(PASSWORD_INPUT, password);
    }

    public void enterEmail(String email) {
        enterText(EMAIL_INPUT, email);
    }

    public void enterPhone(String phone) {
        enterText(PHONE_INPUT, phone);
    }

    public void selectGroup(String visibleText) {
        new Select(waitUntilClickable(GROUP_DROPDOWN)).selectByVisibleText(visibleText);
    }

    public String getSelectedGroup() {
        return new Select(waitUntilVisible(GROUP_DROPDOWN)).getFirstSelectedOption().getText().trim();
    }

    public boolean isGroupDropdownNativeSelect() {
        return "select".equalsIgnoreCase(waitUntilVisible(GROUP_DROPDOWN).getTagName());
    }

    public boolean hasGroupOption(String visibleText) {
        return new Select(waitUntilVisible(GROUP_DROPDOWN)).getOptions().stream()
                .anyMatch(option -> option.getText().trim().equals(visibleText));
    }

    public void clickRegister() {
        // A visible button can still be covered while the panel is animating.
        getWait().until(currentDriver -> {
            try {
                WebElement button = ExpectedConditions.elementToBeClickable(REGISTER_BUTTON)
                        .apply(currentDriver);
                if (button == null) {
                    return false;
                }
                button.click();
                return true;
            } catch (ElementClickInterceptedException | StaleElementReferenceException exception) {
                return false;
            }
        });
    }

    public String getAccountErrorMessage() {
        return getValidationMessage(ACCOUNT_ERROR);
    }

    public String getFullNameErrorMessage() {
        return getValidationMessage(FULL_NAME_ERROR);
    }

    public String getPasswordErrorMessage() {
        return getValidationMessage(PASSWORD_ERROR);
    }

    public String getEmailErrorMessage() {
        return getValidationMessage(EMAIL_ERROR);
    }

    public String getPhoneErrorMessage() {
        return getValidationMessage(PHONE_ERROR);
    }

    // Call these only after a known validation warning confirms form validation.
    public boolean isAccountErrorDisplayedNow() {
        return isErrorDisplayedNow(ACCOUNT_ERROR);
    }

    public boolean isPasswordErrorDisplayedNow() {
        return isErrorDisplayedNow(PASSWORD_ERROR);
    }

    public boolean isEmailErrorDisplayedNow() {
        return isErrorDisplayedNow(EMAIL_ERROR);
    }

    public boolean isPhoneErrorDisplayedNow() {
        return isErrorDisplayedNow(PHONE_ERROR);
    }

    private boolean isErrorDisplayedNow(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    public boolean isSuccessfulRegistrationPopupDisplayed() {
        return isControlDisplayed(SUCCESS_POPUP);
    }

    public boolean isExistingAccountPopupDisplayed() {
        return isControlDisplayed(EXISTING_ACCOUNT_POPUP);
    }

    public boolean isExistingEmailPopupDisplayed() {
        return isControlDisplayed(EXISTING_EMAIL_POPUP);
    }

    // Return absence on a visibility timeout so SoftAssert can check every field.
    // Other WebDriver errors still propagate as test failures.
    private boolean isControlDisplayed(By locator) {
        try {
            return isDisplayed(locator);
        } catch (TimeoutException exception) {
            return false;
        }
    }

    private String getValidationMessage(By locator) {
        try {
            return getText(locator);
        } catch (TimeoutException exception) {
            return "";
        }
    }
    public void clearRegisterForm() {
        clearField(ACCOUNT_INPUT);
        clearField(FULL_NAME_INPUT);
        clearField(PASSWORD_INPUT);
        clearField(EMAIL_INPUT);
        clearField(PHONE_INPUT);
    }

    private void clearField(By locator) {
        WebElement input = waitUntilVisible(locator);

        input.click();
        input.sendKeys(Keys.CONTROL, "a");
        input.sendKeys(Keys.BACK_SPACE);

        getWait().until(driver ->
                input.getDomProperty("value") == null
                        || input.getDomProperty("value").isEmpty()
        );
    }
}
