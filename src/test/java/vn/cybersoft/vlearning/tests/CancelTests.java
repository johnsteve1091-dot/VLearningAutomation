package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;
import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.cancel.CancellationSupport;
import vn.cybersoft.vlearning.data.enroll.EnrollInput;
import vn.cybersoft.vlearning.pages.CourseDetailPage;

@Epic("V Learning")
@Feature("Cancellation")
public class CancelTests extends BaseTest {
    private static final Logger LOGGER = LogManager.getLogger(CancelTests.class);

    @Test(groups = {"cancel-tc1"}, testName = "CANCEL-001 - Successful Course cancellation",
            description = "CANCEL-001 - Cancel one exact test-owned Course and verify its removal")
    @Story("Learner cancels an enrolled Course")
    @Description("Enroll an absent Course, verify exact success and one My Courses entry, click its available "
            + "Hủy khóa học control and verify zero occurrences while preserving original Courses.")
    public void enrolledCourseShouldBeCancelled() {
        var support = new CancellationSupport(driver, "CANCEL-001");
        Throwable primaryFailure = null;
        try {
            support.loginAndCaptureBaseline();
            var course = support.selectAbsentCourse();
            Assert.assertEquals(support.enroll(course), EnrollInput.SUCCESS_TITLE, "Exact enrollment-success title.");
            var page = support.openMyCourses();
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "The owned Course must appear.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "Exactly one owned Course before cancellation.");
            Assert.assertEquals(support.captureCourses(page, "Before cancellation"), support.expectedCourses(course),
                    "Original Courses plus the single owned enrollment.");
            Assert.assertTrue(page.isExactCourseCancellationAvailable(course.courseName()), "Exact Hủy khóa học control must be available.");
            Assert.assertTrue(page.cancelCourseIfPresent(course.courseName()), "Cancellation must remove the exact selected Course.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "The selected Course must disappear.");
            Assert.assertEquals(support.captureCourses(page, "After cancellation"), support.originalCourses(),
                    "Only the selected enrollment may be removed; every original identity/count remains.");
            LOGGER.info("CANCEL-001 | Exact Course cancellation verified | course={}", course.courseName());
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            support.finish(primaryFailure);
        }
    }

    @Test(groups = {"cancel-tc2"}, testName = "CANCEL-002 - Cancellation leaves other Courses unchanged",
            description = "CANCEL-002 - Cancel A while B and all pre-existing Courses retain their identities/counts")
    @Story("Cancellation affects only the selected enrollment")
    @Description("Capture original Courses read-only. Select and enroll distinct absent A/B, cancel only A, "
            + "verify B exactly once and the original identity/count multiset unchanged. Finally remove owned leftovers.")
    public void cancellingOneCourseShouldPreserveOtherCourses() {
        var support = new CancellationSupport(driver, "CANCEL-002");
        Throwable primaryFailure = null;
        try {
            support.loginAndCaptureBaseline();
            var courseA = support.selectAbsentCourse();
            var courseB = support.selectAbsentCourse();
            Assert.assertNotEquals(courseA.courseName(), courseB.courseName(), "Courses A/B need distinct exact titles.");
            Assert.assertFalse(CourseDetailPage.sameBrowserUrl(courseA.href(), courseB.href()), "Courses A/B need different captured hrefs.");
            Assert.assertEquals(support.enroll(courseA), EnrollInput.SUCCESS_TITLE, "Course A must enroll successfully.");
            Assert.assertEquals(support.enroll(courseB), EnrollInput.SUCCESS_TITLE, "Course B must enroll successfully.");
            var page = support.openMyCourses();
            Assert.assertTrue(page.waitUntilCoursePresent(courseA.courseName()), "Course A must appear.");
            Assert.assertTrue(page.waitUntilCoursePresent(courseB.courseName()), "Course B must appear.");
            Assert.assertEquals(page.countExactCourseItems(courseA.courseName()), 1, "One Course A before cancellation.");
            Assert.assertEquals(page.countExactCourseItems(courseB.courseName()), 1, "One Course B before cancellation.");
            Assert.assertEquals(support.captureCourses(page, "Original Courses plus A and B"), support.expectedCourses(courseA, courseB),
                    "Both owned Courses must coexist with the original per-Course counts.");
            Assert.assertTrue(page.isExactCourseCancellationAvailable(courseA.courseName()), "Course A cancellation control must be available.");
            Assert.assertTrue(page.cancelCourseIfPresent(courseA.courseName()), "Cancel Course A only.");
            Assert.assertEquals(page.countExactCourseItems(courseA.courseName()), 0, "Course A must disappear.");
            Assert.assertEquals(page.countExactCourseItems(courseB.courseName()), 1, "Course B must remain exactly once.");
            Assert.assertEquals(support.captureCourses(page, "After cancelling A only"), support.expectedCourses(courseB),
                    "Every original Course identity/count and Course B must remain unchanged.");
            LOGGER.info("CANCEL-002 | A removed; B and all pre-existing Courses preserved");
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            support.finish(primaryFailure);
        }
    }

    @Test(groups = {"cancel-tc3"}, testName = "CANCEL-003 - Cancellation survives page refresh",
            description = "CANCEL-003 - A cancelled Course remains absent after authenticated My Courses reload")
    @Story("Course cancellation persists")
    @Description("Enroll one absent Course, verify one entry, cancel and verify zero. Refresh My Courses, "
            + "wait for the account response/active panel, then verify zero and the original per-Course baseline.")
    public void cancelledCourseShouldRemainAbsentAfterRefresh() {
        var support = new CancellationSupport(driver, "CANCEL-003");
        Throwable primaryFailure = null;
        try {
            support.loginAndCaptureBaseline();
            var course = support.selectAbsentCourse();
            Assert.assertEquals(support.enroll(course), EnrollInput.SUCCESS_TITLE, "Exact enrollment-success title.");
            var page = support.openMyCourses();
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "The owned Course must appear.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "One enrollment before cancellation.");
            Assert.assertTrue(page.isExactCourseCancellationAvailable(course.courseName()), "Exact cancellation control must be available.");
            Assert.assertTrue(page.cancelCourseIfPresent(course.courseName()), "The exact Course must be cancelled.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Cancelled Course is absent before refresh.");
            Assert.assertEquals(support.captureCourses(page, "Cancelled before refresh"), support.originalCourses(), "Original Courses remain.");

            page = page.refreshMyCourses(EnrollInput.ACCOUNT);
            var refreshed = support.captureCourses(page, "After authenticated My Courses refresh");
            Assert.assertEquals(refreshed.getOrDefault(course.courseName(), 0).intValue(), 0, "Cancelled Course must not reappear after refresh.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Exact occurrence count remains zero.");
            Assert.assertEquals(refreshed, support.originalCourses(), "Loaded per-Course identities/counts match the original baseline.");
            LOGGER.info("CANCEL-003 | Cancellation persisted after refresh | course={}", course.courseName());
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            support.finish(primaryFailure);
        }
    }
}
