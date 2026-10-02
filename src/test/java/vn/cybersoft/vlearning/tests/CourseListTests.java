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
import vn.cybersoft.vlearning.data.CourseList.CourseSubCardContentSupport;

@Epic("V Learning")
@Feature("Course List")
public class CourseListTests extends BaseTest {

    private static final Logger LOGGER =
            LogManager.getLogger(CourseListTests.class);

    @Test(
            groups = {"course-integrity-tc1"},
            testName = "COURSE-INTEGRITY-001A - Homepage Course Detail URL ID Integrity",
            description = "COURSE-INTEGRITY-001A - Homepage Course Detail URL ID Integrity"
    )
    @Story("Every Homepage Course card contains a Course Detail ID")
    @Description(
            "Inspect every Course card rendered on the Homepage and verify that "
                    + "every href targets /chitiet/ and contains a non-empty "
                    + "Course ID. No Course Detail navigation is performed."
    )
    public void everyHomepageCourseShouldHaveCourseDetailId() {

        List<CourseListSupport.CourseUrlProblem> problems =
                new ArrayList<>();

        LOGGER.info(
                "COURSE-INTEGRITY-001A | Starting Homepage URL integrity sweep"
        );

        HomePage homePage =
                new HomePage(driver);

        List<CourseCardSnapshot> courses =
                homePage.captureHomepageCourseCards();

        for (CourseCardSnapshot course : courses) {

            String reason =
                    CourseListSupport.findCourseUrlProblem(
                            course.href()
                    );

            if (reason != null) {

                problems.add(
                        new CourseListSupport.CourseUrlProblem(
                                "Homepage",
                                driver.getCurrentUrl(),
                                course.cardIndex(),
                                course.name(),
                                course.href(),
                                reason
                        )
                );

                LOGGER.warn(
                        "INVALID COURSE URL | surface=Homepage | card={} | name=\"{}\" | href={}",
                        course.cardIndex(),
                        course.name(),
                        course.href()
                );
            }
        }

        String summary =
                CourseListSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-001A",
                        "Homepage",
                        "Pages checked",
                        1,
                        courses.size(),
                        problems
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-001A Homepage URL integrity summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-001A COMPLETE | checked={} | invalidUrls={}",
                courses.size(),
                problems.size()
        );

        Assert.assertTrue(
                problems.isEmpty(),
                "COURSE-INTEGRITY-001A failed: "
                        + problems.size()
                        + " Homepage Course card(s) have invalid Course Detail URLs. "
                        + "See Allure summary."
        );
    }
    @Test(
            groups = {"course-integrity-tc1"},
            testName = "COURSE-INTEGRITY-001B - Khóa học Course Detail URL ID Integrity",
            description = "COURSE-INTEGRITY-001B - Khóa học Course Detail URL ID Integrity"
    )
    @Story("Every Khóa học Course card contains a Course Detail ID")
    @Description(
            "Traverse every Khóa học pagination page and verify that every "
                    + "rendered Course card href targets /chitiet/ and contains "
                    + "a non-empty Course ID. No Course Detail navigation is performed."
    )
    public void everyCourseListCourseShouldHaveCourseDetailId() {

        List<CourseListSupport.CourseUrlProblem> problems =
                new ArrayList<>();

        Set<Integer> visitedPages =
                new HashSet<>();

        Set<List<String>> visitedDatasets =
                new HashSet<>();

        int totalCardsChecked = 0;

        LOGGER.info(
                "COURSE-INTEGRITY-001B | Starting Khóa học URL integrity sweep"
        );

        CourseListPage catalog =
                new HomePage(driver)
                        .goToCourseListPage();

        CatalogPage page =
                catalog.captureCurrentPage();

        while (page != null) {

            if (!visitedPages.add(
                    page.number()
            )) {

                throw new IllegalStateException(
                        "Pagination revisited page "
                                + page.number()
                );
            }

            if (!visitedDatasets.add(
                    page.hrefs()
            )) {

                throw new IllegalStateException(
                        "Pagination returned duplicate dataset at page "
                                + page.number()
                );
            }

            int invalidOnThisPage = 0;

            for (CourseCandidate course
                    : page.courses()) {

                totalCardsChecked++;

                String reason =
                        CourseListSupport.findCourseUrlProblem(
                                course.href()
                        );

                if (reason != null) {

                    invalidOnThisPage++;

                    problems.add(
                            new CourseListSupport.CourseUrlProblem(
                                    "Khóa học",
                                    "Page " + page.number(),
                                    course.cardIndex(),
                                    course.name(),
                                    course.href(),
                                    reason
                            )
                    );

                    LOGGER.warn(
                            "INVALID COURSE URL | surface=Khóa học | global={} | page={} | card={} | name=\"{}\" | href={}",
                            totalCardsChecked,
                            page.number(),
                            course.cardIndex(),
                            course.name(),
                            course.href()
                    );
                }
            }

            LOGGER.info(
                    "PAGE {} | cards={} | checked={} | invalidThisPage={} | totalInvalid={}",
                    page.number(),
                    page.courses().size(),
                    totalCardsChecked,
                    invalidOnThisPage,
                    problems.size()
            );

            page =
                    catalog.goToNextPage(
                            page
                    );
        }

        String summary =
                CourseListSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-001B",
                        "Khóa học",
                        "Pagination pages checked",
                        visitedPages.size(),
                        totalCardsChecked,
                        problems
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-001B Khóa học URL integrity summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-001B COMPLETE | pages={} | checked={} | invalidUrls={}",
                visitedPages.size(),
                totalCardsChecked,
                problems.size()
        );

        Assert.assertTrue(
                problems.isEmpty(),
                "COURSE-INTEGRITY-001B failed: "
                        + problems.size()
                        + " Khóa học Course card(s) have invalid Course Detail URLs. "
                        + "See Allure summary."
        );
    }

    @Test(
            groups = {"course-integrity-tc1"},
            testName = "COURSE-INTEGRITY-001C - Danh mục Course Detail URL ID Integrity",
            description = "COURSE-INTEGRITY-001C - Danh mục Course Detail URL ID Integrity"
    )
    @Story("Every Danh mục Course card contains a Course Detail ID")
    @Description(
            "Discover the real Course category URLs from the Homepage, visit "
                    + "every Danh mục Course page, and verify that every rendered "
                    + "Course card href targets /chitiet/ and contains a non-empty "
                    + "Course ID. No Course Detail navigation is performed."
    )
    public void everyCategoryCourseShouldHaveCourseDetailId() {

        List<CourseListSupport.CourseUrlProblem> problems =
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

        int categoriesChecked = 0;
        int totalCardsChecked = 0;

        LOGGER.info(
                "COURSE-INTEGRITY-001C | Starting Danh mục URL integrity sweep | categories={}",
                categoryUrls.size()
        );

        CourseCategoryPage categoryPage =
                new CourseCategoryPage(driver);

        for (String categoryUrl
                : categoryUrls) {

            categoryPage.open(
                    categoryUrl
            );

            String categoryName =
                    categoryPage.getCategoryName();

            List<CourseCardSnapshot> courses =
                    categoryPage.captureCourseCards();

            categoriesChecked++;

            int invalidInThisCategory = 0;

            for (CourseCardSnapshot course
                    : courses) {

                totalCardsChecked++;

                String reason =
                        CourseListSupport.findCourseUrlProblem(
                                course.href()
                        );

                if (reason != null) {

                    invalidInThisCategory++;

                    problems.add(
                            new CourseListSupport.CourseUrlProblem(
                                    "Danh mục",
                                    categoryName
                                            + " | "
                                            + categoryUrl,
                                    course.cardIndex(),
                                    course.name(),
                                    course.href(),
                                    reason
                            )
                    );

                    LOGGER.warn(
                            "INVALID COURSE URL | category=\"{}\" | card={} | name=\"{}\" | href={}",
                            categoryName,
                            course.cardIndex(),
                            course.name(),
                            course.href()
                    );
                }
            }

            LOGGER.info(
                    "CATEGORY \"{}\" | cards={} | checked={} | invalidThisCategory={} | totalInvalid={}",
                    categoryName,
                    courses.size(),
                    totalCardsChecked,
                    invalidInThisCategory,
                    problems.size()
            );
        }

        String summary =
                CourseListSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-001C",
                        "Danh mục",
                        "Categories checked",
                        categoriesChecked,
                        totalCardsChecked,
                        problems
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-001C Danh mục URL integrity summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-001C COMPLETE | categories={} | checked={} | invalidUrls={}",
                categoriesChecked,
                totalCardsChecked,
                problems.size()
        );

        Assert.assertTrue(
                problems.isEmpty(),
                "COURSE-INTEGRITY-001C failed: "
                        + problems.size()
                        + " Danh mục Course card(s) have invalid Course Detail URLs. "
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
    @Test(
            groups = {"course-integrity-tc3"},
            testName = "COURSE-INTEGRITY-003A - Homepage SubCard Content Integrity",
            description = "COURSE-INTEGRITY-003A - Homepage SubCard Content Integrity"
    )
    @Story("Homepage subCard information matches its Course card")
    @Description(
            "Inspect every Homepage Course card and validate the complete subCard "
                    + "content contract. Compare Course name, instructor name, hours, "
                    + "weeks and level with the parent card. Also verify that the "
                    + "subCard contains an instructor avatar, non-blank description "
                    + "and XEM CHI TIẾT button. No hover interaction is performed."
    )
    public void everyHomepageSubCardShouldContainCorrectCourseInformation() {

        List<CourseSubCardContentSupport.CardProblem> problems =
                new ArrayList<>();

        int fieldFailures = 0;

        LOGGER.info(
                "COURSE-INTEGRITY-003A | Starting Homepage subCard content sweep"
        );

        HomePage homePage =
                new HomePage(driver);

        List<CourseCardSnapshot> courses =
                homePage.captureHomepageCourseCards();

        for (CourseCardSnapshot course
                : courses) {

            List<CourseSubCardContentSupport.FieldIssue> issues =
                    CourseSubCardContentSupport.inspect(
                            course
                    );

            if (!issues.isEmpty()) {

                fieldFailures +=
                        issues.size();

                CourseSubCardContentSupport.CardProblem problem =
                        new CourseSubCardContentSupport.CardProblem(
                                "Homepage",
                                driver.getCurrentUrl(),
                                course.cardIndex(),
                                course.name(),
                                course.href(),
                                issues
                        );

                problems.add(problem);

                LOGGER.warn(
                        "SUBCARD CONTENT FAIL | surface=Homepage | card={} | name=\"{}\" | fields={}",
                        course.cardIndex(),
                        course.name(),
                        problem.fields()
                );
            }
        }

        String summary =
                CourseSubCardContentSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-003A",
                        "Homepage",
                        "Pages checked",
                        1,
                        courses.size(),
                        problems
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-003A Homepage subCard content summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-003A COMPLETE | checked={} | problemCards={} | fieldFailures={}",
                courses.size(),
                problems.size(),
                fieldFailures
        );

        Assert.assertTrue(
                problems.isEmpty(),
                "COURSE-INTEGRITY-003A failed: "
                        + problems.size()
                        + " Homepage course card(s) contain subCard content problems "
                        + "across "
                        + fieldFailures
                        + " field(s). See Allure summary."
        );
    }
    @Test(
            groups = {"course-integrity-tc3"},
            testName = "COURSE-INTEGRITY-003B - Khóa học SubCard Content Integrity",
            description = "COURSE-INTEGRITY-003B - Khóa học SubCard Content Integrity"
    )
    @Story("Khóa học subCard information matches its Course card")
    @Description(
            "Traverse every Khóa học pagination page and validate the complete "
                    + "subCard content contract for every rendered Course card. "
                    + "Compare Course name, instructor name, hours, weeks and level "
                    + "with the parent card. Also verify instructor avatar, description "
                    + "and XEM CHI TIẾT button. No hover interaction is performed."
    )
    public void everyCourseListSubCardShouldContainCorrectCourseInformation() {

        List<CourseSubCardContentSupport.CardProblem> problems =
                new ArrayList<>();

        Set<Integer> visitedPages =
                new HashSet<>();

        Set<List<String>> visitedDatasets =
                new HashSet<>();

        int totalCardsChecked = 0;
        int fieldFailures = 0;

        LOGGER.info(
                "COURSE-INTEGRITY-003B | Starting Khóa học subCard content sweep"
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

            int problemCardsOnThisPage = 0;

            for (CourseCandidate course
                    : page.courses()) {

                totalCardsChecked++;

                List<CourseSubCardContentSupport.FieldIssue> issues =
                        CourseSubCardContentSupport.inspect(
                                course.snapshot()
                        );

                if (!issues.isEmpty()) {

                    problemCardsOnThisPage++;
                    fieldFailures +=
                            issues.size();

                    CourseSubCardContentSupport.CardProblem problem =
                            new CourseSubCardContentSupport.CardProblem(
                                    "Khóa học",
                                    "Page " + page.number(),
                                    course.cardIndex(),
                                    course.name(),
                                    course.href(),
                                    issues
                            );

                    problems.add(problem);

                    LOGGER.warn(
                            "SUBCARD CONTENT FAIL | surface=Khóa học | page={} | card={} | name=\"{}\" | fields={}",
                            page.number(),
                            course.cardIndex(),
                            course.name(),
                            problem.fields()
                    );
                }
            }

            LOGGER.info(
                    "PAGE {} | cards={} | checked={} | problemCardsThisPage={} | totalProblemCards={} | fieldFailures={}",
                    page.number(),
                    page.courses().size(),
                    totalCardsChecked,
                    problemCardsOnThisPage,
                    problems.size(),
                    fieldFailures
            );

            page =
                    catalog.goToNextPage(page);
        }

        String summary =
                CourseSubCardContentSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-003B",
                        "Khóa học",
                        "Pagination pages checked",
                        visitedPages.size(),
                        totalCardsChecked,
                        problems
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-003B Khóa học subCard content summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-003B COMPLETE | pages={} | checked={} | problemCards={} | fieldFailures={}",
                visitedPages.size(),
                totalCardsChecked,
                problems.size(),
                fieldFailures
        );

        Assert.assertTrue(
                problems.isEmpty(),
                "COURSE-INTEGRITY-003B failed: "
                        + problems.size()
                        + " Khóa học course card(s) contain subCard content problems "
                        + "across "
                        + fieldFailures
                        + " field(s). See Allure summary."
        );
    }
    @Test(
            groups = {"course-integrity-tc3"},
            testName = "COURSE-INTEGRITY-003C - Danh mục SubCard Content Integrity",
            description = "COURSE-INTEGRITY-003C - Danh mục SubCard Content Integrity"
    )
    @Story("Danh mục subCard information matches its Course card")
    @Description(
            "Discover the real Danh mục Course category links from the Homepage, "
                    + "visit every category and validate the complete subCard content "
                    + "contract for every rendered Course card. Compare Course name, "
                    + "instructor name, hours, weeks and level with the parent card. "
                    + "Also verify instructor avatar, description and XEM CHI TIẾT button. "
                    + "No hover interaction is performed."
    )
    public void everyCategorySubCardShouldContainCorrectCourseInformation() {

        List<CourseSubCardContentSupport.CardProblem> problems =
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

        int categoriesChecked = 0;
        int totalCardsChecked = 0;
        int fieldFailures = 0;

        LOGGER.info(
                "COURSE-INTEGRITY-003C | Starting Danh mục subCard content sweep | categories={}",
                categoryUrls.size()
        );

        CourseCategoryPage categoryPage =
                new CourseCategoryPage(driver);

        for (String categoryUrl
                : categoryUrls) {

            categoryPage.open(
                    categoryUrl
            );

            String categoryName =
                    categoryPage.getCategoryName();

            List<CourseCardSnapshot> courses =
                    categoryPage.captureCourseCards();

            categoriesChecked++;

            int problemCardsInThisCategory = 0;

            for (CourseCardSnapshot course
                    : courses) {

                totalCardsChecked++;

                List<CourseSubCardContentSupport.FieldIssue> issues =
                        CourseSubCardContentSupport.inspect(
                                course
                        );

                if (!issues.isEmpty()) {

                    problemCardsInThisCategory++;
                    fieldFailures +=
                            issues.size();

                    CourseSubCardContentSupport.CardProblem problem =
                            new CourseSubCardContentSupport.CardProblem(
                                    "Danh mục",
                                    categoryName
                                            + " | "
                                            + categoryUrl,
                                    course.cardIndex(),
                                    course.name(),
                                    course.href(),
                                    issues
                            );

                    problems.add(problem);

                    LOGGER.warn(
                            "SUBCARD CONTENT FAIL | category=\"{}\" | card={} | name=\"{}\" | fields={}",
                            categoryName,
                            course.cardIndex(),
                            course.name(),
                            problem.fields()
                    );
                }
            }

            LOGGER.info(
                    "CATEGORY \"{}\" | cards={} | checked={} | problemCardsThisCategory={} | totalProblemCards={} | fieldFailures={}",
                    categoryName,
                    courses.size(),
                    totalCardsChecked,
                    problemCardsInThisCategory,
                    problems.size(),
                    fieldFailures
            );
        }

        String summary =
                CourseSubCardContentSupport.buildDetailedSummary(
                        "COURSE-INTEGRITY-003C",
                        "Danh mục",
                        "Categories checked",
                        categoriesChecked,
                        totalCardsChecked,
                        problems
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-003C Danh mục subCard content summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-003C COMPLETE | categories={} | checked={} | problemCards={} | fieldFailures={}",
                categoriesChecked,
                totalCardsChecked,
                problems.size(),
                fieldFailures
        );

        Assert.assertTrue(
                problems.isEmpty(),
                "COURSE-INTEGRITY-003C failed: "
                        + problems.size()
                        + " Danh mục course card(s) contain subCard content problems "
                        + "across "
                        + fieldFailures
                        + " field(s). See Allure summary."
        );
    }
}