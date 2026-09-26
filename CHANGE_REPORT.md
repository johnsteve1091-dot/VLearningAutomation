# VLearningAutomation v1.1.1 — Allure reporting fix

This revision fixes the reporting workflow in the supplied v1.1.0 project without
changing Selenium Page Objects, XPath locators, Register test data, or business assertions.

## What changed

- Simplified `build.gradle` to the supported Allure Gradle 4.3.0 integration.
- Kept Allure report runtime 3.14.3 and Allure Java/TestNG adapter 2.35.5.
- Kept AspectJ weaving disabled.
- Removed the unnecessary custom `allurerc.json` configuration.
- Kept raw Allure results in `build/allure-results`.
- Configured a single-file Allure HTML report.
- Made `test` finalize with `allureReport`, so report generation still runs when genuine
  test failures make the `test` task fail. The overall test failure remains a failure.
- Explicitly assigned `allureReport`, `allureServe`, and `downloadAllure` to the `allure`
  Gradle task group to make `tasks --group allure` useful.
- Kept `allureServe` independent from test execution so it can serve already-created results.
- Preserved the Log4j SLF4J2 bridge.
- Preserved failure-only Allure screenshot attachment in `BaseTest` before `driver.quit()`.
- Removed unused Selenium imports from `LoginTests` only.
- Updated README with the correct report workflow and paths.
- Project version: **1.1.0 → 1.1.1**.

## Important behavior

Known V Learning defects are still expected to produce FAILED tests. Nothing in this
revision catches, suppresses, skips, or rewrites those failures. Allure reports them.

## Commands

```powershell
.\gradlew.bat clean test
.\gradlew.bat allureReport
.\gradlew.bat allureServe
.\gradlew.bat tasks --all | Select-String allure
```

A normal `test` run automatically finalizes by generating `allureReport`, including after
test failure. `allureReport` and `allureServe` can also operate on existing raw results.

## Validation note

The project was statically checked in the packaging environment. Full Gradle dependency
resolution and browser execution could not be performed there because outbound dependency
downloads are unavailable. The Allure configuration follows the official Gradle plugin
4.3.0 task and TestNG-adapter model.
