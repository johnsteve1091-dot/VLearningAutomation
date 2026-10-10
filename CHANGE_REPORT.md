# VLearningAutomation — v1.1.15

## Baseline comparison

Fresh GitHub main was inspected first, followed by the current local project.
GitHub still reports **v1.1.10**, tree
`411ea3f139739f72ec1b5011e73255a51de45f35`, with ENROLL-001 only.
Local **v1.1.14** contains ENROLL-001–005, guest redirect/return, refresh
persistence, and application logout/login persistence. Its 42 files matched the
previous v1.1.14 delivery ZIP byte for byte.

Nine local files differ from GitHub: build.gradle, README, CHANGE_REPORT,
AccountPage, CourseDetailPage, HomePage, LoginPage, EnrollmentSupport and
EnrollTests. The other 33 files match GitHub; neither side has additional files.
The current local version was authoritative. No older GitHub source replaced it.

Project version increments once: **1.1.14 → 1.1.15**. Dependency, Wrapper,
Java 26 and Allure versions remain unchanged.

## ENROLL-006 — Same Course twice

Maps **TC_ENR_010 + TC_ENR_011**. The test logs in using existing EnrollInput
credentials, selects one healthy rendered Course through the existing first
category/card path, and captures its category, href and normalized stikerCard name.
The read-only My Courses absence guard runs before acquiring ownership.

The first enrollment's exact success title and initial My Courses presence are
setup guards. The test returns through the captured category to the same card,
clicks Đăng ký a second time and performs exactly two business assertions:

1. The generic popup reader returns **`Đã đăng ký khóa học này rồi!`**,
   centralized as `EnrollInput.DUPLICATE_TITLE`.
2. After the popup closes, `AccountPage.countExactCourseItems(name)` returns
   exactly **1**.

The new count operation uses exact normalized visible h6 title text inside the
existing My Courses item locator. It returns duplicate counts rather than throwing
the unique-match cleanup guard. Whitespace/NBSP are normalized; case, accents and
quotes remain meaningful. It does not count unrelated items or build dynamic XPath.
The existing enrollment button, popup reader and explicit popup-close wait are reused;
CourseDetailPage is unchanged. Duplicate body text is not asserted.

## ENROLL-007 — Two different Courses together

Maps **TC_ENR_006** and enrolls **two Courses**, A and B.
A's absence guard precedes its attempt. Both success titles are setup guards.
B is checked independently for pre-existing enrollment before its own attempt;
A is expected to be enrolled by then.

CourseCategoryPage's existing first-course selector delegates to one shared card
selection implementation. The new distinct-course operation applies the same
visible-card, valid Detail href and nonblank visible stikerCard rules, skipping A's
normalized href and title. A different title is also necessary because My Courses
provides names rather than useful IDs.

HomePage tries the same rendered category, then at most the first other discovered
category. Selection stops at the first usable B. No pagination, fixed course
identity, direct navigation shortcut, API or catalog sweep was added.
The bounded search fails setup clearly if no unambiguous distinct B is found;
it makes no claim to inspect every category.

After B succeeds and the popup closes, exactly two business assertions verify
exact A presence and exact B presence in the same My Courses state.

## Ownership, cleanup and preservation

TC6 sets its attempt flag immediately before the first enrollment click, after
the absence guard. TC7 appends each exact name to its owned list immediately before
that Course's click, after its respective absence guard. Thus accepted requests
can still be cleaned when a response, setup guard or business assertion fails.
Names selected but not attempted never become owned.

EnrollmentSupport now accepts a list of owned names and tries each cleanup
independently. A failure cancelling or verifying removal of A does not skip B.
The existing unique exact-title cancellation and removal verification remain
unchanged. Pre-existing, similarly named and unrelated Courses are never cancelled.

Original failures remain primary; independent cleanup/evidence failures are
suppressed on them. Cleanup failure after scenario success fails the testcase.
Logs and one compact cleanup attachment identify each Course and its outcome.
A failure screenshot is retained before cancellation where available.

If a duplicate-enrollment defect creates multiple exact-title items, TC6 can
observe count 2. Existing cleanup deliberately refuses ambiguous cancellation,
preserves the count assertion and reports cleanup failure. This is a safety
limitation, not a live product-defect finding from this environment.

**EnrollmentSupport performs no login, logout, cookie/storage clearing or driver
quit.** BaseTest retains final browser/session teardown.

The actual v1.1.14 support helper contained optional login recovery for TC4/TC5,
which conflicts with the new no-login support requirement. Recovery was moved
to the existing `LoginSupport.restoreSessionForCleanup(...)` operation.
Only TC4/TC5's `finally` orchestration changed to call that operation before
cancellation and preserve restoration errors. Their annotations and scenario
code before `finally` remain byte-for-byte unchanged. TC5 still performs real
application logout once as its scenario step, and can restore its session for
owned-course cancellation after a failed second login.

TC1–TC3 complete declarations and bodies remain byte-for-byte unchanged.
Existing AccountPage methods, HomePage.logoutIfAuthenticated, LoginPage,
CourseDetailPage, BaseTest, credentials and testng.xml are preserved.
EnrollTests has **seven direct methods, all @Test**, with no helpers, nested
types, raw locators, DOM parsing or URL-normalization logic added.
The complete project now has **41 TestNG methods**.

## Files changed

**New project files: none.** Ten files modified relative to local v1.1.14:

| File | Change |
| --- | --- |
| `src/test/java/vn/cybersoft/vlearning/tests/EnrollTests.java` | TC6/TC7; necessary TC4/TC5 cleanup recovery relocation |
| `src/test/java/vn/cybersoft/vlearning/data/enroll/EnrollInput.java` | Exact current duplicate title constant |
| `src/test/java/vn/cybersoft/vlearning/data/enroll/EnrollmentSupport.java` | Independently clean owned Course names; remove login |
| `src/test/java/vn/cybersoft/vlearning/data/login/LoginSupport.java` | Existing TC4/TC5 login recovery moved here |
| `src/main/java/vn/cybersoft/vlearning/pages/AccountPage.java` | Observable exact visible-title item count |
| `src/main/java/vn/cybersoft/vlearning/pages/CourseCategoryPage.java` | Shared selector with distinct-course exclusion |
| `src/main/java/vn/cybersoft/vlearning/pages/HomePage.java` | Bounded same/one-other-category selection |
| `build.gradle` | Project version only |
| `README.md` | Enrollment cases, mappings, counts, run order and version |
| `CHANGE_REPORT.md` | This delivery and actual validation |

The other **32 project files are byte-for-byte unchanged**. Unrelated README
Course Detail, Course List and Allure sections remain unchanged.

## Actual validation — 2026-10-10

All nine requested commands were attempted sequentially:

```sh
bash gradlew compileJava compileTestJava --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.duplicateEnrollmentShouldBePrevented --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.differentCoursesShouldCoexistInMyCourses --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.healthyCourseShouldEnrollAndAppearInMyCourses --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.guestEnrollmentShouldRedirectToLogin --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.guestLoginShouldReturnToSameCourseDetail --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.enrolledCourseShouldPersistAfterMyCoursesRefresh --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.enrolledCourseShouldPersistAfterLogoutAndLogin --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests --console=plain --no-daemon
```

Every command exited **1 before any Gradle task started**. The Wrapper could not
download `https://downloads.gradle.org/distributions/gradle-9.7.1-bin.zip`,
reporting `java.net.SocketException: Network is unreachable`.
The environment's default Java is **17.0.20**; the project's **Java 26**
requirement was retained.

| Requested check | Actual result |
| --- | --- |
| Production/test compilation | Attempted; blocked during Gradle bootstrap |
| ENROLL-006 alone | Attempted; no TestNG/live duplicate-enrollment verdict |
| ENROLL-007 alone | Attempted; no TestNG/live two-course verdict |
| ENROLL-001 alone | Present; attempted; no TestNG execution |
| ENROLL-002 alone | Present; attempted; no TestNG execution |
| ENROLL-003 alone | Present; attempted; no TestNG execution |
| ENROLL-004 alone | Present; attempted; no TestNG execution |
| ENROLL-005 alone | Present; attempted; no TestNG execution |
| Full EnrollTests | Attempted; no TestNG execution |
| Live exact cancellation/removal | Not reached |
| Chrome and Allure runtime | Not reached |
| Live Course B availability / duplicate popup close | Unverified |
| Product defect | No live verdict; no account state was changed |

All **29 Java sources passed syntax parsing**, including direct Course Detail
XPath compilation. AST/file checks verify test-only architecture, unchanged TC1–TC3,
TC4/TC5 preservation before their finalizers, exact assertion counts, additive
Page Object changes and unchanged dependencies/unrelated files.

An isolated dependency-stub slice compiled and passed **23 offline safety checks**.
It exercised actual EnrollTests, AccountPage, HomePage, CourseCategoryPage,
EnrollmentSupport and LoginSupport using fake browser/framework dependencies.
Checks covered duplicate count 2, exact visible quoted/normalized titles, shared
healthy-card selection, bounded B fallback, ownership before clicks, pre-existing
Course protection, partial enrollment, independent A/B cleanup, missing removal,
primary-error preservation, TC1 compatibility and TC4/TC5 scenario/recovery behavior.

These results are **not** project dependency/type compilation, Java 26 compilation,
Selenium/TestNG/Allure runtime execution or live-site PASS results.
The ZIP contains 42 project files under one VLearningAutomation folder. Offline
fixtures, logs, caches and build output are excluded. Archive CRC and source-byte
matching were verified. README includes the live-validation commands for an
environment with JDK 26, Chrome and required downloads.
