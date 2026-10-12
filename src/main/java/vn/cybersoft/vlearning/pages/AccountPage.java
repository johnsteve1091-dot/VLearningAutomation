package vn.cybersoft.vlearning.pages;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import vn.cybersoft.vlearning.core.BasePage;

/** Account/My Courses DOM operations; course identity is exact normalized visible title text. */
public class AccountPage extends BasePage {

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
}
