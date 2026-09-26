# VLearningAutomation — v1.1.1

Cybersoft V Learning Selenium capstone with **2 Login tests and 7 Register tests**,
plus Allure reporting, readable LOG/REG titles, descriptions, Epic/Feature/Story
grouping, and a failure screenshot attached before Chrome closes.

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
- `LoginTests`: two Login form visibility tests.
- `RegisterTests`: seven Register scenarios covering form controls, empty/invalid submission,
  the 17-character Account boundary, valid registration, duplicate Account, and duplicate Email.
- `RegisterInput`, `RegisterMessage`, `RegisterSupport`: Register data, expected messages,
  generated unique values, and successful-registration reuse.

Generated Gradle, test, and Allure output stays under `build/`, which is ignored by Git.
