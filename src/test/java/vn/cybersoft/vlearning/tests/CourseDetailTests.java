package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.CourseDetail.CourseDetailConsistencySupport;
import vn.cybersoft.vlearning.data.CourseDetail.CourseDetailContractSupport;
import vn.cybersoft.vlearning.data.CourseDetail.CourseDetailNavigationSupport;
import vn.cybersoft.vlearning.pages.CourseDetailPage;
import vn.cybersoft.vlearning.pages.CourseDetailPage.CoreSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.CurriculumSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.RelatedCoursesSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.RelatedNavigationTarget;

@Epic("V Learning")
@Feature("Course Detail")
public class CourseDetailTests extends BaseTest {

    private static final Logger LOGGER = LogManager.getLogger(CourseDetailTests.class);
    private static final String TEST_ID = "COURSE-DETAIL-001";

    @Test(
            groups = {"course-detail-tc1"},
            testName = "COURSE-DETAIL-001 - Course Detail Core Contract",
            description = "COURSE-DETAIL-001 - Course Detail Core Contract"
    )
    @Story("Course Detail page exposes its required core information")
    @Description(
            "Validate the core contract of one known real Course Detail page, /chitiet/.100. "
                    + "Consolidates TC_CD_001–TC_CD_017 into one result with all field-level problems. "
                    + "Curriculum, related courses and enrollment are outside this testcase."
    )
    public void knownCourseShouldExposeCompleteCoreContract() {
        LOGGER.info("{} | Starting Course Detail core contract | url={}",
                TEST_ID, CourseDetailPage.KNOWN_COURSE_URL);

        CoreSnapshot state = new CourseDetailPage(driver).openKnownCourse().captureCoreState();
        var result = CourseDetailContractSupport.inspectCore(state);
        List<String> issues = result.issues();

        String summary = result.summary();
        Allure.addAttachment(TEST_ID + " Course Detail core contract summary", "text/plain", summary, ".txt");
        LOGGER.info("{} | course=\"{}\" | issues={}", TEST_ID, state.courseName().text(), issues.size());
        for (String issue : issues) {
            LOGGER.warn("COURSE DETAIL CONTRACT FAIL | {}", issue);
        }
        LOGGER.info("{} COMPLETE | issues={}", TEST_ID, issues.size());
        Assert.assertTrue(issues.isEmpty(), TEST_ID + " failed with " + issues.size()
                + " core-contract issue(s):\n" + String.join("\n", issues));
    }

    @Test(
            groups = {"course-detail-tc2"},
            testName = "COURSE-DETAIL-002 - Course Curriculum Contract",
            description = "COURSE-DETAIL-002 - Course Curriculum Contract"
    )
    @Story("Course Detail page exposes a populated curriculum")
    @Description(
            "Verify that the known Course Detail page displays the Nội dung khóa học curriculum "
                    + "and that every rendered module and lesson exposes a nonblank title. "
                    + "Covers legacy TC_CD_019 and TC_CD_020. No preview interaction is performed."
    )
    public void knownCourseShouldExposePopulatedCurriculum() {
        String testId = "COURSE-DETAIL-002";
        LOGGER.info("{} | Starting curriculum contract | url={}", testId, CourseDetailPage.KNOWN_COURSE_URL);
        CurriculumSnapshot state = new CourseDetailPage(driver).openKnownCourse().captureCurriculumState();
        var result = CourseDetailContractSupport.inspectCurriculum(state);
        List<String> issues = result.issues();

        Allure.addAttachment(testId + " Course curriculum summary", "text/plain",
                result.summary(), ".txt");
        issues.forEach(issue -> LOGGER.warn("CURRICULUM CONTRACT FAIL | {}", issue));
        LOGGER.info("{} COMPLETE | modules={} | lessons={} | issues={}",
                testId, state.modules().size(), state.lessonCount(), issues.size());
        Assert.assertTrue(issues.isEmpty(), testId + " failed with " + issues.size()
                + " curriculum issue(s):\n" + String.join("\n", issues));
    }

    @Test(
            groups = {"course-detail-tc3"},
            testName = "COURSE-DETAIL-003 - Related Courses Contract",
            description = "COURSE-DETAIL-003 - Related Courses Contract"
    )
    @Story("Course Detail page exposes valid related Course recommendations")
    @Description("Verify the Khóa học tham khảo section and required content of every rendered related Course card. "
            + "Covers legacy TC_CD_021-TC_CD_030. No hover or navigation is performed.")
    public void knownCourseShouldExposeValidRelatedCourses() {
        String testId = "COURSE-DETAIL-003";
        LOGGER.info("{} | Starting related-course contract | url={}", testId, CourseDetailPage.KNOWN_COURSE_URL);
        RelatedCoursesSnapshot state = new CourseDetailPage(driver).openKnownCourse().captureRelatedCourses();
        var result = CourseDetailContractSupport.inspectRelated(state);
        List<String> issues = result.issues();

        Allure.addAttachment(testId + " Related courses summary", "text/plain", result.summary(), ".txt");
        issues.forEach(issue -> LOGGER.warn("RELATED COURSE CONTRACT FAIL | {}", issue));
        LOGGER.info("{} COMPLETE | cards={} | problemCards={} | fieldFailures={}",
                testId, state.cards().size(), result.problemCards(), issues.size());
        Assert.assertTrue(issues.isEmpty(), testId + " failed with " + issues.size()
                + " field failure(s):\n" + String.join("\n", issues));
    }

    @Test(
            groups = {"course-detail-tc4"},
            testName = "COURSE-DETAIL-004 - Related Course Navigation",
            description = "COURSE-DETAIL-004 - Related Course Navigation"
    )
    @Story("Related Course recommendations navigate to their advertised href")
    @Description("For every rendered Related Course card on /chitiet/.100, capture only card index and href, "
            + "then verify the main card and retained XEM CHI TIẾT interaction path navigate to that href and "
            + "load a Course Detail root. Hover is only an interaction mechanic; no Course data is compared.")
    public void relatedCoursesShouldNavigateToAdvertisedHref() {
        String testId = "COURSE-DETAIL-004";
        CourseDetailPage page = new CourseDetailPage(driver).openKnownCourse();
        List<RelatedNavigationTarget> targets = page.captureRelatedNavigationTargets();
        LOGGER.info("{} | Starting related Course navigation | cards={}", testId, targets.size());
        var result = CourseDetailNavigationSupport.inspect(page, targets);
        List<String> issues = result.issues();

        Allure.addAttachment(testId + " Related navigation summary", "text/plain", result.summary(), ".txt");
        issues.forEach(issue -> LOGGER.warn("RELATED COURSE NAVIGATION FAIL | {}", issue));
        LOGGER.info("{} COMPLETE | cards={} | issues={}", testId, targets.size(), issues.size());

        Assert.assertTrue(issues.isEmpty(), testId + " failed with " + issues.size()
                + " navigation issue(s):\n" + String.join("\n", issues));
    }

    @Test(
            groups = {"course-detail-tc5"},
            testName = "COURSE-DETAIL-005 - Course Card and Detail Frontend Consistency",
            description = "COURSE-DETAIL-005 - Course Card and Detail Frontend Consistency"
    )
    @Story("Course cards represent the same Course shown by Course Detail")
    @Description("Compare a deterministic representative Course from each frontend Course-card source against its "
            + "Course Detail page. A healthy source checks one Course; a failing source expands diagnostics up to "
            + "five Courses. This is frontend consistency validation, not a full-catalog data audit.")
    public void courseCardsShouldMatchTheirCourseDetailIdentity() {
        String testId = "COURSE-DETAIL-005";
        LOGGER.info("{} | Starting frontend consistency validation", testId);
        var result = CourseDetailConsistencySupport.inspectSources(driver);

        Allure.addAttachment(testId + " Frontend consistency summary", "text/plain", result.summary(), ".txt");

        result.defects().forEach(defect -> LOGGER.warn("COURSE CONSISTENCY FAIL | {}", defect.logLine()));
        LOGGER.info("{} COMPLETE | sources={} | detailsInspected={} | mismatches={}",
                testId, result.sources().size(), result.detailPagesInspected(), result.defects().size());

        Assert.assertTrue(result.defects().isEmpty(), testId + " failed with " + result.defects().size()
                + " frontend consistency mismatch(es). See Allure summary.");
    }
}
