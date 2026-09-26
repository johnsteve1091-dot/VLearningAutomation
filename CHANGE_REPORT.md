# VLearningAutomation v1.1.2 — Login scenarios

Based on GitHub `main` inspected before editing:
`59c14d97f67334012239ea473114c12f06d9771d` (v1.1.1).
No changes were pushed to GitHub.

## Files changed

- `build.gradle`: only the project version, from `1.1.1` to `1.1.2`.
- `src/main/java/vn/cybersoft/vlearning/pages/LoginPage.java`: supplied Login
  locators, control checks, input actions, masking/copy checks and popup checks.
  Existing Register navigation is preserved.
- `src/test/java/vn/cybersoft/vlearning/tests/LoginTests.java`: replaces the two
  obsolete visibility tests with exactly five independent Login scenarios.
- `README.md`: version, scope and Login execution instructions.
- `CHANGE_REPORT.md`: this implementation and verification report.

Created:

- `src/test/java/vn/cybersoft/vlearning/data/login/LoginSupport.java`: fresh
  UUID-based synthetic invalid credentials for every LOG-005 execution.

## Exact test set and live results

| Java method | Exact TestNG / Allure name | Live result |
| --- | --- | --- |
| `loginFormShouldDisplayAllRequiredControls` | LOG-001 - Login form displays all required controls | BLOCKED / SKIPPED |
| `passwordShouldBeMasked` | LOG-002 - Password is masked | BLOCKED / SKIPPED |
| `passwordShouldNotBeCopyable` | LOG-003 - Password cannot be copied | BLOCKED / SKIPPED |
| `emptySubmissionShouldDisplayInvalidCredentialsPopup` | LOG-004 - Empty Login submission displays invalid-credentials popup | BLOCKED / SKIPPED |
| `randomInvalidCredentialsShouldDisplayInvalidCredentialsPopup` | LOG-005 - Random invalid credentials display invalid-credentials popup | BLOCKED / SKIPPED |

The real Login-only run discovered all five scenarios. Chrome failed in the existing
`BaseTest.setUp` with `SessionNotCreatedException: Chrome instance exited` in this
execution environment. TestNG reported one configuration failure and five skipped
scenarios. No scenario reached its live assertions. None is reported as PASS or as
an observed product failure. No Login product defect was established by this run.
Local execution with headed Chrome and a desktop clipboard remains necessary.

## Verification

- Java 26 compilation succeeded for all main and test sources, including Register.
  Package folders and imports are valid.
- All five supplied Login XPaths are exact; no alternate selectors were added.
  Each test navigates independently from HomePage to LoginPage through BaseTest.
- Raw Allure results were generated in `build/allure-results`, with the exact LOG
  titles, `Epic = V Learning`, `Feature = Login`, and requested Story/Description.
- The existing finalizer generated the single-file Allure HTML report at
  `build/reports/allure-report/allureReport/index.html`. Its summary records five
  skipped scenarios and one broken setup, preserving the actual failed run.
- Failure screenshot attachment remains active. A separate controlled WebDriver
  fixture verified that unchanged BaseTest attaches a valid PNG named
  `Failure Screenshot` to Allure before quitting and clears the driver afterward.
  The live startup failure produced no screenshot because no browser session existed.
- Separate fixtures verified that SoftAssert collects missing controls, an unmasked
  field fails LOG-002, missing/wrong popup titles fail, and inaccessible desktop
  clipboard access fails. These are implementation checks, not live-site passes.
- Synthetic JavaScript checks confirmed that the copy-event logic rejects an
  uncanceled event and a canceled event carrying the dummy password. They did not
  execute the website's actual handler or Chrome's native context menu.
- Temporary fixtures and generated outputs are excluded from the ZIP. A local
  Gradle test run regenerates test results and the Allure report.

## Copy behavior

LOG-003 uses a dummy password, selects it with Ctrl+A and confirms the full selection,
seeds a harmless clipboard sentinel, then attempts Ctrl+C through Selenium. It observes
the clipboard for one second and restores its prior contents. Clipboard access errors
fail rather than pass the test.

A separate cancelable `copy` event checks page cancellation and verifies that its
payload does not expose the dummy password. Missing cancellation is a separate
assertion failure; it alone does not establish that plaintext reached the clipboard.
Chrome's native right-click menu is not automated. Actual copy allowance remains a
failure; no assertions are suppressed or converted to passes.

## Preserved scope

- Java, Selenium, TestNG, Log4j, Gradle and Allure versions are unchanged.
- Allure configuration, results directory and test finalizer are unchanged.
- BaseTest, BasePage, HomePage, RegisterPage, all Register tests/data, resource
  configuration and the Gradle Wrapper are unchanged.
- No successful Login test, known valid credentials, new listener or reporting
  framework was added. Random passwords are not attached as report parameters.
- `accountInputShouldBeDisplayedOnLoginPage` and
  `passwordInputShouldBeDisplayedOnLoginPage` were removed as separate tests.

## Run locally

Use JDK 26 and installed Chrome. From the extracted project root in PowerShell:

```powershell
.\gradlew.bat clean test --tests "vn.cybersoft.vlearning.tests.LoginTests"
```

The existing Allure finalizer generates the report even when tests fail. To regenerate
from current raw results without rerunning tests, use `.\gradlew.bat allureReport`
without first running `clean`.
