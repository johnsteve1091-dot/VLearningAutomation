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
import vn.cybersoft.vlearning.data.CourseList.CourseCardContentSupport;
import vn.cybersoft.vlearning.pages.CourseDetailPage;

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
    @Test(
            groups = {"course-integrity-tc4"},
            testName = "COURSE-INTEGRITY-004 - Khóa học Main Course Card Content Integrity",
            description = "COURSE-INTEGRITY-004 - Khóa học Main Course Card Content Integrity"
    )
    @Story("Every Khóa học main Course card contains its required visible information")
    @Description(
            "Traverse every Khóa học pagination page and inspect every rendered "
                    + "main Course card. Verify Course image, Course name, instructor "
                    + "avatar, instructor name, original price, conditional discount "
                    + "price and original-price strikethrough, rating score and review "
                    + "count. Collect all field-level defects and fail once after the "
                    + "complete Course catalog sweep. Course Detail pages are not opened."
    )
    public void everyCourseListMainCardShouldContainRequiredContent() {

        List<CourseCardContentSupport.CardProblem> problems =
                new ArrayList<>();

        Set<Integer> visitedPages =
                new HashSet<>();

        Set<List<String>> visitedDatasets =
                new HashSet<>();

        int totalCardsChecked =
                0;

        int fieldFailures =
                0;

        LOGGER.info(
                "COURSE-INTEGRITY-004 | Starting Khóa học main-card content sweep"
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

            int problemCardsOnThisPage =
                    0;

            int fieldFailuresOnThisPage =
                    0;

            for (CourseCandidate course
                    : page.courses()) {

                totalCardsChecked++;

                List<CourseCardContentSupport.FieldIssue> issues =
                        CourseCardContentSupport.inspect(
                                course.snapshot()
                        );

                if (!issues.isEmpty()) {

                    problemCardsOnThisPage++;

                    fieldFailuresOnThisPage +=
                            issues.size();

                    fieldFailures +=
                            issues.size();

                    CourseCardContentSupport.CardProblem problem =
                            new CourseCardContentSupport.CardProblem(
                                    page.number(),
                                    course.cardIndex(),
                                    course.name(),
                                    course.href(),
                                    issues
                            );

                    problems.add(
                            problem
                    );

                    LOGGER.warn(
                            "MAIN CARD CONTENT FAIL | page={} | card={} | name=\"{}\" | fields={}",
                            page.number(),
                            course.cardIndex(),
                            course.name(),
                            problem.fields()
                    );
                }
            }

            LOGGER.info(
                    "PAGE {} | cards={} | checked={} | problemCardsThisPage={} | fieldFailuresThisPage={} | totalProblemCards={} | totalFieldFailures={}",
                    page.number(),
                    page.courses().size(),
                    totalCardsChecked,
                    problemCardsOnThisPage,
                    fieldFailuresOnThisPage,
                    problems.size(),
                    fieldFailures
            );

            page =
                    catalog.goToNextPage(
                            page
                    );
        }

        String summary =
                CourseCardContentSupport.buildDetailedSummary(
                        visitedPages.size(),
                        totalCardsChecked,
                        problems
                );

        Allure.addAttachment(
                "COURSE-INTEGRITY-004 Main Course Card Content Summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-INTEGRITY-004 COMPLETE | pages={} | checked={} | problemCards={} | fieldFailures={}",
                visitedPages.size(),
                totalCardsChecked,
                problems.size(),
                fieldFailures
        );

        Assert.assertTrue(
                problems.isEmpty(),
                "COURSE-INTEGRITY-004 failed: "
                        + problems.size()
                        + " Course card(s) contain main-card content problems across "
                        + fieldFailures
                        + " field(s). See Allure summary."
        );
    }
    @Test(
            groups = {"course-list-final"},
            testName = "COURSE-LIST-005A - Course List Page Contract",
            description = "COURSE-LIST-005A - Course List Page Contract"
    )
    @Story("Course List page opens and exposes its required page-level content")
    @Description(
            "Covers the simple Course List page-level contract from "
                    + "TC_CL_001 through TC_CL_004. Navigate from Homepage using "
                    + "the KHÓA HỌC menu, verify /khoahoc loads, verify the page "
                    + "title and subtitle, verify all six Insight Information "
                    + "items, verify the Danh sách khóa học heading and confirm "
                    + "that visible Course cards are present."
    )
    public void courseListPageShouldExposeRequiredContract() {

        LOGGER.info(
                "COURSE-LIST-005A | Starting Course List page contract"
        );

        CourseListPage catalog =
                new HomePage(driver)
                        .goToCourseListPage();

        /*
         * TC_CL_001
         * Homepage → KHÓA HỌC → /khoahoc
         */
        Assert.assertTrue(
                driver.getCurrentUrl()
                        .endsWith(
                                "/khoahoc"
                        ),
                "TC_CL_001 failed: KHÓA HỌC menu did not open /khoahoc. "
                        + "Actual URL: "
                        + driver.getCurrentUrl()
        );

        /*
         * TC_CL_002
         */
        Assert.assertEquals(
                catalog.getPageTitle(),
                "Khóa học",
                "TC_CL_002 failed: Course List title is incorrect."
        );

        Assert.assertEquals(
                catalog.getPageSubtitle(),
                "Bắt đầu hành trình nào!!!",
                "TC_CL_002 failed: Course List subtitle is incorrect."
        );

        /*
         * TC_CL_003
         *
         * Exact label/count requirements from the legacy scope.
         *
         * The captured DOM establishes an <i> element for each Insight
         * item. We verify that an icon exists by requiring a nonblank
         * icon class. We do NOT invent unverified Font Awesome class
         * names for each individual icon.
         */
        String[][] expectedInsights = {
                {
                        "CHƯƠNG TRÌNH HỌC",
                        "300"
                },
                {
                        "NHÀ SÁNG TẠO",
                        "10000"
                },
                {
                        "NHÀ THIẾT KẾ",
                        "400"
                },
                {
                        "BÀI GIẢNG",
                        "3000"
                },
                {
                        "VIDEO",
                        "40000"
                },
                {
                        "LĨNH VỰC",
                        "200"
                }
        };

        List<CourseListPage.InsightItem> insights =
                catalog.getInsightItems();

        Assert.assertEquals(
                insights.size(),
                expectedInsights.length,
                "TC_CL_003 failed: expected exactly six Insight items."
        );

        StringBuilder insightReport =
                new StringBuilder(
                        "COURSE-LIST-005A Insight Information\n"
                );

        for (int index = 0;
             index < expectedInsights.length;
             index++) {

            CourseListPage.InsightItem actual =
                    insights.get(index);

            String expectedLabel =
                    expectedInsights[index][0];

            String expectedCount =
                    expectedInsights[index][1];

            Assert.assertEquals(
                    actual.label(),
                    expectedLabel,
                    "TC_CL_003 failed: wrong Insight label at position "
                            + (index + 1)
            );

            Assert.assertEquals(
                    actual.count(),
                    expectedCount,
                    "TC_CL_003 failed: wrong Insight count for "
                            + expectedLabel
            );

            Assert.assertFalse(
                    actual.iconClass() == null
                            || actual.iconClass().isBlank(),
                    "TC_CL_003 failed: Insight item "
                            + expectedLabel
                            + " has no icon."
            );

            insightReport
                    .append(index + 1)
                    .append(". ")
                    .append(actual.label())
                    .append(" | ")
                    .append(actual.count())
                    .append(" | icon=")
                    .append(actual.iconClass())
                    .append('\n');
        }

        /*
         * TC_CL_004
         */
        Assert.assertEquals(
                catalog.getCourseListHeading(),
                "Danh sách khóa học",
                "TC_CL_004 failed: Danh sách khóa học heading is missing or incorrect."
        );

        int visibleCards =
                catalog.getVisibleCourseCardCount();

        Assert.assertTrue(
                visibleCards > 0,
                "TC_CL_004 failed: no visible Course cards were rendered."
        );

        Assert.assertTrue(
                catalog.areAllCourseCardContainersDisplayed(),
                "TC_CL_004 failed: one or more Course card containers are hidden "
                        + "or have invalid dimensions."
        );

        Allure.addAttachment(
                "COURSE-LIST-005A Page Contract Summary",
                "text/plain",
                insightReport
                        .append(
                                "\nVisible Course cards: "
                        )
                        .append(
                                visibleCards
                        )
                        .toString(),
                ".txt"
        );

        LOGGER.info(
                "COURSE-LIST-005A COMPLETE | insights={} | visibleCards={}",
                insights.size(),
                visibleCards
        );
    }
    @Test(
            groups = {"course-list-final"},
            testName = "COURSE-LIST-005B - Course Card Detail Navigation",
            description = "COURSE-LIST-005B - Course Card Detail Navigation"
    )
    @Story("A Course List card opens a healthy Course Detail page")
    @Description(
            "Covers TC_CL_013. Open the Course List, select the first Course "
                    + "card that already satisfies the Course Detail URL-ID contract, "
                    + "click that actual rendered card and verify that the resulting "
                    + "browser URL matches the selected card href and that a healthy "
                    + "Course Detail title is rendered. TC_CL_014 deep card-versus-detail "
                    + "content comparison is intentionally handled later in the Course "
                    + "Detail test phase."
    )
    public void courseCardShouldOpenHealthyCourseDetailPage() {

        LOGGER.info(
                "COURSE-LIST-005B | Starting Course Detail navigation check"
        );

        CourseListPage catalog =
                new HomePage(driver)
                        .goToCourseListPage();

        CatalogPage page =
                catalog.captureCurrentPage();

        CourseCandidate selectedCourse =
                null;

        /*
         * TC1 already owns invalid /chitiet/ URL detection.
         *
         * Do not deliberately select the known /chitiet/ missing-ID card
         * here because TC5B is testing navigation behavior, not TC1 again.
         */
        for (CourseCandidate candidate
                : page.courses()) {

            String urlProblem =
                    CourseListSupport.findCourseUrlProblem(
                            candidate.href()
                    );

            if (urlProblem == null) {

                selectedCourse =
                        candidate;

                break;
            }
        }

        Assert.assertNotNull(
                selectedCourse,
                "TC_CL_013 cannot run: Page 1 contains no Course card "
                        + "with a valid /chitiet/<id> href."
        );

        String expectedHref =
                selectedCourse.href();

        String selectedName =
                selectedCourse.name();

        int selectedCardIndex =
                selectedCourse.cardIndex();

        LOGGER.info(
                "COURSE-LIST-005B | Selected card={} | name=\"{}\" | href={}",
                selectedCardIndex,
                selectedName,
                expectedHref
        );

        /*
         * IMPORTANT:
         * actually click the rendered Course card.
         *
         * Do NOT replace this with driver.get(expectedHref),
         * because TC_CL_013 specifically tests card navigation.
         */
        CourseDetailPage detailPage =
                catalog.openCourseAt(
                        selectedCardIndex
                );

        String detailTitle =
                detailPage.getCourseTitle();

        String resultingUrl =
                driver.getCurrentUrl();

        Assert.assertEquals(
                resultingUrl,
                expectedHref,
                "TC_CL_013 failed: clicking the Course card navigated "
                        + "to an unexpected URL."
        );

        Assert.assertFalse(
                detailTitle == null
                        || detailTitle.isBlank(),
                "TC_CL_013 failed: Course Detail page opened but the "
                        + "healthy titleDetailCourse marker is missing or blank."
        );

        String summary =
                "COURSE-LIST-005B - Course Card Detail Navigation\n"
                        + "Source page: "
                        + page.number()
                        + "\nCard position: "
                        + selectedCardIndex
                        + "\nCourse card name: "
                        + selectedName
                        + "\nExpected href: "
                        + expectedHref
                        + "\nResulting URL: "
                        + resultingUrl
                        + "\nDetail title: "
                        + detailTitle
                        + "\n";

        Allure.addAttachment(
                "COURSE-LIST-005B Navigation Summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-LIST-005B COMPLETE | result={} | detailTitle=\"{}\"",
                resultingUrl,
                detailTitle
        );
    }
    @Test(
            groups = {"course-list-final"},
            testName = "COURSE-LIST-005C - Pagination Contract",
            description = "COURSE-LIST-005C - Pagination Contract"
    )
    @Story("Course List pagination behaves correctly")
    @Description(
            "Combines the desktop Course List pagination contract from TC_CL_015 "
                    + "through TC_CL_029 into one interaction test. Validate the "
                    + "single-page branch when applicable, or for a multi-page "
                    + "catalog validate controls, maximum visible cards, Previous "
                    + "and Next behavior, numbered-page navigation, active-page "
                    + "state, dataset refresh, loading indicator, last-page behavior "
                    + "and ellipsis page-group navigation."
    )
    public void courseListPaginationShouldBehaveCorrectly() {

        LOGGER.info(
                "COURSE-LIST-005C | Starting pagination contract"
        );

        CourseListPage catalog =
                new HomePage(driver)
                        .goToCourseListPage();

        /*
         * ============================================================
         * TC_CL_015
         * SINGLE-PAGE DATASET BRANCH
         * ============================================================
         *
         * The current live environment normally has multiple pages.
         *
         * If the dataset ever falls to <= 12 courses and pagination
         * disappears, this branch validates TC_CL_015 naturally.
         */
        if (!catalog.isPaginationDisplayed()) {

            int visibleCards =
                    catalog.getVisibleCourseCardCount();

            Assert.assertTrue(
                    visibleCards <= 12,
                    "TC_CL_015 failed: pagination is absent but more than "
                            + "12 Course cards are displayed."
            );

            Allure.addAttachment(
                    "COURSE-LIST-005C Pagination Summary",
                    "text/plain",
                    "Single-page dataset detected.\n"
                            + "Visible Course cards: "
                            + visibleCards
                            + "\nPagination displayed: false\n",
                    ".txt"
            );

            LOGGER.info(
                    "COURSE-LIST-005C COMPLETE | singlePage=true | cards={}",
                    visibleCards
            );

            return;
        }

        /*
         * ============================================================
         * MULTI-PAGE CONTRACT
         * ============================================================
         */

        CatalogPage firstPage =
                catalog.captureCurrentPage();

        Assert.assertEquals(
                firstPage.number(),
                1,
                "Pagination should initially activate page 1."
        );

        /*
         * TC_CL_016
         */
        Assert.assertTrue(
                catalog.isPreviousControlDisplayed(),
                "TC_CL_016 failed: Previous control is missing."
        );

        Assert.assertTrue(
                catalog.isNextControlDisplayed(),
                "TC_CL_016 failed: Next control is missing."
        );

        List<Integer> initialPageNumbers =
                catalog.getVisiblePageNumbers();

        Assert.assertFalse(
                initialPageNumbers.isEmpty(),
                "TC_CL_016 failed: no numeric page buttons are displayed."
        );

        /*
         * TC_CL_017
         */
        Assert.assertTrue(
                firstPage.courses().size() <= 12,
                "TC_CL_017 failed: page 1 displays more than 12 Course cards."
        );

        /*
         * TC_CL_021
         */
        Assert.assertTrue(
                catalog.isPreviousDisabled(),
                "TC_CL_021 failed: Previous should be disabled on page 1."
        );

        /*
         * Determine the last page dynamically from the pagination
         * numbers currently rendered by the frontend.
         *
         * Current frontend exposes the final page numbers even while
         * the active page is near the beginning.
         */
        int lastPageNumber =
                0;

        for (int pageNumber
                : initialPageNumbers) {

            if (pageNumber > lastPageNumber) {

                lastPageNumber =
                        pageNumber;
            }
        }

        Assert.assertTrue(
                lastPageNumber > 1,
                "Multi-page pagination is displayed but no page number greater "
                        + "than 1 was discovered."
        );

        /*
         * If > 8 pages exist, the legacy requirement expects "...".
         */
        if (lastPageNumber > 8) {

            Assert.assertTrue(
                    catalog.getVisibleEllipsisCount() > 0,
                    "TC_CL_016 failed: pagination has more than 8 pages "
                            + "but no ellipsis is displayed."
            );
        }

        Assert.assertFalse(
                catalog.isNextDisabled(),
                "Next should be usable on the first page of a multi-page catalog."
        );

        /*
         * ============================================================
         * TC_CL_018 + TC_CL_024 + TC_CL_025
         * NEXT + DATASET CHANGE + LOADING INDICATOR
         * ============================================================
         */

        catalog.clickNextPage();

        /*
         * Loader captured from the real frontend:
         *
         * <div id="preloader">
         *     <div id="loader"></div>
         * </div>
         */
        boolean loaderAppeared =
                catalog.waitForLoadingIndicatorToAppear();

        Assert.assertTrue(
                loaderAppeared,
                "TC_CL_025 failed: #preloader was not observed after "
                        + "pagination navigation."
        );

        catalog.waitForLoadingIndicatorToDisappear();

        CatalogPage secondPage =
                catalog.waitForPageChange(
                        firstPage
                );

        Assert.assertEquals(
                secondPage.number(),
                firstPage.number() + 1,
                "TC_CL_018 failed: Next did not activate the following page."
        );

        Assert.assertNotEquals(
                secondPage.hrefs(),
                firstPage.hrefs(),
                "TC_CL_018/TC_CL_024 failed: Course dataset did not change."
        );

        Assert.assertFalse(
                secondPage.courses().isEmpty(),
                "TC_CL_024 failed: destination page contains no Course cards."
        );

        Assert.assertTrue(
                secondPage.courses().size() <= 12,
                "TC_CL_017 failed: destination page displays more than 12 cards."
        );

        /*
         * No duplicate Course cards on the current page.
         */
        Assert.assertEquals(
                new HashSet<>(
                        secondPage.hrefs()
                ).size(),
                secondPage.hrefs().size(),
                "TC_CL_024 failed: duplicate Course hrefs exist on the current page."
        );

        /*
         * TC_CL_023
         *
         * captureCurrentPage() obtains the number from the element
         * carrying class 'active', so this explicit assertion verifies
         * the active-page state too.
         */
        Assert.assertEquals(
                catalog.getCurrentPageNumber(),
                secondPage.number(),
                "TC_CL_023 failed: active pagination number is incorrect."
        );

        /*
         * ============================================================
         * TC_CL_020
         * PREVIOUS
         * ============================================================
         */

        catalog.clickPreviousPage();

        CatalogPage returnedFirstPage =
                catalog.waitForPageChange(
                        secondPage
                );

        Assert.assertEquals(
                returnedFirstPage.number(),
                1,
                "TC_CL_020 failed: Previous did not return to page 1."
        );

        Assert.assertEquals(
                returnedFirstPage.hrefs(),
                firstPage.hrefs(),
                "TC_CL_020 failed: returning to page 1 did not restore "
                        + "the page 1 Course dataset."
        );

        Assert.assertTrue(
                catalog.isPreviousDisabled(),
                "TC_CL_021 failed: Previous is not disabled after returning "
                        + "to page 1."
        );

        /*
         * ============================================================
         * TC_CL_022 + TC_CL_023
         * NUMBERED PAGE
         * ============================================================
         */

        List<Integer> selectableNumbers =
                catalog.getVisiblePageNumbers();

        int selectedPageNumber =
                -1;

        for (int pageNumber
                : selectableNumbers) {

            if (pageNumber > 1
                    && pageNumber < lastPageNumber) {

                selectedPageNumber =
                        pageNumber;

                break;
            }
        }

        if (selectedPageNumber == -1) {

            selectedPageNumber =
                    lastPageNumber;
        }

        CatalogPage beforeNumberClick =
                returnedFirstPage;

        catalog.clickPageNumber(
                selectedPageNumber
        );

        CatalogPage selectedPage =
                catalog.waitForPageChange(
                        beforeNumberClick
                );

        Assert.assertEquals(
                selectedPage.number(),
                selectedPageNumber,
                "TC_CL_022 failed: numbered pagination did not activate "
                        + "the selected page."
        );

        Assert.assertEquals(
                catalog.getCurrentPageNumber(),
                selectedPageNumber,
                "TC_CL_023 failed: selected page is not visually represented "
                        + "by the active pagination item."
        );

        Assert.assertNotEquals(
                selectedPage.hrefs(),
                firstPage.hrefs(),
                "TC_CL_022 failed: numbered-page navigation did not refresh "
                        + "the Course dataset."
        );

        /*
         * ============================================================
         * TC_CL_019
         * LAST PAGE
         * ============================================================
         */

        CatalogPage lastPage;

        if (selectedPage.number()
                == lastPageNumber) {

            lastPage =
                    selectedPage;

        } else {

            catalog.clickPageNumber(
                    lastPageNumber
            );

            lastPage =
                    catalog.waitForPageChange(
                            selectedPage
                    );
        }

        Assert.assertEquals(
                lastPage.number(),
                lastPageNumber,
                "Failed to navigate to the final pagination page."
        );

        Assert.assertTrue(
                catalog.isNextDisabled(),
                "TC_CL_019 failed: Next is not disabled on the last page."
        );

        Assert.assertTrue(
                lastPage.courses().size() <= 12,
                "TC_CL_017 failed: last page displays more than 12 Course cards."
        );

        /*
         * ============================================================
         * TC_CL_026 + TC_CL_027 + TC_CL_028 + TC_CL_029
         * ELLIPSIS GROUP NAVIGATION
         * ============================================================
         *
         * These requirements only apply when enough pages exist for
         * the frontend to render the "..." controls.
         */
        if (lastPageNumber > 8) {

            /*
             * TC_CL_027
             *
             * From the last page, clicking the left-side ellipsis should
             * jump to lastPage - 3 according to the legacy contract.
             */
            Assert.assertTrue(
                    catalog.getVisibleEllipsisCount() > 0,
                    "TC_CL_027 failed: no ellipsis exists on the last page."
            );

            catalog.clickFirstEllipsis();

            CatalogPage lastGroupJump =
                    catalog.waitForPageChange(
                            lastPage
                    );

            Assert.assertEquals(
                    lastGroupJump.number(),
                    lastPageNumber - 3,
                    "TC_CL_027 failed: last-page ellipsis did not jump to "
                            + "last page minus 3."
            );

            /*
             * Return directly to page 1.
             */
            catalog.clickPageNumber(
                    1
            );

            CatalogPage firstAgain =
                    catalog.waitForPageChange(
                            lastGroupJump
                    );

            Assert.assertEquals(
                    firstAgain.number(),
                    1,
                    "Failed to return to page 1 before testing the "
                            + "right-side ellipsis."
            );

            /*
             * TC_CL_026
             *
             * Legacy expected result:
             * first-page ellipsis → page 4.
             */
            Assert.assertTrue(
                    catalog.getVisibleEllipsisCount() > 0,
                    "TC_CL_026 failed: first page has no ellipsis."
            );

            catalog.clickLastEllipsis();

            CatalogPage pageFour =
                    catalog.waitForPageChange(
                            firstAgain
                    );

            Assert.assertEquals(
                    pageFour.number(),
                    4,
                    "TC_CL_026 failed: first-page ellipsis did not jump to page 4."
            );

            /*
             * TC_CL_028
             *
             * Right-side ellipsis advances to another page group.
             *
             * We do not invent a fixed destination beyond the documented
             * page-4 first jump. We verify forward movement.
             */
            if (catalog.getVisibleEllipsisCount() > 0) {

                int beforeForwardGroup =
                        pageFour.number();

                catalog.clickLastEllipsis();

                CatalogPage forwardGroup =
                        catalog.waitForPageChange(
                                pageFour
                        );

                Assert.assertTrue(
                        forwardGroup.number()
                                > beforeForwardGroup,
                        "TC_CL_028 failed: right-side ellipsis did not move "
                                + "to a later page group."
                );

                /*
                 * TC_CL_029
                 *
                 * Once the pagination exposes a left-side group ellipsis,
                 * clicking the first ellipsis must move backwards.
                 */
                if (catalog.getVisibleEllipsisCount() >= 2) {

                    int beforeBackwardGroup =
                            forwardGroup.number();

                    catalog.clickFirstEllipsis();

                    CatalogPage backwardGroup =
                            catalog.waitForPageChange(
                                    forwardGroup
                            );

                    Assert.assertTrue(
                            backwardGroup.number()
                                    < beforeBackwardGroup,
                            "TC_CL_029 failed: left-side ellipsis did not move "
                                    + "back to an earlier page group."
                    );
                }
            }
        }

        String summary =
                "COURSE-LIST-005C - Pagination Contract\n"
                        + "First page cards: "
                        + firstPage.courses().size()
                        + "\nSecond page cards: "
                        + secondPage.courses().size()
                        + "\nLast page number: "
                        + lastPageNumber
                        + "\nLast page cards: "
                        + lastPage.courses().size()
                        + "\nLoading indicator observed: "
                        + loaderAppeared
                        + "\nEllipsis used: "
                        + (lastPageNumber > 8)
                        + "\n";

        Allure.addAttachment(
                "COURSE-LIST-005C Pagination Summary",
                "text/plain",
                summary,
                ".txt"
        );

        LOGGER.info(
                "COURSE-LIST-005C COMPLETE | lastPage={} | loaderObserved={} | ellipsisContract={}",
                lastPageNumber,
                loaderAppeared,
                lastPageNumber > 8
        );
    }
}