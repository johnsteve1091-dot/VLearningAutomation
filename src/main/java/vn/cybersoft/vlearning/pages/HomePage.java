package vn.cybersoft.vlearning.pages;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import vn.cybersoft.vlearning.core.BasePage;

public class HomePage extends BasePage {

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

    public HomePage(WebDriver driver) {
        super(driver);
    }

    public LoginPage goToLoginPage() {
        click(LOGIN_BUTTON);
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

            WebElement courseElement =
                    courseElements.get(index);

            String href =
                    courseElement.getAttribute("href");

            String name = "";

            List<WebElement> nameElements =
                    courseElement.findElements(COURSE_NAME);

            if (!nameElements.isEmpty()) {
                name = nameElements.getFirst()
                        .getText()
                        .trim();
            }

            boolean hasSubCard =
                    !courseElement.findElements(SUB_CARD)
                            .isEmpty();

            courses.add(
                    new CourseCardSnapshot(
                            name,
                            href,
                            index + 1,
                            hasSubCard
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
}