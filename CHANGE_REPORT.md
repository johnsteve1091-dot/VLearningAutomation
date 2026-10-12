# VLearningAutomation — v1.1.16

## Baseline and scope

GitHub **main** was inspected first at commit
`964b5d9095d0b1a6bf539918ef82119e2efd9dd9`, version **1.1.15**.
All **42 current local files** matched that commit's Git blob hashes; no files
were added, missing or different. No newer local source was overwritten.
No applicable AGENTS.md was found. No Git push, merge or branch deletion occurred.

Project version increments exactly once: **1.1.15 → 1.1.16**.
Java 26, Gradle Wrapper 9.7.1, all dependencies and Allure configuration remain
unchanged. Exactly four Account tests were added: **41 → 45 TestNG methods**.

## Account implementation

| Test | Scenario and assertions |
| --- | --- |
| ACCOUNT-001 | Existing EnrollInput login, authenticated header avatar, Account path `/thongtincanhan`, visible root, active visible Personal Information and exact authenticated username. |
| ACCOUNT-002 | Generate account/email once before registration with unchanged RegisterSupport methods. Use existing valid registration fixtures and confirmed GP01, verify registration success, explicitly log in with that generated account and RegisterInput.Correct.PASSWORD, then compare username, email, full name, phone string, group and `Học viên`. |
| ACCOUNT-003 | Existing login and Account readiness, displayed/enabled Cập nhật, correct visible modal and exact heading, four visible enabled inputs with observed markup types, and visible Hoàn thành/Đóng buttons. No entry or submission. |
| ACCOUNT-004 | Existing login and modal, existing valid fixtures plus one generated email, exact input DOM-value comparisons, boolean password retention and password-type masking, then verified header dismissal and closed-modal assertion. No form submission or account update. |

All four methods are independent, with no dependsOnMethods or shared registration
fixture storage. AccountTests contains exactly four direct @Test methods, one
logger field and no helpers, nested types, locators, DOM parsing or randomizers.
Browser interactions remain in Page Objects. No additional support framework,
Account Page Object, profile-save testcase or deletion workflow was created.

Personal fields are read by actual labels and their span.ml-2 values within
`#infoPersonal.tabContent.active section.userInfo`. Readiness requires the expected
username and populated data, avoiding empty shells and another account's response.
Whitespace/NBSP are normalized; case, accents, punctuation and leading phone zeros
are retained. Sidebar identity, skills/statistics and My Courses are not asserted.

Modal locators are scoped to #myModal and reject ambiguous matches. ACCOUNT-003
reads the observed phone **DOM attribute** `type="phone"`; the browser's corresponding
DOM property falls back to text. ACCOUNT-004 reads input `value` properties,
checks masking before password entry, and keeps only password equality/masking
booleans in its snapshot. No raw password enters custom logs, failure messages or
Allure attachments. An input that becomes unmasked is cleared before reported
failure evidence. The modal header dismiss control was confirmed in the supplied
DevTools capture as `type="button"` with `data-dismiss="modal"`; it cannot submit
the form. Hoàn thành and the footer Đóng are read for visibility only.

RegisterPage gains only a success-dialog disappearance wait and normal
Register-to-Login navigation. All existing members are preserved. Public frontend
inspection confirmed a native enabled group select with GP01 available/selected,
and the visible enabled overlay-left **#signIn** button with text **Đăng nhập**;
clicking that button revealed the Login form. No credentials were entered and
no registration was submitted during this inspection.

ACCOUNT-002 fails setup before submission when the expected Group is unavailable
or selection differs. If registration authenticates automatically, existing
HomePage logout runs before explicit new-user login. The test's generated account
persists because the project has no verified account-deletion workflow. BaseTest
retains final browser/session cleanup for all four methods.

## File inventory and preservation

**One new project file, six modified files:**

| File | Reason |
| --- | --- |
| `src/test/java/vn/cybersoft/vlearning/tests/AccountTests.java` — added | Exactly ACCOUNT-001–004 with existing metadata, logs and compact non-secret evidence |
| `src/main/java/vn/cybersoft/vlearning/pages/AccountPage.java` | Add personal-information readers, route/readiness, scoped modal controls, input values and safe dismissal |
| `src/main/java/vn/cybersoft/vlearning/pages/RegisterPage.java` | Add explicit popup-close wait and verified frontend return to Login |
| `src/test/resources/testng.xml` | Add AccountTests exactly once and include Account in the test name; retain all five existing class entries |
| `build.gradle` | Project version only |
| `README.md` | Account contracts/commands, counts, architecture and current baseline/version |
| `CHANGE_REPORT.md` | This delivery and actual validation results |

The other **36 baseline files are byte-for-byte unchanged**, including all existing
LoginTests, RegisterTests, CourseListTests, CourseDetailTests and EnrollTests;
BaseTest, BasePage, HomePage, LoginPage, registration fixtures/randomizers,
EnrollInput, EnrollmentSupport, LoginSupport and Wrapper files.

AST/source comparison confirms **every existing AccountPage and RegisterPage
member is preserved verbatim**, including openMyCourses, refreshMyCourses,
requireCourseNotEnrolled, waitUntilCoursePresent, countExactCourseItems,
cancelCourseIfPresent and their private exact-title/cleanup helpers.
Existing enrollment ownership, cleanup and assertions were not edited.
Existing Course Detail, Course List and Allure contract sections of README are
preserved. No dependency changes or new APIs were added.

## Actual validation — 2026-10-11

### Compilation and browser execution

All eight required checks were attempted sequentially on the available Bash host:

```sh
bash gradlew compileJava compileTestJava --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.AccountTests.accountPageShouldOpenAfterLogin --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.AccountTests.newlyRegisteredAccountShouldShowCorrectProfile --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.AccountTests.updateProfileModalShouldOpen --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.AccountTests.updateProfileInputsShouldAcceptValues --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.AccountTests --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests --console=plain --no-daemon
bash gradlew test --console=plain --no-daemon
```

Every command exited **1 before any Gradle task started**. The Wrapper could not
download `https://downloads.gradle.org/distributions/gradle-9.7.1-bin.zip`:

```text
Exception in thread "main" java.net.SocketException: Network is unreachable
```

The available default Java is **17.0.20**. The project's **Java 26** requirement
was retained. No dependency/type compilation, TestNG body, Chrome setup or Allure
runtime was reached; no live Account or Enrollment result can be claimed.

| Test | Compilation | Execution | Result |
| --- | --- | --- | --- |
| ACCOUNT-001 | Attempted; Wrapper blocked | Isolated command attempted; no TestNG execution | BLOCKED |
| ACCOUNT-002 | Attempted; Wrapper blocked | Isolated command attempted; no TestNG execution | BLOCKED |
| ACCOUNT-003 | Attempted; Wrapper blocked | Isolated command attempted; no TestNG execution | BLOCKED |
| ACCOUNT-004 | Attempted; Wrapper blocked | Isolated command attempted; no TestNG execution | BLOCKED |
| Full AccountTests | Attempted; Wrapper blocked | Class command attempted; no TestNG execution | BLOCKED |
| Enrollment regression | Attempted; Wrapper blocked | Full EnrollTests attempted; no TestNG execution | BLOCKED |
| Complete 45-test suite | Attempted; Wrapper blocked | Full test command attempted; no TestNG execution | BLOCKED |

No authenticated live Account inspection, registration/profile consistency verdict,
live modal entry/dismissal or enrollment cleanup/removal verification occurred.
The provided Account DevTools screenshots ground the selectors; they are not
screenshots of this delivery's test execution. Registration-dialog auto-close and
auto-authentication behavior remain unverified live. No product defect was
confirmed and no live account state was mutated during validation.

### Separate source and offline checks

- All **30 Java sources** passed syntax parsing; 34 existing direct Course Detail
  XPaths compiled. This is not dependency/type or Java 26 compilation.
- AST/file checks verified four direct Account @Test methods, no helpers/nested
  types/locators, 45 total methods, untouched existing tests, additive Page Object
  members, unchanged fixtures/randomizers/dependencies and one suite entry.
- A dependency-stub slice compiled under the available Java 17 and passed
  **20 offline Account safety checks**. Actual AccountTests, AccountPage,
  RegisterPage, HomePage and registration randomizers ran against fake browser
  and framework dependencies. Checks covered exact registration-pair reuse and
  explicit registration-password login, auto-authentication/logout branch,
  unavailable/mismatched Group, email case mismatch, labeled/normalized data,
  leading zero, wrong-user/empty response rejection, modal/input uniqueness,
  raw phone attribute, DOM-value acceptance, dismissal after value mismatch,
  pre-entry masking, clearing an unmasked secret, failed dismissal and secret-free
  custom evidence. No network, backend registration or update occurred.
- The unchanged prior Enrollment safety fixture passed **23 offline checks**
  against the updated AccountPage. It exercised existing exact-title lookup,
  duplicate counts, owned-course protection, independent cleanup, removal failure,
  primary failure preservation and existing TC1/TC4/TC5/TC6/TC7 callers.

Offline checks are **not live Selenium/TestNG/Allure PASS results** and do not replace
the required JDK 26/dependency compilation or real browser regression run.
README contains the precise PowerShell commands for validation on a supported host.

## Delivery

`VLearningAutomation-v1.1.16-AccountTC1-TC4.zip` contains the complete **43-file**
project under one `VLearningAutomation` folder, ready to open in IntelliJ.
Validation fixtures, images, helper scripts, logs, IDE files, caches and build
output are excluded. Archive CRC and every source file's byte equality are checked
before delivery. Configure JDK 26 and Chrome and allow the existing Wrapper and
dependency downloads before executing the live validation commands.
