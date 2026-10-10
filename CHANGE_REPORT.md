# VLearningAutomation v1.1.14 — Enrollment persistence TC4 and TC5

## Inspected baseline

GitHub was inspected **first on 2026-10-10**. Main remains **v1.1.10**, tree
`411ea3f139739f72ec1b5011e73255a51de45f35`, with ENROLL-001 and the established
Enrollment architecture. Current local is **v1.1.13**; all 42 files matched the
previously delivered v1.1.13 ZIP before changes.

Local already contains ENROLL-002, ENROLL-003, Login-route waiting, Course Detail
return-URL support and cancellation-only TC1 cleanup. These newer changes were
retained. Against GitHub, 35 files match and seven differ: EnrollTests,
EnrollmentSupport, LoginPage, CourseDetailPage, build.gradle, README and CHANGE_REPORT.
There are no local-only or GitHub-only project files.

An untouched local baseline was retained. No applicable AGENTS.md was found.
No GitHub push was made. **One patch increment: 1.1.13 → 1.1.14.** Dependency
versions, Java 26 requirement, Wrapper and TestNG class entries are unchanged.

## New tests and traceability

| Current case | Legacy case | Persistence behavior |
| --- | --- | --- |
| ENROLL-004 | TC_ENR_008 | Exact enrolled Course remains after My Courses refresh |
| ENROLL-005 | TC_ENR_009 | Exact enrolled Course remains after application logout and login again |

Both methods independently use BaseTest's fresh browser, the existing EnrollInput
credentials and authenticated-header wait, then the existing first-category and
first-healthy-card selection. They retain the EnrollmentCourse name, href and
category URL. `requireCourseNotEnrolled` runs before any enrollment attempt:
a pre-existing exact-title enrollment fails setup and remains untouched.

Each returns through the same category/card path, clicks the existing Đăng ký
once, requires `EnrollInput.SUCCESS_TITLE`, waits for the popup to close, and
requires initial exact-title My Courses presence. These are setup guards that
throw on failure, not additional TestNG business assertions. No Course ID, URL
or name is hardcoded. No selection algorithm, catalog crawl or API request was added.

### ENROLL-004

Method: `enrolledCourseShouldPersistAfterMyCoursesRefresh`, group `enroll-tc4`.
`AccountPage.refreshMyCourses(expectedAccount)` calls `driver.navigate().refresh()`
and the existing `openMyCourses` account-data wait/tab operation, restoring the
Courses panel if refresh resets it. **One business assertion** checks the same
captured title after refresh. Logs contain name/href, transition and final presence.

### ENROLL-005

Method: `enrolledCourseShouldPersistAfterLogoutAndLogin`, group `enroll-tc5`.
It reuses **the unchanged** `HomePage.logoutIfAuthenticated`: hover the avatar,
click the existing scoped `span.logout` normally, and wait for the Login control
to be visible and the authenticated link to be absent. The logout result and
second-login readiness are setup guards.

It opens Login through the frontend, uses the same EnrollInput account, waits
for authentication, and reopens My Courses. **One business assertion** checks the
same selected Course. Application logout occurs once as the scenario transition,
never after cleanup. No JavaScript click, session deletion, driver replacement
or sleep substitutes for application logout.

The public Homepage rendered in a separate browser inspection. The guest page
did not expose authenticated logout DOM; no sign-in or enrollment was performed
there. Live authenticated logout remains unverified because the requested
automated test did not get past Gradle bootstrap.

## Cleanup and preservation

Each new test sets `enrollmentAttempted` immediately before its single enrollment
click and invokes EnrollmentSupport in `finally`. Existing AccountPage logic
still performs unique exact-title cancellation and waits for removal. Other
courses and any pre-existing selected enrollment remain untouched.

The small finish overload accepts testcase identity and recovery credentials,
giving TC4/TC5 correctly labelled logs and attachments. After an enrollment attempt,
if the browser is a guest, cleanup restores the same test login through Page Objects
before targeted cancellation. This covers failure after TC5 logout or during its
second login. Recovery failure is reported separately and never hides the primary failure.

HomePage's new `isAuthenticated` waits for a known header state or standalone
`/login` route. `goToLoginPage` now accepts an already-open Login route, so recovery
does not depend on a header link absent from the Login screen. No header/logout
locator was added; `logoutIfAuthenticated` is unchanged.

The old five-argument finish caller delegates with ENROLL-001 identity and no
recovery password. TC1 retains cancellation-only behavior. Failure evidence is
still captured before cleanup; cleanup errors remain suppressed on the primary
failure. Cleanup/removal or evidence failure after scenario success fails the test.

**No application logout was reintroduced into TC1 or EnrollmentSupport.** BaseTest
owns final browser/session teardown. TC1–TC3 complete declarations and bodies
are unchanged. Course Detail, Login, category selection, credentials and unrelated
tests/support classes remain unchanged.

EnrollTests has **five direct methods, all @Test**, no helpers or nested types,
raw locators, DOM parsing or raw refresh/wait machinery. The full project has
**39 TestNG methods**. No duplicate-enrollment or additional legacy scenario was added.

## Files changed

**New project files: none.** Seven modified files relative to local v1.1.13:

1. `src/test/java/vn/cybersoft/vlearning/tests/EnrollTests.java` — TC4 and TC5 only.
2. `src/main/java/vn/cybersoft/vlearning/pages/AccountPage.java` — one refresh/reopen operation.
3. `src/main/java/vn/cybersoft/vlearning/pages/HomePage.java` — auth-state wait and Login recovery navigation.
4. `src/test/java/vn/cybersoft/vlearning/data/enroll/EnrollmentSupport.java` — labelled overload and optional recovery.
5. `build.gradle` — project version only.
6. `README.md` — version/counts, scenarios/mappings and requested run order.
7. `CHANGE_REPORT.md` — this report.

The other **35 project files are byte-for-byte unchanged**, including
CourseDetailPage, LoginPage, CourseCategoryPage, EnrollInput, BaseTest, Wrapper
files and testng.xml. Existing AccountPage methods and HomePage.logoutIfAuthenticated
are unchanged. Unrelated Course List/Course Detail documentation is preserved.

## Actual validation — 2026-10-10

All seven requested commands were attempted sequentially in this order:

```sh
bash gradlew compileJava compileTestJava --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.enrolledCourseShouldPersistAfterMyCoursesRefresh --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.enrolledCourseShouldPersistAfterLogoutAndLogin --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.healthyCourseShouldEnrollAndAppearInMyCourses --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.guestEnrollmentShouldRedirectToLogin --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.guestLoginShouldReturnToSameCourseDetail --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests --console=plain --no-daemon
```

Every command exited **1 before any Gradle task started**: the Wrapper could not
download `https://downloads.gradle.org/distributions/gradle-9.7.1-bin.zip` and
reported `java.net.SocketException: Network is unreachable`. Installed Java is
**17.0.20**; the project's **Java 26** requirement was retained.

| Requested check | Actual result |
| --- | --- |
| Production/test compilation | Attempted; blocked during Gradle bootstrap |
| ENROLL-004 alone | Attempted; no TestNG execution or live refresh-persistence verdict |
| ENROLL-005 alone | Attempted; no TestNG execution or live logout/login-persistence verdict |
| ENROLL-001 regression | Attempted; no TestNG execution |
| ENROLL-002 regression | Attempted; no TestNG execution |
| ENROLL-003 regression | Present locally; attempted; no TestNG execution |
| Full EnrollTests | Attempted; no TestNG execution |
| Automated cancellation/removal | Not executed against the application |
| Automated Chrome/Allure runtime | Not reached by these commands |
| Product defect | No verdict because persistence was not reached live |

All **29 Java sources passed syntax parsing**. AST/file checks verified unchanged
TC1–TC3, five test-only methods, one assertion/one enrollment click per new method,
and preserved logout, exact-title and cancellation methods. No helper methods,
raw locators, fixed Course identities or session-deletion operations were added to EnrollTests.

An isolated dependency-stub harness ran **15 safety checks** using the actual
changed AccountPage, HomePage and EnrollmentSupport classes. All passed. They
covered tab restoration after refresh; guest detection; login recovery after logout
or while already on Login; recovery/cancellation failures; original-failure
preservation; TC1 cancellation-only compatibility; exact quoted/normalized titles;
similar/unrelated/pre-existing Course protection; ambiguous-title refusal;
removal failure; and correctly labelled evidence.

These checks use fake dependencies/state. They are **not** real Selenium, TestNG
or Allure execution, a project dependency/type compile, Java 26 validation, or
evidence of website persistence/cleanup passing. No account state was changed here.

The ZIP contains 42 project files under one VLearningAutomation folder. Archive
CRC and byte-for-byte source matching were checked. Fixtures, logs, caches and
build output are excluded. README provides the exact live-validation sequence
for an environment with JDK 26, Chrome and required downloads.
