package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.cancel.CancellationSupport;
import vn.cybersoft.vlearning.data.enroll.EnrollInput;
import vn.cybersoft.vlearning.data.enroll.EnrollmentSupport;
import vn.cybersoft.vlearning.data.login.LoginSupport;
import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.CourseDetailPage;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;

@Epic("V Learning")
@Feature("Enrollment")
public class EnrollTests extends BaseTest {

    private static final Logger LOGGER = LogManager.getLogger(EnrollTests.class);

    @Test(
            groups = {"enroll-tc1"},
            testName = "ENROLL-001 - Successful course enrollment",
            description = "ENROLL-001 - Successfully enroll in one healthy Course and verify it in My Courses"
    )
    @Story("A logged-in learner enrolls in a Course")
    @Description("Browse one Danh mục Course and capture its stikerCard name. Verify Đăng kí thành công "
            + "and the exact Course in My Courses. Cancel only this run's enrollment and verify removal, "
            + "including after a business assertion fails. Existing enrollments are never removed.")
    public void healthyCourseShouldEnrollAndAppearInMyCourses() {
        String selectedCourseName = null;
        boolean enrollmentAttempted = false;
        Throwable primaryFailure = null;
        LOGGER.info("ENROLL-001 | Starting successful Course enrollment");

        try {
            HomePage homePage = new HomePage(driver);
            LoginPage loginPage = homePage.goToLoginPage();
            loginPage.enterAccount(EnrollInput.ACCOUNT);
            loginPage.enterPassword(EnrollInput.PASSWORD);
            loginPage.clickLogin();
            if (!loginPage.waitUntilLoggedIn()) {
                throw new IllegalStateException("ENROLL-001 setup failed: Login did not complete.");
            }
            homePage.waitUntilAuthenticated();

            var selected = homePage.goToFirstCourseCategory().selectCourseForEnrollment();
            selectedCourseName = selected.courseName();
            Allure.addAttachment("ENROLL-001 selected Course", "text/plain",
                    "Category: " + selected.categoryUrl() + "\nHref: " + selected.href()
                            + "\nstikerCard name: " + selectedCourseName + "\n", ".txt");

            AccountPage accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            accountPage.requireCourseNotEnrolled(selectedCourseName);
            CourseDetailPage detailPage = homePage.goToCourseCategory(selected.categoryUrl())
                    .openCourseForEnrollment(selected);

            enrollmentAttempted = true;
            detailPage.clickEnroll();
            String popupTitle = detailPage.waitForEnrollmentPopupTitle();
            Allure.addAttachment("ENROLL-001 enrollment popup", "text/plain", popupTitle, ".txt");
            Assert.assertEquals(popupTitle, EnrollInput.SUCCESS_TITLE, "Enrollment success SweetAlert title.");

            detailPage.waitUntilEnrollmentPopupClosed();
            accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            Assert.assertTrue(accountPage.waitUntilCoursePresent(selectedCourseName),
                    "The exact selected Course must appear in My Courses: " + selectedCourseName);
            LOGGER.info("ENROLL-001 | Business assertions passed | course=\"{}\"", selectedCourseName);
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            EnrollmentSupport.finish(driver, EnrollInput.ACCOUNT, selectedCourseName,
                    enrollmentAttempted, primaryFailure);
        }
    }

    @Test(
            groups = {"enroll-tc2"},
            testName = "ENROLL-002 - Guest enrollment redirects to Login",
            description = "ENROLL-002 - Guest user is redirected to Login when attempting Course enrollment"
    )
    @Story("Guest enrollment requires authentication")
    @Description("Open one healthy Course while logged out, click Đăng ký and verify "
            + "that the browser is redirected to the /login route.")
    public void guestEnrollmentShouldRedirectToLogin() {
        HomePage homePage = new HomePage(driver);
        var categoryPage = homePage.goToFirstCourseCategory();
        var selected = categoryPage.selectCourseForEnrollment();
        CourseDetailPage detailPage = categoryPage.openCourseForEnrollment(selected);

        detailPage.clickEnroll();

        LoginPage loginPage = new LoginPage(driver);
        Assert.assertTrue(loginPage.waitUntilAtLoginPage(),
                "Guest enrollment attempt should redirect to /login.");
        LOGGER.info("ENROLL-002 | Guest enrollment redirected to /login");
    }

    @Test(
            groups = {"enroll-tc3"},
            testName = "ENROLL-003 - Login returns guest to same Course Detail",
            description = "ENROLL-003 - Successful login returns the guest to the Course Detail that requested authentication"
    )
    @Story("Guest resumes Course Detail after authentication")
    @Description("Attempt enrollment as a guest, log in after the /login redirect and verify "
            + "that the browser returns to the exact original Course Detail.")
    public void guestLoginShouldReturnToSameCourseDetail() {
        HomePage homePage = new HomePage(driver);
        var categoryPage = homePage.goToFirstCourseCategory();
        var selected = categoryPage.selectCourseForEnrollment();
        CourseDetailPage detailPage = categoryPage.openCourseForEnrollment(selected);
        String originalCourseUrl = driver.getCurrentUrl();

        detailPage.clickEnroll();

        LoginPage loginPage = new LoginPage(driver);
        if (!loginPage.waitUntilAtLoginPage()) {
            throw new IllegalStateException("ENROLL-003 setup failed: guest enrollment did not reach /login.");
        }
        loginPage.enterAccount(EnrollInput.ACCOUNT);
        loginPage.enterPassword(EnrollInput.PASSWORD);
        loginPage.clickLogin();

        Assert.assertTrue(detailPage.waitUntilAtCourseUrl(originalCourseUrl),
                "Successful login should return to the same Course Detail: " + originalCourseUrl
                        + " | Actual: " + driver.getCurrentUrl());
        LOGGER.info("ENROLL-003 | Login returned to original Course Detail | original=\"{}\" | resulting=\"{}\"",
                originalCourseUrl, driver.getCurrentUrl());
    }

    @Test(
            groups = {"enroll-tc4"},
            testName = "ENROLL-004 - Enrollment persists after My Courses refresh",
            description = "ENROLL-004 - Exact enrolled Course remains in My Courses after page refresh"
    )
    @Story("Enrollment survives My Courses refresh")
    @Description("TC_ENR_008: Enroll one healthy Course, confirm setup, refresh My Courses and verify "
            + "that the exact Course remains. Cancel only this run's enrollment and verify removal.")
    public void enrolledCourseShouldPersistAfterMyCoursesRefresh() {
        String selectedCourseName = null;
        boolean enrollmentAttempted = false;
        Throwable primaryFailure = null;

        try {
            HomePage homePage = new HomePage(driver);
            LoginPage loginPage = homePage.goToLoginPage();
            loginPage.enterAccount(EnrollInput.ACCOUNT);
            loginPage.enterPassword(EnrollInput.PASSWORD);
            loginPage.clickLogin();
            if (!loginPage.waitUntilLoggedIn()) {
                throw new IllegalStateException("ENROLL-004 setup failed: Login did not complete.");
            }
            homePage.waitUntilAuthenticated();

            var selected = homePage.goToFirstCourseCategory().selectCourseForEnrollment();
            selectedCourseName = selected.courseName();
            LOGGER.info("ENROLL-004 SETUP | course=\"{}\" | href=\"{}\"", selectedCourseName, selected.href());

            AccountPage accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            accountPage.requireCourseNotEnrolled(selectedCourseName);
            CourseDetailPage detailPage = homePage.goToCourseCategory(selected.categoryUrl())
                    .openCourseForEnrollment(selected);

            enrollmentAttempted = true;
            detailPage.clickEnroll();
            String popupTitle = detailPage.waitForEnrollmentPopupTitle();
            if (!EnrollInput.SUCCESS_TITLE.equals(popupTitle)) {
                throw new IllegalStateException("ENROLL-004 setup failed: expected enrollment popup \""
                        + EnrollInput.SUCCESS_TITLE + "\", actual \"" + popupTitle + "\".");
            }
            detailPage.waitUntilEnrollmentPopupClosed();
            accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            if (!accountPage.waitUntilCoursePresent(selectedCourseName)) {
                throw new IllegalStateException("ENROLL-004 setup failed: selected Course is absent before refresh: "
                        + selectedCourseName);
            }

            LOGGER.info("ENROLL-004 | Refreshing My Courses | course=\"{}\"", selectedCourseName);
            accountPage.refreshMyCourses(EnrollInput.ACCOUNT);
            Assert.assertTrue(accountPage.waitUntilCoursePresent(selectedCourseName),
                    "The enrolled Course must remain in My Courses after refresh: " + selectedCourseName);
            LOGGER.info("ENROLL-004 | Enrollment persisted after refresh | course=\"{}\"", selectedCourseName);
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            try {
                if (enrollmentAttempted) {
                    LoginSupport.restoreSessionForCleanup(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
                }
            } catch (RuntimeException | AssertionError recoveryFailure) {
                if (primaryFailure == null) {
                    primaryFailure = recoveryFailure;
                    throw recoveryFailure;
                }
                primaryFailure.addSuppressed(recoveryFailure);
            } finally {
                EnrollmentSupport.finish(driver, "ENROLL-004", EnrollInput.ACCOUNT,
                        selectedCourseName, enrollmentAttempted, primaryFailure);
            }
        }
    }

    @Test(
            groups = {"enroll-tc5"},
            testName = "ENROLL-005 - Enrollment persists after logout and login",
            description = "ENROLL-005 - Exact enrolled Course remains in My Courses after application logout and successful login again"
    )
    @Story("Enrollment survives authentication session transition")
    @Description("TC_ENR_009: Enroll one healthy Course, confirm setup, perform application logout and login "
            + "again, then verify that the exact Course remains. Cancel only this run's enrollment and verify removal.")
    public void enrolledCourseShouldPersistAfterLogoutAndLogin() {
        String selectedCourseName = null;
        boolean enrollmentAttempted = false;
        Throwable primaryFailure = null;

        try {
            HomePage homePage = new HomePage(driver);
            LoginPage loginPage = homePage.goToLoginPage();
            loginPage.enterAccount(EnrollInput.ACCOUNT);
            loginPage.enterPassword(EnrollInput.PASSWORD);
            loginPage.clickLogin();
            if (!loginPage.waitUntilLoggedIn()) {
                throw new IllegalStateException("ENROLL-005 setup failed: initial Login did not complete.");
            }
            homePage.waitUntilAuthenticated();

            var selected = homePage.goToFirstCourseCategory().selectCourseForEnrollment();
            selectedCourseName = selected.courseName();
            LOGGER.info("ENROLL-005 SETUP | course=\"{}\" | href=\"{}\"", selectedCourseName, selected.href());

            AccountPage accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            accountPage.requireCourseNotEnrolled(selectedCourseName);
            CourseDetailPage detailPage = homePage.goToCourseCategory(selected.categoryUrl())
                    .openCourseForEnrollment(selected);

            enrollmentAttempted = true;
            detailPage.clickEnroll();
            String popupTitle = detailPage.waitForEnrollmentPopupTitle();
            if (!EnrollInput.SUCCESS_TITLE.equals(popupTitle)) {
                throw new IllegalStateException("ENROLL-005 setup failed: expected enrollment popup \""
                        + EnrollInput.SUCCESS_TITLE + "\", actual \"" + popupTitle + "\".");
            }
            detailPage.waitUntilEnrollmentPopupClosed();
            accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            if (!accountPage.waitUntilCoursePresent(selectedCourseName)) {
                throw new IllegalStateException("ENROLL-005 setup failed: selected Course is absent before logout: "
                        + selectedCourseName);
            }

            LOGGER.info("ENROLL-005 | Performing application logout/login | course=\"{}\"", selectedCourseName);
            if (!homePage.logoutIfAuthenticated()) {
                throw new IllegalStateException("ENROLL-005 setup failed: expected an authenticated session to log out.");
            }
            loginPage = homePage.goToLoginPage();
            loginPage.enterAccount(EnrollInput.ACCOUNT);
            loginPage.enterPassword(EnrollInput.PASSWORD);
            loginPage.clickLogin();
            if (!loginPage.waitUntilLoggedIn()) {
                throw new IllegalStateException("ENROLL-005 setup failed: second Login did not complete.");
            }
            homePage.waitUntilAuthenticated();

            accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            Assert.assertTrue(accountPage.waitUntilCoursePresent(selectedCourseName),
                    "The enrolled Course must remain in My Courses after logout and login: " + selectedCourseName);
            LOGGER.info("ENROLL-005 | Enrollment persisted after logout/login | course=\"{}\"", selectedCourseName);
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            try {
                if (enrollmentAttempted) {
                    LoginSupport.restoreSessionForCleanup(driver, EnrollInput.ACCOUNT, EnrollInput.PASSWORD);
                }
            } catch (RuntimeException | AssertionError recoveryFailure) {
                if (primaryFailure == null) {
                    primaryFailure = recoveryFailure;
                    throw recoveryFailure;
                }
                primaryFailure.addSuppressed(recoveryFailure);
            } finally {
                EnrollmentSupport.finish(driver, "ENROLL-005", EnrollInput.ACCOUNT,
                        selectedCourseName, enrollmentAttempted, primaryFailure);
            }
        }
    }

    @Test(
            groups = {"enroll-tc6"},
            testName = "ENROLL-006 - Duplicate course enrollment is prevented",
            description = "ENROLL-006 - Re-enrolling the same Course is rejected and My Courses keeps only one copy"
    )
    @Story("Duplicate enrollment is prevented")
    @Description("TC_ENR_010 + TC_ENR_011: Enroll one absent Course, attempt the same Course again, "
            + "verify the exact duplicate title and one My Courses entry, then cancel only the owned Course.")
    public void duplicateEnrollmentShouldBePrevented() {
        String selectedCourseName = null;
        boolean enrollmentAttempted = false;
        Throwable primaryFailure = null;

        try {
            HomePage homePage = new HomePage(driver);
            LoginPage loginPage = homePage.goToLoginPage();
            loginPage.enterAccount(EnrollInput.ACCOUNT);
            loginPage.enterPassword(EnrollInput.PASSWORD);
            loginPage.clickLogin();
            if (!loginPage.waitUntilLoggedIn()) {
                throw new IllegalStateException("ENROLL-006 setup failed: Login did not complete.");
            }
            homePage.waitUntilAuthenticated();

            var selected = homePage.goToFirstCourseCategory().selectCourseForEnrollment();
            selectedCourseName = selected.courseName();
            LOGGER.info("ENROLL-006 SETUP | course=\"{}\" | href=\"{}\"", selectedCourseName, selected.href());
            AccountPage accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            accountPage.requireCourseNotEnrolled(selectedCourseName);
            CourseDetailPage detailPage = homePage.goToCourseCategory(selected.categoryUrl())
                    .openCourseForEnrollment(selected);

            enrollmentAttempted = true;
            detailPage.clickEnroll();
            String popupTitle = detailPage.waitForEnrollmentPopupTitle();
            if (!EnrollInput.SUCCESS_TITLE.equals(popupTitle)) {
                throw new IllegalStateException("ENROLL-006 setup failed: expected enrollment popup \""
                        + EnrollInput.SUCCESS_TITLE + "\", actual \"" + popupTitle + "\".");
            }
            detailPage.waitUntilEnrollmentPopupClosed();
            accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            if (!accountPage.waitUntilCoursePresent(selectedCourseName)) {
                throw new IllegalStateException("ENROLL-006 setup failed: selected Course is absent after enrollment: "
                        + selectedCourseName);
            }
            LOGGER.info("ENROLL-006 SETUP | First enrollment succeeded | course=\"{}\"", selectedCourseName);

            detailPage = homePage.goToCourseCategory(selected.categoryUrl()).openCourseForEnrollment(selected);
            detailPage.clickEnroll();
            String duplicateTitle = detailPage.waitForEnrollmentPopupTitle();
            LOGGER.info("ENROLL-006 | Duplicate popup title=\"{}\"", duplicateTitle);
            Assert.assertEquals(duplicateTitle, EnrollInput.DUPLICATE_TITLE,
                    "Second enrollment of the same Course must show the current duplicate warning.");
            detailPage.waitUntilEnrollmentPopupClosed();

            accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            int occurrences = accountPage.countExactCourseItems(selectedCourseName);
            LOGGER.info("ENROLL-006 | course=\"{}\" | exact My Courses occurrences={}", selectedCourseName, occurrences);
            Assert.assertEquals(occurrences, 1,
                    "The exact selected Course must occur once in My Courses: " + selectedCourseName);
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            EnrollmentSupport.finish(driver, "ENROLL-006", EnrollInput.ACCOUNT, selectedCourseName,
                    enrollmentAttempted, primaryFailure);
        }
    }

    @Test(
            groups = {"enroll-tc7"},
            testName = "ENROLL-007 - Multiple different courses can be enrolled",
            description = "ENROLL-007 - Two distinct Courses can coexist in My Courses"
    )
    @Story("Learner can enroll in multiple different Courses")
    @Description("TC_ENR_006: Enroll two absent Courses with different hrefs and exact titles, verify "
            + "that both coexist in My Courses, then independently cancel only the two owned Courses.")
    public void differentCoursesShouldCoexistInMyCourses() {
        List<String> ownedCourseNames = new ArrayList<>(2);
        Throwable primaryFailure = null;

        try {
            HomePage homePage = new HomePage(driver);
            LoginPage loginPage = homePage.goToLoginPage();
            loginPage.enterAccount(EnrollInput.ACCOUNT);
            loginPage.enterPassword(EnrollInput.PASSWORD);
            loginPage.clickLogin();
            if (!loginPage.waitUntilLoggedIn()) {
                throw new IllegalStateException("ENROLL-007 setup failed: Login did not complete.");
            }
            homePage.waitUntilAuthenticated();

            var courseA = homePage.goToFirstCourseCategory().selectCourseForEnrollment();
            LOGGER.info("ENROLL-007 SETUP | Course A=\"{}\" | href=\"{}\"", courseA.courseName(), courseA.href());
            AccountPage accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            accountPage.requireCourseNotEnrolled(courseA.courseName());
            CourseDetailPage detailPage = homePage.goToCourseCategory(courseA.categoryUrl())
                    .openCourseForEnrollment(courseA);

            ownedCourseNames.add(courseA.courseName());
            detailPage.clickEnroll();
            String popupTitle = detailPage.waitForEnrollmentPopupTitle();
            if (!EnrollInput.SUCCESS_TITLE.equals(popupTitle)) {
                throw new IllegalStateException("ENROLL-007 setup failed for Course A: expected \""
                        + EnrollInput.SUCCESS_TITLE + "\", actual \"" + popupTitle + "\".");
            }
            detailPage.waitUntilEnrollmentPopupClosed();
            LOGGER.info("ENROLL-007 SETUP | Course A enrollment succeeded");

            var courseB = homePage.selectDifferentCourseForEnrollment(courseA);
            LOGGER.info("ENROLL-007 SETUP | Distinct Course B=\"{}\" | href=\"{}\"", courseB.courseName(), courseB.href());
            accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            accountPage.requireCourseNotEnrolled(courseB.courseName());
            detailPage = homePage.goToCourseCategory(courseB.categoryUrl()).openCourseForEnrollment(courseB);

            ownedCourseNames.add(courseB.courseName());
            detailPage.clickEnroll();
            popupTitle = detailPage.waitForEnrollmentPopupTitle();
            if (!EnrollInput.SUCCESS_TITLE.equals(popupTitle)) {
                throw new IllegalStateException("ENROLL-007 setup failed for Course B: expected \""
                        + EnrollInput.SUCCESS_TITLE + "\", actual \"" + popupTitle + "\".");
            }
            detailPage.waitUntilEnrollmentPopupClosed();
            LOGGER.info("ENROLL-007 SETUP | Course B enrollment succeeded");

            accountPage = homePage.goToAccountPage().openMyCourses(EnrollInput.ACCOUNT);
            boolean courseAPresent = accountPage.waitUntilCoursePresent(courseA.courseName());
            LOGGER.info("ENROLL-007 | Course A=\"{}\" | present={}", courseA.courseName(), courseAPresent);
            Assert.assertTrue(courseAPresent, "Course A must coexist in My Courses: " + courseA.courseName());
            boolean courseBPresent = accountPage.waitUntilCoursePresent(courseB.courseName());
            LOGGER.info("ENROLL-007 | Course B=\"{}\" | present={}", courseB.courseName(), courseBPresent);
            Assert.assertTrue(courseBPresent, "Course B must coexist in My Courses: " + courseB.courseName());
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            EnrollmentSupport.finish(driver, "ENROLL-007", EnrollInput.ACCOUNT, ownedCourseNames, primaryFailure);
        }
    }

    @Test(groups = {"enroll-tc8"}, testName = "ENROLL-008 - Re-enrollment after cancellation",
            description = "ENROLL-008 - The same captured Course can be enrolled again after cancellation")
    @Story("Learner re-enrolls after cancelling a Course")
    @Description("Enroll an absent Course, verify one entry, cancel and verify zero, return through the same "
            + "captured category/href, enroll again, verify exact success and one entry, then clean up the owned Course.")
    public void cancelledCourseShouldAllowReenrollment() {
        var support = new CancellationSupport(driver, "ENROLL-008");
        Throwable primaryFailure = null;
        try {
            support.loginAndCaptureBaseline();
            var course = support.selectAbsentCourse();
            Assert.assertEquals(support.enroll(course), EnrollInput.SUCCESS_TITLE, "First enrollment must succeed.");
            var page = support.openMyCourses();
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "First enrollment must appear.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "Exactly one first enrollment.");
            Assert.assertEquals(support.captureCourses(page, "After first enrollment"), support.expectedCourses(course),
                    "Original Courses and one owned enrollment.");
            Assert.assertTrue(page.isExactCourseCancellationAvailable(course.courseName()), "Exact cancellation control must be available.");
            Assert.assertTrue(page.cancelCourseIfPresent(course.courseName()), "Cancellation must remove the owned Course.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Course must be absent before re-enrollment.");
            Assert.assertEquals(support.captureCourses(page, "After cancellation"), support.originalCourses(), "Original Courses remain intact.");

            // enroll reuses this exact immutable category/href/title and rechecks absence; ownership is retained.
            Assert.assertEquals(support.enroll(course), EnrollInput.SUCCESS_TITLE, "Re-enrollment after cancellation must succeed.");
            page = support.openMyCourses();
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "Re-enrolled Course must appear.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "Exactly one re-enrolled Course.");
            Assert.assertEquals(support.captureCourses(page, "After re-enrollment"), support.expectedCourses(course),
                    "The same Course appears exactly once alongside unchanged original Courses.");
            LOGGER.info("ENROLL-008 | Re-enrollment after cancellation verified | course={} | href={}", course.courseName(), course.href());
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = failure;
            throw failure;
        } finally {
            support.finish(primaryFailure);
        }
    }
}
