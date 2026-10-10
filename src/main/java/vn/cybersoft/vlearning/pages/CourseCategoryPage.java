package vn.cybersoft.vlearning.pages;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.cybersoft.vlearning.core.BasePage;

public class CourseCategoryPage extends BasePage {

    private static final By CATEGORY_NAME =
            By.xpath(
                    "//section"
                            + "//div[contains(@class,'listCourseCategory')]"
                            + "//div[contains(@class,'courseCateName')]"
            );

    private static final By COURSE_CARDS =
            By.xpath(
                    "//section"
                            + "//div[contains(@class,'listCourseCategory')]"
                            + "//div[contains(@class,'cardGlobalRes')]"
                            + "//a[contains(@class,'cardGlobal')]"
            );

    private static final By COURSE_NAME =
            By.xpath(
                    ".//div[contains(@class,'cardBodyGlobal')]//h6"
            );

    private static final By SUB_CARD =
            By.xpath(
                    ".//div[contains(@class,'subCard')]"
            );

    // Enrollment identity is the sticker text, not the existing h6 card-content field.
    private static final By ENROLLMENT_COURSE_NAME = By.cssSelector("span.stikerCard");
    private static final By MAIN_CARD_IMAGE = By.xpath("./img[1]");

    public record EnrollmentCourse(String categoryUrl, String href, String courseName) {
    }

    public CourseCategoryPage(WebDriver driver) {
        super(driver);
    }

    public CourseCategoryPage open(
            String categoryUrl
    ) {

        driver.get(categoryUrl);

        waitUntilVisible(CATEGORY_NAME);

        return this;
    }

    public String getCategoryName() {

        return getText(CATEGORY_NAME);
    }

    public List<CourseCardSnapshot> captureCourseCards() {

        List<WebElement> courseElements =
                getWait().until(driver -> {

                    List<WebElement> elements =
                            driver.findElements(COURSE_CARDS);

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

    public CourseCategoryPage waitUntilLoaded() {
        waitUntilVisible(CATEGORY_NAME);
        return this;
    }

    /** Stops at the first usable rendered card; no pagination or catalog audit. */
    public EnrollmentCourse selectCourseForEnrollment() {
        return selectCourseForEnrollment(null);
    }

    /** A second course needs a different href and an unambiguous My Courses title. */
    public EnrollmentCourse selectDifferentCourseForEnrollment(EnrollmentCourse excludedCourse) {
        if (excludedCourse == null) throw new IllegalArgumentException("The first selected Course is required.");
        return selectCourseForEnrollment(excludedCourse);
    }

    private EnrollmentCourse selectCourseForEnrollment(EnrollmentCourse excludedCourse) {
        waitUntilLoaded();
        String categoryUrl = driver.getCurrentUrl();
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            for (WebElement card : currentDriver.findElements(COURSE_CARDS)) {
                if (!card.isDisplayed()) continue;
                String href = card.getAttribute("href");
                if (!isCourseDetailHref(href, categoryUrl)) continue;
                List<WebElement> names = card.findElements(ENROLLMENT_COURSE_NAME);
                if (names.isEmpty() || !names.get(0).isDisplayed()) continue;
                String name = normalize(names.get(0).getText());
                if (excludedCourse != null && (CourseDetailPage.sameBrowserUrl(excludedCourse.href(), href)
                        || normalize(excludedCourse.courseName()).equals(name))) continue;
                if (!name.isBlank()) return new EnrollmentCourse(categoryUrl, href, name);
            }
            return null;
        });
    }

    public CourseDetailPage openCourseForEnrollment(EnrollmentCourse course) {
        WebElement target = getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            for (WebElement card : currentDriver.findElements(COURSE_CARDS)) {
                if (!course.href().equals(card.getAttribute("href")) || !card.isDisplayed() || !card.isEnabled()) {
                    continue;
                }
                List<WebElement> names = card.findElements(ENROLLMENT_COURSE_NAME);
                if (!names.isEmpty() && course.courseName().equals(normalize(names.get(0).getText()))) {
                    for (WebElement image : card.findElements(MAIN_CARD_IMAGE)) {
                        if (image.isDisplayed() && image.isEnabled()) return image;
                    }
                }
            }
            return null;
        });
        target.click();
        getWait().until(currentDriver -> CourseDetailPage.sameBrowserUrl(course.href(), currentDriver.getCurrentUrl()));
        return new CourseDetailPage(driver).waitUntilEnrollmentReady();
    }

    private static boolean isCourseDetailHref(String href, String sourceUrl) {
        if (href == null || href.isBlank()) return false;
        try {
            URI target = URI.create(href);
            URI source = URI.create(sourceUrl);
            String path = target.getRawPath();
            return source.getScheme().equals(target.getScheme()) && source.getAuthority().equals(target.getAuthority())
                    && path != null && path.startsWith("/chitiet/") && path.length() > "/chitiet/".length();
        } catch (IllegalArgumentException invalidHref) {
            return false;
        }
    }

    private static String normalize(String text) {
        return text == null ? "" : text.replace('\u00a0', ' ').replaceAll("\\s+", " ").strip();
    }
}
