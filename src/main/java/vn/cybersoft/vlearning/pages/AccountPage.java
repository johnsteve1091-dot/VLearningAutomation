package vn.cybersoft.vlearning.pages;

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

    public AccountPage(WebDriver driver) {
        super(driver);
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
