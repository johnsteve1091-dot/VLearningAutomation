package vn.cybersoft.vlearning.pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
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
}