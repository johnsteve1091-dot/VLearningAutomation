# VLearningAutomation — v1.1.21

Cybersoft V Learning Selenium capstone. The active source contains **61 TestNG
methods**:

| Test class | Methods | Current scope |
| --- | ---: | --- |
| LoginTests | 5 | Existing Login scenarios, including successful login |
| RegisterTests | 14 | Existing Register scenarios and smoke test |
| CourseListTests | 10 | TC1 URL integrity, TC2 subCard structure, TC3 subCard content, TC4 main-card content |
| CourseDetailTests | 5 | TC1 core, TC2 curriculum, TC3 related-card content, TC4 navigation, TC5 frontend consistency |
| EnrollTests | 8 | ENROLL-001 authenticated enrollment; ENROLL-002 guest redirect; ENROLL-003 return to original Detail; ENROLL-004 refresh persistence; ENROLL-005 logout/login persistence; ENROLL-006 duplicate protection; ENROLL-007 two distinct courses; ENROLL-008 re-enrollment after cancellation |
| AccountTests | 16 | ACCOUNT-001 navigation; ACCOUNT-002 registered profile; ACCOUNT-003 modal controls; ACCOUNT-004 input retention; ACCOUNT-005 unchanged profile submission; ACCOUNT-006 changed profile persistence and restoration; ACCOUNT-007/009 rejection; ACCOUNT-008/010 isolated field validation; ACCOUNT-011/012 unsaved edits; ACCOUNT-013 guest blank page; ACCOUNT-014 account switching; ACCOUNT-015 stale profile actions; ACCOUNT-016 stale cancellation and owned cleanup |
| CancelTests | 3 | CANCEL-001 exact cancellation; CANCEL-002 preserve B and original Courses; CANCEL-003 cancellation persists after refresh |

The Course List TC1, TC2 and TC3 families each cover Homepage, Khóa học and
Danh mục separately. TC4 covers the Khóa học main cards. Existing source controls
their behavior and execution order.

Stack: Java **26**, Gradle Wrapper **9.7.1**, Selenium **4.49.0**, TestNG
**7.12.0**, Log4j **2.26.1**, Google Chrome. Allure Gradle plugin **4.3.0**,
report runtime **3.14.3**, Java/TestNG adapter **2.35.5**. AspectJ is disabled.

## Open and run

1. Extract the ZIP and open its `VLearningAutomation` folder in IntelliJ.
2. Select JDK 26 for the Project SDK and Gradle JVM, then sync Gradle.
3. Install Google Chrome. The existing setup uses Selenium Manager for ChromeDriver.
4. Run each new Cancellation/Enrollment method independently using the commands below,
   then CancelTests, EnrollTests and the full suite. Existing departments remain registered.

PowerShell, from the project root:

```powershell
.\gradlew.bat compileJava compileTestJava
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.duplicateEnrollmentShouldBePrevented"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.differentCoursesShouldCoexistInMyCourses"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.healthyCourseShouldEnrollAndAppearInMyCourses"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.guestEnrollmentShouldRedirectToLogin"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.guestLoginShouldReturnToSameCourseDetail"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.enrolledCourseShouldPersistAfterMyCoursesRefresh"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.enrolledCourseShouldPersistAfterLogoutAndLogin"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests"
```

Linux/macOS equivalents (invoking Bash also works when the extracted Wrapper lacks
the executable permission):

```sh
bash gradlew compileJava compileTestJava
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests.duplicateEnrollmentShouldBePrevented"
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests.differentCoursesShouldCoexistInMyCourses"
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests.healthyCourseShouldEnrollAndAppearInMyCourses"
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests.guestEnrollmentShouldRedirectToLogin"
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests.guestLoginShouldReturnToSameCourseDetail"
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests.enrolledCourseShouldPersistAfterMyCoursesRefresh"
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests.enrolledCourseShouldPersistAfterLogoutAndLogin"
bash gradlew test --tests "vn.cybersoft.vlearning.tests.EnrollTests"
```

Run Course Detail TC2 independently with:

```powershell
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseDetailTests.knownCourseShouldExposePopulatedCurriculum"
```

To run all 61 methods, use `src/test/resources/testng.xml` in IntelliJ or
`gradlew.bat clean test`. For another class, change the fully qualified class
name in `--tests`. Existing Login/Register tests include real authentication and
registration; their existing behavior and fixtures are preserved. ENROLL-001, TC4,
TC5, TC6, TC7, TC8 and CANCEL-001–003 mutate account state and must complete targeted cleanup before another run.

## Cancellation tests and ENROLL-008

| Case / group | Business assertion |
| --- | --- |
| CANCEL-001 / `cancel-tc1` | Enroll one absent Course; require the exact existing success title, one My Courses entry and an available exact cancellation control; cancel it and assert zero entries plus unchanged original Courses. |
| CANCEL-002 / `cancel-tc2` | Capture the account's original Course identities/counts, then enroll distinguishable A/B. After cancelling only A, require A=0, B=1 and every original Course count unchanged. Finally remove owned B (and A if needed). |
| CANCEL-003 / `cancel-tc3` | Enroll, verify one entry, cancel and require zero; refresh through `AccountPage.refreshMyCourses`, wait for the expected account response and active panel, and require zero again plus the original Course baseline. |
| ENROLL-008 / `enroll-tc8` | Enroll A, cancel A, return through its captured category/href/name, enroll the same A again, and require the exact success title and one entry. Finally remove owned A. |

Each method starts with BaseTest's fresh Chrome session and the existing confirmed
Enrollment account. `CancellationSupport` captures a read-only baseline before any
mutation. Frontend discovery excludes existing Course names and previously selected
names/hrefs; setup fails clearly if no eligible Course is found. No existing
Course is cancelled to make room. Existing exact normalized name matching is used
in My Courses; the captured href/name is used to revisit the original frontend card.
No Course IDs, new locators or cancellation-popup text are introduced.

Ownership is recorded immediately before **each** enrollment click, including the
second ENROLL-008 attempt, and is retained across cancellation. Every method uses
`finally` cleanup. Before recovery/navigation, a primary failure screenshot is
attached; safe logs and Allure snapshots record selected identities and Course counts.
Cleanup restores the existing account session when needed and delegates owned titles
to unchanged `EnrollmentSupport.finish`, which attempts each title independently.
Ambiguous exact matches are rejected. Pre-existing titles are never cleanup targets.
An unrelated snapshot/evidence failure does not skip exact-title cleanup of other
owned Courses. A refreshed final snapshot must contain zero owned entries and match
the original per-Course counts. Missing originals are reported, never recreated to
hide collateral damage. Cleanup failures are suppressed on a primary failure or fail
an otherwise successful test. Do not run concurrent account-mutating sessions.

A temporarily empty panel is insufficient evidence of absence: snapshots require
the expected account's populated response, the active Courses panel, and valid
rendered titles for every Course row. This reuses the existing Account readiness
contract. Live application behavior and cancellation persistence still require the
configured browser run; no new loading selector or API shortcut is assumed.

Run each new case independently (PowerShell):

```powershell
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CancelTests.enrolledCourseShouldBeCancelled"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CancelTests.cancellingOneCourseShouldPreserveOtherCourses"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CancelTests.cancelledCourseShouldRemainAbsentAfterRefresh"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests.cancelledCourseShouldAllowReenrollment"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CancelTests" --tests "vn.cybersoft.vlearning.tests.EnrollTests.cancelledCourseShouldAllowReenrollment"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests"
.\gradlew.bat test
```

Linux/macOS: replace `.\gradlew.bat` with `bash gradlew`. Groups are available to
TestNG; the commands above use existing Gradle method/class filtering without adding
new build configuration.

**v1.1.21 verification:** source/AST preservation, package/import references, suite
registration and ZIP checks passed. **56 focused offline checks passed** with
simulated dependencies/UI, including the four scenario bodies and cleanup failures.
The complete `compileJava compileTestJava` attempt was **BLOCKED** before compilation:
the Wrapper download failed with `Network is unreachable`. This environment also
lacks JDK 26 and Chrome. The four new live tests, Enrollment regression and full suite
were **not run**; no live PASS or application defect is claimed. Full details and
limitations are in `CHANGE_REPORT.md`.

## Account tests

Account contains sixteen independent methods in `AccountTests`; each starts with the fresh
Homepage browser supplied by BaseTest. The existing Enrollment account is used for ACCOUNT-001, ACCOUNT-003–005,
ACCOUNT-007–012, User A in ACCOUNT-014 and ACCOUNT-016. ACCOUNT-006 and ACCOUNT-015
use the dedicated `testergroup7` fixture, also User B in ACCOUNT-014.

| Test | Behavior and assertions |
| --- | --- |
| ACCOUNT-001 | Login, click the header avatar, verify `/thongtincanhan`, visible `section.infoPage`, active visible Personal Information panel, and the authenticated username. |
| ACCOUNT-002 | Generate one unique account/email pair with the unchanged RegisterSupport randomizers. Register with existing valid fixtures and GP01, verify success, explicitly log in with that new account's registration password, and compare all six profile values: username, email, full name, phone, group and `Học viên`. |
| ACCOUNT-003 | Click available Cập nhật, verify visible Edit Profile modal, exact heading, four visible enabled inputs and observed markup types, and displayed Hoàn thành/Đóng buttons. No data entry or submit. |
| ACCOUNT-004 | Enter valid fixtures and one generated email, compare input DOM values and password match/masking booleans, then verify the modal closes through the confirmed header dismiss button. Never click Hoàn thành or submit. |
| ACCOUNT-005 | Capture the current Full Name, Email and Phone; submit those same values with EnrollInput.PASSWORD unchanged; assert the exact update-success title, refresh and verify all three values remain unchanged. |
| ACCOUNT-006 | Prepare three different valid values, submit once with the dedicated account password unchanged, assert the exact success title, refresh and compare each field with both submitted and original values; restore and verify the original profile in finally. |
| ACCOUNT-007 | Verify four empty inputs, submit once, observe rejection with the modal open and no success, close and refresh, compare all three original values. No exact wording assertions. |
| ACCOUNT-008 | Four isolated submissions with one empty field and three original valid values. Check its exact required warning, close and refresh after each. Wrong Password wording remains a failure. |
| ACCOUNT-009 | Reuse the four invalid Register fixtures, verify entered values, submit once and verify rejection and unchanged originals after refresh. No exact wording assertions. |
| ACCOUNT-010 | Four isolated submissions with one invalid field and three original valid values. Verify its format warning, close and refresh after each. Incorrect Phone spelling remains a failure. |
| ACCOUNT-011 | Enter three different valid profile values and the existing password; Đóng; reopen and verify the unsaved draft is retained; Đóng again; refresh and compare the original stored profile. |
| ACCOUNT-012 | Four independent Name/Password/Email/Phone attempts. Change only one input, verify the other three are untouched, Đóng, reopen and check retention, Đóng and refresh before the next attempt. |
| ACCOUNT-013 | Preserve the corrected short flow: open Account as guest and assert body text is blank. |
| ACCOUNT-014 | Login A, verify labeled identity and capture profile; actual logout; Login B, verify B rather than A; refresh and compare B's six captured values. |
| ACCOUNT-015 | Seven independent same-browser two-tab arrangements: four inputs, Cập nhật, Hoàn thành, Đóng. Logout in B; observe selected action in A; freshly log in, refresh and verify the original three stored values. Restore detected mutations while retaining failure. |
| ACCOUNT-016 | Select an absent exact Course and enroll it for this test. Logout in B, attempt exact cancellation once in A, then verify count=1 after fresh login/refresh. Finally perform authorized cleanup and verify refreshed count=0. |

Profile values are read by their associated labels inside the active Personal
Information section. Whitespace/NBSP are normalized; case, accents, punctuation
and leading phone zeros remain meaningful. Readers wait for the expected user's
populated response. Decorative sidebar identity and learning statistics are not
authenticated identity. ACCOUNT-016 uses the existing My Courses exact-title operations. The raw phone markup is `type="phone"`; browsers expose its
unsupported type property as `text`, so ACCOUNT-003 reads the DOM attribute.

ACCOUNT-002 keeps its own generated inputs and does not use cross-test static
registration storage. If registration authenticates automatically, the existing
application logout runs before explicit login. The expected Group must be available
and selected; setup fails before registration otherwise. The success-dialog close
wait and normal Register-to-Login overlay button are small additions to RegisterPage.
A created account persists because no verified deletion workflow exists.

ACCOUNT-004 checks masking before typing and exposes no raw password in snapshots,
assertion messages, logs or custom attachments. The header dismiss button is confirmed
as `type="button"` with `data-dismiss="modal"`, so dismissal does not submit the form.
BaseTest retains normal session cleanup and browser quit for all sixteen cases.

ACCOUNT-005/006 read only Full Name, Email and Phone into a password-free snapshot.
They reuse the existing modal entry operation and verify `Cập nhật thành công` in
the visible SweetAlert. Popup disappearance and an unobstructed Update control
are explicitly awaited before refreshing. There are no JavaScript clicks or sleeps.

ACCOUNT-006 reuses `RegisterSupport.generateUniqueEmail()` once and the existing
valid name/phone fixtures. Small letter/space name and ten-digit phone alternatives
are selected only when the canonical fixture equals the original field. The
original password is supplied unchanged for update and restoration; it is never
generated, captured in profile snapshots or written to custom evidence.

`AccountSupport` restores after a submission attempt, including business failures.
It refreshes and verifies the original three fields, recovers an expired login
through existing Page Objects when necessary. Its profile-restoration operation never touches courses. A cleanup
failure is attached to the original failure; if business assertions passed, cleanup
failure fails the test separately. No profile cleanup is added to ACCOUNT-005.

ACCOUNT-007–010 all reuse `EnrollInput.ACCOUNT` / `EnrollInput.PASSWORD` and capture
only the original Full Name, Email and Phone from the live Account page. ACCOUNT-009/010
reuse `RegisterInput.Wrong`; expected warnings remain in the unchanged `RegisterMessage`.
The modal reader associates each error with its immediately preceding input.
Invalid responses are polled for at least two seconds with 500 ms of stable field
state, within the existing 15-second explicit-wait limit. Observed popup titles
remain in the evidence even if the popup disappears. These are bounded UI observations,
not a guarantee that a server can never respond later.

The exact supplied ACCOUNT-008 and ACCOUNT-010 corrections are included: four
independent single-empty/single-invalid-field attempts, with close and refresh after
each. Their existing method names remain for command compatibility. They no longer
use cumulative corrections or end with a successful update. Existing expected
Password wording and Phone spelling remain unchanged, and mismatches remain failures.

ACCOUNT-011/012 reuse the existing valid replacement generator and actual original
profile snapshots. They click the footer **Đóng**, never Hoàn thành, the header X,
Escape or refresh to dismiss the modal. A close observation waits for the modal to
be hidden and Cập nhật to be interactable, while retaining observed popup titles.
Each individual-field attempt begins after refresh with independently checked input
state. Password comparisons remain inside AccountPage; attachments contain only
match/masking/empty booleans. No intentional Password prefill exception is assumed.
Retention mismatches remain failures even when later attempts complete. Cleanup
checks actual persisted values and invokes existing UI restoration only if it
finds a mutation; normal runs submit no update.

ACCOUNT-011 retains its existing method name `unsavedProfileChangesShouldBeDiscarded`,
but the corrected GitHub assertions require draft retention. ACCOUNT-012's corrected
method is `individualUnsavedProfileChangesShouldBeRetained`. Both are preserved verbatim.
ACCOUNT-013 is the corrected immediate blank-body check; no old HTTP/redirect polling
has been reintroduced.

ACCOUNT-014 compares labeled account identifiers. It does not assume the users must
have different names, emails or phone numbers, and never edits either profile.

ACCOUNT-015 runs seven stages in enum order: Full Name, Password, Email, Phone,
Cập nhật, Hoàn thành, Đóng. Every stage starts with a fresh normal login and original
stored values, opens a second tab with `WindowType.TAB`, verifies the shared account,
and uses application logout there. Tab A is untouched until the selected action.
For input stages, the other three fields are prepared before logout. Only a complete,
valid draft may then be submitted. The dedicated Hoàn thành draft changes exactly
one profile field; the existing password is always unchanged. Cập nhật only checks
the stale button/modal behavior, and Đóng only checks closing an unsaved draft.

The new stale-page observer polls for at most two seconds and ends immediately on
blank body text. Evidence distinguishes a blank screen before the action from blank
rendering afterward. It records control/modal state, known validation/popup text,
URL transitions and whether the action was reached. No invisible form is searched
after a blank result. A blank UI is not proof of server-side token revocation: every
stage still performs fresh-login/refresh profile verification. Only detected stored
changes invoke existing restoration; successful restoration does not erase failure.

ACCOUNT-016 tries an existing suitable selection and, if already enrolled, one
different candidate through the existing selector. If both are owned already, setup
fails without modifying them. Ownership starts only after an absence guard,
immediately before enrollment. Stale cancellation clicks only a unique exact-title
course control once, without waiting for removal. The final count after fresh login
must be one. Cleanup reauthenticates the original owner, reuses `EnrollmentSupport.finish`
when the owned Course remains, and checks zero again after refresh. An already absent
Course is never reenrolled; the original security failure remains. Evidence failures
must not prevent data cleanup. Secondary tabs close even after failures.

PowerShell, from the project root:

```powershell
.\gradlew.bat compileJava compileTestJava
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.accountPageShouldOpenAfterLogin"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.newlyRegisteredAccountShouldShowCorrectProfile"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.updateProfileModalShouldOpen"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.updateProfileInputsShouldAcceptValues"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.updateProfileShouldSucceedWithExistingInformation"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.updateProfileShouldPersistChangedInformation"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.emptyProfileUpdateShouldBeRejected"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.requiredProfileWarningsShouldClearIncrementally"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.invalidProfileUpdateShouldBeRejected"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.formatWarningsShouldClearIncrementally"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.unsavedProfileChangesShouldBeDiscarded"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.individualUnsavedProfileChangesShouldBeRetained"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.guestShouldNotAccessPersonalInformation"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.accountSwitchShouldDisplayCorrectUser"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.loggedOutTabShouldNotUpdateProfile"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests.loggedOutTabShouldNotCancelEnrolledCourse"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.AccountTests"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.EnrollTests"
.\gradlew.bat test
```

On Linux/macOS, replace `.\gradlew.bat` with `bash gradlew`.
Historical v1.1.20 validation: the wrapper compilation attempt was **blocked** by an unreachable Gradle
download. This environment also lacks Java 26 and Chrome. No new test or Account
suite was executed, and no live mutation, restoration or enrollment cleanup result
is claimed. Syntax/preservation and 113 offline checks passed using simulated
dependencies/UI. These are not a full Gradle build or live Selenium PASS.
The current v1.1.21 validation is summarized in the Cancellation section and `CHANGE_REPORT.md`.

## Enrollment tests

Enrollment contains eight independent tests: ENROLL-001 verifies authenticated
enrollment, exact My Courses identity and targeted cancellation; ENROLL-002 verifies
the guest redirect to `/login`; ENROLL-003 verifies return to the original Course
Detail after login; ENROLL-004 verifies persistence after My Courses refresh;
ENROLL-005 verifies persistence after real application logout and login again;
ENROLL-006 verifies duplicate enrollment protection and one exact entry;
ENROLL-007 verifies that two different Courses coexist in My Courses;
ENROLL-008 verifies the same Course can be enrolled again after successful cancellation.
BaseTest owns final browser/session cleanup for every test.
See `CHANGE_REPORT.md` for the source comparison and actual validation results.

### ENROLL-001 — Successful enrollment

**ENROLL-001 — Successful course enrollment**, group `enroll-tc1`, covers authenticated
enrollment. It uses the confirmed existing Login credentials in
`data/enroll/EnrollInput`; the unrelated registration-form sample password is not used.
Existing Login/Register source remains unchanged.

The test logs in through the existing Login Page Object, waits for the authenticated
header, opens the first discovered Danh mục through the normal menu, and captures the
first usable rendered card's nonblank **`span.stikerCard`** name and Detail href.
It does not inspect all Course fields, traverse pagination or sweep other categories.
The existing card parser is preserved; its h6 content is not substituted for the
Enrollment identity bridge.

Before any enrollment click, a read-only visit to My Courses confirms that this exact
name is not already enrolled. If it exists, setup fails and leaves it untouched.
The test returns through the same category menu, rediscovers the selected card by
href and name, clicks its main image and waits for the advertised Detail URL, visible
root and clickable existing Đăng ký button. No title, Course ID or category ID is a
hardcoded test expectation.

There are exactly two business assertions:

1. The visible SweetAlert title is exactly **`Đăng kí thành công`**, with `kí` and
   no period. The test waits for this dialog to close before account navigation.
2. My Courses contains one visible item whose h6 equals the captured `.stikerCard`
   name after whitespace normalization. Case, accents and quotation marks are
   preserved; names are compared as Java strings, not concatenated into XPath.

The `finally` path runs after any enrollment attempt, including an assertion failure
after the server may have accepted the action. Because the exact name was absent
before the attempt, cleanup can target this run's course without deleting an existing
enrollment. It clicks Hủy khóa học only inside the unique exact-title item and waits
until that same item is absent. Multiple same-title matches cause an explicit failure
instead of an ambiguous cancellation. Other account courses are never cancelled.

EnrollmentSupport handles targeted cancellation only. BaseTest clears browser
storage/cookies and quits Chrome after each test; application logout is not an
Enrollment cleanup requirement.

If a business assertion already failed, cleanup failures are logged and added
as suppressed exceptions to that original failure. If business assertions passed but
cleanup fails, the testcase fails. Allure receives selected-course and popup
evidence, a cleanup summary, and the original failing screen before cleanup
when screenshot capture is available. No Cancel Course testcase is added.

### ENROLL-002 — Guest redirect

**ENROLL-002 — Guest enrollment redirects to Login**, group `enroll-tc2`, covers
legacy TC_ENR_002. BaseTest supplies a fresh guest browser on the Homepage.
The test reuses `goToFirstCourseCategory()`, `selectCourseForEnrollment()` and
`openCourseForEnrollment(...)` on the same category Page Object, then clicks the
existing Đăng ký button once. It performs exactly one business assertion:
`LoginPage.waitUntilAtLoginPage()` confirms that the URL path reaches `/login`
using the existing explicit wait. Query parameters and fragments do not affect
the path comparison; Login form fields are not inspected.

TC2 performs no login, My Courses visit, enrollment-success popup check, cancellation
or application logout. Its implementation is unchanged in this release.

### ENROLL-003 — Login returns to the original Course Detail

**ENROLL-003 — Login returns guest to same Course Detail**, group `enroll-tc3`,
uses the same guest category/card path as TC2. After Detail readiness, it captures
the actual browser URL, then clicks Đăng ký once. `waitUntilAtLoginPage()` is a
required setup guard, not another business assertion. The test enters the existing
EnrollInput credentials through LoginPage and submits login.

Its one business assertion calls `CourseDetailPage.waitUntilAtCourseUrl(...)` with
the captured URL. That small explicit wait reuses `sameBrowserUrl(...)`, retaining
the project's normalized URI equality behavior. A concise completion log includes
the original and resulting URLs; the assertion message includes the actual URL
if the expected return times out.

The test ends after confirming the return. It performs no second Đăng ký click,
success-popup assertion, enrollment, My Courses visit, cancellation or application
logout. It does not call EnrollmentSupport; BaseTest performs normal session teardown.
See `CHANGE_REPORT.md` for this release's actual validation results.

### ENROLL-004 — Persistence after My Courses refresh

**ENROLL-004 — Enrollment persists after My Courses refresh**, group `enroll-tc4`,
maps to **TC_ENR_008**. It independently logs in, selects one healthy Course through
the existing category/card path, and uses `requireCourseNotEnrolled(...)` before
clicking Đăng ký once. Existing enrollments remain untouched if this setup guard fails.

The exact success title from `EnrollInput.SUCCESS_TITLE` and initial exact-title
My Courses presence are setup guards that throw on failure. The test then calls
`AccountPage.refreshMyCourses(expectedAccount)`: normal WebDriver refresh followed
by the existing account-data wait and `openMyCourses(...)`. This restores the Khóa học
tab even if refresh resets it to personal information. Its one business assertion
verifies that the same captured `.stikerCard` title remains after refresh.

### ENROLL-005 — Persistence after application logout and login

**ENROLL-005 — Enrollment persists after logout and login**, group `enroll-tc5`,
maps to **TC_ENR_009**. It independently performs the same guarded enrollment setup
and confirms initial My Courses presence. It then reuses the unchanged
`HomePage.logoutIfAuthenticated()`: hover the authenticated avatar, click the
existing `span.logout` normally, and wait for the Login control with no visible
account link. That guest-state wait is a setup guard.

The test opens Login through the frontend, logs into the same EnrollInput account,
waits for authenticated state, and reopens My Courses. Its one business assertion
verifies that the exact selected Course remains after this session transition.
Application logout happens once as the TC5 scenario step, never after cleanup.

Both persistence tests call the shared failure-safe cleanup in `finally`. It cancels
only the owned exact-title Course and verifies removal, preserving any primary
failure and suppressing cleanup errors. Cleanup failure after scenario success fails
the test. If authentication was lost after enrollment, TC4/TC5 call
`LoginSupport.restoreSessionForCleanup(...)` through Page Objects before cancellation.
This preserves their existing recovery behavior while keeping login outside
EnrollmentSupport. Those existing TC4/TC5 methods and all other ENROLL-001–007
implementations are unchanged in v1.1.21. Generic cleanup never logs in,
logs out, clears browser storage or quits the driver.

### ENROLL-006 — Duplicate enrollment protection

**ENROLL-006 — Duplicate course enrollment is prevented**, group `enroll-tc6`,
maps to **TC_ENR_010 + TC_ENR_011**. It selects one healthy Course using the
existing card path and guards that exact name is absent. The first enrollment's
success title and exact My Courses presence establish setup. The test returns to
the same captured category/card href and name, then clicks Đăng ký a second time.

Its two business assertions check the exact current duplicate title
**`Đã đăng ký khóa học này rồi!`** from `EnrollInput.DUPLICATE_TITLE`, then that
the selected Course occurs **exactly once** in My Courses after the popup closes.
`AccountPage.countExactCourseItems(...)` counts items with an exact normalized
visible h6 title, including multiple matches; it never substitutes the account's
total course count. The existing generic popup reader/explicit close wait is reused.

### ENROLL-007 — Two different Courses coexist

**ENROLL-007 — Multiple different courses can be enrolled**, group `enroll-tc7`,
maps to **TC_ENR_006** and uses **two Courses**. It guards A absent, enrolls A and
confirms the success title as setup. The shared card selector then skips A's href
and normalized title to find the first healthy B with a distinct identity. A
different exact title is required because My Courses exposes names rather than IDs.

Selection tries the same rendered category and at most one other discovered
category, without pagination or a catalog sweep. Failure to find B in that bounded
search fails setup clearly. B's absence is checked independently; A's existing
enrollment is expected. After B's success setup guard and popup close, exactly
two business assertions verify exact A presence and exact B presence together.

TC6 owns one attempted Course; TC7 adds each name to its owned list immediately
before its respective enrollment click, after its absence guard. Shared cleanup
attempts every owned exact-title cancellation independently and verifies removal.
A failure cleaning A does not skip B. Pre-existing and unrelated enrollments are
never cancelled. Any cleanup error is suppressed on the original failure, or fails
an otherwise successful scenario. No cleanup login or application logout is added
to TC6/TC7; BaseTest owns final session teardown.

### ENROLL-008 — Re-enrollment after successful cancellation

`cancelledCourseShouldAllowReenrollment`, group `enroll-tc8`, retains one captured
Course identity and ownership across both enrollment attempts. It asserts the exact
existing enrollment-success popup and one My Courses entry for each successful
enrollment, with confirmed cancellation and zero entries in between. Its final
cleanup removes only this test-owned Course and verifies the original baseline.
See the Cancellation section for independent run commands and shared cleanup details.
**ENROLL-006 is unchanged:** its duplicate attempt happens while enrollment still exists;
ENROLL-008 attempts enrollment after that enrollment has been removed.

## Course Detail TC1

**COURSE-DETAIL-001 — Course Detail Core Contract** is one independent TestNG method
in group `course-detail-tc1`. After the existing Homepage setup, it opens the known
specimen directly:

`https://demo2.cybersoft.edu.vn/chitiet/.100`

The dot in `.100` is intentional. This test implements the supplied 27 core
requirements and consolidates **TC_CD_001–TC_CD_017** into one result:

| Legacy cases | Core information |
| --- | --- |
| TC_CD_001–003 | Visible detail root/content, fixed title/subtitle, nonblank course name |
| TC_CD_004–007 | Instructor name/avatar, field/category, rendered rating icons, rating/review text |
| TC_CD_008–010 | Description, learning-objectives heading/content, loaded course image |
| TC_CD_011–016 | Nonblank price, enrollments, duration, lessons, videos and level |
| TC_CD_017 | Displayed and enabled Đăng ký button |

Both images must be displayed, have a nonblank `src`, and satisfy the browser's
`complete && naturalWidth > 0` loading state. No separate HTTP image request is made.
Sidebar values are found by their labels, rather than row indexes. Course-specific
names, amounts and counts are read from the page and are not hardcoded expectations.

The Page Object uses the observed `detailCoures` and `instrutorTitle` spellings.
One shared 15-second
explicit-wait budget covers the root, asynchronous field content and image
completion. Missing fields remain in the captured snapshot when that budget expires,
so the test can collect all ordinary field problems and fail once. Unusable sessions,
WebDriver command failures and persistently unobservable DOM state remain
infrastructure failures.

The button's displayed/enabled state is checked without clicking or hovering it.
There is no enrollment, curriculum traversal, related-card inspection, responsive
layout check, API comparison or catalog/detail sweep in this testcase.

## Course Detail TC2

**COURSE-DETAIL-002 — Course Curriculum Contract**, group `course-detail-tc2`,
covers only **TC_CD_019 and TC_CD_020**. It independently opens the same known
specimen through the existing navigation method; it does not depend on TC1.

The test checks the displayed curriculum and exact `Nội dung khóa học` heading,
at least one module, each module's content/header structure and readable title,
lesson containers, at least one lesson across the whole curriculum, and every
discovered lesson's visible nonblank title. A module may contain zero lessons if
the complete curriculum still contains lessons. Counts, titles, numbering and
ordering are not fixed expectations.

Module and lesson lookup uses relative locators. Immutable snapshots retain
strings, booleans and indexes, never WebElements. The existing explicit-wait pattern
is reused with two matching ready observations. If ordinary missing/blank content
persists through the wait, the latest complete observation is inspected and all
actionable defects are collected before one final assertion. A missing parent
does not generate redundant failures for unavailable children. Available lessons
are still inspected when another part of their module is defective.

TC2 does not require durations or preview buttons and performs no preview/media
interaction. TC_CD_018 (register-button hover), enrollment and related courses are
outside its scope.

## Course Detail TC3, TC4 and TC5

These recommendations are inside Course Detail, so TC3 and TC4 remain in
CourseDetailTests. TC5 then checks whether Course-card identity rendered on each
frontend card surface corresponds to the Course Detail identity for that advertised
href. All three reuse the existing Course-card parser/snapshot model.

**COURSE-DETAIL-003 — Related Courses Contract** (`course-detail-tc3`) covers
**TC_CD_021–TC_CD_030**. It checks the displayed section/heading, one to four
recommendation cards, every card's nonblank name and valid `/chitiet/<non-empty-id>`
href, course/instructor image health, instructor name and original/current price.
A rendered discount must be nonblank and its original price must have the shared
line-through decoration. No sale is required. A DOM-present subCard must have a
nonblank description; specifications are captured without fixed value expectations.
TC3 performs no hover, card click or destination navigation. It collects all ordinary
field problems and fails once.

**COURSE-DETAIL-004 — Related Course Navigation** (`course-detail-tc4`) covers only
navigation. Starting from `https://demo2.cybersoft.edu.vn/chitiet/.100`, it captures
only each rendered related card's immutable 1-based index and href before interaction.
For every card it:

1. Reopens the known source and rediscovers the card from the fresh DOM.
2. Clicks the normal main-card path and verifies the resulting browser URL matches the
   captured href and a Course Detail root displays.
3. Reopens the source, rediscovers the card again, hovers only to expose the existing
   `XEM CHI TIẾT` control, clicks it normally, and verifies the same URL/root contract.

TC4 does **not** compare Course names, instructors, prices, ratings, descriptions,
images, specifications, curriculum or any other Course data. Hover is only an
interaction mechanic. No WebElement survives navigation, no Course ID is required to
be numeric, no `Thread.sleep()` or JavaScript click is used, and failures are limited
to actual navigation/click/root problems.

**COURSE-DETAIL-005 — Course Card and Detail Frontend Consistency**
(`course-detail-tc5`) is one independent frontend consistency testcase. It visits
these distinct card surfaces:

- Homepage Course cards.
- The initial rendered `/khoahoc` page only; TC5 does not traverse its pagination.
- Every Danh mục URL discovered through the existing Homepage support.
- Related Courses from the known `/chitiet/.100` source, one depth only.

For each source instance TC5 captures up to the first five valid immutable card
snapshots in natural rendered order, then compares **candidate 1** against Course
Detail. A healthy representative ends checking for that source immediately. If the
first representative fails, TC5 retains that failure and inspects candidates 2–5 only
for diagnosis. Sampling is deterministic and never becomes an exhaustive catalog
audit.

TC5 compares only shared frontend identity information whose equivalence is clear:
main-card Course name and instructor name, plus subCard Course name/instructor when a
subCard exists. Course-name comparison reuses the existing ellipsis-aware matcher;
instructor names use normalized whitespace equality. TC5 deliberately does not map
hours to duration, weeks to lesson count, card level to unrelated detail metadata,
raw image URLs, descriptions, prices, ratings or review counts without a proven shared
business contract. It uses direct `driver.get(candidate.href())` after source snapshots
are captured, so click/back recovery is not part of this consistency test. Detail
identity snapshots may be reused when multiple sources advertise the same href.

Run the three tests independently:

```powershell
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseDetailTests.knownCourseShouldExposeValidRelatedCourses"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseDetailTests.relatedCoursesShouldNavigateToAdvertisedHref"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseDetailTests.courseCardsShouldMatchTheirCourseDetailIdentity"
```

No exhaustive API validation, recursive Related Course traversal or sixth Course
Detail testcase is included.

## Architecture

**Permanent rule: test classes contain testcase methods only.** Reusable
helper/validation/reporting logic belongs in support classes. Browser interaction
belongs in Page Objects. A test arranges the scenario, invokes Page Objects/support,
logs the high-level result, attaches Allure evidence and performs its final assertion.
Do not inline helper machinery into test methods to bypass this rule.

All test classes must follow this rule, including `EnrollTests`, `CancelTests`
and future Profile tests. Test classes must not declare additional
helper/utility methods of any visibility or nested records, classes or enums.
Constants, logger fields, annotations and imports are allowed. Existing Login,
Register and Course List test classes are unchanged in this release.

- `BaseTest`: fresh Chrome for each method, EAGER loading, Homepage setup, failure
  screenshot before session cleanup and browser quit.
- `BasePage`: existing explicit-wait and element helpers.
- `HomePage`, `CourseListPage`, `CourseCategoryPage`: existing navigation,
  card extraction and catalog traversal.
- `CourseCardDomReader`, `CourseCardSnapshot`: existing Course card capture/model.
- `CourseDetailPage`: all Course Detail locators, known URL navigation, core/image
  capture, scoped curriculum/module/lesson snapshots, minimal identity capture for
  TC5, and href-only related-card navigation support for TC4.
- `CourseDetailTests`: exactly five declared methods, each an independent `@Test`;
  scenario setup, support invocation, concise logs, one Allure summary and the final
  assertion. It has no helpers or nested types.
- `CourseDetailContractSupport`: TC1 core, TC2 curriculum and TC3 related-card
  snapshot validation, issue collection, result models and summary generation.
- `CourseDetailNavigationSupport`: TC4 orchestration through existing Page Object
  navigation methods, href/root validation, issue collection and navigation evidence.
  It performs no Course-name or instructor comparison.
- `CourseDetailConsistencySupport`: TC5 source orchestration, deterministic
  first-valid sampling (1 normally, up to 5 only after representative failure),
  Course-name/instructor comparisons, href-keyed Detail caching, source/defect models
  and consistency summary generation. It receives the current WebDriver; Page
  Objects continue to own DOM discovery and capture.
- `EnrollTests`: eight declared `@Test` methods; TC1 has two business assertions,
  TC2 has one Login-redirect assertion, TC3 one return-URL assertion, and TC4/TC5
  one persistence assertion each. TC6 checks the duplicate popup and exact count; TC7
  checks both selected Courses. TC8 checks successful enrollment → cancellation →
  successful re-enrollment of the same captured Course and exact occurrence counts.
  No helpers, nested types, raw locators or DOM parsing.
- `LoginPage.waitUntilAtLoginPage`: explicit wait for the `/login` URL path only.
- `CourseDetailPage.waitUntilAtCourseUrl`: explicit wait reusing `sameBrowserUrl`.
- `AccountTests`: exactly sixteen independent direct `@Test` methods with scenario
  orchestration, assertions, compact logs and non-secret evidence; no helpers or locators.
- `AccountPage`: labeled personal-information snapshots, Account route/readiness,
  scoped Edit Profile controls, input DOM values and safe dismissal. Every existing
  My Courses selection/refresh, exact-title lookup and cancellation member is preserved.
  The new read-only `captureMyCourseCounts` returns an immutable exact-title/count map
  after the expected account response and active Courses panel are ready.
- `HomePage`, `CourseCategoryPage`, `CourseDetailPage`: additive header/category
  navigation, Enrollment selection and popup operations; existing methods are preserved.
- `EnrollmentSupport`: existing failure-safe targeted cancellation, evidence and error
  preservation; unchanged. Callers use `LoginSupport` for session recovery when needed.
  BaseTest owns final browser/session cleanup.
- `CancellationSupport`: per-test read-only baseline, selection excluding existing Courses,
  ownership before each enrollment attempt, pre-cleanup evidence and authenticated
  targeted cleanup through unchanged `EnrollmentSupport.finish`.
- `CancelTests`: exactly three independent public `@Test` scenario methods; no helpers,
  models, locators or low-level browser operations.
- `EnrollInput`: the small Enrollment credential/expected-message holder.
- `CourseCardDomReader`: reused card parsing; existing Course List calls retain their
  behavior.
- `CourseSubCardContentSupport.courseNamesMatch`: existing ellipsis-aware Course-name
  comparison reused without changing its algorithm.
- `RegisterPage`: additive success-dialog close wait and normal Register-to-Login
  navigation; all existing members remain unchanged. Existing LoginPage methods, registration
  fixtures/randomizers remain unchanged. All 57 prior test methods are preserved.

This release uses the complete local **v1.1.20** project, including ACCOUNT-014–016.
GitHub main was checked on 2026-10-14 at
`6dad733c11f98a2b0637d591e1a52cbb2f455b7e` and remains v1.1.19; its older Account
files did not overwrite the newer local work. All 57 previous tests and every existing
Java member remain intact. Exactly three Cancellation methods and ENROLL-008 are added.
Version increments once: **1.1.20 → 1.1.21**. Dependencies, Wrapper, BaseTest,
credentials and existing locators are unchanged. The TestNG XML preserves all six
previous class registrations and adds `CancelTests` once. No GitHub push/merge.

## Allure and results

The current build retains its automatic `test` → `allureReport` finalizer.
Existing failure conditions and final assertions are preserved. Raw Allure JSON and
attachments use `build/allure-results/`; Gradle's test report is
`build/reports/tests/test/index.html`.

When the Course Detail test body completes its inspection, it attaches one
`COURSE-DETAIL-001 Course Detail core contract summary` containing requested/resulting
URLs, captured values, image health, button state and every issue. The console logs
start/completion, a course/issue count, and warnings for actual problems. The existing
BaseTest adds its normal final screenshot on assertion failure.

TC2 attaches `COURSE-DETAIL-002 Course curriculum summary`: URLs, section/title
state, discovered module/lesson totals, per-module titles/counts, lesson title
evidence and every issue. Its console logs one line per module and warnings only
for defects; successful lessons are detailed only in the attachment.

TC3 adds one related-content summary with every card, URLs, image health, instructor,
prices, description/specifications, problem-card count and all field failures. TC4
adds a compact navigation summary for both main-card and `XEM CHI TIẾT` paths with
captured href, resulting URL, Course Detail root state and navigation failures only.
TC5 attaches one detailed consistency summary covering source instances, representative
href/name/result, any failure-triggered diagnostic expansion, per-comparison advertised
versus Detail identity values, totals and mismatch records. No raw HTML or per-success
screenshots are attached.

If browser setup fails, TestNG/Allure show the failed configuration and skipped test;
there is no core summary or screenshot from a test body that never ran.
If the Gradle Wrapper cannot bootstrap, neither TestNG nor Allure runs at all.

```powershell
.\gradlew.bat allureReport
.\gradlew.bat allureServe
```

`allureReport` regenerates from existing results without rerunning tests. Avoid
`clean` between a run and report regeneration. Report provisioning requires its
Node/Allure runtime to be available.

See `CHANGE_REPORT.md` for the exact validation performed for this deliverable.
Generated output, IDE files and local verification caches are excluded from the ZIP.
