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
import vn.cybersoft.vlearning.data.account.AccountInput;
import vn.cybersoft.vlearning.data.account.AccountSupport;
import vn.cybersoft.vlearning.data.cancel.CancellationSupport;
import vn.cybersoft.vlearning.data.e2e.CrossAccountEnrollmentSupport;
import vn.cybersoft.vlearning.data.e2e.E2ESupport;
import vn.cybersoft.vlearning.data.enroll.EnrollInput;
import vn.cybersoft.vlearning.data.register.RegisterInput;
import vn.cybersoft.vlearning.data.register.RegisterSupport;
import vn.cybersoft.vlearning.pages.AccountPage;
import vn.cybersoft.vlearning.pages.CourseDetailPage;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.LoginPage;

@Epic("V Learning")
@Feature("End-to-End")
public class E2ETests extends BaseTest {
    private static final Logger LOGGER = LogManager.getLogger(E2ETests.class);

    @Test(groups = {"e2e-tc1"}, testName = "E2E-001 - Complete New Student Lifecycle",
            description = "E2E-001 - A new student's enrollment survives logout/login, then cancellation survives refresh")
    @Story("New student completes the Course lifecycle")
    @Description("Register unique credentials, explicitly log in and verify all six profile values. Browse a real "
            + "Course, enroll, verify persistence across application logout/login, cancel and refresh. "
            + "Protect original enrollments and clean only this generated student's owned Course on failure.")
    public void newStudentShouldCompleteCourseLifecycle() {
        String account = RegisterSupport.generateUniqueAccount();
        String email = RegisterSupport.generateUniqueEmail();
        String password = RegisterInput.Correct.PASSWORD;
        var expectedProfile = new AccountPage.PersonalInformation(account, email, RegisterInput.Correct.FULL_NAME,
                RegisterInput.Correct.PHONE, RegisterInput.Correct.GROUP, "Học viên");
        var support = new E2ESupport(driver, account, password);
        AssertionError primaryFailure = null;
        try {
            support.record("Generated registration profile for this execution", expectedProfile);
            HomePage home = new HomePage(driver);
            var register = home.goToLoginPage().goToRegisterPage();
            register.enterAccount(account);
            register.enterFullName(expectedProfile.fullName());
            register.enterPassword(password);
            register.enterEmail(email);
            register.enterPhone(expectedProfile.phone());
            Assert.assertTrue(register.isGroupDropdownNativeSelect(), "Registration Group must be a native dropdown.");
            Assert.assertTrue(register.hasGroupOption(expectedProfile.group()), "Expected registration Group must be available.");
            register.selectGroup(expectedProfile.group());
            Assert.assertEquals(register.getSelectedGroup(), expectedProfile.group(), "Explicitly selected registration Group.");
            register.clickRegister();
            Assert.assertTrue(register.isSuccessfulRegistrationPopupDisplayed(), "This execution's unique student must register successfully.");
            support.record("Registration confirmed", "Account=" + account + "; Email=" + email);
            register.waitUntilRegistrationPopupClosed();

            // Retain the local post-registration fix: reload before explicit Login.
            driver.navigate().refresh();
            AccountPage page = AccountSupport.loginAndOpenAccount(driver, account, password);
            Assert.assertTrue(home.isAuthenticated(), "Explicit Login must expose authenticated navigation.");
            var profile = page.readPersonalInformation(account);
            support.record("Explicit Login and loaded Account profile", profile);
            Assert.assertEquals(profile.account(), expectedProfile.account(), "Account must belong to this execution.");
            Assert.assertEquals(profile.email(), expectedProfile.email(), "Email must match this execution's registration.");
            Assert.assertEquals(profile.fullName(), expectedProfile.fullName(), "Full Name must match registration.");
            Assert.assertEquals(profile.phone(), expectedProfile.phone(), "Phone must retain the original digits.");
            Assert.assertEquals(profile.group(), expectedProfile.group(), "Group must match the explicit registration selection.");
            Assert.assertEquals(profile.learnerType(), expectedProfile.learnerType(), "Learner Type must be Học viên.");

            page = page.openMyCourses(account).refreshMyCourses(account);
            var originalCourses = support.captureBaseline(page);
            var course = support.selectAbsentCourse();
            Assert.assertFalse(course.categoryUrl().isBlank() || course.href().isBlank() || course.courseName().isBlank(),
                    "The frontend must provide a complete category/href/exact-name identity.");
            page = home.goToAccountPage().openMyCourses(account).refreshMyCourses(account);
            page.requireCourseNotEnrolled(course.courseName());
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Selected Course must be absent before enrollment.");
            var detail = home.goToCourseCategory(course.categoryUrl()).openCourseForEnrollment(course);
            Assert.assertTrue(detail.waitUntilAtCourseUrl(course.href()), "The captured Course Detail URL must be reached.");
            detail.waitUntilEnrollmentReady(); // Existing explicit root/dialog/clickable enrollment-control checks.
            support.record("Course Detail ready; enrollment control clickable", course);

            support.markEnrollmentAttempt(course);
            detail.clickEnroll();
            String popupTitle = detail.waitForEnrollmentPopupTitle();
            support.record("Enrollment response", popupTitle);
            Assert.assertEquals(popupTitle, EnrollInput.SUCCESS_TITLE, "Exact confirmed enrollment-success popup.");
            detail.waitUntilEnrollmentPopupClosed();
            page = home.goToAccountPage().openMyCourses(account);
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "The enrolled Course must appear.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "Exactly one enrollment for this Course.");
            Assert.assertEquals(support.captureCourses(page, "My Courses after enrollment"), support.expectedEnrolledCourses(),
                    "The owned Course and every original Course identity/count must coexist.");

            Assert.assertTrue(home.logoutIfAuthenticated(), "The application's logout control must end the session.");
            Assert.assertFalse(home.isAuthenticated(), "Authenticated navigation must be absent after application logout.");
            support.record("Application logout confirmed", "Generated student's authenticated session ended.");
            page = AccountSupport.loginAndOpenAccount(driver, account, password);
            Assert.assertTrue(home.isAuthenticated(), "Normal UI Login must restore authenticated navigation.");
            profile = page.readPersonalInformation(account);
            support.record("Same student after second Login", profile);
            Assert.assertEquals(profile, expectedProfile, "The same generated student's six profile values must remain intact.");
            page = page.openMyCourses(account);
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "The original enrollment must survive logout/login.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "Persistent enrollment occurs exactly once.");
            Assert.assertEquals(support.captureCourses(page, "My Courses after logout/Login"), support.expectedEnrolledCourses(),
                    "The same owned enrollment and all original Course identities/counts must persist.");

            Assert.assertTrue(page.isExactCourseCancellationAvailable(course.courseName()), "The exact owned Course's cancellation control must be available.");
            Assert.assertTrue(page.cancelCourseIfPresent(course.courseName()), "Cancellation must remove this exact owned Course.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Cancelled Course occurrence count is zero.");
            Assert.assertEquals(support.captureCourses(page, "My Courses after cancellation"), originalCourses,
                    "Only the owned enrollment is removed; the original baseline remains intact.");

            page = page.refreshMyCourses(account);
            var refreshed = support.captureCourses(page, "My Courses after authenticated refresh");
            Assert.assertEquals(refreshed.getOrDefault(course.courseName(), 0).intValue(), 0, "Cancelled Course must remain absent after refresh.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Exact refreshed occurrence count remains zero.");
            Assert.assertEquals(refreshed, originalCourses, "Loaded data for the generated account must match its original Course baseline.");
            LOGGER.info("E2E-001 | Lifecycle verified | account={} | email={} | course={}", account, email, course.courseName());
        } catch (RuntimeException | AssertionError failure) {
            // Reuse existing failure conversion to retain business assertions and redact secret-bearing WebDriver errors.
            primaryFailure = AccountSupport.staleAttemptFailure("E2E-001 lifecycle", failure, password);
            throw primaryFailure;
        } finally {
            support.finish(primaryFailure);
        }
    }

    @Test(groups = {"e2e-tc2"}, testName = "E2E-002 - Cross-Account Enrollment Isolation",
            description = "E2E-002 - Both accounts enroll in the same Course; A's cancellation must preserve B after refresh")
    @Story("Enrollment and cancellation belong to the authenticated account")
    @Description("Capture two independent loaded baselines. Enroll A in a shared absent Course, prove B has not "
            + "inherited it, then enroll B in that exact Course. Cancel A and verify B still owns one entry after "
            + "logout/Login and refresh. Independently restore only each account's test-owned enrollment.")
    public void courseEnrollmentShouldRemainIsolatedBetweenAccounts() {
        String accountA = EnrollInput.ACCOUNT;
        String accountB = AccountInput.UPDATE_ACCOUNT;
        var support = new CrossAccountEnrollmentSupport(driver, accountA, EnrollInput.PASSWORD,
                accountB, AccountInput.UPDATE_PASSWORD);
        AssertionError primaryFailure = null;
        try {
            HomePage home = new HomePage(driver);
            AccountPage page = support.loginAs(accountA);
            String actualA = page.getPersonalAccount();
            Assert.assertEquals(actualA, accountA, "Initial loaded Account must belong to A.");
            page = page.openMyCourses(accountA).refreshMyCourses(accountA);
            var originalA = support.captureBaseline(accountA, page);

            page = support.loginAs(accountB);
            String actualB = page.getPersonalAccount();
            Assert.assertEquals(actualB, accountB, "Initial loaded Account must belong to B.");
            Assert.assertNotEquals(actualB, actualA, "B must not display A's Account information.");
            page = page.openMyCourses(accountB).refreshMyCourses(accountB);
            var originalB = support.captureBaseline(accountB, page);

            var course = support.selectCourseAbsentFromBothAccounts();
            Assert.assertFalse(course.categoryUrl().isBlank() || course.href().isBlank() || course.courseName().isBlank(),
                    "Course X must have a captured category, detail href and exact frontend name.");
            Assert.assertFalse(originalA.containsKey(course.courseName()) || originalB.containsKey(course.courseName()),
                    "Course X must be outside both original baselines.");

            page = support.loginAs(accountA);
            Assert.assertEquals(page.getPersonalAccount(), accountA, "A must own the session before enrollment.");
            page = page.openMyCourses(accountA).refreshMyCourses(accountA);
            page.requireCourseNotEnrolled(course.courseName());
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "A must not already own Course X.");
            var detail = home.goToCourseCategory(course.categoryUrl()).openCourseForEnrollment(course);
            Assert.assertTrue(detail.waitUntilAtCourseUrl(course.href()), "A must reach the captured Course X Detail.");
            detail.waitUntilEnrollmentReady();
            support.markEnrollmentAttempt(accountA, course);
            detail.clickEnroll();
            String popupTitle = detail.waitForEnrollmentPopupTitle();
            support.record("A enrollment response", popupTitle);
            Assert.assertEquals(popupTitle, EnrollInput.SUCCESS_TITLE, "A must receive the exact enrollment-success title.");
            detail.waitUntilEnrollmentPopupClosed();
            page = home.goToAccountPage().openMyCourses(accountA);
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "Course X must appear for A.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "A must own exactly one Course X.");
            Assert.assertEquals(support.captureCourses(accountA, page, "A enrolled"), support.expectedEnrolledCourses(accountA),
                    "A's loaded panel must contain X once and every original Course identity/count unchanged.");

            page = support.loginAs(accountB);
            Assert.assertEquals(page.getPersonalAccount(), accountB, "B must see B's loaded Account, not A's.");
            page = page.openMyCourses(accountB).refreshMyCourses(accountB);
            var beforeBEnrollment = support.captureCourses(accountB, page, "B before any enrollment attempt");
            Assert.assertEquals(beforeBEnrollment.getOrDefault(course.courseName(), 0).intValue(), 0,
                    "ISOLATION: B must not inherit A's enrollment before attempting enrollment itself.");
            Assert.assertEquals(beforeBEnrollment, originalB, "B's original baseline must be unchanged before B enrollment.");
            page.requireCourseNotEnrolled(course.courseName());
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Recheck B's exact absence before enrollment.");
            detail = home.goToCourseCategory(course.categoryUrl()).openCourseForEnrollment(course);
            Assert.assertTrue(detail.waitUntilAtCourseUrl(course.href()), "B must reach the same captured Course X Detail.");
            detail.waitUntilEnrollmentReady();
            support.markEnrollmentAttempt(accountB, course);
            detail.clickEnroll();
            popupTitle = detail.waitForEnrollmentPopupTitle();
            support.record("B independent enrollment response for the same Course X", popupTitle);
            Assert.assertEquals(popupTitle, EnrollInput.SUCCESS_TITLE,
                    "B must independently enroll in the same Course; A's enrollment must not cause rejection.");
            detail.waitUntilEnrollmentPopupClosed();
            page = home.goToAccountPage().openMyCourses(accountB);
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "Course X must now appear for B.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "B must independently own one Course X.");
            Assert.assertEquals(support.captureCourses(accountB, page, "B independently enrolled"), support.expectedEnrolledCourses(accountB),
                    "B's original identities/counts must coexist with exactly one owned Course X.");

            page = support.loginAs(accountA);
            Assert.assertEquals(page.getPersonalAccount(), accountA, "Only A's verified session may cancel A's Course X.");
            page = page.openMyCourses(accountA).refreshMyCourses(accountA);
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "A must still own exactly one Course X.");
            Assert.assertEquals(support.captureCourses(accountA, page, "A before cancellation"), support.expectedEnrolledCourses(accountA),
                    "A's enrollment and original Courses must survive B's enrollment.");
            Assert.assertTrue(page.isExactCourseCancellationAvailable(course.courseName()), "A's exact Course X cancellation control must be available.");
            Assert.assertTrue(page.cancelCourseIfPresent(course.courseName()), "Cancellation must remove A's exact owned Course X.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "A's cancelled Course X count must be zero.");
            Assert.assertEquals(support.captureCourses(accountA, page, "A cancelled"), originalA, "A's original baseline must be restored.");
            page = page.refreshMyCourses(accountA);
            var refreshedA = support.captureCourses(accountA, page, "A cancellation after refresh");
            Assert.assertEquals(refreshedA.getOrDefault(course.courseName(), 0).intValue(), 0, "A's cancellation must persist after refresh.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "A's exact refreshed count must remain zero.");
            Assert.assertEquals(refreshedA, originalA, "A's complete original baseline must survive refresh.");

            page = support.loginAs(accountB);
            Assert.assertEquals(page.getPersonalAccount(), accountB, "The final loaded Account must belong to B.");
            page = page.openMyCourses(accountB).refreshMyCourses(accountB);
            var refreshedB = support.captureCourses(accountB, page, "B surviving enrollment after A cancellation and refresh");
            Assert.assertEquals(refreshedB.getOrDefault(course.courseName(), 0).intValue(), 1,
                    "ISOLATION: A's cancellation must not remove B's independent enrollment after refresh.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "B must still own exactly one Course X.");
            Assert.assertEquals(refreshedB, support.expectedEnrolledCourses(accountB),
                    "B's own Course X and every original Course identity/count must survive A's cancellation.");
            support.record("Cross-account isolation verified", "A=" + accountA + "; B=" + accountB + "; Course=" + course);
            LOGGER.info("E2E-002 | Cross-account assertions passed; independent cleanup follows");
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = support.failure("E2E-002 cross-account scenario", failure);
            throw primaryFailure;
        } finally {
            support.finish(primaryFailure);
        }
    }

    @Test(groups = {"e2e-tc3"}, testName = "E2E-003 - Guest to Authenticated Course Enrollment",
            description = "E2E-003 - Guest Login returns to the original Course, completes enrollment and retains it after refresh")
    @Story("Guest resumes and completes the interrupted enrollment journey")
    @Description("Capture a read-only existing-account baseline and absent Course, then log out. Browse that "
            + "Course as a guest, require the Login redirect and actual return to the same Detail after Login. "
            + "Enroll directly on that returned page, verify My Courses and refresh persistence, then clean only the owned enrollment.")
    public void guestShouldCompleteEnrollmentAfterLogin() {
        String account = EnrollInput.ACCOUNT;
        var support = new CancellationSupport(driver, "E2E-003");
        AssertionError primaryFailure = null;
        try {
            // Read-only setup; the guest business journey starts only after application logout below.
            HomePage home = new HomePage(driver);
            AccountPage page = AccountSupport.loginAndOpenAccount(driver, account, EnrollInput.PASSWORD);
            String initialAccount = page.getPersonalAccount();
            support.record("Read-only setup Account identity", initialAccount);
            Assert.assertEquals(initialAccount, account, "Setup must load the confirmed Enrollment account.");
            page = page.openMyCourses(account).refreshMyCourses(account);
            support.captureBaseline(page);
            var originalCourses = support.originalCourses();
            var course = support.selectAbsentCourse();
            Assert.assertFalse(course.categoryUrl().isBlank() || course.href().isBlank() || course.courseName().isBlank(),
                    "Course X must have a real category, detail href and exact stikerCard name.");
            page = support.openMyCourses().refreshMyCourses(account);
            page.requireCourseNotEnrolled(course.courseName());
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Course X must be absent before the guest journey.");
            Assert.assertEquals(support.captureCourses(page, "Read-only setup complete"), originalCourses,
                    "Safe selection must leave the complete original Course baseline unchanged.");
            Assert.assertTrue(home.logoutIfAuthenticated(), "Setup must end through the application's logout control.");
            Assert.assertFalse(home.isAuthenticated(), "The actual enrollment journey must start as a guest.");
            support.record("Guest journey begins", "Read-only setup ended; application logout confirmed.");

            home.open();
            Assert.assertFalse(home.isAuthenticated(), "Homepage must remain unauthenticated before browsing Course X.");
            var detail = home.goToCourseCategory(course.categoryUrl()).openCourseForEnrollment(course);
            Assert.assertTrue(detail.waitUntilAtCourseUrl(course.href()), "Guest must open the captured Course card's Detail URL.");
            detail.waitUntilEnrollmentReady();
            String originalCourseUrl = driver.getCurrentUrl();
            Assert.assertTrue(CourseDetailPage.sameBrowserUrl(course.href(), originalCourseUrl), "The guest's actual Detail must match captured Course X.");
            support.record("Original guest Course Detail", "Course=" + course + "; actual URL=" + originalCourseUrl);

            detail.clickEnroll(); // Guest click has no authenticated enrollment ownership.
            LoginPage login = new LoginPage(driver);
            boolean reachedLogin = login.waitUntilAtLoginPage();
            support.record("Guest enrollment redirect", "Login reached=" + reachedLogin + "; actual URL=" + driver.getCurrentUrl());
            Assert.assertTrue(reachedLogin, "Guest enrollment must redirect to the existing Login route.");
            Assert.assertTrue(login.isAccountInputDisplayed(), "Redirected Login must expose the Account field.");
            Assert.assertTrue(login.isPasswordInputDisplayed(), "Redirected Login must expose the Password field.");
            Assert.assertTrue(login.isLoginButtonDisplayed(), "Redirected Login must expose its normal submit control.");
            login.enterAccount(account);
            login.enterPassword(EnrollInput.PASSWORD);
            login.clickLogin();
            boolean loginCompleted = login.waitUntilLoggedIn();
            boolean returnedToOriginal = detail.waitUntilAtCourseUrl(originalCourseUrl);
            String returnedUrl = driver.getCurrentUrl();
            support.record("Login completion and actual return", "Login completed=" + loginCompleted
                    + "; original URL=" + originalCourseUrl + "; returned URL=" + returnedUrl);
            Assert.assertTrue(loginCompleted, "Normal Login must complete successfully.");
            Assert.assertTrue(returnedToOriginal && CourseDetailPage.sameBrowserUrl(originalCourseUrl, returnedUrl),
                    "Login must return to the original Course Detail without test-driven navigation or refresh.");
            home.waitUntilAuthenticated();
            Assert.assertTrue(home.isAuthenticated(), "The returned Course Detail must expose authenticated navigation.");
            detail.waitUntilEnrollmentReady();

            // The header has no confirmed account-name locator. Inspect the loaded Account in a read-only
            // sibling tab after proving the return; the original Detail tab is never navigated or refreshed.
            try (var accountTabs = new AccountSupport.AccountTabs(driver)) {
                try {
                    page = accountTabs.openAccountInSecondTab(account);
                    String authenticatedAccount = page.getPersonalAccount();
                    support.record("Returned journey's authenticated Account", authenticatedAccount);
                    Assert.assertEquals(authenticatedAccount, account, "The resumed journey must belong to the expected account before mutation.");
                    page = page.openMyCourses(account).refreshMyCourses(account);
                    page.requireCourseNotEnrolled(course.courseName());
                    Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Course X must still be absent before claiming enrollment ownership.");
                    Assert.assertEquals(support.captureCourses(page, "Owner and absence verified before authenticated click"), originalCourses,
                            "The guest/Login journey must not silently change the original Course baseline.");
                } catch (RuntimeException | AssertionError observationFailure) {
                    support.preserveFailureEvidence(observationFailure);
                    throw observationFailure;
                }
            }
            Assert.assertTrue(detail.waitUntilAtCourseUrl(originalCourseUrl), "Closing the observation tab must reveal the unchanged returned Detail.");
            detail.waitUntilEnrollmentReady();
            support.markEnrollmentAttempt(course);
            detail.clickEnroll(); // Directly on the application's returned original Detail, with no helper navigation.
            String popupTitle = detail.waitForEnrollmentPopupTitle();
            support.record("Authenticated enrollment response", "Course=" + course + "; popup=" + popupTitle);
            Assert.assertEquals(popupTitle, EnrollInput.SUCCESS_TITLE, "Resumed enrollment must receive the exact confirmed success title.");
            detail.waitUntilEnrollmentPopupClosed();

            page = home.goToAccountPage().waitUntilPersonalInformationReady(account);
            Assert.assertEquals(page.getPersonalAccount(), account, "The enrolled Course must be observed under the same expected account.");
            page = page.openMyCourses(account);
            Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "The completed enrollment must appear in My Courses.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "Course X must be enrolled exactly once.");
            Assert.assertEquals(support.captureCourses(page, "My Courses after resumed enrollment"), support.expectedCourses(course),
                    "Exactly one Course X must coexist with every original Course identity/count.");

            page = page.refreshMyCourses(account);
            var refreshed = support.captureCourses(page, "Authenticated My Courses after refresh");
            Assert.assertEquals(refreshed.getOrDefault(course.courseName(), 0).intValue(), 1, "Course X must persist after refresh.");
            Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "The exact refreshed Course X count must be one.");
            Assert.assertEquals(refreshed, support.expectedCourses(course), "Loaded data for the expected account must preserve X and all original Courses.");
            LOGGER.info("E2E-003 | Guest journey completed; original Detail return and enrollment persistence verified");
        } catch (RuntimeException | AssertionError failure) {
            primaryFailure = AccountSupport.staleAttemptFailure("E2E-003 guest enrollment", failure, EnrollInput.PASSWORD);
            throw primaryFailure;
        } finally {
            support.finish(primaryFailure);
        }
    }

    @Test(groups = {"e2e-tc4"}, testName = "E2E-004 - Cross-Account Session Hijack Protection",
            description = "E2E-004 - A stale Account A tab cannot cancel Courses after Account B authenticates in the shared browser")
    @Story("Cross-account authorization and stale Course cancellation")
    @Description("Capture both existing accounts' original Courses, enroll one shared-absent Course only under A, "
            + "then use Tab B to log A out and B in. Observe and attempt A's old exact cancellation once if still safe. "
            + "Independently refresh B and A to prove B unchanged and A's enrollment intact. Finally clean only A's owned Course.")
    public void staleAccountTabShouldNotMutateCoursesAfterAccountSwitch() {
        String accountA = EnrollInput.ACCOUNT;
        String accountB = AccountInput.UPDATE_ACCOUNT;
        var support = new CrossAccountEnrollmentSupport(driver, "E2E-004",
                accountA, EnrollInput.PASSWORD, accountB, AccountInput.UPDATE_PASSWORD);
        try (var tabs = new AccountSupport.AccountTabs(driver)) {
            AssertionError primaryFailure = null;
            try {
                Assert.assertNotEquals(accountA, accountB, "This scenario requires different existing accounts.");
                HomePage home = new HomePage(driver);
                AccountPage page = support.loginAs(accountB);
                Assert.assertEquals(page.getPersonalAccount(), accountB, "Capture B's original data under B's loaded identity.");
                page = page.openMyCourses(accountB).refreshMyCourses(accountB);
                var originalB = support.captureBaseline(accountB, page);

                page = support.loginAs(accountA); // Existing application logout, guest check and normal Login.
                Assert.assertEquals(page.getPersonalAccount(), accountA, "Capture A's independent original data under A.");
                page = page.openMyCourses(accountA).refreshMyCourses(accountA);
                var originalA = support.captureBaseline(accountA, page);
                var course = support.selectCourseAbsentFromBothAccounts();
                Assert.assertFalse(course.categoryUrl().isBlank() || course.href().isBlank() || course.courseName().isBlank(),
                        "Course X must have a captured frontend category, href and exact stikerCard name.");
                Assert.assertFalse(originalA.containsKey(course.courseName()) || originalB.containsKey(course.courseName()),
                        "Course X must be absent from both original baselines; original Courses cannot be borrowed.");

                page = support.loginAs(accountA);
                Assert.assertEquals(page.getPersonalAccount(), accountA, "Only verified Account A may enroll Course X.");
                page = page.openMyCourses(accountA).refreshMyCourses(accountA);
                page.requireCourseNotEnrolled(course.courseName());
                Assert.assertEquals(page.countExactCourseItems(course.courseName()), 0, "Recheck exact absence immediately before A's enrollment.");
                Assert.assertEquals(support.captureCourses(accountA, page, "A before enrollment"), originalA,
                        "Read-only setup must preserve every original Course under A.");
                var detail = home.goToCourseCategory(course.categoryUrl()).openCourseForEnrollment(course);
                Assert.assertTrue(detail.waitUntilAtCourseUrl(course.href()), "Open the exact captured Course X Detail.");
                detail.waitUntilEnrollmentReady();
                support.markEnrollmentAttempt(accountA, course);
                detail.clickEnroll();
                String popupTitle = detail.waitForEnrollmentPopupTitle();
                support.record("A successful enrollment response", "Course=" + course + "; popup=" + popupTitle);
                Assert.assertEquals(popupTitle, EnrollInput.SUCCESS_TITLE, "A's test-owned enrollment must succeed exactly.");
                detail.waitUntilEnrollmentPopupClosed();

                page = home.goToAccountPage().waitUntilPersonalInformationReady(accountA);
                Assert.assertEquals(page.getPersonalAccount(), accountA, "Tab A's loaded Account must belong to A.");
                page = page.openMyCourses(accountA);
                Assert.assertTrue(page.waitUntilCoursePresent(course.courseName()), "A's owned Course X must appear before switching sessions.");
                Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1, "Exactly one X in A before Tab B opens.");
                Assert.assertEquals(support.captureCourses(accountA, page, "Tab A My Courses before opening Tab B"),
                        support.expectedEnrolledCourses(accountA), "A's originals plus one owned X must be loaded.");
                Assert.assertTrue(page.isExactCourseCancellationAvailable(course.courseName()), "A's exact old cancellation control must initially be available.");

                var logout = tabs.logoutInSecondTab(accountA);
                support.record("Tab B shared authentication and application logout", logout);
                Assert.assertTrue(logout.sharedAuthentication(), "Both tabs must use this one WebDriver's shared authentication.");
                Assert.assertEquals(logout.sharedAccount(), accountA, "Tab B must initially identify A's shared session.");
                Assert.assertTrue(logout.logoutClicked() && logout.guestConfirmed(), "Tab B must log A out through the application and confirm guest state.");
                AccountPage pageB = AccountSupport.loginAndOpenAccount(driver, accountB, AccountInput.UPDATE_PASSWORD);
                home.waitUntilAuthenticated();
                String activeB = pageB.getPersonalAccount();
                support.record("Tab B authenticated as B before returning to stale A", activeB);
                Assert.assertTrue(home.isAuthenticated(), "B must have authenticated navigation before the stale attempt.");
                Assert.assertEquals(activeB, accountB, "Tab B must complete B Login; logout-only is not this scenario.");

                tabs.returnToOriginalTab(); // No navigation, refresh, Login or session repair in stale Tab A.
                var stale = page.cancelStaleCourseForAccountOnceAndObserve(accountA, course.courseName());
                support.record("Stale Tab A identity, UI and single cancellation observation", stale);

                // Stored state decides security. Always attempt BOTH independent verifications.
                AssertionError storedStateFailure = null;
                try {
                    pageB = tabs.loginFresh(accountB, AccountInput.UPDATE_PASSWORD);
                    Assert.assertEquals(pageB.getPersonalAccount(), accountB, "Fresh B inspection must load B's actual identity.");
                    pageB = pageB.openMyCourses(accountB).refreshMyCourses(accountB);
                    var actualB = support.captureCourses(accountB, pageB, "B persisted Courses after stale A interaction");
                    Assert.assertEquals(pageB.countExactCourseItems(course.courseName()), 0, "Course X must remain absent under B.");
                    Assert.assertEquals(actualB, originalB, "SECURITY: A's stale action must not add, remove or change any Course under B.");
                } catch (RuntimeException | AssertionError verificationFailure) {
                    storedStateFailure = support.failure("B persisted Course integrity", verificationFailure);
                    support.preserveFailureEvidence(storedStateFailure);
                }
                try {
                    page = tabs.loginFresh(accountA, EnrollInput.PASSWORD);
                    Assert.assertEquals(page.getPersonalAccount(), accountA, "Fresh A inspection must load A's actual identity.");
                    page = page.openMyCourses(accountA).refreshMyCourses(accountA);
                    var actualA = support.captureCourses(accountA, page, "A persisted Courses after B Login and stale interaction");
                    Assert.assertEquals(page.countExactCourseItems(course.courseName()), 1,
                            "SECURITY: A's stale cancellation after B Login must leave A's Course X enrolled exactly once.");
                    Assert.assertEquals(actualA, support.expectedEnrolledCourses(accountA),
                            "SECURITY: A must retain every original Course plus X; an unloaded/empty panel cannot pass.");
                } catch (RuntimeException | AssertionError verificationFailure) {
                    AssertionError failure = support.failure("A persisted Course integrity", verificationFailure);
                    support.preserveFailureEvidence(failure);
                    if (storedStateFailure == null) storedStateFailure = failure;
                    else storedStateFailure.addSuppressed(failure);
                }
                if (storedStateFailure != null) throw storedStateFailure;
                LOGGER.info("E2E-004 | Stale Account A action after B Login left B unchanged and A's enrollment intact");
            } catch (RuntimeException | AssertionError failure) {
                primaryFailure = support.failure("E2E-004 cross-account stale cancellation", failure);
                throw primaryFailure;
            } finally {
                // Runs before AccountTabs closes: preserve evidence, clean only A, then independently verify B read-only.
                support.finishAndVerifyReadOnlyAccount(accountB, primaryFailure);
            }
        }
    }
}
