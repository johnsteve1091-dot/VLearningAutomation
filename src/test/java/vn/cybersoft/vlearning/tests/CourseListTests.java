package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.testng.Assert;
import org.testng.annotations.Test;

import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.CourseList.CourseListSupport;
import vn.cybersoft.vlearning.pages.CourseListPage;
import vn.cybersoft.vlearning.pages.CourseListPage.CatalogPage;
import vn.cybersoft.vlearning.pages.CourseListPage.CourseCandidate;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.data.CourseList.CourseSubCardSupport;
import vn.cybersoft.vlearning.pages.CourseCardSnapshot;
import vn.cybersoft.vlearning.pages.CourseCategoryPage;


@Epic("V Learning")
@Feature("Course List")
public class CourseListTests extends BaseTest {

    private static final Logger LOGGER =
            LogManager.getLogger(CourseListTests.class);

    @Test(
            testName = "COURSE-LIST-001 - Course Detail URL ID Integrity",
            description = "COURSE-LIST-001 - Course Detail URL ID Integrity"
    )
    @Story("Every advertised course contains a Course Detail ID")
    @Description(
            "Navigate from Homepage to Khóa học and traverse every Course List "
                    + "pagination page. Inspect every course card href without opening "
                    + "Course Detail pages. Verify that every /chitiet/ URL contains "
                    + "a non-empty course ID. Record all missing-ID defects and fail "
                    + "once after the complete catalog sweep."
    )
    public void everyAdvertisedCourseShouldHaveCourseDetailId() {

        List<CourseListSupport.MissingCourseId> missingIds =
                new ArrayList<>();

        Set<Integer> visitedPages = new HashSet<>();
        Set<List<String>> visitedDatasets = new HashSet<>();

        int totalCardsChecked = 0;

        LOGGER.info("COURSE-LIST-001 | Starting URL integrity sweep");

        CourseListPage catalog =
                new HomePage(driver).goToCourseListPage();

        CatalogPage page =
                catalog.captureCurrentPage();

        while (page != null) {

            if (!visitedPages.add(page.number())) {
                throw new IllegalStateException(
                        "Pagination revisited page " + page.number()
                );
            }

            if (!visitedDatasets.add(page.hrefs())) {
                throw new IllegalStateException(
                        "Pagination returned duplicate dataset at page "
                                + page.number()
                );
            }

            int missingOnThisPage = 0;

            for (CourseCandidate course : page.courses()) {

                totalCardsChecked++;

                String problem =
                        CourseListSupport.findCourseUrlProblem(
                                course.href()
                        );

                if (problem != null) {

                    missingIds.add(
                            new CourseListSupport.MissingCourseId(
                                    course,
                                    problem
                            )
                    );

                    missingOnThisPage++;

                    LOGGER.warn(
                            "MISSING ID | global={} | page={} | card={} | name=\"{}\" | href={}",
                            totalCardsChecked,
                            course.page(),
                            course.cardIndex(),
                            course.name(),
                            course.href()
                    );
                }
            }

            LOGGER.info(
                    "PAGE {} | cards={} | checked={} | missingThisPage={} | totalMissing={}",
                    page.number(),
                    page.courses().size(),
                    totalCardsChecked,
                    missingOnThisPage,
                    missingIds.size()
            );

            page = catalog.goToNextPage(page);
        }

        String summary =
                CourseListSupport.buildDetailedSummary(
                        visitedPages.size(),
                        totalCardsChecked,
                        missingIds
                );

        Allure.addAttachment(
                "COURSE-LIST-001 URL integrity summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-LIST-001 COMPLETE | pages={} | checked={} | missingIds={}",
                visitedPages.size(),
                totalCardsChecked,
                missingIds.size()
        );

        Assert.assertTrue(
                missingIds.isEmpty(),
                "COURSE-LIST-001 failed: "
                        + missingIds.size()
                        + " course link(s) have missing Course Detail IDs. "
                        + "See Allure summary."
        );
    }

    @Test(
            testName = "COURSE-INTEGRITY-002A - Homepage subCard Structure Integrity",
            description = "COURSE-INTEGRITY-002A - Homepage subCard Structure Integrity"
    )
    @Story("Every Homepage course card contains hover-detail structure")
    @Description(
            "Inspect every Course card rendered on the Homepage and verify that "
                    + "the card contains its subCard hover-detail element in the DOM. "
                    + "No hover interaction or Course Detail navigation is performed."
    )
    public void everyHomepageCourseCardShouldContainSubCard() {

        List<CourseSubCardSupport.MissingSubCard> missingSubCards =
                new ArrayList<>();

        LOGGER.info(
                "COURSE-INTEGRITY-002A | Starting Homepage subCard structure sweep"
        );

        HomePage homePage =
                new HomePage(driver);

        List<CourseCardSnapshot> courses =
                homePage.captureHomepageCourseCards();

        for (CourseCardSnapshot course : courses) {

            if (!course.hasSubCard()) {

                missingSubCards.add(
                        new CourseSubCardSupport.MissingSubCard(
                                "Homepage",
                                driver.getCurrentUrl(),
                                course.cardIndex(),
                                course.name(),
                                course.href()
                        )
                );

                LOGGER.warn(
                        "MISSING SUBCARD | surface=Homepage | card={} | name=\"{}\" | href={}",
                        course.cardIndex(),
                        course.name(),
                        course.href()
                );
            }
        }

        String summary =
                CourseSubCardSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-002A",
                        "Homepage",
                        "Pages checked",
                        1,
                        courses.size(),
                        missingSubCards
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-002A Homepage subCard summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-002A COMPLETE | checked={} | missingSubCards={}",
                courses.size(),
                missingSubCards.size()
        );

        Assert.assertTrue(
                missingSubCards.isEmpty(),
                "COURSE-INTEGRITY-002A failed: "
                        + missingSubCards.size()
                        + " Homepage course card(s) are missing subCard. "
                        + "See Allure summary."
        );
    }

    @Test(
            testName = "COURSE-INTEGRITY-002B - Khóa học subCard Structure Integrity",
            description = "COURSE-INTEGRITY-002B - Khóa học subCard Structure Integrity"
    )
    @Story("Every Khóa học course card contains hover-detail structure")
    @Description(
            "Traverse every Khóa học pagination page and verify that every rendered "
                    + "Course card contains its subCard hover-detail element in the DOM. "
                    + "No hover interaction or Course Detail navigation is performed."
    )
    public void everyCourseListCardShouldContainSubCard() {

        List<CourseSubCardSupport.MissingSubCard> missingSubCards =
                new ArrayList<>();

        Set<Integer> visitedPages =
                new HashSet<>();

        Set<List<String>> visitedDatasets =
                new HashSet<>();

        int totalCardsChecked = 0;

        LOGGER.info(
                "COURSE-INTEGRITY-002B | Starting Khóa học subCard structure sweep"
        );

        CourseListPage catalog =
                new HomePage(driver)
                        .goToCourseListPage();

        CatalogPage page =
                catalog.captureCurrentPage();

        while (page != null) {

            if (!visitedPages.add(page.number())) {

                throw new IllegalStateException(
                        "Pagination revisited page "
                                + page.number()
                );
            }

            if (!visitedDatasets.add(page.hrefs())) {

                throw new IllegalStateException(
                        "Pagination returned duplicate dataset at page "
                                + page.number()
                );
            }

            int missingOnThisPage = 0;

            for (CourseCandidate course
                    : page.courses()) {

                totalCardsChecked++;

                if (!course.hasSubCard()) {

                    missingOnThisPage++;

                    missingSubCards.add(
                            new CourseSubCardSupport.MissingSubCard(
                                    "Khóa học",
                                    "Page " + page.number(),
                                    course.cardIndex(),
                                    course.name(),
                                    course.href()
                            )
                    );

                    LOGGER.warn(
                            "MISSING SUBCARD | surface=Khóa học | page={} | card={} | name=\"{}\" | href={}",
                            page.number(),
                            course.cardIndex(),
                            course.name(),
                            course.href()
                    );
                }
            }

            LOGGER.info(
                    "PAGE {} | cards={} | checked={} | missingThisPage={} | totalMissing={}",
                    page.number(),
                    page.courses().size(),
                    totalCardsChecked,
                    missingOnThisPage,
                    missingSubCards.size()
            );

            page =
                    catalog.goToNextPage(page);
        }

        String summary =
                CourseSubCardSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-002B",
                        "Khóa học",
                        "Pagination pages checked",
                        visitedPages.size(),
                        totalCardsChecked,
                        missingSubCards
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-002B Khóa học subCard summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-002B COMPLETE | pages={} | checked={} | missingSubCards={}",
                visitedPages.size(),
                totalCardsChecked,
                missingSubCards.size()
        );

        Assert.assertTrue(
                missingSubCards.isEmpty(),
                "COURSE-INTEGRITY-002B failed: "
                        + missingSubCards.size()
                        + " Khóa học course card(s) are missing subCard. "
                        + "See Allure summary."
        );
    }
    @Test(
            testName = "COURSE-INTEGRITY-002C - Danh mục subCard Structure Integrity",
            description = "COURSE-INTEGRITY-002C - Danh mục subCard Structure Integrity"
    )
    @Story("Every Danh mục course card contains hover-detail structure")
    @Description(
            "Discover the real Danh mục Course category links from the Homepage, "
                    + "visit each category, and verify that every rendered Course card "
                    + "contains its subCard hover-detail element in the DOM. "
                    + "No hover interaction or Course Detail navigation is performed."
    )
    public void everyCategoryCourseCardShouldContainSubCard() {

        List<CourseSubCardSupport.MissingSubCard> missingSubCards =
                new ArrayList<>();

        HomePage homePage =
                new HomePage(driver);

        List<String> categoryUrls =
                homePage.getCourseCategoryUrls();

        if (categoryUrls.isEmpty()) {

            throw new IllegalStateException(
                    "No /danhmuckhoahoc/ category links were discovered on the Homepage."
            );
        }

        int totalCardsChecked = 0;
        int categoriesChecked = 0;

        LOGGER.info(
                "COURSE-INTEGRITY-002C | Starting Danh mục subCard structure sweep | categories={}",
                categoryUrls.size()
        );

        CourseCategoryPage categoryPage =
                new CourseCategoryPage(driver);

        for (String categoryUrl : categoryUrls) {

            categoryPage.open(categoryUrl);

            String categoryName =
                    categoryPage.getCategoryName();

            List<CourseCardSnapshot> courses =
                    categoryPage.captureCourseCards();

            categoriesChecked++;

            int missingInThisCategory = 0;

            for (CourseCardSnapshot course
                    : courses) {

                totalCardsChecked++;

                if (!course.hasSubCard()) {

                    missingInThisCategory++;

                    missingSubCards.add(
                            new CourseSubCardSupport.MissingSubCard(
                                    "Danh mục",
                                    categoryName
                                            + " | "
                                            + categoryUrl,
                                    course.cardIndex(),
                                    course.name(),
                                    course.href()
                            )
                    );

                    LOGGER.warn(
                            "MISSING SUBCARD | category=\"{}\" | card={} | name=\"{}\" | href={}",
                            categoryName,
                            course.cardIndex(),
                            course.name(),
                            course.href()
                    );
                }
            }

            LOGGER.info(
                    "CATEGORY \"{}\" | cards={} | checked={} | missingThisCategory={} | totalMissing={}",
                    categoryName,
                    courses.size(),
                    totalCardsChecked,
                    missingInThisCategory,
                    missingSubCards.size()
            );
        }

        String summary =
                CourseSubCardSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-002C",
                        "Danh mục",
                        "Categories checked",
                        categoriesChecked,
                        totalCardsChecked,
                        missingSubCards
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-002C Danh mục subCard summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-002C COMPLETE | categories={} | checked={} | missingSubCards={}",
                categoriesChecked,
                totalCardsChecked,
                missingSubCards.size()
        );

        Assert.assertTrue(
                missingSubCards.isEmpty(),
                "COURSE-INTEGRITY-002C failed: "
                        + missingSubCards.size()
                        + " Danh mục course card(s) are missing subCard. "
                        + "See Allure summary."
        );
    }
}