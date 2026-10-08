package vn.cybersoft.vlearning.tests;

import io.qameta.allure.Allure;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import vn.cybersoft.vlearning.base.BaseTest;
import vn.cybersoft.vlearning.data.enroll.EnrollInput;
import vn.cybersoft.vlearning.data.enroll.EnrollmentSupport;
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
}
