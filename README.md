# VLearningAutomation — v1.1.4

Cybersoft V Learning Selenium capstone: **5 Login + 14 Register + 1 Course List =
20 active TestNG methods**. Course List TC1 is one test that checks every unique
course advertised by the live paginated catalog.

Stack: **Java 26**, Gradle Wrapper **9.7.1**, Selenium **4.49.0**,
TestNG **7.12.0**, Log4j **2.26.1**, Chrome. Allure Gradle plugin **4.3.0**,
report runtime **3.14.3**, Java/TestNG adapter **2.35.5**. AspectJ is disabled.

## Open and run

1. Extract the ZIP and open its `VLearningAutomation` folder directly in IntelliJ IDEA.
2. Use **JDK 26** for both Project SDK and Gradle JVM.
3. Allow Gradle to sync.
4. Install Google Chrome; Selenium Manager resolves ChromeDriver automatically.
5. Run `CourseListTests` from IntelliJ or use the filtered Gradle command below.

From PowerShell in the project root:

```powershell
.\gradlew.bat clean test --tests "vn.cybersoft.vlearning.tests.CourseListTests"
```

On macOS/Linux:

```sh
./gradlew clean test --tests "vn.cybersoft.vlearning.tests.CourseListTests"
```

To run all 20 methods, use `testng.xml` or `gradlew.bat clean test`. To run only
Login or Register, replace `CourseListTests` in the filtered command. The existing
Login suite includes successful login; the Register suite includes real registration.
Their current data and behavior are preserved from main.

## Course List TC1

`COURSE-LIST-001 - Full /khoahoc Course Integrity Sweep` starts with BaseTest's
Homepage and clicks the supplied Khóa học header link. It snapshots each page's
course names, resolved hrefs, page numbers and 1-based card indexes, then checks
each unique href in a temporary detail tab. Returning to the catalog reconfirms
the same page and href dataset before continuing.

The supplied visible Course Detail marker is the positive health contract. Missing
markers, an empty `/chitiet/` detail path and course-specific Selenium failures are
recorded with immediate detail-tab screenshots. Missing course-name data is retained
as a collection issue so it does not silently skip the course. No 404 locator is used.

The sweep follows live Next controls until absent/disabled/unusable, without fixed
page or course totals. Explicit waits require a stable snapshot and, after Next,
both the active page and course href dataset to change. Repeated pages/datasets,
skipped page numbers or stuck Next abort clearly as traversal infrastructure failures.

Each detail navigation has a 30-second page-load timeout; existing explicit waits
are 15 seconds. Course failures accumulate and cause **one final assertion** after
the full sweep. An unusable session, failed catalog recovery or unavailable evidence
aborts with an infrastructure error and a partial summary. A setup failure before the
test starts is reported by TestNG/Allure; no course count is inferred from it.

## Architecture

- `BaseTest`: fresh Chrome, EAGER loading, Homepage setup, final failure screenshot,
  session cleanup and quit. Unchanged in v1.1.4.
- `BasePage`: existing explicit-wait and element helpers. Unchanged.
- `HomePage`: navigation to Login and Course List.
- `CourseListPage`: authoritative catalog locators, immutable snapshots and Next traversal.
- `CourseDetailPage`: opening discovered hrefs and checking the supplied healthy marker.
- `CourseListTests`: orchestration, href deduplication, evidence, summary and assertion.
- Existing Login/Register Page Objects, data/support classes and tests are preserved.
  Login IDs are LOG-001–005; Register has REG-001–013 plus REG-SMOKE-001. Current
  main runs successful registration REG-002 first, followed by the other Register cases.
  Known-existing duplicate Account/Email prerequisites remain in `RegisterInput.Existing`.

## Allure

The existing `test` finalizer generates `allureReport` even when tests fail. Genuine
failures remain failures. Raw JSON and PNG evidence go to `build/allure-results/`.
The self-contained report is `build/reports/allure-report/allureReport/index.html`.

Allure includes human-readable titles, descriptions, V Learning → Feature → Story,
status, duration and stack traces. Course TC1 adds per-broken-course screenshots and
details plus a final complete/partial summary. BaseTest's final screenshot is preserved.

```powershell
.\gradlew.bat allureServe
.\gradlew.bat allureReport
.\gradlew.bat tasks --group allure
.\gradlew.bat tasks --all | Select-String allure
```

`allureReport` regenerates from existing results without rerunning tests. Do not run
`clean` between a test run and manual report regeneration. Expected tasks include
`allureReport`, `allureServe` and `downloadAllure`. Generated output is ignored by Git
and excluded from the ZIP. See `CHANGE_REPORT.md` for this revision's actual validation.
