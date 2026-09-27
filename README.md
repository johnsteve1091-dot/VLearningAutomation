# VLearningAutomation — v1.1.3

Cybersoft V Learning Selenium capstone with **4 Login tests and 14 Register tests (18 active test methods)**,
plus Allure reporting, readable LOG/REG titles, descriptions, Epic/Feature/Story
grouping, and a failure screenshot attached before Chrome closes.

Source-count note: the requested target was 5 Login + 14 Register = 19 tests, but
current GitHub `main` (`1e3317acfdef3ad2a3a7fbf89c6ef542f0bd511c`) contains only four
Login tests. `passwordShouldNotBeCopyable` is absent. This Register-only revision
preserves Login source and LOG IDs unchanged, so the actual total is 18.

Stack: **Java 26**, Gradle Wrapper **9.7.1**, Selenium **4.49.0**,
TestNG **7.12.0**, Log4j **2.26.1**, Chrome.
Reporting: Allure Gradle plugin **4.3.0**, Allure report runtime **3.14.3**,
Allure Java/TestNG adapter **2.35.5**. AspectJ weaving is disabled.

## Open and run

1. Extract the ZIP and open the `VLearningAutomation` folder in IntelliJ IDEA.
2. Set **Project SDK = JDK 26** and **Gradle JVM = JDK 26**.
3. Reload/sync Gradle.
4. Install Google Chrome. Selenium Manager resolves ChromeDriver automatically.
5. Run `src/test/resources/testng.xml`, `LoginTests`, or `RegisterTests` as usual.

### Normal test run

From PowerShell in the project root:

```powershell
.\gradlew.bat clean test
```

The project intentionally contains assertions that expose known V Learning defects,
so the Gradle build may correctly finish as **FAILED**. The test task is finalized by
`allureReport`, so the Allure report is still generated after the test run even when
one or more tests fail. Test failures are not suppressed or converted to passes.

### Open the Allure dashboard

The generated report is located at:

```text
build\reports\allure-report\allureReport\
```

The report is configured as a self-contained single-file report, so you can open its
`index.html` directly. You can also let Allure serve it locally:

```powershell
.\gradlew.bat allureServe
```

To regenerate the report from the raw results without rerunning tests:

```powershell
.\gradlew.bat allureReport
```

Raw result JSON files and failure screenshot attachments are written to:

```text
build\allure-results\
```

Do **not** run `clean` between a failed test run and a manual `allureReport` command,
because `clean` deletes the raw results. A normal `clean test` is fine because the
report is generated automatically after that test task finishes.

### Verify that the Allure Gradle tasks exist

Do not use this as the only check:

```powershell
.\gradlew.bat tasks --group allure
```

Task grouping is a presentation detail and was the source of earlier confusion.
This project explicitly assigns the main Allure tasks to the `allure` group, so that
command should now show them. The more general check is:

```powershell
.\gradlew.bat tasks --all | Select-String allure
```

Expected relevant tasks include:

```text
allureReport
allureServe
downloadAllure
```

## Reporting behavior

Allure shows:

- LOG/REG human-readable test titles
- long `@Description` text
- V Learning → Login/Register → Story grouping
- pass/fail status and duration
- TestNG assertion messages and stack traces
- a **Failure Screenshot** PNG attachment for a failed test when Chrome is available

`BaseTest` keeps `PageLoadStrategy.EAGER`, captures the screenshot before
`driver.quit()`, and still closes Chrome if screenshot capture itself fails.

## Current scope

- `BaseTest`: fresh Chrome per test, Homepage setup, EAGER page load, failure screenshot,
  and browser cleanup.
- `BasePage` plus Home/Login/Register Page Objects: existing explicit waits and page actions.
- `LoginTests`: required controls, password masking, empty Login and randomized invalid Login (unchanged from main).
- `LoginSupport`: new synthetic Account/Password values for each invalid Login attempt.
- `RegisterTests`: 13 authoritative matrix scenarios (REG-001 through REG-013), plus
  REG-SMOKE-001, the preserved combined invalid-validation regression test.
  Compound Account, Name, Password, Email and Phone cases remain one test per matrix ID.
  REG-002 successful registration runs last by design, with priority 99.
- `RegisterInput`, `RegisterMessage`, `RegisterSupport`: Register data, expected messages,
  generated unique values, and temporary created-registration state. Duplicate scenarios
  use manually configured `RegisterInput.Existing` values independently of REG-002.

Generated Gradle, test, and Allure output stays under `build/`, which is ignored by Git.

## Login-only execution

```powershell
.\gradlew.bat clean test --tests "vn.cybersoft.vlearning.tests.LoginTests"
```

The existing Allure finalizer also runs for this filtered command. There are exactly
four independent Login scenarios (LOG-001 through LOG-004); the old two separate
visibility tests were replaced. No successful Login or real credentials are used.

The current LoginPage retains its clipboard/copy-event helpers, but no active Login
`@Test` method invokes the password-copy check in the inspected main revision.
Restoring that fifth Login scenario requires a separate Login change.

## Register-only execution

Before duplicate tests run, supply known-existing database values for
`RegisterInput.Existing.ACCOUNT` and `RegisterInput.Existing.EMAIL` in
`src/test/java/vn/cybersoft/vlearning/data/register/RegisterInput.java`.
Unfilled placeholders deliberately fail REG-005 and REG-010. These tests run before
REG-002 and do not use the final success test as their prerequisite.

```powershell
.\gradlew.bat clean test --tests "vn.cybersoft.vlearning.tests.RegisterTests"
```

Execution order: REG-001, REG-003, REG-SMOKE-001, REG-004 through REG-013, then REG-002.
There are no method dependencies. REG-012/013 do not submit registration.

Valid Account-boundary, Password and Email checks use an unrelated invalid field to
block registration. REG-011 uses valid generated Account/Email for invalid Phone
variations, then switches Account to the invalid short value for its valid-Phone check.
Known warning-message defects remain failed assertions. See `CHANGE_REPORT.md` for
coverage, source preservation and verification details.
