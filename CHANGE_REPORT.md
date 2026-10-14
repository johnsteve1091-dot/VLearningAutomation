# VLearningAutomation v1.1.21 — CANCEL-001–003 and ENROLL-008

## Verified baseline

GitHub main was checked on **2026-10-14** at
`https://github.com/johnsteve1091-dot/VLearningAutomation`:

- Commit `6dad733c11f98a2b0637d591e1a52cbb2f455b7e`, tree `dbac48ee9989b63bc0c16f38d6c48f22c887bb5d`.
- Main remains **v1.1.19**, 45 files; every pinned file matches its Git blob identity.
  EnrollTests, EnrollmentSupport, EnrollInput, AccountPage, CourseCategoryPage,
  CourseDetailPage, HomePage, BaseTest and testng.xml were inspected.
- The newer complete local archive, **VLearningAutomation-v1.1.20-AccountTC14-TC16.zip**,
  is the implementation baseline: 165285 bytes, SHA-256
  `d2df1fea58dab31edd803d6abda9e3781e705443df77bb20b822c06cdc424b84`.
  All 45 archive entries match its local project.
- v1.1.20 differs from main in build.gradle, README, CHANGE_REPORT, AccountPage,
  HomePage, AccountSupport and AccountTests. These newer files were retained,
  including ACCOUNT-014–016 and the existing ACCOUNT-011/012 input-retention and
  ACCOUNT-013 short guest-blank behavior. No GitHub push/merge was performed.

Version increments once: **1.1.20 → 1.1.21**. The complete project has **47 files,
34 Java sources and 61 TestNG methods**: Login 5, Register 14, Course List 10,
Course Detail 5, Enrollment 8, Account 16, Cancellation 3. All **57** previous tests
and every prior Java member are preserved verbatim.

## Exact changes

Paths are relative to the project root. **2 added + 7 modified; 38 files unchanged.**

| File | Change |
| --- | --- |
| `build.gradle` | Project version only: 1.1.21. |
| `src/test/java/vn/cybersoft/vlearning/tests/CancelTests.java` | Added exactly three independent public scenarios, visible assertions, existing Log4j/Allure conventions and `finally` cleanup. |
| `src/test/java/vn/cybersoft/vlearning/tests/EnrollTests.java` | Added ENROLL-008 and support import; seven previous methods and annotations remain verbatim. |
| `src/test/java/vn/cybersoft/vlearning/data/cancel/CancellationSupport.java` | Added per-test baseline, frontend selection, ownership, setup, evidence, session recovery and cleanup orchestration. |
| `src/main/java/vn/cybersoft/vlearning/pages/AccountPage.java` | Added immutable exact Course-title/count snapshot with expected-account/active-panel readiness. All old members and locators retained. |
| `src/main/java/vn/cybersoft/vlearning/pages/CourseCategoryPage.java` | Added read-only selection excluding original/current names and previously selected names/hrefs; existing card/sticker locators and URL rules reused. All old members retained. |
| `src/test/resources/testng.xml` | Added CancelTests once and updated display name; all six existing registrations and suite configuration retained. |
| `README.md` | New purposes, commands, counts, architecture, cleanup rules and actual validation limitations. Existing department documentation retained. |
| `CHANGE_REPORT.md` | This release's baseline, changes and actual results. |

BaseTest, BasePage, HomePage, AccountTests, AccountSupport, EnrollInput,
EnrollmentSupport, LoginSupport, CourseDetailPage, other departments, settings,
gradle.properties, Wrapper files and logging/Allure configuration are unchanged.
No extra TestNG testcase, listener, dependency or retry layer was introduced.

Existing versions remain **Java 26 / Selenium 4.49.0 / TestNG 7.12.0 / Gradle 9.7.1 /
Log4j 2.26.1 / Allure plugin 4.3.0 / report runtime 3.14.3 / adapter 2.35.5**.
The current project was not downgraded to the older foundation request's versions.

## Business coverage

| Case / method / group | Assertions |
| --- | --- |
| CANCEL-001 / `enrolledCourseShouldBeCancelled` / `cancel-tc1` | Course absent before enrollment; exact existing success popup; one entry and available exact cancellation button; cancellation removes it, count becomes zero, original Courses remain unchanged. |
| CANCEL-002 / `cancellingOneCourseShouldPreserveOtherCourses` / `cancel-tc2` | Capture original identities/counts first. A/B have distinct names and hrefs, start absent and enroll successfully once each. Cancel A only; assert A=0, B=1 and every original Course count unchanged. Finally clean owned B and any remaining A. |
| CANCEL-003 / `cancelledCourseShouldRemainAbsentAfterRefresh` / `cancel-tc3` | Enroll successfully, verify one entry, cancel and verify zero; use existing My Courses refresh; wait for account data/active panel; verify zero again and original Course counts unchanged. |
| ENROLL-008 / `cancelledCourseShouldAllowReenrollment` / `enroll-tc8` | Enroll A successfully once, cancel and verify absence, reopen the same captured category/href/name, enroll again, require exact success popup and one entry, then targeted cleanup. |

**ENROLL-006 is unchanged:** its duplicate attempt happens while still enrolled.
ENROLL-008 verifies the opposite state after removal. No cancellation-popup text is
asserted. Each new method starts from BaseTest's fresh browser, confirmed Enrollment
account and its own baseline/ownership; no test order or other testcase state is used.
Tests contain only public `@Test` methods and class annotations/logger configuration;
no helper methods, nested models, locators or low-level DOM operations were added.

## Cleanup, readiness and evidence

- An immutable exact normalized Course-title/count multiset records the original
  account, including duplicate counts. A non-empty starting account is supported.
  Frontend category discovery excludes original/current names and selected names/hrefs.
  If no eligible Course exists, setup fails without cancelling an existing enrollment.
  Existing detail-opening logic checks captured href/name and enrollment readiness.
- Exact absence is checked before every attempt. Ownership is recorded immediately
  before **each enrollment click**, including ENROLL-008's second attempt, and survives
  cancellation. A partial server write followed by a failed response remains owned.
- Exact-title cancellation rejects ambiguous rows. Individual counts and complete
  identity/count maps establish success; clicking or total-count changes alone cannot.
- New snapshots reuse existing `openMyCourses` readiness: expected populated account
  label and active Courses panel. Every row needs one visible nonblank title; missing,
  hidden or blank rows wait, multiple visible titles fail. The owner is rechecked after
  capture. An unpopulated empty shell is not accepted as proof of absence. No loading
  locator, fixed delay, API shortcut or DOM injection was invented.
- Every method uses `finally`. A primary failure screenshot is attached **before**
  recovery/navigation. Cleanup reuses `LoginSupport.restoreSessionForCleanup`, confirms
  the owner and delegates only owned titles to unchanged `EnrollmentSupport.finish`.
  It independently tries each title, so failure on A does not skip B. A malformed
  unrelated snapshot or evidence failure does not stop those exact-title attempts.
  Ambiguous owned matches fail safely rather than selecting an arbitrary enrollment.
- A final authenticated refresh must show zero owned entries and the full original
  baseline. Missing originals are reported, never recreated to hide collateral damage.
  Cleanup failures are suppressed on the primary failure; after a successful scenario,
  cleanup/integrity/evidence failure fails the test. Logs/Allure record selected identities,
  observed popup and Course counts with credential redaction. BaseTest's final screenshot,
  browser/session teardown and existing Allure report generation remain intact.

## Actual validation — 2026-10-14

| Check | Result |
| --- | --- |
| Baseline comparison | **PASS**: pinned GitHub main verified; newer complete v1.1.20 chosen; original archive bytes matched. |
| Source/AST preservation | **PASS**: 34 Java files parsed, package paths matched; every existing Java member compared verbatim; exactly 61 tests. Syntax/AST verification only, not dependency/type compilation. |
| Scope/static references | **PASS**: exactly three Cancellation additions plus ENROLL-008; all 57 previous tests retained; no test helpers/nested models/locators; local imports match source declarations; original locators preserved. External imports await real dependency resolution. |
| Suite/configuration | **PASS**: XML parses, seven unique registered classes, previous six retained, four groups present; build configuration changes only version. |
| Offline scenarios/cleanup | **PASS: 38 checks** using the actual four new scenario bodies, CancellationSupport and existing EnrollmentSupport/LoginSupport with fake Page Objects/framework APIs. Covers normal flows, collateral-course loss, cancellation reappearing after refresh, rejected re-enrollment, partial writes, A failure with B cleanup, session recovery, evidence/snapshot failures, ambiguity and cleanup failure after business success. |
| Offline Page Objects | **PASS: 18 checks** using actual additive AccountPage/CourseCategoryPage methods with synthetic Selenium/UI: loading/wrong/changing owner, hidden panel, delayed populated response, immutable normalized counts, malformed rows, exact identity, exclusions and invalid cards. |
| Complete Gradle compilation | **BLOCKED**: `./gradlew compileJava compileTestJava --no-daemon` exited 1 before compilation. Wrapper download of `https://downloads.gradle.org/distributions/gradle-9.7.1-bin.zip` failed with `java.net.SocketException: Network is unreachable`. Only Java 17.0.20 is available; JDK 26 and Chrome are unavailable. |
| CANCEL-001, CANCEL-002, CANCEL-003 and ENROLL-008 in Chrome/TestNG | **BLOCKED / NOT RUN**: build/toolchain/browser prerequisites unavailable. No live persistent account mutation or cleanup was performed. |
| Relevant Enrollment regression, ENROLL-001–007 | **BLOCKED / NOT RUN** for the same prerequisites. All seven source methods remain verbatim. |
| Full 61-test suite | **BLOCKED / NOT RUN** for the same prerequisites. No fabricated TestNG/Allure results. |
| ZIP | **PASS**: 47 files, valid CRCs and byte equality with inspected project; all four Wrapper files present, executable Unix gradlew; no build/cache/IDE/offline-verification artifacts included. |

The **56 offline checks are simulated checks, not extra project testcases or live
Selenium/TestNG PASS results**. Selected real sources were compiled against synthetic
APIs on Java 17. Page Object validation copies alone replace existing `getFirst()`
with `get(0)` for that JDK; delivered Java 26 sources remain unchanged. This does not
verify actual dependencies, Gradle plugins, Chrome rendering or backend behavior.

## Application defects and remaining verification

**No new application defect is confirmed**, because live tests could not run.
Injected offline failures demonstrate detection paths only. Existing product-defect
assertions were preserved and were not rerun here.

Current frontend/course availability, real cancellation persistence, re-enrollment
responses, account-response/course-panel synchronization and actual cleanup still
require a configured run against `https://demo2.cybersoft.edu.vn/`. The implementation
uses the established account-response readiness contract; it does not invent a new
application loading state or cancellation message. Ambiguous exact names fail safely.
External concurrent account changes can produce an integrity mismatch; tests do not
erase or recreate such data to force a PASS.

## Run

Extract and open the `VLearningAutomation` root in IntelliJ, select **JDK 26** for
Project SDK/Gradle JVM, sync Gradle and install Chrome. Run sequentially and allow
cleanup to finish. PowerShell:

```powershell
.\gradlew.bat compileJava compileTestJava
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CancelTests.enrolledCourseShouldBeCancelled"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CancelTests.cancellingOneCourseShouldPreserveOtherCourses"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CancelTests.cancelledCourseShouldRemainAbsentAfterRefresh"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.cancelledCourseShouldAllowReenrollment"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests"
.\gradlew.bat test
```

Linux/macOS: replace `.\gradlew.bat` with `bash gradlew`. IntelliJ may run individual
methods/classes or `src/test/resources/testng.xml`. README includes a four-case
combined command. Existing automatic Allure reports remain configured.

Suggested commit: `test: add cancellation coverage and re-enrollment in v1.1.21`
