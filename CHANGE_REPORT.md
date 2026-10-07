# VLearningAutomation v1.1.9 — Course Detail Architecture Refactor

## Baseline and scope

The actual local `VLearningAutomation-v1.1.8-CourseDetailTC4-TC5.zip` was inspected
first. Its `build.gradle` version is **1.1.8** and its Course Detail implementation
is the source of truth. An untouched copy and file hashes were retained for comparison.

GitHub was inspected afterward for historical conventions only. Main at
`853db432d2a49c8b75b683b7e4aab9774b2a6596` reports version **1.1.4** and does not
contain the current Course Detail implementation. No Course Detail code was rebuilt
from that stale revision.

**One patch increment: 1.1.8 → 1.1.9.** This is an architecture-only release.
No dependency versions, test contracts, locators or sampling rules changed.
No sixth Course Detail testcase was added. The suite remains **34 TestNG methods**:
5 Login, 14 Register, 10 Course List and 5 Course Detail.

## Strict test-class structure

| CourseDetailTests measure | Before: v1.1.8 | After: v1.1.9 |
| --- | ---: | ---: |
| Source lines | 819 | 161 |
| Directly declared methods | 29 | 5 |
| `@Test` methods | 5 | 5 |
| Helper methods | 24 | 0 |
| Nested records | 2 | 0 |
| Other nested classes/enums | 0 | 0 |

Method counts refer to methods directly declared in `CourseDetailTests`; the old
record's `logLine()` is part of its nested record, not a direct test-class method.
The Java syntax-tree inspection confirms that the only remaining methods are:

1. `knownCourseShouldExposeCompleteCoreContract()`
2. `knownCourseShouldExposePopulatedCurriculum()`
3. `knownCourseShouldExposeValidRelatedCourses()`
4. `relatedCoursesShouldNavigateToAdvertisedHref()`
5. `courseCardsShouldMatchTheirCourseDetailIdentity()`

Every method retains its original `@Test`, group, test name, description, Story and
Allure Description. There are no extra methods, constructors or nested types in
the test class. Each scenario performs setup/capture, invokes support, attaches
evidence, logs its result and asserts the final outcome. Validation loops, reports,
comparison machinery and data models were not inlined into the test methods.

## Support responsibilities

### Created: CourseDetailContractSupport.java

Owns TC1 core, TC2 curriculum and TC3 related-card validation and evidence.
Its public entry points are `inspectCore`, `inspectCurriculum` and `inspectRelated`.
They receive immutable Page Object snapshots and return issues, summary text and,
for TC3, the problem-card count. The result lists are defensively copied.

Moved helpers: `require`, `checkDisplayed`, `checkReadable`, `checkText`, `checkImage`,
`checkRelatedImage`, `inspectRelatedCard`, `addRelatedIssues`, `buildSummary`,
`buildCurriculumSummary`, `relatedSummaryHeader`, `appendRelatedCard`,
`appendRelatedIssues`, `appendField` and `appendImage`.

The small shared `require`, `checkDisplayed` and `appendRelatedIssues` helpers have
package visibility for reuse by navigation support. Browser interaction, Allure calls
and final TestNG assertions are not in this class.

### Created: CourseDetailNavigationSupport.java

Owns TC4's per-card orchestration through the existing Page Object navigation methods,
navigation snapshot inspection, issue collection, result model and summary generation.
Its public `inspect` entry point receives the Page Object and captured card targets.

Moved helpers: `inspectRelatedNavigation`, `addRelatedNavigationIssues` and
`appendRelatedNavigation`.

TC4 still checks every rendered related card through both the main-card and
`XEM CHI TIẾT` paths: click performed, navigation observed, advertised href matched
and Course Detail root displayed. It does not compare Course names, instructors
or other Course data. Page Objects still perform the clicks, hover and observation.

### Expanded: CourseDetailConsistencySupport.java

The existing sampling and comparison members are unchanged. The class now also owns
TC5 source traversal, the local href-keyed Detail identity cache, defect conversion,
source assessments, result models and the full consistency report. The public
`inspectSources(WebDriver)` entry point uses existing Page Objects to capture sources
and Detail identity; existing direct URL navigation is retained.

Moved helpers: `checkConsistencySource`, `compareCandidate`, `addConsistencyDefects`,
`buildConsistencySummary`, `sourceLabel` and `display`. The two browser orchestration
helpers are now static and receive the current WebDriver explicitly.

Moved records: `ConsistencySourceResult` and `ConsistencyDefect`, including the
unchanged defect `logLine()` formatting. A `ConsistencyResult` exposes the collected
sources, defects, counts and summary to the test method.

TC5 still checks candidate 1 for each source; only a failed representative triggers
diagnosis of candidates 2–5, up to five total Courses. Sources remain Homepage,
the initial Khóa học page, every discovered Danh mục and related Courses from
`/chitiet/.100`. Category-discovery and missing-candidate defects remain failures.
Main-card and available subCard Course-name/instructor comparisons are preserved,
including ellipsis-aware Course names and normalized instructor whitespace.
The shared href cache remains local to one testcase. No pagination, exhaustive
audit, related recursion, API checks or enrollment behavior was added.

## Exact changed-file scope

Modified existing files:

1. `src/test/java/vn/cybersoft/vlearning/tests/CourseDetailTests.java`
2. `src/test/java/vn/cybersoft/vlearning/data/CourseDetail/CourseDetailConsistencySupport.java`
3. `build.gradle` — project version only.
4. `README.md` — version, support responsibilities and permanent architecture rule.
5. `CHANGE_REPORT.md` — this release report.

Created files:

1. `src/test/java/vn/cybersoft/vlearning/data/CourseDetail/CourseDetailContractSupport.java`
2. `src/test/java/vn/cybersoft/vlearning/data/CourseDetail/CourseDetailNavigationSupport.java`

The other **31 original project files are byte-for-byte unchanged**, including
`CourseDetailPage`, Login/Register/Course List implementations, shared card readers,
base classes, `testng.xml`, Gradle Wrapper files and logging configuration.
All Page Object locators and the frontend spellings `detailCoures` and
`instrutorTitle` are preserved. There are no unrelated changes or dependency changes.
The final ZIP contains 38 project files, including 25 Java files; temporary
verification tools and generated build/IDE output are excluded. No push was made.

README now makes the rule permanent for all new test classes, including future
Enroll, CancelCourse and Profile tests: testcase methods only; reusable validation,
helpers and reports in support; browser interaction in Page Objects.

## Validation performed — 2026-10-07

### Source preservation and structure

- All 25 Java source files parsed successfully using the available compiler's syntax
  parser. The 34 direct Course Detail XPath expressions also parsed successfully.
  This does not perform dependency resolution or Java 26 type checking.
- The syntax tree confirms five directly declared methods, five original `@Test`
  annotations and zero nested types in `CourseDetailTests`.
- All 24 moved helper bodies were compared to v1.1.8. Bodies are unchanged except
  the explicit driver argument forwarded to `compareCandidate` from its new static
  caller. Model changes are visibility changes needed by the new support API.
- Complete TC1–TC3 validation/report blocks, the TC4 navigation/report loop and the
  TC5 source/cache/report orchestration were compared to their original blocks.
  The only orchestration changes are support delegation and explicit driver passing.
- All original members of the pre-existing consistency support class are unchanged.
- The five Allure attachment calls, warning/completion messages and final assertions
  were compared to the original code, allowing only access through the support result.
  Per-module/card/source logs now originate in their responsible support classes.
- File hashes verify the preserved baseline, unchanged Page Objects and unrelated
  files. The no-index Git diff against the supplied local snapshot was reviewed.

### Requested real build and runtime checks

Before refactoring, both the main/test compilation command and the full Course Detail
test command were attempted against v1.1.8. After refactoring, these commands were
attempted against v1.1.9:

```sh
bash gradlew compileJava compileTestJava --console=plain --no-daemon
bash gradlew clean test --tests 'vn.cybersoft.vlearning.tests.CourseDetailTests' --console=plain --no-daemon
```

Both before and after, commands exited **1 before any Gradle task started**:
the Wrapper could not download
`https://downloads.gradle.org/distributions/gradle-9.7.1-bin.zip`, reporting
`java.net.SocketException: Network is unreachable`.
The environment has Java **17.0.20**; the project remains configured for Java **26**.
Neither dependency versions nor the Java requirement was downgraded.

| Requested result | Observed outcome |
| --- | --- |
| Main compilation | Blocked before Gradle tasks; no compile verdict |
| Test compilation | Blocked before Gradle tasks; no compile verdict |
| TC1 core contract | Not executed |
| TC2 curriculum contract | Not executed |
| TC3 related-card contract | Not executed |
| TC4 navigation | Not executed |
| TC5 frontend consistency | Not executed |
| Five tests independently discovered by TestNG | Original method names/groups/annotations preserved; runtime discovery unverified |
| Pre-existing live product failures unchanged | Unverified; no before/after browser execution was possible |
| Allure evidence | All five summary calls and logical report content preserved; no new runtime attachments/report generated |

No PASS result or live product defect verdict is claimed. The source comparison
confirms that validation conditions, failure collection and final assertions were
preserved; it cannot establish current frontend outcomes. Full verification still
requires JDK 26, Chrome and access to the Gradle distribution/dependencies or a
compatible populated cache. Genuine product defects must continue to fail when run.
