# V Learning Automation

**Selenium WebDriver automation testing framework for the Cybersoft V Learning demo website.**

This project is a Java-based UI test automation capstone covering the learner journey from authentication and course discovery through enrollment, account management, course cancellation, and end-to-end scenarios.

- **System under test:** [Cybersoft V Learning](https://demo2.cybersoft.edu.vn/)
- **Test framework:** Selenium WebDriver + TestNG
- **Build and reporting:** Gradle Wrapper + Allure
- **Current automated scope:** **65 TestNG test methods in 8 test departments**

> **Testing status:** This framework tests a live demo website with documented defects and unresolved UI-contract questions. A failing assertion is not automatically a unique website defect. See the [Test Execution Report](docs/TEST_EXECUTION_REPORT.md) for run outcomes and the [Known Defects and Test Exceptions](docs/KNOWN_DEFECTS.md) for product findings and questions. No fully passing regression is claimed.

## Test coverage

The suite is organized into eight independent functional departments, with shared end-to-end scenarios connecting them.

| Department | Test class | Methods | Coverage |
| --- | --- | ---: | --- |
| **Login** | `LoginTests` | 5 | Successful and invalid login, required controls, password masking |
| **Register** | `RegisterTests` | 14 | Registration, required fields, data-format validation, boundaries, duplicate credentials |
| **Course List** | `CourseListTests` | 10 | Course-card links, SubCard structure/content, main-card integrity across Homepage, catalogue, and categories |
| **Course Detail** | `CourseDetailTests` | 5 | Core details, curriculum, related courses, navigation, cross-page content consistency |
| **Enrollment** | `EnrollTests` | 8 | Enrollment, guest redirection, return after login, persistence, duplicate prevention, multiple courses, re-enrollment |
| **Account** | `AccountTests` | 16 | Personal information, profile editing and validation, unsaved changes, account switching, stale-session actions |
| **Cancellation** | `CancelTests` | 3 | Cancel a specific course, preserve other enrollments, verify cancellation after refresh |
| **End-to-End (E2E)** | `E2ETests` | 4 | New-student lifecycle, cross-account isolation, guest-to-enrollment flow, stale-tab protection |
| **Total** | **8 test classes** | **65** | |

These are implemented test methods, **not** a claim of 65 passes. The authoritative suite declaration is [`src/test/resources/testng.xml`](src/test/resources/testng.xml).

## Technology stack

| Component | Version / role |
| --- | --- |
| Java | **26** (configured Gradle toolchain) |
| Selenium WebDriver | **4.49.0** |
| TestNG | **7.12.0** |
| Gradle Wrapper | **9.7.1** |
| Google Chrome | Browser used by the test setup |
| Allure Gradle plugin | **4.3.0** |
| Allure report runtime | **3.14.3** |
| Allure Java/TestNG adapter | **2.35.5** |
| Log4j | **2.26.1** |
| Architecture | Page Object Model (POM), reusable test support and fixtures |

Exact dependencies and reporting settings are maintained in [`build.gradle`](build.gradle).

## Getting started

### Prerequisites

- **JDK 26** installed and selected for the project (including the Gradle JVM in IntelliJ IDEA).
- **Google Chrome** installed.
- An internet connection for the Gradle Wrapper, dependencies, Allure provisioning, and the demo website.
- **Git** to clone the repository; IntelliJ IDEA is optional.

The repository includes the Gradle Wrapper, so **you do not need to install Gradle separately**. The tests create a `ChromeDriver` using Selenium Manager rather than requiring a manually configured ChromeDriver executable.

### Clone and run (Windows PowerShell)

```powershell
git clone https://github.com/johnsteve1091-dot/VLearningAutomation.git
cd VLearningAutomation

# Confirm the Java installation (should report Java 26)
java -version

# Check that the Gradle Wrapper can start
.\gradlew.bat --version

# Run all eight TestNG departments
.\gradlew.bat test
```

For **macOS/Linux**, use `./gradlew test` (or `bash gradlew test` if the wrapper lacks execute permission). Run `java -version` first on those systems as well.

**Check your setup:** `java -version` should show JDK 26. `gradlew --version` checks the Wrapper and the JVM running Gradle; the Java 26 toolchain required by `build.gradle` must also be available. If a Java toolchain error occurs, verify your JDK installation and the Gradle JVM selected in your IDE before rerunning tests.

To use **IntelliJ IDEA**, open the cloned repository as a Gradle project, select JDK 26 for the Project SDK and Gradle JVM, sync the Gradle project, then run the Gradle `test` task or the existing TestNG suite.

### Run a department or individual test

From the repository root, for example:

```powershell
# All Login tests
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.LoginTests"

# All E2E tests
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.E2ETests"

# One specific enrollment test method
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.duplicateEnrollmentShouldBePrevented"
```

Replace the class or method name with one from `src/test/java/vn/cybersoft/vlearning/tests/`. The configured full suite registers each of the eight test classes once.

**Important:** Tests interact with real demo-site state. Some tests register accounts, update profiles, enroll in courses, and cancel enrollments. The support classes implement targeted cleanup, but an interrupted or failed run may still require checking test data. Do not run account-mutating suites concurrently against the same accounts.

## Running, observing and reviewing tests

### Observe a live test run

Tests use a visible Chrome browser by default. When you run the suite on a desktop:

1. `BaseTest` launches a fresh Chrome window and opens the V Learning homepage before each test method.
2. Selenium interacts with the website: for example, navigating, filling forms, and checking page behavior. Watch the Chrome window to observe those actions.
3. After a test method, `BaseTest` captures a failure screenshot if possible, clears the session, and closes that Chrome window. This is expected behavior, not necessarily a failure.
4. Watch the terminal or the IntelliJ Gradle run window for build progress and errors. To request more detailed Gradle diagnostics, run:

```powershell
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.LoginTests" --info
```

A red `BUILD FAILED` message does **not** mean every test failed. Check the generated test report for individual passed/failed counts and failure details. If compilation, browser startup, or Gradle provisioning fails, some tests may never execute.

### Open the Gradle/TestNG HTML report

After tests have run, open the generated report from the project root.

```powershell
# Windows PowerShell
Start-Process ".\build\reports\tests\test\index.html"
```

Report path: `build/reports/tests/test/index.html`. In the report, review the summary and open the relevant test class or failed test to see its assertion message and stack trace.

### Generate and open the Allure report

The project uses **TestNG assertions**, **Log4j diagnostics**, and **Allure** annotations/attachments. `BaseTest` attempts to attach a screenshot for a failed test when the browser is available.

The Gradle `test` task runs `allureReport` as a finalizer, even when tests fail. To regenerate Allure from the results already collected, and **open the generated HTML report**:

```powershell
# Rebuild the report from existing Allure results (does not rerun tests)
.\gradlew.bat allureReport

# Open the self-contained report in your browser on Windows
Start-Process ".\build\reports\allure-report\allureReport\index.html"
```

Alternatively, launch Allure's local report viewer:

```powershell
.\gradlew.bat allureServe
```

`allureServe` opens a local web viewer; stop its server process with **Ctrl+C** when finished. The project configures Allure to generate a single-file HTML report.

- Raw Allure data and attachments: `build/allure-results/`
- Generated HTML directory: `build/reports/allure-report/allureReport/`
- Report to open: `build/reports/allure-report/allureReport/index.html`

For **macOS**, replace `Start-Process` with `open`; for **Linux desktops**, use `xdg-open` with the same relative HTML file path.

Inside Allure, open the test overview, inspect individual passed or failed test cases, read their failure details, and check available attachments such as **Failure Screenshot**. Not every failure has a screenshot (for example, if the browser could not start).

**Important:** Generate reports only after test results exist. Do **not** run `clean` between a test run and regenerating its report, because `clean` removes saved build outputs. If the Gradle Wrapper fails to start, neither TestNG nor Allure runs. Real application defects can leave tests red while report generation succeeds.

## Project structure

```text
VLearningAutomation/
├── build.gradle                         # Dependencies, Java toolchain, TestNG and Allure
├── settings.gradle
├── gradlew / gradlew.bat                # Gradle Wrapper launchers
├── gradle/wrapper/                      # Wrapper configuration and bootstrap JAR
├── src/
│   ├── main/java/vn/cybersoft/vlearning/
│   │   ├── core/                        # BasePage and common WebDriver interactions
│   │   └── pages/                       # Page Objects and course-card DOM models
│   └── test/
│       ├── java/vn/cybersoft/vlearning/
│       │   ├── base/                    # BaseTest browser lifecycle and evidence
│       │   ├── data/                    # Test inputs, fixtures and scenario support
│       │   └── tests/                   # Eight TestNG test classes
│       └── resources/testng.xml         # Suite registration
├── docs/
│   ├── KNOWN_DEFECTS.md                 # Confirmed defects and unresolved UI contracts
│   └── TEST_EXECUTION_REPORT.md        # Historical run results and failure analysis
└── README.md
```

**Separation of responsibilities:**

- **Test classes** express scenarios and assertions.
- **Page Objects** own UI locators, browser interactions, and page-specific observations.
- **Support/fixture classes** handle reusable preparation, test data, result collection, and targeted cleanup.
- **`BaseTest`** starts a fresh Chrome browser for each test method, opens the demo homepage, captures failure evidence when possible, and cleans up the browser session.

## Test results and known defects

The two reporting documents serve different purposes:

- **[Test Execution Report](docs/TEST_EXECUTION_REPORT.md):** Execution date, pass/fail totals for all eight departments, failed-test inventory, evidence locations, and the later targeted verification.
- **[Known Defects and Test Exceptions](docs/KNOWN_DEFECTS.md):** Confirmed website problems and UI-contract expectations that still need product confirmation.

The historical full regression on **18 October 2026** ran **65 tests: 51 passed, 14 failed, 0 skipped**, before the STAB-010 test correction. Three Course List tests were rerun afterward, but **no new full-suite outcome** has been established. Do not present this historical total as results from the latest merged code.

The project intentionally preserves failing assertions that may reveal real frontend defects. See the local Gradle/Allure reports for evidence from a specific execution.

## Project scope

This is an **educational UI automation project** exercising a third-party demo application. It demonstrates browser-based functional testing, Page Object separation, assertions, test-data handling, cleanup, and reporting. It does not claim ownership of the website or guarantee that its live content and behavior remain unchanged.

For implementation details, consult the source and [`build.gradle`](build.gradle). For historical execution evidence see [`docs/TEST_EXECUTION_REPORT.md`](docs/TEST_EXECUTION_REPORT.md); for confirmed defects and open specification questions see [`docs/KNOWN_DEFECTS.md`](docs/KNOWN_DEFECTS.md).
