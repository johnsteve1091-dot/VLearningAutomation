# VLearningAutomation v1.1.3 — Register matrix completion

Based on GitHub `main` inspected before editing:
`1e3317acfdef3ad2a3a7fbf89c6ef542f0bd511c` (v1.1.2).
All 23 repository files were fetched and checked against their Git blob hashes.
No changes were pushed to GitHub.

## Source-of-truth discrepancy

The prompt and baseline README describe five Login tests, but actual LoginTests.java
in the inspected commit contains four: required controls, masking, empty submission
and randomized invalid submission. Their current IDs are LOG-001 through LOG-004.
`passwordShouldNotBeCopyable` is absent, although its Page Object helpers remain.

The explicit instruction to preserve Login source was followed. LoginTests,
LoginPage and LoginSupport are byte-for-byte unchanged. The delivered project has
**4 Login + 14 Register = 18 active test methods**, not the requested 19. README now
states this verified count and the discrepancy. Restoring the missing Login case
would require a separate authorized Login change.

## Files changed

- `build.gradle`: only project version **1.1.2 → 1.1.3**.
- `src/main/java/vn/cybersoft/vlearning/pages/RegisterPage.java`: authorized title
  locator/visibility, Select-based Group actions/checks, immediate error-presence helpers.
- `src/test/java/vn/cybersoft/vlearning/data/register/RegisterInput.java`: deterministic
  boundary and rule-violation inputs in the existing Correct/Wrong categories;
  clarified Existing data prerequisites.
- `src/test/java/vn/cybersoft/vlearning/tests/RegisterTests.java`: completed the
  13 matrix cases, retained the combined smoke test, corrected IDs and priorities.
- `README.md`: version, actual counts, Register scope/order and fixture setup;
  existing Allure instructions preserved.
- `CHANGE_REPORT.md`: this implementation and verification report.

No new source files, data classes, packages or dependencies were added.
RegisterMessage and RegisterSupport remain unchanged.

## Exact Register test set and live results

| Java method | Exact TestNG / Allure name | Priority | Live result |
| --- | --- | ---: | --- |
| `registerFormShouldDisplayAllRequiredControls` | REG-001 - Register form displays all required controls | 1 | NOT RUN |
| `emptySubmissionShouldDisplayRequiredValidationMessages` | REG-003 - Empty Register submission displays all required warnings | 2 | NOT RUN |
| `invalidSubmissionShouldDisplayFormatValidationMessages` | REG-SMOKE-001 - Combined invalid Register submission displays validation warnings | 3 | NOT RUN |
| `accountLengthShouldValidateMinimumAndMaximumBoundaries` | REG-004 - Account length validation | 4 | NOT RUN |
| `registrationShouldFailWhenAccountAlreadyExists` | REG-005 - Registration rejects duplicate Account | 5 | NOT RUN |
| `fullNameShouldRejectNumbersAndSpecialCharacters` | REG-006 - Full Name rejects invalid characters | 6 | NOT RUN |
| `passwordShouldRejectRuleViolations` | REG-007 - Password validation rules | 7 | NOT RUN |
| `validPasswordShouldHaveNoValidationError` | REG-008 - Valid Password is accepted | 8 | NOT RUN |
| `emailShouldValidateInvalidAndValidFormats` | REG-009 - Email validation | 9 | NOT RUN |
| `registrationShouldFailWhenEmailAlreadyExists` | REG-010 - Registration rejects duplicate Email | 10 | NOT RUN |
| `phoneShouldValidateLengthAndCharacters` | REG-011 - Phone validation | 11 | NOT RUN |
| `courseGroupShouldBeSelectable` | REG-012 - Course Group can be selected | 12 | NOT RUN |
| `courseGroupShouldRejectArbitraryInput` | REG-013 - Course Group rejects arbitrary manual input | 13 | NOT RUN |
| `registrationShouldSucceedWithValidUniqueData` | REG-002 - Registration succeeds with valid unique data | 99 | NOT RUN |

There are exactly 14 Register methods. Each matrix ID occurs once; the combined
invalid-format regression keeps its original Java method and all assertions under
REG-SMOKE-001. REG-002 executes last with priority 99. No dependsOnMethods is used.

Live-site execution was not performed for this revision. No live PASS/FAIL results,
new product defect observations, or successful database registration are claimed.
Compile and controlled implementation checks are described separately below.

## Scenario changes and ID mapping

- REG-001 adds the exact authorized title locator and enables Group visibility,
  for eight controls checked with SoftAssert.
- Empty submission moves from old REG-002 to REG-003; all required-message
  assertions, including correct Password wording, remain unchanged.
- The original combined invalid test moves from old REG-003 to REG-SMOKE-001,
  priority 3. Password-warning semantics and the correct Phone spelling are preserved.
- REG-004 expands the old 17-character-only test to 1, 16 and 17 characters in one
  testcase. Invalid Full Name blocks registration at the accepted 16-character boundary.
- Duplicate Account moves from old REG-006 to REG-005. Duplicate Email moves from
  old REG-007 to REG-010. They use configured Existing values directly, with a
  generated unique opposite field, and retain fail-fast missing-data assertions.
- REG-006 adds number and special-character Name checks in one testcase.
- REG-007 adds all five Password rule violations in one testcase. It verifies
  enforcement by requiring a non-empty Password warning, without inventing messages.
- REG-008 proves the valid Password has no warning after the known Account blocker.
- REG-009 covers invalid then valid Email in one testcase with an Account blocker.
- REG-011 covers all four invalid Phone variations using generated valid Account
  and Email. It then switches Account to ACCOUNT_TOO_SHORT and verifies the valid
  10-digit Phone produces no warning after the Account warning is observed.
- REG-012 selects GP01 and checks selected text. REG-013 verifies a native select
  and that ABC123 is absent from the options. Neither submits registration.
- Successful registration moves from old REG-005 to REG-002, priority 99. Its
  business logic is preserved: unique Account/Email, assert success popup, then save.

## Data and Page Object additions

RegisterInput.Correct adds ACCOUNT_MAX_LENGTH (exactly 16 alphanumeric characters)
and GROUP. RegisterInput.Wrong adds FULL_NAME_WITH_SPECIAL_CHARACTER, five isolated
Password-rule values, four Phone-rule values and GROUP_ARBITRARY. Existing values
and nested Correct/Wrong/Existing categories are retained. No random boundary value
or extra Register data file is used.

RegisterPage adds title visibility, Select-based Group selection and retrieval,
native-select/option checks, and immediate Account/Password/Email/Phone error checks.
The immediate checks use findElements and displayed state; their callers first wait
for and assert a known unrelated validation warning. The original field, error,
Group and popup locators are unchanged. The sole new XPath is the authorized title.

REG-004/008/009 and the valid-Phone phase use unrelated blockers to prevent registration.
Name, Password and invalid-Phone rule tests use valid unrelated data as requested;
an application defect accepting the target input can create an unexpected account.
Missing Password/Phone warnings stop their collection with a failure. Expected
message defects are not accepted or converted to passes.

## Verification

- All main and test sources compiled successfully on Java 26 with the existing
  Gradle Wrapper and pinned dependencies, including all unchanged Login classes.
- Static checks verified package paths, exact locators, input lengths/composition,
  unique IDs, complete metadata, explicit priorities and the 14 Register methods.
- The empty, combined invalid and successful-registration method bodies were
  checked against current main; their existing business assertions remain intact.
- A temporary TestNG run invoked all 14 Register methods with a controlled WebDriver
  model. It verified collection iterations, blockers, Group interactions, fresh-driver
  cleanup and actual execution order with REG-002 last. Twelve methods passed the
  compliant model; the duplicate cases failed as expected because their configured
  database values remain placeholders. These are not live-site test results.
- The REG-011 submission data was checked: four invalid phones have valid unrelated
  fields; the fifth submission uses a valid Phone and the short Account blocker.
- Defective models confirmed that known warning defects, missing blockers, missing
  Password enforcement, an erroneous valid-Password error and an allowed ABC123
  option remain failures. The Phone typo fails all four message comparisons.
- BaseTest, including failure screenshot attachment before quit, is unchanged.
  Allure plugin/runtime/adapter versions, results directory, listener configuration
  and report finalizer are unchanged. No live Allure HTML report is claimed here.
- Temporary verification code/results and generated build output are excluded from
  the project ZIP. They do not add project tests or framework components.

## Preserved scope

LoginPage, LoginTests, LoginSupport, BasePage, BaseTest, HomePage, RegisterMessage,
RegisterSupport, TestNG/resources and Gradle Wrapper are unchanged. build.gradle
changes only its version field. No implicit waits, sleeps, parallelism, retries,
DataProvider, external data files, API/database integration, screenshot listeners
or additional reporting framework were added.

## Run locally

Open the extracted VLearningAutomation folder in IntelliJ; use JDK 26 for Project SDK
and Gradle JVM, install Chrome, and sync Gradle. Supply known-existing Account and
Email values in RegisterInput.Existing before running duplicate cases.

```powershell
.\gradlew.bat clean test --tests "vn.cybersoft.vlearning.tests.RegisterTests"
```

The existing Allure finalizer generates the report even when tests fail. To regenerate
from current raw results without rerunning tests, use `.\gradlew.bat allureReport`
without first running clean. See README for the unchanged report location and commands.
