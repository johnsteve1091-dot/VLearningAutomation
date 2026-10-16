package vn.cybersoft.vlearning.pages;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import vn.cybersoft.vlearning.core.BasePage;

/** Account/My Courses DOM operations; course identity is exact normalized visible title text. */
public class AccountPage extends BasePage {

    public static final String ACCOUNT_URL = "https://demo2.cybersoft.edu.vn/thongtincanhan";

    private static final By ROOT = By.cssSelector("section.infoPage");
    private static final By PERSONAL_INFO = By.id("infoPersonal");
    private static final By ACCOUNT_FIELDS = By.tagName("p");
    private static final By MY_COURSES_TAB = By.xpath("//section[contains(@class,'infoPage')]"
            + "//button[contains(@class,'tabLink') and normalize-space()='Khóa học']");
    private static final By MY_COURSES_PANEL = By.cssSelector("#infoCourse.tabContent.active");
    private static final By MY_COURSE_ITEMS = By.cssSelector("#infoCourse.tabContent.active .myCourseInfo .myCourseItem");
    private static final By COURSE_TITLE = By.tagName("h6");
    private static final By CANCEL_COURSE = By.xpath(".//button[contains(@class,'btnGlobal')"
            + " and normalize-space()='Hủy khóa học']");

    private static final By ACTIVE_PERSONAL_INFO = By.cssSelector("#infoPersonal.tabContent.active");
    private static final By PERSONAL_ROWS = By.cssSelector("#infoPersonal.tabContent.active section.userInfo p");
    private static final By PERSONAL_VALUE = By.cssSelector("span.ml-2");
    private static final By UPDATE_PROFILE = By.cssSelector("#infoPersonal.tabContent.active"
            + " button[data-toggle='modal'][data-target='#myModal']");
    private static final By EDIT_MODAL = By.id("myModal");
    private static final By EDIT_HEADING = By.cssSelector("#myModal .modal-header h5.modal-title");
    private static final By EDIT_FULL_NAME = By.cssSelector("#myModal form input[name='hoTen']");
    private static final By EDIT_PASSWORD = By.cssSelector("#myModal form input[name='matKhau']");
    private static final By EDIT_EMAIL = By.cssSelector("#myModal form input[name='email']");
    private static final By EDIT_PHONE = By.cssSelector("#myModal form input[name='soDT']");
    private static final By EDIT_COMPLETE = By.xpath("//*[@id='myModal']"
            + "//div[contains(@class,'modal-footer')]//button[normalize-space()='Hoàn thành']");
    private static final By EDIT_CLOSE = By.xpath("//*[@id='myModal']"
            + "//div[contains(@class,'modal-footer')]//button[normalize-space()='Đóng']");
    private static final By EDIT_DISMISS = By.cssSelector("#myModal .modal-header"
            + " button[type='button'][data-dismiss='modal']");
    private static final By UPDATE_DIALOG = By.cssSelector("div.swal-modal[role='dialog']");
    private static final By UPDATE_DIALOG_TITLE = By.cssSelector("div.swal-modal[role='dialog'] .swal-title");
    // The confirmed error belongs to the immediately preceding input, never to another field.
    private static final By EDIT_FIELD_ERROR = By.xpath("./following-sibling::*[1]"
            + "[self::div and contains(concat(' ', normalize-space(@class), ' '), ' errorMessage ')]");

    public AccountPage(WebDriver driver) {
        super(driver);
    }

    public boolean waitUntilAtAccountPage() {
        try {
            return getWait().until(currentDriver ->
                    "/thongtincanhan".equals(URI.create(currentDriver.getCurrentUrl()).getPath()));
        } catch (TimeoutException routeNotReached) {
            return false;
        }
    }

    public boolean isAccountRootDisplayed() {
        return isAccountControlDisplayed(ROOT);
    }

    public boolean isPersonalInformationActive() {
        return isAccountControlDisplayed(ACTIVE_PERSONAL_INFO);
    }

    public String getPersonalAccount() {
        waitForAccountControl(ACTIVE_PERSONAL_INFO, false);
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            String account = readPersonalValue("Tài khoản");
            return account.isBlank() ? null : account;
        });
    }

    public AccountPage waitUntilPersonalInformationReady(String expectedAccount) {
        waitForAccountControl(ROOT, false);
        waitForAccountControl(ACTIVE_PERSONAL_INFO, false);
        getWait().ignoring(StaleElementReferenceException.class).until(currentDriver ->
                expectedAccount.equals(readPersonalValue("Tài khoản")));
        return this;
    }

    /** Wait for this account's populated response; expected field values remain test inputs. */
    public PersonalInformation readPersonalInformation(String expectedAccount) {
        waitUntilPersonalInformationReady(expectedAccount);
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            PersonalInformation information = new PersonalInformation(
                    readPersonalValue("Tài khoản"), readPersonalValue("Email"),
                    readPersonalValue("Họ và tên"), readPersonalValue("Số điện thoại"),
                    readPersonalValue("Nhóm"), readPersonalValue("Đối tượng"));
            return expectedAccount.equals(information.account()) && !information.email().isBlank()
                    && !information.fullName().isBlank() && !information.phone().isBlank()
                    && !information.group().isBlank() && !information.learnerType().isBlank()
                    ? information : null;
        });
    }

    public boolean isUpdateProfileAvailable() {
        try {
            return waitForAccountControl(UPDATE_PROFILE, true) != null;
        } catch (TimeoutException updateUnavailable) {
            return false;
        }
    }

    public AccountPage openEditProfile() {
        waitForAccountControl(UPDATE_PROFILE, true).click();
        waitForAccountControl(EDIT_MODAL, false);
        return this;
    }

    public EditProfileControls readEditProfileControls() {
        waitForAccountControl(EDIT_MODAL, false);
        return new EditProfileControls(isAccountControlDisplayed(EDIT_MODAL),
                normalize(waitForAccountControl(EDIT_HEADING, false).getText()),
                readInputControl(EDIT_FULL_NAME), readInputControl(EDIT_PASSWORD),
                readInputControl(EDIT_EMAIL), readInputControl(EDIT_PHONE),
                isAccountControlDisplayed(EDIT_COMPLETE), isAccountControlDisplayed(EDIT_CLOSE));
    }

    /** Input only: no submit action is exposed or invoked. */
    public void enterEditProfileValues(String fullName, String password, String email, String phone) {
        waitForAccountControl(EDIT_MODAL, false);
        enterEditValue(EDIT_FULL_NAME, fullName);
        WebElement passwordInput = waitForAccountControl(EDIT_PASSWORD, true);
        if (!"password".equals(passwordInput.getDomProperty("type"))) {
            throw new IllegalStateException("Edit Profile password input must be masked before typing.");
        }
        enterEditValue(EDIT_PASSWORD, password);
        if (!"password".equals(passwordInput.getDomProperty("type"))) {
            passwordInput.clear();
            throw new IllegalStateException("Edit Profile password input stopped being masked.");
        }
        enterEditValue(EDIT_EMAIL, email);
        enterEditValue(EDIT_PHONE, phone);
    }

    /** Password evidence is boolean only; raw secret values never enter the snapshot. */
    public EditProfileValues readEditProfileValues(String expectedPassword) {
        WebElement passwordInput = waitForAccountControl(EDIT_PASSWORD, false);
        boolean passwordMatches = expectedPassword.equals(passwordInput.getDomProperty("value"));
        boolean passwordMasked = "password".equals(passwordInput.getDomProperty("type"));
        if (!passwordMasked) passwordInput.clear();
        return new EditProfileValues(waitForAccountControl(EDIT_FULL_NAME, false).getDomProperty("value"),
                waitForAccountControl(EDIT_EMAIL, false).getDomProperty("value"),
                waitForAccountControl(EDIT_PHONE, false).getDomProperty("value"),
                passwordMatches, passwordMasked);
    }

    /** Verified header button has type=button and data-dismiss=modal, so it cannot submit the form. */
    public boolean closeEditProfile() {
        waitForAccountControl(EDIT_DISMISS, true).click();
        try {
            return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver ->
                    currentDriver.findElements(EDIT_MODAL).stream().noneMatch(WebElement::isDisplayed));
        } catch (TimeoutException modalStillOpen) {
            return false;
        }
    }

    public record PersonalInformation(String account, String email, String fullName,
                                      String phone, String group, String learnerType) {}

    public record InputControl(boolean displayed, boolean enabled, String declaredType) {}

    public record EditProfileControls(boolean displayed, String heading, InputControl fullName,
                                      InputControl password, InputControl email, InputControl phone,
                                      boolean completeDisplayed, boolean closeDisplayed) {}

    public record EditProfileValues(String fullName, String email, String phone,
                                    boolean passwordMatches, boolean passwordMasked) {}

    /** Only the three editable personal fields; never retains a password. */
    public record ProfileValues(String fullName, String email, String phone) {}

    public ProfileValues readProfileValues(String expectedAccount) {
        return readProfileValues(expectedAccount, true);
    }

    private ProfileValues readProfileValues(String expectedAccount, boolean requirePopulated) {
        waitUntilPersonalInformationReady(expectedAccount);
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            ProfileValues values = new ProfileValues(readPersonalValue("Họ và tên"),
                    readPersonalValue("Email"), readPersonalValue("Số điện thoại"));
            return expectedAccount.equals(readPersonalValue("Tài khoản"))
                    && (!requirePopulated || (!values.fullName().isBlank()
                    && !values.email().isBlank() && !values.phone().isBlank()))
                    ? values : null;
        });
    }

    /** Reuse the existing input operation, always supplying this account's current password. */
    public void enterProfileUpdateValues(ProfileValues values, String currentPassword) {
        try {
            enterEditProfileValues(values.fullName(), currentPassword, values.email(), values.phone());
        } catch (RuntimeException inputFailure) {
            // A WebDriver sendKeys exception can contain its command arguments.
            throw new IllegalStateException("Could not enter profile values ("
                    + inputFailure.getClass().getSimpleName() + "); password details omitted.");
        }
    }

    /** One click only; a slow response must not trigger another submission. */
    public void submitEditProfile() {
        waitForAccountControl(EDIT_COMPLETE, true).click();
    }

    /** Return the actual visible response so the test can assert and report its exact title. */
    public String waitForUpdatePopupTitle() {
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            List<WebElement> titles = currentDriver.findElements(UPDATE_DIALOG_TITLE).stream()
                    .filter(WebElement::isDisplayed).toList();
            if (titles.size() > 1) throw new IllegalStateException("Ambiguous visible update response.");
            if (titles.isEmpty()) return null;
            String title = titles.getFirst().getText().strip();
            return title.isBlank() ? null : title;
        });
    }

    public void waitUntilUpdatePopupClosed() {
        // Reuse the site's existing timed SweetAlert-close workflow; no guessed dismiss control.
        getWait().ignoring(StaleElementReferenceException.class).until(currentDriver ->
                currentDriver.findElements(UPDATE_DIALOG).stream().noneMatch(WebElement::isDisplayed));
    }

    public void closeEditProfileIfOpen() {
        if (driver.findElements(EDIT_MODAL).stream().anyMatch(WebElement::isDisplayed)
                && !closeEditProfile()) {
            throw new IllegalStateException("Edit Profile did not close through its verified dismiss control.");
        }
    }

    public AccountPage waitUntilProfileInteractable() {
        waitUntilUpdatePopupClosed();
        closeEditProfileIfOpen();
        getWait().ignoring(StaleElementReferenceException.class)
                .until(currentDriver -> isUpdateProfileInteractableNow());
        return this;
    }

    public ProfileValues refreshProfileValues(String expectedAccount) {
        waitUntilProfileInteractable();
        driver.navigate().refresh();
        return readProfileValues(expectedAccount);
    }

    /** Also reads blank values after a defective accepted request, so cleanup can restore them. */
    public ProfileValues refreshProfileValues(String expectedAccount, boolean requirePopulated) {
        waitUntilProfileInteractable();
        driver.navigate().refresh();
        return readProfileValues(expectedAccount, requirePopulated);
    }

    public void clearEditProfileFields() {
        waitForAccountControl(EDIT_MODAL, false);
        for (By locator : List.of(EDIT_FULL_NAME, EDIT_PASSWORD, EDIT_EMAIL, EDIT_PHONE)) {
            WebElement input = waitForAccountControl(locator, true);
            input.clear();
            // A real keystroke also updates controlled-form state after WebDriver clear().
            input.sendKeys(Keys.SPACE, Keys.BACK_SPACE);
            getWait().ignoring(StaleElementReferenceException.class).until(currentDriver ->
                    "".equals(currentDriver.findElement(locator).getDomProperty("value")));
        }
    }

    public void enterEditFullName(String fullName) {
        enterEditValue(EDIT_FULL_NAME, fullName);
    }

    public void enterEditPassword(String password) {
        try {
            WebElement input = waitForAccountControl(EDIT_PASSWORD, true);
            if (!"password".equals(input.getDomProperty("type"))) {
                throw new IllegalStateException("Edit Profile password input must be masked before typing.");
            }
            enterEditValue(EDIT_PASSWORD, password);
            if (!"password".equals(waitForAccountControl(EDIT_PASSWORD, true).getDomProperty("type"))) {
                waitForAccountControl(EDIT_PASSWORD, true).clear();
                throw new IllegalStateException("Edit Profile password input stopped being masked.");
            }
        } catch (RuntimeException inputFailure) {
            throw new IllegalStateException("Could not enter the existing profile password ("
                    + inputFailure.getClass().getSimpleName() + "); password details omitted.");
        }
    }

    public void enterEditEmail(String email) {
        enterEditValue(EDIT_EMAIL, email);
    }

    public void enterEditPhone(String phone) {
        enterEditValue(EDIT_PHONE, phone);
    }

    public record EditWarnings(String fullName, String password, String email, String phone) {
        public boolean hasAny() {
            return !fullName.isBlank() || !password.isBlank() || !email.isBlank() || !phone.isBlank();
        }
    }

    /** Titles are latched across polling, including a popup that has already disappeared. */
    public record EditValidationState(EditWarnings warnings, boolean modalDisplayed,
                                      List<String> popupTitles, boolean settled) {}

    /** Behavior-only rejection tests require observable validation, not four particular warnings. */
    public EditValidationState waitForEditRejection() {
        return waitForEditResponse(EditWarnings::hasAny, false);
    }

    /** Wait for each field's appearance/disappearance without waiting for particular wording. */
    public EditValidationState waitForEditValidation(boolean fullNameWarning, boolean passwordWarning,
                                                    boolean emailWarning, boolean phoneWarning) {
        boolean expectPopup = !fullNameWarning && !passwordWarning && !emailWarning && !phoneWarning;
        return waitForEditResponse(warnings ->
                !warnings.fullName().isBlank() == fullNameWarning
                && !warnings.password().isBlank() == passwordWarning
                && !warnings.email().isBlank() == emailWarning
                && !warnings.phone().isBlank() == phoneWarning, expectPopup);
    }

    private EditValidationState waitForEditResponse(Predicate<EditWarnings> expectedWarnings,
                                                     boolean expectPopup) {
        long started = System.nanoTime();
        long[] stableSince = {started};
        List<String> observedTitles = new ArrayList<>();
        EditValidationState[] last = {null};
        try {
            return getWait().pollingEvery(Duration.ofMillis(100))
                    .ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                        for (WebElement title : currentDriver.findElements(UPDATE_DIALOG_TITLE)) {
                            if (!title.isDisplayed()) continue;
                            String actual = title.getText().strip();
                            if (!actual.isBlank() && !observedTitles.contains(actual)) observedTitles.add(actual);
                        }
                        boolean modal = currentDriver.findElements(EDIT_MODAL).stream()
                                .anyMatch(WebElement::isDisplayed);
                        EditWarnings warnings = modal ? readEditWarnings() : new EditWarnings("", "", "", "");
                        EditValidationState current = new EditValidationState(warnings, modal,
                                List.copyOf(observedTitles), false);
                        long now = System.nanoTime();
                        if (!current.equals(last[0])) stableSince[0] = now;
                        last[0] = current;
                        // A popup during an invalid submission is a terminal response to report, not retry.
                        if (!expectPopup && !observedTitles.isEmpty()) {
                            return new EditValidationState(warnings, modal, current.popupTitles(), false);
                        }
                        boolean response = expectPopup ? !observedTitles.isEmpty() : modal && warnings.hasAny();
                        boolean stable = now - stableSince[0] >= Duration.ofMillis(500).toNanos();
                        // Observe every invalid submission for at least two seconds, polling for late success.
                        boolean observed = expectPopup || now - started >= Duration.ofSeconds(2).toNanos();
                        if (response && expectedWarnings.test(warnings) && stable && observed) {
                            return new EditValidationState(warnings, modal, current.popupTitles(), true);
                        }
                        return null;
                    });
        } catch (TimeoutException unmetValidationState) {
            // Return the actual mismatch for business assertions; do not turn missing/wrong errors into PASS.
            if (last[0] != null) return last[0];
            throw new IllegalStateException("Edit Profile validation could not be observed within 15 seconds.");
        }
    }

    private EditWarnings readEditWarnings() {
        return new EditWarnings(readEditFieldWarning(EDIT_FULL_NAME), readEditFieldWarning(EDIT_PASSWORD),
                readEditFieldWarning(EDIT_EMAIL), readEditFieldWarning(EDIT_PHONE));
    }

    private String readEditFieldWarning(By inputLocator) {
        List<WebElement> inputs = driver.findElements(inputLocator);
        if (inputs.size() != 1) throw new IllegalStateException("Expected one Edit Profile input: " + inputLocator);
        List<WebElement> errors = inputs.get(0).findElements(EDIT_FIELD_ERROR);
        if (errors.size() > 1) throw new IllegalStateException("Ambiguous field-specific Edit Profile warning.");
        return inputs.get(0).isDisplayed() && !errors.isEmpty() && errors.get(0).isDisplayed()
                ? normalize(errors.get(0).getText()) : "";
    }

    /** Opaque password baseline: no password accessor and no password in its string representation. */
    public static final class EditProfileSnapshot {
        private final EditProfileValues values;
        private final String password;

        private EditProfileSnapshot(EditProfileValues values, String password) {
            this.values = values;
            this.password = password;
        }

        public EditProfileValues values() { return values; }
        public boolean passwordEmpty() { return password.isEmpty(); }
        @Override public String toString() {
            return values + ", passwordEmpty=" + passwordEmpty();
        }
    }

    public EditProfileSnapshot captureEditProfileSnapshot(String expectedPassword) {
        EditProfileValues values = readEditProfileValues(expectedPassword);
        String password = waitForAccountControl(EDIT_PASSWORD, false).getDomProperty("value");
        return new EditProfileSnapshot(values, password);
    }

    public boolean isPasswordUnchangedSince(EditProfileSnapshot baseline) {
        return baseline.password.equals(waitForAccountControl(EDIT_PASSWORD, false).getDomProperty("value"));
    }

    public boolean isEditProfileDisplayedNow() {
        return isDisplayedNow(EDIT_MODAL);
    }

    public record EditCloseObservation(String finalUrl, String closeButtonType, boolean modalDisplayed,
                                       boolean updateInteractable, List<String> popupTitles, boolean settled) {}

    /** Click only the confirmed footer Đóng control. Never fall back to the header X or refresh. */
    public EditCloseObservation closeEditProfileWithCloseButton() {
        WebElement close = waitForAccountControl(EDIT_CLOSE, true);
        String buttonType = close.getDomProperty("type");
        boolean submitsForm = Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "return Boolean(arguments[0].form) && arguments[0].type === 'submit';", close));
        if (submitsForm) {
            throw new IllegalStateException("Đóng is associated with a form as a submit control; "
                    + "the non-submitting close scenario cannot safely click it.");
        }
        close.click();
        long started = System.nanoTime();
        long[] stableSince = {started};
        List<String> titles = new ArrayList<>();
        EditCloseObservation[] last = {null};
        try {
            return getWait().pollingEvery(Duration.ofMillis(100))
                    .ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                        captureVisibleUpdateTitles(titles);
                        EditCloseObservation current = new EditCloseObservation(currentDriver.getCurrentUrl(),
                                buttonType, isEditProfileDisplayedNow(), isUpdateProfileInteractableNow(),
                                List.copyOf(titles), false);
                        long now = System.nanoTime();
                        if (!current.equals(last[0])) stableSince[0] = now;
                        last[0] = current;
                        if (!current.modalDisplayed() && current.updateInteractable()
                                && now - started >= Duration.ofSeconds(2).toNanos()
                                && now - stableSince[0] >= Duration.ofMillis(500).toNanos()) {
                            return new EditCloseObservation(current.finalUrl(), buttonType, false, true,
                                    current.popupTitles(), true);
                        }
                        return null;
                    });
        } catch (TimeoutException closeDidNotSettle) {
            if (last[0] != null) return last[0];
            throw new IllegalStateException("Could not observe the Đóng result within 15 seconds.");
        }
    }

    /** The close tests must prove Đóng closed the modal before any refresh is permitted. */
    public ProfileValues refreshProfileAfterUnsavedClose(String expectedAccount) {
        if (isEditProfileDisplayedNow()) {
            throw new IllegalStateException("Đóng has not closed Edit Profile; refusing to use refresh as dismissal.");
        }
        getWait().ignoring(StaleElementReferenceException.class)
                .until(currentDriver -> isUpdateProfileInteractableNow());
        driver.navigate().refresh();
        return readProfileValues(expectedAccount, false);
    }

    private boolean isDisplayedNow(By locator) {
        return driver.findElements(locator).stream().anyMatch(WebElement::isDisplayed);
    }

    private boolean isUpdateProfileInteractableNow() {
        List<WebElement> controls = driver.findElements(UPDATE_PROFILE);
        if (controls.size() != 1) return false;
        WebElement button = controls.get(0);
        if (!button.isDisplayed() || !button.isEnabled()) return false;
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript("""
                const button = arguments[0];
                const rect = button.getBoundingClientRect();
                const hit = document.elementFromPoint(rect.left + rect.width / 2,
                        rect.top + rect.height / 2);
                return hit === button || button.contains(hit);
                """, button));
    }

    private void captureVisibleUpdateTitles(List<String> titles) {
        for (WebElement title : driver.findElements(UPDATE_DIALOG_TITLE)) {
            if (!title.isDisplayed()) continue;
            String actual = title.getText().strip();
            if (!actual.isBlank() && !titles.contains(actual)) titles.add(actual);
        }
    }

    public record GuestAccountObservation(String requestedUrl, String finalUrl, boolean loginDisplayed,
                                           boolean accountDisplayed, boolean personalInformationAccessible,
                                           boolean updateAccessible, boolean editModalDisplayed,
                                           boolean profileDataExposed, boolean protectedContentObserved,
                                           int httpResponseStatus, boolean documentComplete, boolean settled) {}

    /** A real browser navigation, with no login, injected token, API request or account click. */
    public GuestAccountObservation openAccountAsGuestAndObserve() {
        driver.get(ACCOUNT_URL);
        LoginPage login = new LoginPage(driver);
        long started = System.nanoTime();
        long[] stableSince = {started};
        boolean[] protectedObserved = {false};
        GuestAccountObservation[] last = {null};
        try {
            return getWait().pollingEvery(Duration.ofMillis(100))
                    .ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                        boolean personal = isDisplayedNow(ACTIVE_PERSONAL_INFO);
                        boolean exposed = currentDriver.findElements(PERSONAL_INFO).stream()
                                .filter(WebElement::isDisplayed)
                                .flatMap(panel -> panel.findElements(PERSONAL_VALUE).stream())
                                .anyMatch(value -> value.isDisplayed() && !value.getText().isBlank());
                        boolean update = isUpdateProfileInteractableNow();
                        boolean edit = isEditProfileDisplayedNow();
                        protectedObserved[0] |= exposed || update || edit;
                        // Read the current document's navigation entry, never make a second HTTP request.
                        // responseStatus can be unavailable/0; that is not evidence of denial.
                        List<?> navigation = (List<?>) ((JavascriptExecutor) currentDriver).executeScript("""
                                const entry = performance.getEntriesByType('navigation')[0];
                                return [document.readyState === 'complete',
                                    entry && Number.isFinite(entry.responseStatus) ? entry.responseStatus : 0];
                                """);
                        int status = ((Number) navigation.get(1)).intValue();
                        GuestAccountObservation current = new GuestAccountObservation(ACCOUNT_URL,
                                currentDriver.getCurrentUrl(), login.isLoginFormDisplayedNow(), isDisplayedNow(ROOT),
                                personal, update, edit, exposed, protectedObserved[0], status,
                                Boolean.TRUE.equals(navigation.get(0)), false);
                        long now = System.nanoTime();
                        if (!current.equals(last[0])) stableSince[0] = now;
                        last[0] = current;
                        boolean conclusive = current.loginDisplayed() || status == 401 || status == 403
                                || protectedObserved[0];
                        if (current.documentComplete() && conclusive
                                && now - started >= Duration.ofSeconds(2).toNanos()
                                && now - stableSince[0] >= Duration.ofMillis(500).toNanos()) {
                            return new GuestAccountObservation(current.requestedUrl(), current.finalUrl(),
                                    current.loginDisplayed(), current.accountDisplayed(), personal, update, edit,
                                    exposed, protectedObserved[0], status, true, true);
                        }
                        return null;
                    });
        } catch (TimeoutException noConclusiveAccessDecision) {
            if (last[0] != null) return last[0];
            throw new IllegalStateException("No Account guest-access state could be observed within 15 seconds.");
        }
    }

    private String readPersonalValue(String label) {
        String result = "";
        int matches = 0;
        for (WebElement row : driver.findElements(PERSONAL_ROWS)) {
            String text = normalize(row.getDomProperty("textContent"));
            int colon = text.indexOf(':');
            if (colon < 0 || !label.equals(text.substring(0, colon).strip())) continue;
            if (++matches > 1) throw new IllegalStateException("Ambiguous Personal Information label: " + label);
            List<WebElement> values = row.findElements(PERSONAL_VALUE);
            if (values.size() > 1) throw new IllegalStateException("Ambiguous Personal Information value: " + label);
            if (row.isDisplayed() && values.size() == 1 && values.get(0).isDisplayed()) {
                result = normalize(values.get(0).getText());
            }
        }
        return result;
    }

    private WebElement waitForAccountControl(By locator, boolean requireEnabled) {
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            List<WebElement> matches = currentDriver.findElements(locator);
            if (matches.size() > 1) throw new IllegalStateException("Ambiguous Account control: " + locator);
            if (matches.isEmpty()) return null;
            WebElement control = matches.get(0);
            return control.isDisplayed() && (!requireEnabled || control.isEnabled()) ? control : null;
        });
    }

    private boolean isAccountControlDisplayed(By locator) {
        try {
            return waitForAccountControl(locator, false) != null;
        } catch (TimeoutException controlNotVisible) {
            return false;
        }
    }

    private InputControl readInputControl(By locator) {
        try {
            WebElement input = waitForAccountControl(locator, false);
            // "phone" is observed markup; browsers expose the unsupported type property as "text".
            return new InputControl(true, input.isEnabled(), input.getDomAttribute("type"));
        } catch (TimeoutException inputNotVisible) {
            return new InputControl(false, false, null);
        }
    }

    private void enterEditValue(By locator, String value) {
        WebElement input = waitForAccountControl(locator, true);
        input.clear();
        input.sendKeys(value);
        getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            List<WebElement> matches = currentDriver.findElements(locator);
            return matches.size() == 1 && value.equals(matches.get(0).getDomProperty("value"));
        });
    }

    public AccountPage openMyCourses(String expectedAccount) {
        waitUntilVisible(ROOT);
        // Wait for the account response, not just a static empty tab shell. The
        // personal pane can be hidden when cleanup revisits the current Courses tab.
        getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            for (WebElement info : currentDriver.findElements(PERSONAL_INFO)) {
                for (WebElement field : info.findElements(ACCOUNT_FIELDS)) {
                    String text = normalize(field.getDomProperty("textContent"));
                    if (text.startsWith("Tài khoản:")) {
                        return expectedAccount.equals(text.substring("Tài khoản:".length()).strip());
                    }
                }
            }
            return false;
        });
        click(MY_COURSES_TAB);
        waitUntilVisible(MY_COURSES_PANEL);
        return this;
    }

    public AccountPage refreshMyCourses(String expectedAccount) {
        driver.navigate().refresh();
        return openMyCourses(expectedAccount);
    }

    /** Read-only setup guard: never take ownership of an existing account course. */
    public void requireCourseNotEnrolled(String courseName) {
        requireName(courseName);
        waitUntilVisible(MY_COURSES_PANEL);
        if (!findExactCourseItems(courseName).isEmpty()) {
            throw new IllegalStateException("Enrollment setup requires the selected Course to be absent: " + courseName
                    + ". Existing enrollment was left untouched.");
        }
    }

    public boolean waitUntilCoursePresent(String courseName) {
        requireName(courseName);
        try {
            return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                List<WebElement> matches = findExactCourseItems(courseName);
                requireUnambiguous(courseName, matches);
                return matches.size() == 1 && matches.get(0).isDisplayed();
            });
        } catch (TimeoutException courseNotVisible) {
            return false;
        }
    }

    /** Observable count for duplicate enrollment; does not reject multiple matches. */
    public int countExactCourseItems(String courseName) {
        requireName(courseName);
        waitUntilVisible(MY_COURSES_PANEL);
        String expected = normalize(courseName);
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            int count = 0;
            for (WebElement item : currentDriver.findElements(MY_COURSE_ITEMS)) {
                if (item.findElements(COURSE_TITLE).stream().anyMatch(title -> title.isDisplayed()
                        && expected.equals(normalize(title.getText())))) count++;
            }
            return count;
        });
    }

    /** Cancels only a unique exact-title match and waits for that DOM item to disappear. */
    public boolean cancelCourseIfPresent(String courseName) {
        requireName(courseName);
        waitUntilVisible(MY_COURSES_PANEL);
        if (!waitUntilCoursePresent(courseName)) {
            if (!findExactCourseItems(courseName).isEmpty()) {
                throw new IllegalStateException("Selected Course exists but is not visible for safe cleanup: " + courseName);
            }
            return false;
        }
        WebElement cancel = getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            List<WebElement> matches = findExactCourseItems(courseName);
            requireUnambiguous(courseName, matches);
            if (matches.isEmpty()) return null;
            for (WebElement button : matches.get(0).findElements(CANCEL_COURSE)) {
                if (button.isDisplayed() && button.isEnabled()) return button;
            }
            return null;
        });
        cancel.click();
        getWait().ignoring(StaleElementReferenceException.class).until(currentDriver ->
                currentDriver.findElements(MY_COURSES_PANEL).stream().anyMatch(WebElement::isDisplayed)
                        && findExactCourseItems(courseName).isEmpty());
        return true;
    }

    private List<WebElement> findExactCourseItems(String courseName) {
        String expected = normalize(courseName);
        List<WebElement> matches = new ArrayList<>();
        for (WebElement item : driver.findElements(MY_COURSE_ITEMS)) {
            boolean match = item.findElements(COURSE_TITLE).stream()
                    .anyMatch(title -> expected.equals(normalize(title.getDomProperty("textContent"))));
            if (match) matches.add(item);
        }
        return matches;
    }

    private static void requireUnambiguous(String courseName, List<WebElement> matches) {
        if (matches.size() > 1) {
            throw new IllegalStateException("Multiple My Courses items have the selected title; refusing ambiguous cleanup: "
                    + courseName);
        }
    }

    private static void requireName(String courseName) {
        if (normalize(courseName).isBlank()) throw new IllegalArgumentException("A captured Course name is required.");
    }

    private static String normalize(String text) {
        return text == null ? "" : text.replace('\u00a0', ' ').replaceAll("\\s+", " ").strip();
    }


    public AccountPage openAccountAsGuest() {
        driver.get(ACCOUNT_URL);
        return this;
    }

    public boolean isGuestAccountPageBlank() {
        return driver.findElement(By.tagName("body"))
                .getText()
                .isBlank();
    }


    /** Read-only multiset of exact titles, gated by the account response and active Courses panel. */
    public Map<String, Integer> captureMyCourseCounts(String expectedAccount) {
        waitUntilVisible(ROOT);
        waitUntilVisible(MY_COURSES_PANEL);
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            // The same response-readiness guard used by openMyCourses; never accept just an empty shell.
            if (!hasLoadedCourseOwner(expectedAccount) || !isDisplayedNow(MY_COURSES_PANEL)) return null;
            Map<String, Integer> counts = new LinkedHashMap<>();
            for (WebElement item : currentDriver.findElements(MY_COURSE_ITEMS)) {
                if (!item.isDisplayed()) return null;
                List<WebElement> titles = item.findElements(COURSE_TITLE).stream()
                        .filter(WebElement::isDisplayed).toList();
                if (titles.size() > 1) {
                    throw new IllegalStateException("Ambiguous My Courses item: multiple visible titles.");
                }
                if (titles.isEmpty()) return null;
                String name = normalize(titles.get(0).getText());
                if (name.isBlank()) return null;
                counts.merge(name, 1, Integer::sum);
            }
            return hasLoadedCourseOwner(expectedAccount) && isDisplayedNow(MY_COURSES_PANEL)
                    ? Map.copyOf(counts) : null;
        });
    }

    private boolean hasLoadedCourseOwner(String expectedAccount) {
        List<String> accounts = new ArrayList<>();
        for (WebElement info : driver.findElements(PERSONAL_INFO)) {
            for (WebElement field : info.findElements(ACCOUNT_FIELDS)) {
                String text = normalize(field.getDomProperty("textContent"));
                if (text.startsWith("Tài khoản:")) accounts.add(text.substring("Tài khoản:".length()).strip());
            }
        }
        if (accounts.size() > 1) throw new IllegalStateException("Ambiguous loaded Account identity.");
        return accounts.size() == 1 && expectedAccount.equals(accounts.get(0));
    }

    public PersonalInformation refreshPersonalInformation(String expectedAccount) {
        driver.navigate().refresh();
        return readPersonalInformation(expectedAccount);
    }

    public enum StaleProfileAction {
        FULL_NAME, PASSWORD, EMAIL, PHONE, UPDATE, COMPLETE, CLOSE;

        public boolean isField() {
            return this == FULL_NAME || this == PASSWORD || this == EMAIL || this == PHONE;
        }
    }

    public record PreparedStaleProfile(ProfileValues intended, EditProfileValues inputs,
                                       boolean ready) {}

    /** Prepare all OTHER fields before logout; the selected field is left untouched. */
    public PreparedStaleProfile prepareProfileBeforeLogout(StaleProfileAction action,
            ProfileValues original, ProfileValues replacement, String password) {
        ProfileValues intended = switch (action) {
            case FULL_NAME, PASSWORD, COMPLETE, CLOSE ->
                    new ProfileValues(replacement.fullName(), original.email(), original.phone());
            case EMAIL -> new ProfileValues(original.fullName(), replacement.email(), original.phone());
            case PHONE -> new ProfileValues(original.fullName(), original.email(), replacement.phone());
            case UPDATE -> original;
        };
        if (action == StaleProfileAction.UPDATE) {
            return new PreparedStaleProfile(intended, null,
                    !isEditProfileDisplayedNow() && isUpdateProfileInteractableNow());
        }
        openEditProfile();
        EditProfileSnapshot baseline = captureEditProfileSnapshot(password);
        if (action != StaleProfileAction.FULL_NAME) enterEditFullName(intended.fullName());
        if (action != StaleProfileAction.PASSWORD) enterEditPassword(password);
        if (action != StaleProfileAction.EMAIL) enterEditEmail(intended.email());
        if (action != StaleProfileAction.PHONE) enterEditPhone(intended.phone());
        EditProfileValues inputs = readEditProfileValues(password);
        EditWarnings warnings = readEditWarnings();
        boolean ready = inputs.fullName().equals(action == StaleProfileAction.FULL_NAME
                    ? baseline.values().fullName() : intended.fullName())
                && inputs.email().equals(action == StaleProfileAction.EMAIL
                    ? baseline.values().email() : intended.email())
                && inputs.phone().equals(action == StaleProfileAction.PHONE
                    ? baseline.values().phone() : intended.phone())
                && (action == StaleProfileAction.PASSWORD
                    ? isPasswordUnchangedSince(baseline) : inputs.passwordMatches())
                && inputs.passwordMasked()
                && (action == StaleProfileAction.FULL_NAME || warnings.fullName().isBlank())
                && (action == StaleProfileAction.PASSWORD || warnings.password().isBlank())
                && (action == StaleProfileAction.EMAIL || warnings.email().isBlank())
                && (action == StaleProfileAction.PHONE || warnings.phone().isBlank());
        return new PreparedStaleProfile(intended, inputs, ready);
    }

    /** Read-only, immediate check. Never wait for fields on a blank stale tab. */
    public boolean isCompleteStaleDraftReady(ProfileValues expected, String password) {
        if (isGuestAccountPageBlank() || !isDisplayedNow(EDIT_MODAL)) return false;
        WebElement name = availableNow(EDIT_FULL_NAME);
        WebElement secret = availableNow(EDIT_PASSWORD);
        WebElement email = availableNow(EDIT_EMAIL);
        WebElement phone = availableNow(EDIT_PHONE);
        return name != null && secret != null && email != null && phone != null
                && expected.fullName().equals(name.getDomProperty("value"))
                && expected.email().equals(email.getDomProperty("value"))
                && expected.phone().equals(phone.getDomProperty("value"))
                && "password".equals(secret.getDomProperty("type"))
                && password.equals(secret.getDomProperty("value"))
                && !readEditWarnings().hasAny();
    }

    public record StaleUiState(String url, boolean blank, boolean loginDisplayed,
                               boolean modalDisplayed, boolean targetAvailable,
                               boolean formUsable, int exactCourseCount,
                               EditWarnings warnings, List<String> popupTitles) {}

    public record StaleActionObservation(String action, StaleUiState before, StaleUiState after,
                                         boolean attempted, boolean completed, String problem) {}

    /** One selected interaction only. No navigation, login, retry or successful-response wait. */
    public StaleActionObservation actOnStaleProfileOnce(StaleProfileAction action,
                                                        ProfileValues intended, String password) {
        By target = staleProfileTarget(action);
        StaleUiState before = readStaleUi(target, null);
        if (before.blank() || !before.targetAvailable()) {
            return new StaleActionObservation(action.name(), before, before, false, false,
                    before.blank() ? "Blank before action; interaction not reached."
                            : "Selected control unavailable before action; not clicked/typed.");
        }
        boolean completed = false;
        String problem = "";
        try {
            WebElement element = availableNow(target);
            if (element == null) {
                return new StaleActionObservation(action.name(), before, readStaleUi(target, null),
                        false, false, "Selected control disappeared before action.");
            }
            if (action.isField()) {
                if (action == StaleProfileAction.PASSWORD
                        && !"password".equals(element.getDomProperty("type"))) {
                    throw new IllegalStateException("Refusing to enter a password into an unmasked field.");
                }
                element.clear();
                // clear() itself can trigger the stale React screen to disappear.
                StaleUiState afterClear = readStaleUi(target, null);
                if (!afterClear.blank() && afterClear.targetAvailable()) {
                    WebElement input = availableNow(target);
                    if (input != null) {
                        String value = switch (action) {
                            case FULL_NAME -> intended.fullName();
                            case PASSWORD -> password;
                            case EMAIL -> intended.email();
                            case PHONE -> intended.phone();
                            default -> throw new IllegalStateException("Expected a field action.");
                        };
                        input.sendKeys(value);
                        completed = true;
                    }
                } else {
                    problem = "Field clear triggered blank/removed control; no further input sent.";
                }
            } else {
                element.click();
                completed = true;
            }
        } catch (org.openqa.selenium.WebDriverException interactionFailure) {
            // Never include sendKeys command arguments or password-bearing exception messages.
            problem = interactionFailure.getClass().getSimpleName();
        }
        return new StaleActionObservation(action.name(), before, observeStaleUi(target, null),
                true, completed, problem);
    }

    /** Reuses the exact-title and ambiguity guards, but never waits for successful cancellation. */
    public StaleActionObservation cancelExactCourseOnceAndObserve(String courseName) {
        requireName(courseName);
        StaleUiState before = readStaleUi(null, courseName);
        if (before.blank() || !before.targetAvailable()) {
            return new StaleActionObservation("Hủy khóa học: " + courseName, before, before, false, false,
                    before.blank() ? "Blank before action; cancellation not reached."
                            : "Exact Course cancellation control unavailable; not clicked.");
        }
        boolean completed = false;
        String problem = "";
        try {
            WebElement button = exactCancellationNow(courseName);
            if (button == null) {
                return new StaleActionObservation("Hủy khóa học: " + courseName, before,
                        readStaleUi(null, courseName), false, false, "Exact control disappeared before click.");
            }
            button.click();
            completed = true;
        } catch (org.openqa.selenium.WebDriverException interactionFailure) {
            problem = interactionFailure.getClass().getSimpleName();
        }
        return new StaleActionObservation("Hủy khóa học: " + courseName, before,
                observeStaleUi(null, courseName), true, completed, problem);
    }

    public boolean isExactCourseCancellationAvailable(String courseName) {
        requireName(courseName);
        return exactCancellationNow(courseName) != null;
    }

    public record StaleCourseCancellationObservation(String loadedAccount, StaleActionObservation action) {}

    /** Read only until the old page still identifies its original owner and one exact target. */
    public StaleCourseCancellationObservation cancelStaleCourseForAccountOnceAndObserve(
            String expectedStaleAccount, String courseName) {
        requireName(courseName);
        StaleUiState before = readStaleUi(null, courseName);
        String actual = before.blank() || before.loginDisplayed() ? "" : readLoadedCourseAccountNow();
        if (!expectedStaleAccount.equals(actual) || before.blank() || before.loginDisplayed()
                || !before.targetAvailable() || before.exactCourseCount() != 1
                || !hasLoadedCourseOwner(expectedStaleAccount)) {
            return new StaleCourseCancellationObservation(actual, new StaleActionObservation(
                    "Hủy khóa học: " + courseName, before, before, false, false,
                    "No safe original-account target; loaded Account=" + actual
                            + "; expected stale owner=" + expectedStaleAccount
                            + ". Blank, Login, changed/unknown owner or unavailable target; no click."));
        }
        // Reuse the existing single WebDriver click and observation; no authorized-removal wait or retry.
        return new StaleCourseCancellationObservation(actual, cancelExactCourseOnceAndObserve(courseName));
    }

    /** Same confirmed labeled response used by hasLoadedCourseOwner, including the hidden personal pane. */
    private String readLoadedCourseAccountNow() {
        List<String> accounts = new ArrayList<>();
        for (WebElement info : driver.findElements(PERSONAL_INFO)) {
            for (WebElement field : info.findElements(ACCOUNT_FIELDS)) {
                String text = normalize(field.getDomProperty("textContent"));
                if (text.startsWith("Tài khoản:")) accounts.add(text.substring("Tài khoản:".length()).strip());
            }
        }
        if (accounts.size() > 1) throw new IllegalStateException("Ambiguous loaded Account identity; refusing stale cancellation.");
        return accounts.isEmpty() ? "" : accounts.get(0);
    }

    private By staleProfileTarget(StaleProfileAction action) {
        return switch (action) {
            case FULL_NAME -> EDIT_FULL_NAME;
            case PASSWORD -> EDIT_PASSWORD;
            case EMAIL -> EDIT_EMAIL;
            case PHONE -> EDIT_PHONE;
            case UPDATE -> UPDATE_PROFILE;
            case COMPLETE -> EDIT_COMPLETE;
            case CLOSE -> EDIT_CLOSE;
        };
    }

    private WebElement availableNow(By locator) {
        List<WebElement> matches = driver.findElements(locator);
        if (matches.size() > 1) throw new IllegalStateException("Ambiguous Account control: " + locator);
        if (matches.isEmpty()) return null;
        WebElement element = matches.get(0);
        return element.isDisplayed() && element.isEnabled() ? element : null;
    }

    private WebElement exactCancellationNow(String courseName) {
        List<WebElement> matches = findExactCourseItems(courseName);
        requireUnambiguous(courseName, matches);
        if (matches.isEmpty() || !matches.get(0).isDisplayed()) return null;
        List<WebElement> buttons = matches.get(0).findElements(CANCEL_COURSE).stream()
                .filter(button -> button.isDisplayed() && button.isEnabled()).toList();
        if (buttons.size() > 1) throw new IllegalStateException("Ambiguous cancellation control for: " + courseName);
        return buttons.isEmpty() ? null : buttons.get(0);
    }

    private StaleUiState readStaleUi(By target, String courseName) {
        String url = driver.getCurrentUrl();
        EditWarnings emptyWarnings = new EditWarnings("", "", "", "");
        if (isGuestAccountPageBlank()) {
            return new StaleUiState(url, true, false, false, false, false, -1, emptyWarnings, List.of());
        }
        boolean modal = isDisplayedNow(EDIT_MODAL);
        List<String> titles = new ArrayList<>();
        captureVisibleUpdateTitles(titles);
        int count = courseName != null && isDisplayedNow(MY_COURSES_PANEL)
                ? findExactCourseItems(courseName).size() : -1;
        boolean targetAvailable = courseName == null
                ? availableNow(target) != null : exactCancellationNow(courseName) != null;
        boolean formUsable = modal && availableNow(EDIT_FULL_NAME) != null
                && availableNow(EDIT_PASSWORD) != null && availableNow(EDIT_EMAIL) != null
                && availableNow(EDIT_PHONE) != null && availableNow(EDIT_COMPLETE) != null;
        return new StaleUiState(url, false, new LoginPage(driver).isLoginFormDisplayedNow(), modal,
                targetAvailable, formUsable, count, modal ? readEditWarnings() : emptyWarnings,
                List.copyOf(titles));
    }

    /** At most two seconds of UI observation; return immediately on blank rendering. */
    private StaleUiState observeStaleUi(By target, String courseName) {
        StaleUiState[] latest = {null};
        List<String> seenTitles = new ArrayList<>();
        try {
            getWait().withTimeout(Duration.ofSeconds(2)).pollingEvery(Duration.ofMillis(100))
                    .ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                        StaleUiState state = readStaleUi(target, courseName);
                        for (String title : state.popupTitles()) {
                            if (!seenTitles.contains(title)) seenTitles.add(title);
                        }
                        latest[0] = new StaleUiState(state.url(), state.blank(), state.loginDisplayed(),
                                state.modalDisplayed(), state.targetAvailable(), state.formUsable(),
                                state.exactCourseCount(), state.warnings(), List.copyOf(seenTitles));
                        return state.blank();
                    });
        } catch (TimeoutException observationComplete) {
            if (latest[0] == null) throw observationComplete;
        }
        return latest[0];
    }

}
