# VLearningAutomation v1.1.4 — Course List TC1

## Baseline and preservation

Inspected current local project snapshots and GitHub main before editing. The current
workspace is not a Git checkout. The latest prior local deliverable was v1.1.3;
GitHub main is also v1.1.3 at commit
`596b7bbf5e1f3d1f6345090fc6fb207499e48c15`.
All 23 baseline files were retrieved and checked against their Git blob hashes.

The local v1.1.3 snapshot has no local-only source files. Seven Java files differ:
BasePage, LoginPage, RegisterPage, BaseTest, RegisterInput, LoginTests and RegisterTests.
GitHub contains later Login/Register work, including successful Login, updated Register
locators/form reset, configured duplicate data and session cleanup. The existing local
snapshots were left untouched; this implementation uses a separate copy of current main.
No changes were pushed to GitHub.

The project patch version changed exactly once: **1.1.3 → 1.1.4**. Dependencies, Java 26,
Gradle Wrapper and Allure configuration are preserved. The actual suite now contains
**5 Login + 14 Register + 1 Course List = 20 active TestNG methods**.
The older README's four-Login count and success-last Register order were stale; the
README now describes current main. Login/Register source and behavior were not changed.

## Files created

- `src/main/java/vn/cybersoft/vlearning/pages/CourseListPage.java`
- `src/main/java/vn/cybersoft/vlearning/pages/CourseDetailPage.java`
- `src/test/java/vn/cybersoft/vlearning/tests/CourseListTests.java`

## Files modified

- `src/main/java/vn/cybersoft/vlearning/pages/HomePage.java`: supplied Khóa học link and navigation method.
- `src/test/resources/testng.xml`: add CourseListTests; preserve LoginTests/RegisterTests; update the suite's descriptive test label.
- `build.gradle`: project version only, 1.1.3 → 1.1.4.
- `README.md`: version, verified test count, current scope and Course-only execution.
- `CHANGE_REPORT.md`: this implementation and validation report.

All other baseline files, including BasePage, BaseTest, every Login/Register source,
resource configuration and Wrapper file, remain byte-for-byte unchanged.

## Implemented behavior

Exactly one new TestNG method implements COURSE-LIST-001, Full /khoahoc Course
Integrity Sweep. It uses the existing BaseTest Homepage setup, clicks the real header
link through HomePage, and traverses live Next controls. No fixed course/page total,
previous-page or ellipsis navigation is used.

CourseListPage captures immutable name, browser-resolved href, page and 1-based card
index values before opening detail pages. A missing visible name is retained as a
collection issue, allowing remaining courses to be processed. Snapshot reads retry
transient stale elements and require two matching explicit-wait polls. Next waits for
both the active number and href dataset to change; repeated pages/datasets, skipped
numbers and failure to advance abort as infrastructure failures.

The test deduplicates by the resolved href and preserves the first encountered source
page/index. Each detail opens in a new temporary tab. Its supplied visible healthy
marker is required. The supplied empty `/chitiet/` path is reported as malformed;
there is no guessed 404 locator or assumed 404 text assertion. Missing markers and
course-specific Selenium exceptions become accumulated broken/suspicious findings.

Each failure captures a PNG while the detail tab is still active and attaches its
name, href, source page, index, resulting URL and reason. Then the detail tab closes,
the catalog tab is selected, and its page/dataset is revalidated. Unusable sessions,
invalid selectors, failed evidence capture and failed catalog recovery abort with an
explicit infrastructure error instead of masquerading as individual course defects.

A final Allure text attachment/log reports completion, pages, unique discovered and
scanned counts, healthy/broken counts, and every broken-course detail. The only TestNG
assertion follows the entire sweep and includes all failures. An infrastructure abort
attaches the partial summary when the test body has started. BaseTest's existing final
failure screenshot and cleanup are preserved.

Navigation is limited to 30 seconds within this test's fresh browser; marker/catalog
waits reuse BasePage's 15-second explicit wait. No sleeps, implicit waits, parallelism,
DataProviders, listeners, API mocks or server-failure simulations were added.
The temporary-tab implementation could not be exercised end to end in this runtime;
no claim is made that live tab recovery or per-course evidence was verified here.

## Commands and actual results

Gradle Wrapper tasks were invoked through a local verification launcher that supplies
JDK 26, the existing Gradle cache and the environment's TLS/proxy settings. That launcher
and environment settings are not included in the project. The project commands were:

```sh
./gradlew compileJava compileTestJava
./gradlew test --tests vn.cybersoft.vlearning.tests.CourseListTests
```

- Main and test compilation: **PASSED** with Java 26 and the pinned dependencies.
- Course-only execution: **BLOCKED in BaseTest.setUp**, before the test body.
  ChromeDriver returned `SessionNotCreatedException`: Chrome exited during startup.
- A second Course-only run used `cleanTest test --tests ...` plus an external diagnostic
  init script enabling ChromeDriver logging. It recompiled the final test source and
  confirmed the cause: `Running as root without --no-sandbox is not supported.`
  BaseTest/ChromeOptions were left unchanged. No unsafe browser flag was added.
- TestNG recorded a failed setup configuration and one skipped Course TC1 method in
  each attempt. LoginTests and RegisterTests were not executed.
- **Courses discovered/scanned by Selenium: 0; the sweep never started.**
  Healthy/broken counts and broken-course findings are **unavailable**, not a zero-defect
  result. No full-catalog PASS, website defect verdict or screenshot validation is claimed.
- Allure's existing finalizer ran after both failed test tasks. Raw JSON records exist
  for broken setup and skipped Course TC1, with Epic/Feature/Story and Description.
  The final Course TC1 report title includes COURSE-LIST-001. The self-contained
  `build/reports/allure-report/allureReport/index.html` was generated (3,700,681 bytes).
  No per-course screenshots exist because Chrome setup failed before course navigation.

Separate browser inspection (not the Selenium run) observed the supplied catalog
selectors matching 12 cards on page 1. Clicking the supplied Next item advanced the
active page to 2; after loading, that page also had 12 matching cards. Pagination DOM
showed the disabled class on Previous at page 1. A `/chitiet/` link was observed among
page-1 cards, but its detail health was not checked. These observations are neither a
full catalog count nor a substitute for executing CourseListTests.

## Final inspection and delivery

Package declarations match folders, imports compile, the seven new XPath uses match
the vault exactly, and no raw locators occur in CourseListTests. There is exactly one
new @Test and one final aggregated assertion. XML retains all three test classes.
The Wrapper scripts/JAR/properties are preserved. No extra framework classes or
unrelated feature tests were added.

The ZIP contains the complete `VLearningAutomation/` project root with 26 source,
configuration, Wrapper and documentation files. Generated builds, verification scripts,
logs, local caches and browser inspection artifacts are excluded.

To complete the live sweep on a normal desktop with Chrome installed, open the project
in IntelliJ with JDK 26 and run CourseListTests, or use:

```powershell
.\gradlew.bat clean test --tests "vn.cybersoft.vlearning.tests.CourseListTests"
```

Genuine broken-course assertions remain failures. The automatic Allure report and its
per-course attachments will describe the actual live catalog result from that run.
