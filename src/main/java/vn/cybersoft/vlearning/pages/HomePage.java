package vn.cybersoft.vlearning.pages;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import vn.cybersoft.vlearning.core.BasePage;

public class HomePage extends BasePage {

    public HomePage open() {
        driver.get("https://demo2.cybersoft.edu.vn/");
        return this;
    }

    private static final By LOGIN_BUTTON =
            By.xpath(
                    "//a[@href='/login' and normalize-space()='Đăng nhập']"
            );

    private static final By COURSE_LIST_LINK =
            By.xpath(
                    "//a[@href='/khoahoc' and normalize-space()='Khóa học']"
            );

    /*
     * Every Course card rendered somewhere inside Homepage.
     *
     * We deliberately do NOT filter by href because TC2 cares
     * about the card structure, not whether its URL is valid.
     */
    private static final By HOME_COURSE_CARDS =
            By.xpath(
                    "//div[contains(@class,'homePage')]"
                            + "//div[contains(@class,'cardGlobalRes')]"
                            + "//a[contains(@class,'cardGlobal')]"
            );

    /*
     * Discover actual Danh mục links from the frontend instead
     * of hardcoding category identifiers.
     */
    private static final By COURSE_CATEGORY_LINKS =
            By.xpath(
                    "//a[starts-with(@href,'/danhmuckhoahoc/')]"
            );

    private static final By COURSE_NAME =
            By.xpath(
                    ".//div[contains(@class,'cardBodyGlobal')]//h6"
            );

    private static final By SUB_CARD =
            By.xpath(
                    ".//div[contains(@class,'subCard')]"
            );

    // Header DOM: observed live category menu and supplied authenticated-header capture.
    private static final By CATEGORY_MENU = By.cssSelector("li.courseCate");
    private static final By ACCOUNT_LINK = By.cssSelector("a.infoHeader[href='/thongtincanhan']");
    private static final By ACCOUNT_AVATAR = By.cssSelector("a.infoHeader[href='/thongtincanhan'] img.avatar");
    private static final By LOGOUT_CONTROL = By.cssSelector("a.infoHeader[href='/thongtincanhan'] span.logout");

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public LoginPage goToLoginPage() {
        if (!"/login".equals(URI.create(driver.getCurrentUrl()).getPath())) {
            click(LOGIN_BUTTON);
        }
        return new LoginPage(driver);
    }

    public CourseListPage goToCourseListPage() {
        click(COURSE_LIST_LINK);
        return new CourseListPage(driver);
    }

    public List<CourseCardSnapshot> captureHomepageCourseCards() {

        List<WebElement> courseElements =
                getWait().until(driver -> {

                    List<WebElement> elements =
                            driver.findElements(HOME_COURSE_CARDS);

                    return elements.isEmpty()
                            ? null
                            : elements;
                });

        List<CourseCardSnapshot> courses =
                new ArrayList<>();

        for (int index = 0;
             index < courseElements.size();
             index++) {

            courses.add(
                    CourseCardDomReader.capture(
                            courseElements.get(index),
                            index + 1
                    )
            );
        }

        return List.copyOf(courses);
    }

    public List<String> getCourseCategoryUrls() {

        /*
         * Confirm Homepage/header has rendered before inspecting
         * the category links.
         */
        waitUntilVisible(COURSE_LIST_LINK);

        List<WebElement> categoryLinks =
                driver.findElements(COURSE_CATEGORY_LINKS);

        Set<String> uniqueUrls =
                new LinkedHashSet<>();

        for (WebElement categoryLink : categoryLinks) {

            String href =
                    categoryLink.getAttribute("href");

            if (href != null && !href.isBlank()) {
                uniqueUrls.add(href.trim());
            }
        }

        return List.copyOf(uniqueUrls);
    }

    public HomePage waitUntilAuthenticated() {
        waitUntilVisible(ACCOUNT_LINK);
        return this;
    }

    /** Waits for a known header state or the standalone Login route. */
    public boolean isAuthenticated() {
        String state = getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            if (currentDriver.findElements(ACCOUNT_LINK).stream().anyMatch(WebElement::isDisplayed)) {
                return "authenticated";
            }
            if ("/login".equals(URI.create(currentDriver.getCurrentUrl()).getPath())
                    || currentDriver.findElements(LOGIN_BUTTON).stream().anyMatch(WebElement::isDisplayed)) {
                return "guest";
            }
            return null;
        });
        return "authenticated".equals(state);
    }

    public CourseCategoryPage goToFirstCourseCategory() {
        List<String> categoryUrls = getCourseCategoryUrls();
        if (categoryUrls.isEmpty()) {
            throw new IllegalStateException("No Danh mục link was available for Enrollment setup.");
        }
        return goToCourseCategory(categoryUrls.get(0));
    }

    public CourseCategoryPage goToCourseCategory(String categoryUrl) {
        new Actions(driver).moveToElement(waitUntilVisible(CATEGORY_MENU)).perform();
        WebElement target = getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            for (WebElement link : currentDriver.findElements(COURSE_CATEGORY_LINKS)) {
                if (categoryUrl.equals(link.getAttribute("href")) && link.isDisplayed() && link.isEnabled()) {
                    return link;
                }
            }
            return null;
        });
        target.click();
        return new CourseCategoryPage(driver).waitUntilLoaded();
    }

    /** Search the same rendered category, then at most one other discovered category. */
    public CourseCategoryPage.EnrollmentCourse selectDifferentCourseForEnrollment(
            CourseCategoryPage.EnrollmentCourse firstCourse) {
        CourseCategoryPage categoryPage = goToCourseCategory(firstCourse.categoryUrl());
        try {
            return categoryPage.selectDifferentCourseForEnrollment(firstCourse);
        } catch (TimeoutException noDifferentCourse) {
            String otherCategory = getCourseCategoryUrls().stream()
                    .filter(url -> !CourseDetailPage.sameBrowserUrl(firstCourse.categoryUrl(), url))
                    .findFirst().orElse(null);
            if (otherCategory == null) {
                throw new IllegalStateException("ENROLL-007 setup requires two healthy Courses with different hrefs "
                        + "and exact titles; no other category was available.", noDifferentCourse);
            }
            categoryPage = goToCourseCategory(otherCategory);
            try {
                return categoryPage.selectDifferentCourseForEnrollment(firstCourse);
            } catch (TimeoutException noSecondCourse) {
                throw new IllegalStateException("ENROLL-007 setup could not find a second healthy Course with a "
                        + "different href and exact title in the selected category or one other discovered category.",
                        noSecondCourse);
            }
        }
    }

    public AccountPage goToAccountPage() {
        // Click the avatar itself, avoiding its nested logout control.
        click(ACCOUNT_AVATAR);
        return new AccountPage(driver);
    }

    /** Uses application logout; clearing cookies/storage is not a substitute. */

    public boolean logoutIfAuthenticated() {
        String state = getWait()
                .ignoring(StaleElementReferenceException.class)
                .until(currentDriver -> {
                    if (currentDriver.findElements(ACCOUNT_LINK)
                            .stream().anyMatch(WebElement::isDisplayed)) {
                        return "authenticated";
                    }

                    return currentDriver.findElements(LOGIN_BUTTON)
                            .stream().anyMatch(WebElement::isDisplayed)
                            ? "guest" : null;
                });

        if ("guest".equals(state)) {
            return false;
        }

        WebElement avatar = waitUntilVisible(ACCOUNT_AVATAR);

        // Reveal the logout control through actual mouse hover.
        new Actions(driver)
                .moveToElement(avatar)
                .pause(java.time.Duration.ofMillis(300))
                .perform();

        WebElement logout = getWait()
                .ignoring(StaleElementReferenceException.class)
                .until(currentDriver -> {
                    for (WebElement element :
                            currentDriver.findElements(LOGOUT_CONTROL)) {
                        if (element.isDisplayed() && element.isEnabled()) {
                            return element;
                        }
                    }
                    return null;
                });

        // Move onto the logout control before clicking.
        new Actions(driver)
                .moveToElement(logout)
                .pause(java.time.Duration.ofMillis(200))
                .click()
                .perform();

        getWait()
                .ignoring(StaleElementReferenceException.class)
                .until(currentDriver ->
                        currentDriver.findElements(ACCOUNT_LINK)
                                .stream().noneMatch(WebElement::isDisplayed)
                                && currentDriver.findElements(LOGIN_BUTTON)
                                .stream().anyMatch(WebElement::isDisplayed));

        return true;
    }

}
