package vn.cybersoft.vlearning.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import vn.cybersoft.vlearning.core.BasePage;

public class CourseDetailPage extends BasePage {

    /*
     * Healthy Course Detail marker.
     *
     * Verified DOM:
     *
     * <section class="detailCourses">
     *     ...
     *     <h4 class="titleDetailCourse">...</h4>
     * </section>
     */
    private static final By COURSE_TITLE =
            By.xpath(
                    "//section[contains(@class,'detailCourses')]"
                            + "//h4[contains(@class,'titleDetailCourse')]"
            );

    public CourseDetailPage(
            WebDriver driver
    ) {

        super(
                driver
        );
    }

    public String getCourseTitle() {

        return getText(
                COURSE_TITLE
        );
    }
}