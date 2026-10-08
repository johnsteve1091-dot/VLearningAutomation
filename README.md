# VLearningAutomation — v1.1.10

Cybersoft V Learning Selenium capstone. The active source contains **35 TestNG
methods**:

| Test class | Methods | Current scope |
| --- | ---: | --- |
| LoginTests | 5 | Existing Login scenarios, including successful login |
| RegisterTests | 14 | Existing Register scenarios and smoke test |
| CourseListTests | 10 | TC1 URL integrity, TC2 subCard structure, TC3 subCard content, TC4 main-card content |
| CourseDetailTests | 5 | TC1 core, TC2 curriculum, TC3 related-card content, TC4 navigation, TC5 frontend consistency |
| EnrollTests | 1 | ENROLL-001 success popup and exact Course in My Courses; targeted cancellation and application logout |

The Course List TC1, TC2 and TC3 families each cover Homepage, Khóa học and
Danh mục separately. TC4 covers the Khóa học main cards. Existing source controls
their behavior and execution order.

Stack: Java **26**, Gradle Wrapper **9.7.1**, Selenium **4.49.0**, TestNG
**7.12.0**, Log4j **2.26.1**, Google Chrome. Allure Gradle plugin **4.3.0**,
report runtime **3.14.3**, Java/TestNG adapter **2.35.5**. AspectJ is disabled.

## Open and run

1. Extract the ZIP and open its `VLearningAutomation` folder in IntelliJ.
2. Select JDK 26 for the Project SDK and Gradle JVM, then sync Gradle.
3. Install Google Chrome. The existing setup uses Selenium Manager for ChromeDriver.
4. Run only EnrollTests first for this release. CourseDetailTests still exercises
   the five existing Course Detail contracts independently.

PowerShell, from the project root:

```powershell
.\gradlew.bat compileJava compileTestJava
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests"
```

Linux/macOS equivalents (invoking Bash also works when the extracted Wrapper lacks
the executable permission):

```sh
bash gradlew compileJava compileTestJava
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests"
```

Run TC2 independently with:

```powershell
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseDetailTests.knownCourseShouldExposePopulatedCurriculum"
```

To run all 35 methods, use `src/test/resources/testng.xml` in IntelliJ or
`gradlew.bat clean test`. For another class, change the fully qualified class
name in `--tests`. Existing Login/Register tests include real authentication and
registration; their existing behavior and fixtures are preserved. Enrollment also
mutates account state and must complete its targeted cleanup before another run.

## Enrollment TC1

**ENROLL-001 — Successful course enrollment**, group `enroll-tc1`, is the only
Enrollment testcase. It uses the confirmed existing Login credentials in
`data/enroll/EnrollInput`; the unrelated registration-form sample password is not used.
Existing Login/Register source remains unchanged.

The test logs in through the existing Login Page Object, waits for the authenticated
header, opens the first discovered Danh mục through the normal menu, and captures the
first usable rendered card's nonblank **`span.stikerCard`** name and Detail href.
It does not inspect all Course fields, traverse pagination or sweep other categories.
The existing card parser is preserved; its h6 content is not substituted for the
Enrollment identity bridge.

Before any enrollment click, a read-only visit to My Courses confirms that this exact
name is not already enrolled. If it exists, setup fails and leaves it untouched.
The test returns through the same category menu, rediscovers the selected card by
href and name, clicks its main image and waits for the advertised Detail URL, visible
root and clickable existing Đăng ký button. No title, Course ID or category ID is a
hardcoded test expectation.

There are exactly two business assertions:

1. The visible SweetAlert title is exactly **`Đăng kí thành công`**, with `kí` and
   no period. The test waits for this dialog to close before account navigation.
2. My Courses contains one visible item whose h6 equals the captured `.stikerCard`
   name after whitespace normalization. Case, accents and quotation marks are
   preserved; names are compared as Java strings, not concatenated into XPath.

The `finally` path runs after any enrollment attempt, including an assertion failure
after the server may have accepted the action. Because the exact name was absent
before the attempt, cleanup can target this run's course without deleting an existing
enrollment. It clicks Hủy khóa học only inside the unique exact-title item and waits
until that same item is absent. Multiple same-title matches cause an explicit failure
instead of an ambiguous cancellation. Other account courses are never cancelled.

Application logout uses the observed `span.logout` under the avatar/account link,
then verifies the Login control is visible and the account link is no longer displayed. Logout is
attempted even if cancellation fails. Browser storage clearing in BaseTest remains
additional session hygiene, not a replacement for application logout.

If a business assertion already failed, cleanup/logout failures are logged and added
as suppressed exceptions to that original failure. If business assertions passed but
cleanup or logout fails, the testcase fails. Allure receives selected-course and popup
evidence, a cleanup/logout summary, and the original failing screen before cleanup
when screenshot capture is available. No Cancel Course testcase is added.

Live execution, removal and logout remain unverified in this delivery environment;
see `CHANGE_REPORT.md` for the actual Gradle blocker and offline checks.

## Course Detail TC1

**COURSE-DETAIL-001 — Course Detail Core Contract** is one independent TestNG method
in group `course-detail-tc1`. After the existing Homepage setup, it opens the known
specimen directly:

`https://demo2.cybersoft.edu.vn/chitiet/.100`

The dot in `.100` is intentional. This test implements the supplied 27 core
requirements and consolidates **TC_CD_001–TC_CD_017** into one result:

| Legacy cases | Core information |
| --- | --- |
| TC_CD_001–003 | Visible detail root/content, fixed title/subtitle, nonblank course name |
| TC_CD_004–007 | Instructor name/avatar, field/category, rendered rating icons, rating/review text |
| TC_CD_008–010 | Description, learning-objectives heading/content, loaded course image |
| TC_CD_011–016 | Nonblank price, enrollments, duration, lessons, videos and level |
| TC_CD_017 | Displayed and enabled Đăng ký button |

Both images must be displayed, have a nonblank `src`, and satisfy the browser's
`complete && naturalWidth > 0` loading state. No separate HTTP image request is made.
Sidebar values are found by their labels, rather than row indexes. Course-specific
names, amounts and counts are read from the page and are not hardcoded expectations.

The Page Object uses the observed `detailCoures` and `instrutorTitle` spellings.
One shared 15-second
explicit-wait budget covers the root, asynchronous field content and image
completion. Missing fields remain in the captured snapshot when that budget expires,
so the test can collect all ordinary field problems and fail once. Unusable sessions,
WebDriver command failures and persistently unobservable DOM state remain
infrastructure failures.

The button's displayed/enabled state is checked without clicking or hovering it.
There is no enrollment, curriculum traversal, related-card inspection, responsive
layout check, API comparison or catalog/detail sweep in this testcase.

## Course Detail TC2

**COURSE-DETAIL-002 — Course Curriculum Contract**, group `course-detail-tc2`,
covers only **TC_CD_019 and TC_CD_020**. It independently opens the same known
specimen through the existing navigation method; it does not depend on TC1.

The test checks the displayed curriculum and exact `Nội dung khóa học` heading,
at least one module, each module's content/header structure and readable title,
lesson containers, at least one lesson across the whole curriculum, and every
discovered lesson's visible nonblank title. A module may contain zero lessons if
the complete curriculum still contains lessons. Counts, titles, numbering and
ordering are not fixed expectations.

Module and lesson lookup uses relative locators. Immutable snapshots retain
strings, booleans and indexes, never WebElements. The existing explicit-wait pattern
is reused with two matching ready observations. If ordinary missing/blank content
persists through the wait, the latest complete observation is inspected and all
actionable defects are collected before one final assertion. A missing parent
does not generate redundant failures for unavailable children. Available lessons
are still inspected when another part of their module is defective.

TC2 does not require durations or preview buttons and performs no preview/media
interaction. TC_CD_018 (register-button hover), enrollment and related courses are
outside its scope.

## Course Detail TC3, TC4 and TC5

These recommendations are inside Course Detail, so TC3 and TC4 remain in
CourseDetailTests. TC5 then checks whether Course-card identity rendered on each
frontend card surface corresponds to the Course Detail identity for that advertised
href. All three reuse the existing Course-card parser/snapshot model.

**COURSE-DETAIL-003 — Related Courses Contract** (`course-detail-tc3`) covers
**TC_CD_021–TC_CD_030**. It checks the displayed section/heading, one to four
recommendation cards, every card's nonblank name and valid `/chitiet/<non-empty-id>`
href, course/instructor image health, instructor name and original/current price.
A rendered discount must be nonblank and its original price must have the shared
line-through decoration. No sale is required. A DOM-present subCard must have a
nonblank description; specifications are captured without fixed value expectations.
TC3 performs no hover, card click or destination navigation. It collects all ordinary
field problems and fails once.

**COURSE-DETAIL-004 — Related Course Navigation** (`course-detail-tc4`) covers only
navigation. Starting from `https://demo2.cybersoft.edu.vn/chitiet/.100`, it captures
only each rendered related card's immutable 1-based index and href before interaction.
For every card it:

1. Reopens the known source and rediscovers the card from the fresh DOM.
2. Clicks the normal main-card path and verifies the resulting browser URL matches the
   captured href and a Course Detail root displays.
3. Reopens the source, rediscovers the card again, hovers only to expose the existing
   `XEM CHI TIẾT` control, clicks it normally, and verifies the same URL/root contract.

TC4 does **not** compare Course names, instructors, prices, ratings, descriptions,
images, specifications, curriculum or any other Course data. Hover is only an
interaction mechanic. No WebElement survives navigation, no Course ID is required to
be numeric, no `Thread.sleep()` or JavaScript click is used, and failures are limited
to actual navigation/click/root problems.

**COURSE-DETAIL-005 — Course Card and Detail Frontend Consistency**
(`course-detail-tc5`) is one independent frontend consistency testcase. It visits
these distinct card surfaces:

- Homepage Course cards.
- The initial rendered `/khoahoc` page only; TC5 does not traverse its pagination.
- Every Danh mục URL discovered through the existing Homepage support.
- Related Courses from the known `/chitiet/.100` source, one depth only.

For each source instance TC5 captures up to the first five valid immutable card
snapshots in natural rendered order, then compares **candidate 1** against Course
Detail. A healthy representative ends checking for that source immediately. If the
first representative fails, TC5 retains that failure and inspects candidates 2–5 only
for diagnosis. Sampling is deterministic and never becomes an exhaustive catalog
audit.

TC5 compares only shared frontend identity information whose equivalence is clear:
main-card Course name and instructor name, plus subCard Course name/instructor when a
subCard exists. Course-name comparison reuses the existing ellipsis-aware matcher;
instructor names use normalized whitespace equality. TC5 deliberately does not map
hours to duration, weeks to lesson count, card level to unrelated detail metadata,
raw image URLs, descriptions, prices, ratings or review counts without a proven shared
business contract. It uses direct `driver.get(candidate.href())` after source snapshots
are captured, so click/back recovery is not part of this consistency test. Detail
identity snapshots may be reused when multiple sources advertise the same href.

Run the three tests independently:

```powershell
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseDetailTests.knownCourseShouldExposeValidRelatedCourses"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseDetailTests.relatedCoursesShouldNavigateToAdvertisedHref"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseDetailTests.courseCardsShouldMatchTheirCourseDetailIdentity"
```

No exhaustive API validation, recursive Related Course traversal or sixth Course
Detail testcase is included.

## Architecture

**Permanent rule: test classes contain testcase methods only.** Reusable
helper/validation/reporting logic belongs in support classes. Browser interaction
belongs in Page Objects. A test arranges the scenario, invokes Page Objects/support,
logs the high-level result, attaches Allure evidence and performs its final assertion.
Do not inline helper machinery into test methods to bypass this rule.

All new test classes must follow this rule, including `EnrollTests` and future
`CancelCourseTests` and Profile tests. Test classes must not declare additional
helper/utility methods of any visibility or nested records, classes or enums.
Constants, logger fields, annotations and imports are allowed. Existing Login,
Register and Course List test classes are outside this Enrollment release.

- `BaseTest`: fresh Chrome for each method, EAGER loading, Homepage setup, failure
  screenshot before session cleanup and browser quit.
- `BasePage`: existing explicit-wait and element helpers.
- `HomePage`, `CourseListPage`, `CourseCategoryPage`: existing navigation,
  card extraction and catalog traversal.
- `CourseCardDomReader`, `CourseCardSnapshot`: existing Course card capture/model.
- `CourseDetailPage`: all Course Detail locators, known URL navigation, core/image
  capture, scoped curriculum/module/lesson snapshots, minimal identity capture for
  TC5, and href-only related-card navigation support for TC4.
- `CourseDetailTests`: exactly five declared methods, each an independent `@Test`;
  scenario setup, support invocation, concise logs, one Allure summary and the final
  assertion. It has no helpers or nested types.
- `CourseDetailContractSupport`: TC1 core, TC2 curriculum and TC3 related-card
  snapshot validation, issue collection, result models and summary generation.
- `CourseDetailNavigationSupport`: TC4 orchestration through existing Page Object
  navigation methods, href/root validation, issue collection and navigation evidence.
  It performs no Course-name or instructor comparison.
- `CourseDetailConsistencySupport`: TC5 source orchestration, deterministic
  first-valid sampling (1 normally, up to 5 only after representative failure),
  Course-name/instructor comparisons, href-keyed Detail caching, source/defect models
  and consistency summary generation. It receives the current WebDriver; Page
  Objects continue to own DOM discovery and capture.
- `EnrollTests`: one declared `@Test` method with scenario orchestration and two
  business assertions; no helpers, nested types, raw locators or DOM parsing.
- `AccountPage`: account-data readiness, My Courses selection, exact-title lookup
  and cancellation/removal of only the selected course.
- `HomePage`, `CourseCategoryPage`, `CourseDetailPage`: additive header/category
  navigation, Enrollment selection and popup operations; existing methods are preserved.
- `EnrollmentSupport`: failure-safe cleanup/logout orchestration, evidence and error
  preservation. Page Objects own the application interactions.
- `EnrollInput`: the small Enrollment credential/expected-message holder.
- `CourseCardDomReader`: reused card parsing; existing Course List calls retain their
  behavior.
- `CourseSubCardContentSupport.courseNamesMatch`: existing ellipsis-aware Course-name
  comparison reused without changing its algorithm.
- Existing Login/Register pages, data/support classes and tests remain unchanged.

Course Detail was added locally in v1.1.5 and its support architecture was established
in v1.1.9. This release adds only Enrollment TC1 to that authoritative local v1.1.9
snapshot. The inspected GitHub main remains at v1.1.4. All existing Course Detail
methods, locators and support classes remain unchanged; Enrollment methods are added
to the existing Page Object. Dependency versions are unchanged.

## Allure and results

The current build retains its automatic `test` → `allureReport` finalizer.
Existing failure conditions and final assertions are preserved. Raw Allure JSON and
attachments use `build/allure-results/`; Gradle's test report is
`build/reports/tests/test/index.html`.

When the Course Detail test body completes its inspection, it attaches one
`COURSE-DETAIL-001 Course Detail core contract summary` containing requested/resulting
URLs, captured values, image health, button state and every issue. The console logs
start/completion, a course/issue count, and warnings for actual problems. The existing
BaseTest adds its normal final screenshot on assertion failure.

TC2 attaches `COURSE-DETAIL-002 Course curriculum summary`: URLs, section/title
state, discovered module/lesson totals, per-module titles/counts, lesson title
evidence and every issue. Its console logs one line per module and warnings only
for defects; successful lessons are detailed only in the attachment.

TC3 adds one related-content summary with every card, URLs, image health, instructor,
prices, description/specifications, problem-card count and all field failures. TC4
adds a compact navigation summary for both main-card and `XEM CHI TIẾT` paths with
captured href, resulting URL, Course Detail root state and navigation failures only.
TC5 attaches one detailed consistency summary covering source instances, representative
href/name/result, any failure-triggered diagnostic expansion, per-comparison advertised
versus Detail identity values, totals and mismatch records. No raw HTML or per-success
screenshots are attached.

If browser setup fails, TestNG/Allure show the failed configuration and skipped test;
there is no core summary or screenshot from a test body that never ran.
If the Gradle Wrapper cannot bootstrap, neither TestNG nor Allure runs at all.

```powershell
.\gradlew.bat allureReport
.\gradlew.bat allureServe
```

`allureReport` regenerates from existing results without rerunning tests. Avoid
`clean` between a run and report regeneration. Report provisioning requires its
Node/Allure runtime to be available.

See `CHANGE_REPORT.md` for the exact validation performed for this deliverable.
Generated output, IDE files and local verification caches are excluded from the ZIP.
