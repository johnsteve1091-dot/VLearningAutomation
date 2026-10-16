# VLearningAutomation v1.1.25 — E2E-004

## Implementation baseline

Checked GitHub `johnsteve1091-dot/VLearningAutomation` main on **2026-10-16**.
It remains commit `3d82310b87d4d42b59f41552efaa2acd92cd5605`, tree
`5fbbbf24b02c5fb4c26fb26ae54573c3cb8fde02`, **v1.1.21 / 61 TestNG methods**.
Fetched the current branch/tree and verified all 47 immutable main blobs against
the inspected main files before changing the implementation.

Used the newer complete local **v1.1.24 / 64-test** project:
`VLearningAutomation-v1.1.24-E2ETC3.zip` (197962 bytes, SHA-256
`fe74f3d9ecb4c1f9e243d9922dadb65ea020bbce8ccda349cd85e29c7b4ae3d1`).
All 50 archive entries matched the local project before copying the baseline.
This retains E2E-001's latest reload correction, E2E-002, E2E-003 and their support
infrastructure. The authoritative source, not the older main-only implementation
or stale test-number descriptions, determines existing behavior.

Version increments once: **1.1.24 → 1.1.25**. No commit, push or merge was performed.

## Files created and modified

Paths are relative to the extracted `VLearningAutomation` root.

| File | Change |
| --- | --- |
| `src/test/java/vn/cybersoft/vlearning/tests/E2ETests.java` | Added exactly one public scenario `staleAccountTabShouldNotMutateCoursesAfterAccountSwitch()`, group `e2e-tc4`, test name `E2E-004 - Cross-Account Session Hijack Protection`, under existing Allure Feature `End-to-End`. Visible business assertions cover B/A originals, A's owned enrollment, completed B authentication before the stale action, then independent refreshed B/A integrity checks. |
| `src/main/java/vn/cybersoft/vlearning/pages/AccountPage.java` | Additive `StaleCourseCancellationObservation`, `cancelStaleCourseForAccountOnceAndObserve(...)` and a private immediate loaded-identity reader. Reuses confirmed Account response locators and the unchanged exact single stale-cancellation operation. A changed/unknown owner cannot authorize a click. All prior members and locators preserved. |
| `src/test/java/vn/cybersoft/vlearning/data/e2e/CrossAccountEnrollmentSupport.java` | Reused immutable baselines, frontend discovery, Account + Course ownership and exact cleanup. An overload supplies E2E-004's evidence label while the original constructor retains E2E-002. Extracted one-time pre-recovery failure evidence, and added `finishAndVerifyReadOnlyAccount(...)` to attempt B's independent read-only baseline check after owned A cleanup, including after A cleanup failure. |
| `build.gradle` | Project version only: 1.1.25. |
| `README.md` | Current counts, E2E-004 scenario and safety rules, five run commands, actual verification and environment limits. Prior department instructions retained. |
| `CHANGE_REPORT.md` | This report prepended; all prior reports retained below as history. |

**0 new project files + 6 modified; 44 baseline files unchanged.** Complete project:
**50 files, 37 Java sources, 65 TestNG methods** — Login 5, Register 14, Course List
10, Course Detail 5, Enrollment 8, Account 16, Cancellation 3 and E2E 4.
All **64 previous TestNG methods are preserved verbatim**, including E2E-001's
reload fix, E2E-002, E2E-003 and ACCOUNT-014/015/016. No test-class helper, nested
model, direct locator or low-level DOM operation was added.

Only five existing support members change: CrossAccountEnrollmentSupport's original
constructor, `selectCourseAbsentFromBothAccounts`, `record`, `finish` and `student`.
The constructor delegates to the overload with the same E2E-002 default; discovery
and unknown-account errors use the correct test label; record/cleanup labels are
parameterized; finish reuses its extracted screenshot operation. Its existing
selection, owner guards and cancellation semantics remain in use. All other existing
Java members are preserved; AccountPage changes are additive.

AccountSupport/AccountTabs, E2ESupport, CancellationSupport, EnrollmentSupport,
LoginSupport, all other Page Objects, fixtures and BaseTest are byte-identical.
`testng.xml` is unchanged and valid: eight registered classes, E2ETests exactly
once, no parallel/order/dependency changes. The new method belongs to that existing
class and needs no second registration. No new URL, XPath/CSS locator, Course ID,
credential, popup message, API shortcut, forced click, sleep or mutation retry was added.

Configuration remains **Java 26, Selenium 4.49.0, TestNG 7.12.0, Gradle Wrapper
9.7.1, Log4j 2.26.1, Allure plugin 4.3.0, report 3.14.3, adapter 2.35.5**, AspectJ
disabled and the existing automatic Allure report finalizer. All Wrapper launchers,
JAR/properties, settings, resources and `.gitignore` are included.

## The cross-account stale-tab scenario

1. Login as existing B, verify its loaded identity, open and refresh My Courses,
   capture B's complete immutable exact-name/count baseline. Use application logout
   and normal Login as A; verify A and independently capture its refreshed original
   map. No enrollment precedes either baseline.
2. Reuse frontend category discovery and the shared-course exclusion selector.
   Exclude both original maps and recheck current absence under each verified owner.
   Capture a real category/href/exact stikerCard identity. Existing enrollments are
   never cancelled to create a candidate; exhaustion gives an explicit setup failure.
3. Freshly Login as A, verify A, refresh and recheck exact X absence and the original
   map. Open the captured card/Detail URL and wait for its enrollment control. Mark
   **A + X ownership immediately before** the click. Require `EnrollInput.SUCCESS_TITLE`,
   popup closure, A's loaded Account, one exact X and A's original map plus X.
   Require its cancellation control and remain on A's My Courses in the original tab.
4. Existing AccountTabs retains the original handle and opens a second tab in the
   same WebDriver/profile. `logoutInSecondTab(A)` checks shared authentication and
   A's actual identity, performs application Logout and confirms guest state.
   Then explicitly Login B normally in Tab B, verify authenticated navigation and
   B's loaded Account. Only after these assertions return to original Tab A.
5. Do not navigate, refresh, clear authentication or Login in stale Tab A. Read its
   current UI and loaded Account response. Blank/Login, changed/unknown identity,
   unavailable control or missing exact Course produce a recorded non-attempt.
   A clearly changed B page cannot be clicked. A still-identifying A page with one
   exact X/control rechecks ownership and delegates to the existing single stale
   cancellation click/observer. Ambiguity fails safely. No retry, force-click or
   successful-removal wait is used. Before/after state, owner, attempted/completed
   flags and exception class document the actual observation.
6. After the stale operation, existing `tabs.loginFresh` selects Tab B for independent
   persisted-data verification. Freshly verify B, open and refresh its Courses;
   require X=0 and the complete original B map. Freshly logout/Login A, verify A,
   open and refresh A's Courses; require X=1 and the complete original A map plus X.
   Each verification has its own catch; a failed B inspection cannot skip A.
   The first failure's screenshot is attempted before the next account transition.

Visible stale-page behavior alone cannot pass the test. Blank, redirected, removed
control, blocked click and refused mutation are possible protective observations,
not invented application guarantees. Persisted data must satisfy **both** account
assertions. Unauthorized A deletion or any B Course mutation fails without any
re-enrollment or data change designed to conceal the failure. A blank page is not
automatically labeled a security defect. This scenario requires completed B Login,
which distinguishes it from the unchanged logout-only ACCOUNT-016.

## Cleanup, ownership and evidence

The scenario's `finally` executes before AccountTabs closes. Only A's guarded
enrollment click creates ownership; B never receives an owned Course. Ownership
is retained after a partial persisted write even when the click/response fails.
The selected Course is excluded from both original baselines and cannot be an
arbitrary or pre-existing Course.

`finishAndVerifyReadOnlyAccount(B, primaryFailure)` first reuses existing owned
cleanup. It captures the primary failing UI before recovery, ends an authenticated
session through application Logout and explicitly logs A in again. The loaded A
identity and refreshed owner data are required before checking X's exact count.
One unique X delegates to unchanged EnrollmentSupport; zero skips cancellation;
duplicates refuse arbitrary deletion. After cancellation or its failure, refresh
and compare A's entire map with the original. Recovery or integrity failures remain
visible. An already absent X is not re-enrolled; its earlier unauthorized removal
remains a business failure even if the original map now matches.

Then independently establish verified B Login, refresh My Courses and compare with
B's original map **read-only**, even if A recovery/cancellation/integrity failed.
There is no B enrollment or cancellation. Unexpected B data is reported and left
untouched. If no baseline exists because setup failed early, record that limitation;
no mutation is authorized. No enrollment is performed during cleanup.

The existing recursive failure redaction preserves primary message, stack and nested
diagnostics while removing both fixture passwords. Evidence includes B/A original
maps, captured X, A's response/enrollment, Tab B shared logout, verified B Login,
stale-tab identity/action, both refreshed comparisons and final baselines. Failure
screenshots precede recovery and tab closure; an unmasked visible Login password
causes the screenshot to be omitted with a reason. No cookies/tokens/storage values
are read. Cleanup/evidence failures are suppressed on a primary business failure,
or fail the overall test after business success. Existing try-with-resources tab
closure preserves any earlier exception; BaseTest still owns browser termination.
Run sequentially; do not overlap tests sharing either account.

## Actual verification — 2026-10-16

| Verification | Actual result |
| --- | --- |
| Current repository and complete local baseline | **PASS** — main branch/tree checked; all 47 main blobs verified; all 50 v1.1.24 archive entries equal the chosen baseline. |
| Java syntax/packages and preservation | **PASS** — all 37 sources parsed, package folders match, all 64 prior scenarios preserved verbatim; test classes expose only public TestNG scenario methods. Syntax/AST check, not complete dependency compilation. |
| Counts/import references/configuration/XML | **PASS** — exactly one added test, 65 total; project-local imports resolve to source types; dependencies unchanged; valid unchanged suite with E2ETests registered once. |
| E2E-004 focused offline compile/simulations | **PASS — 102 checks**. Actual complete E2ETests, updated CrossAccountEnrollmentSupport, EnrollmentSupport/LoginSupport, related support/fixtures and selected verbatim AccountSupport methods plus entire AccountTabs compile against simulated Page Objects/framework APIs. The harness executes the actual E2E-004 scenario. Not a live TestNG/Selenium run or complete project build. |
| Actual AccountPage offline checks | **PASS — 55 checks**, including prior stale-profile/cancellation checks and the new owner/blank/target guards. Compiles actual BasePage/AccountPage with simulated Selenium APIs. Validation copy only replaces `.getFirst()` with `.get(0)` for the available JDK 17; production source remains unchanged. Not a Java 26/real dependency build. |
| Existing E2E-002 offline regression | **PASS — 74 checks** executing the unchanged E2E-002 body with the updated reused support/default constructor, ownership, cleanup and evidence. Other E2E methods compile against simulated APIs but are not executed by that harness. |
| `bash gradlew compileJava compileTestJava` | **BLOCKED** — attempted, exit 1 before compilation; Gradle 9.7.1 download reports `java.net.SocketException: Network is unreachable`. |
| E2E-004 independent method filter | **BLOCKED** — attempted, exit 1 at the same Wrapper download before TestNG/Chrome. |
| Complete E2ETests class | **BLOCKED** — attempted, exit 1 before TestNG/Chrome. No live E2E-001–003 regression confirmation. |
| Account + Enrollment + Cancellation class regression | **BLOCKED** — attempted with three class filters, exit 1 before TestNG/Chrome. This includes ACCOUNT-014–016 but none actually executed. |
| Full suite `bash gradlew test` | **BLOCKED** — attempted, exit 1 before TestNG/Chrome. |
| Complete ZIP | **PASS** — all 50 entries match inspected files, CRC valid, complete Wrapper, Unix launcher executable, generated caches/reports and offline doubles excluded. |

Total **231 offline checks**; no live PASS. The environment has OpenJDK 17.0.20,
no configured JDK 26, no Chrome/Chromium and no installed Gradle distribution.
README supplies the exact compile, single scenario, E2E department, relevant
regression and full-suite PowerShell commands, with Linux/macOS launcher equivalents.
No actual application account data was changed here, and no live Allure results,
screenshots or report were generated.

The simulations inject protected and unauthorized outcomes: blank/Login/changed-B
UI, missing/unknown/ambiguous targets, blocked single click, transient-only removal,
actual simulated A deletion, B collateral loss or unexpected X, partial enrollment,
session loss, wrong Login owner, independent inspection failures, loading snapshots,
unsafe/failed A recovery, failed cancellation, cleanup collateral loss, screenshot/
evidence/tab-close failures and no eligible candidate. Injected failures are used
to verify that the actual scenario fails safely and cannot hide persistent data
changes; they are **not discovered application defects**.

**Genuine new authorization defects observed: none; live validation was blocked.**
Actual cross-tab session propagation, B Login, Tab A's resulting DOM, stale-request
authorization, current frontend course availability, server persistence and real
cleanup remain unverified until the configured Chrome runs execute. No unsupported
popup or backend behavior is asserted.

---

# Historical v1.1.24 report

# VLearningAutomation v1.1.24 — E2E-003

## Implementation baseline

Checked GitHub `johnsteve1091-dot/VLearningAutomation` on **2026-10-16**, including
a second check when the repeated E2E-003 instructions arrived. Main remains commit
`3d82310b87d4d42b59f41552efaa2acd92cd5605`, tree
`5fbbbf24b02c5fb4c26fb26ae54573c3cb8fde02`, **v1.1.21 / 61 TestNG methods**.
All 47 immutable main blobs were checked against the inspected main baseline.

Used the newer, complete **v1.1.23 / 63-test** local project:
`VLearningAutomation-v1.1.23-E2ETC2.zip` (191727 bytes, SHA-256
`6665963c03f58906782a2b21e36241bb289ada8ca1bd49e11be1c8169c54d205`).
All 50 archive entries matched the local source before implementation. This retains
E2E-001's latest post-registration reload correction, E2E-002, E2ESupport,
CrossAccountEnrollmentSupport and the current HomePage hover/logout implementation.
Both earlier E2E methods and their dedicated support classes remain unchanged.

Version increments once from the actual local baseline: **1.1.23 → 1.1.24**.
No GitHub commit, push or merge was performed.

## Files added and modified

Paths are relative to the extracted `VLearningAutomation` root.

| File | Change |
| --- | --- |
| `src/test/java/vn/cybersoft/vlearning/tests/E2ETests.java` | Added exactly one public scenario, `guestShouldCompleteEnrollmentAfterLogin()`, group `e2e-tc3`, test name `E2E-003 - Guest to Authenticated Course Enrollment`, under the existing Allure `End-to-End` feature. Assertions and the guest-to-enrolled journey are visible in this method. No helpers or nested models added to the test class. |
| `src/test/java/vn/cybersoft/vlearning/data/cancel/CancellationSupport.java` | Added caller-supplied baseline capture and a guarded, navigation-free enrollment-attempt marker. Existing enrollment delegates to the same guards/marker. Exposed the existing redacted evidence operation; extracted one-time failure evidence so it can run before closing an observation tab. Cleanup verifies an existing session's owner and, if needed, uses application logout before existing Login recovery. Existing exact-title cancellation and refreshed-baseline checks remain in use. |
| `src/test/java/vn/cybersoft/vlearning/data/account/AccountSupport.java` | Added only `AccountTabs.openAccountInSecondTab(expectedAccount)`. Reuses existing Page Objects to observe the authenticated Account in a temporary tab; existing tab close/restore and all prior members remain unchanged. |
| `build.gradle` | Project version only: 1.1.24. |
| `README.md` | Current counts, E2E-003 flow, actual-return preservation, run commands, cleanup and verification limits. Previous department documentation retained. |
| `CHANGE_REPORT.md` | This release report; previous reports retained below as historical records. |

**0 new files + 6 modified; 44 baseline files unchanged.** The complete project
contains **50 files, 37 Java sources and 64 TestNG methods**: Login 5, Register 14,
Course List 10, Course Detail 5, Enrollment 8, Account 16, Cancellation 3 and E2E 3.
All **63 pre-existing test methods are preserved verbatim**, including E2E-001's
reload fix and E2E-002. Only four existing CancellationSupport methods change:
`loginAndCaptureBaseline`, `enroll`, `finish` and `record`; other existing Java
members are preserved. The new members are confined to the two existing support
classes and the single scenario above.

All Page Objects, locators, fixtures, BaseTest, EnrollmentSupport, LoginSupport,
E2ESupport and CrossAccountEnrollmentSupport are byte-identical to v1.1.23.
`testng.xml` is unchanged, valid, and registers eight classes with E2ETests exactly
once. No test ordering or parallel execution was added. No new URL, credential,
Course ID, popup expectation, backend shortcut or force-click was introduced.

The exact dependency/configuration baseline remains **Java 26, Selenium 4.49.0,
TestNG 7.12.0, Gradle Wrapper 9.7.1, Log4j 2.26.1, Allure plugin 4.3.0, report
3.14.3 and adapter 2.35.5**, with AspectJ disabled and the existing report finalizer.
All four Wrapper files, settings, properties, resources and `.gitignore` are included.

## E2E-003 business assertions and return continuity

One TestNG method uses one BaseTest WebDriver session and the existing EnrollInput
account; it never registers a new account.

1. Read-only setup logs in, verifies the loaded Account, refreshes My Courses and
   captures the complete original exact-title/count map. Existing frontend discovery
   selects an absent Course X and captures its category, href and exact stikerCard
   name. Absence and the original map are rechecked. Application logout must succeed;
   the business journey starts only after guest state is verified. No existing
   Course is cancelled to make room. No eligible course produces an explicit setup
   failure through the existing selector.
2. As a guest, open Homepage, the captured category and actual Course X card. Verify
   the captured href, ready Detail/enrollment control, and store the actual Detail
   URL. Click Enroll without ownership. Require the existing Login route and normal
   Account/Password/submit controls, then submit the confirmed credentials normally.
3. Require successful Login and the application's actual return to the original
   Detail URL using the existing URL comparison/wait. A wrong return fails before
   authenticated ownership or enrollment. No test-driven navigation or refresh is
   used to repair the return; leaving `/login` alone cannot pass.
4. After that return assertion, a temporary read-only Account tab verifies the loaded
   owner and refreshed continued absence of X. The original Detail tab is untouched.
   This uses existing AccountTabs infrastructure because the authenticated header
   has no confirmed account-name locator. The observation performs no second Login,
   credential/session manipulation, enrollment or cancellation. Its failure evidence
   is captured before the tab closes. The tab is closed and the original tab restored
   by the existing AutoCloseable implementation. The original URL and control must
   still be ready before the next step.
5. Record ownership immediately before the authenticated `detail.clickEnroll()`.
   The marker only validates/stores identity; it performs no browser operation.
   `CancellationSupport.enroll(...)` is deliberately not used here because it would
   navigate. Require the exact `EnrollInput.SUCCESS_TITLE` and wait for the popup
   to close. An unexpected or duplicate title fails without selecting another course.
6. Open Account → My Courses, verify the owner, require X exactly once and compare
   the complete map with the original map plus X. Refresh My Courses through the
   existing owner/data-ready Page Object operation. Require X exactly once and the
   same complete map again. Loading or temporarily empty data cannot pass these
   owner-gated, positive-count and full-map assertions.

All milestones use existing Log4j/Allure evidence: initial owner/baseline, captured
Course identity, actual guest URL, Login redirect, actual return, verified owner,
actual popup, My Courses maps, refreshed persistence and final cleanup state.
The existing password-redaction operations are reused; no cookies or tokens are read.

## Ownership and cleanup protections

The test calls `CancellationSupport.finish(primaryFailure)` in `finally`.
Only the authenticated click claims ownership, after a second owner/absence check.
Guest-only or failed-return paths have no owned Course and cannot cancel anything.
Ownership is recorded before the click so a partial persisted write remains eligible
for cleanup. The marker rejects an unselected, original or conflicting captured identity.

Failure evidence is attempted before recovery changes the UI, and before a failing
observation tab is closed. The outer catch retains the original diagnostics using
the existing redacting failure wrapper. Cleanup checks the active Account identity,
ends a different authenticated session through the actual logout control if needed,
and reuses LoginSupport recovery. Loaded owner/data checks are required before exact
cancellation. Failed identity/logout/recovery prevents mutation.

Existing EnrollmentSupport performs only exact test-owned cancellation, rejects
ambiguous names and skips absent courses. Cleanup then refreshes and requires the
entire original title/count map. No pre-existing enrollment is intentionally removed
or recreated. Integrity changes remain failures instead of being repaired to hide
them. Primary business failures remain primary, with cleanup/evidence errors
suppressed; cleanup failure after successful business assertions fails the test.
No enrollment is attempted during cleanup. Default sequential execution is retained;
runs sharing this account must not overlap.

## Actual verification results — 2026-10-16

| Verification | Actual result |
| --- | --- |
| GitHub/local baseline | **PASS** — current main checked; all 50 complete v1.1.23 ZIP entries match the chosen baseline. |
| Java syntax, packages and existing members | **PASS** — 37 sources parsed with the available compiler API; package paths match; all 63 prior scenarios preserved verbatim; test classes contain only public scenario methods. This is not dependency/type compilation. |
| Local imports, test counts and configuration | **PASS** — project-local import references resolve; exactly one new test, 64 total; valid unchanged TestNG XML with E2ETests once; dependencies and Wrapper unchanged. |
| Focused offline compile and E2E-003 simulations | **PASS — 74 checks**. Actual E2ETests, CancellationSupport, EnrollmentSupport, LoginSupport, E2ESupport, CrossAccountEnrollmentSupport, fixtures and selected verbatim AccountSupport methods/AccountTabs compile against simulated Page Objects/framework APIs. Only E2E-003 is executed by this harness. Not a complete project build or live TestNG/Selenium run. |
| Offline Cancellation/ENROLL-008 regression | **PASS — 38 checks** against simulated APIs, including execution of the three existing Cancellation scenarios and ENROLL-008 using the updated CancellationSupport and existing EnrollmentSupport/LoginSupport. Not live browser regression. |
| Complete `bash gradlew compileJava compileTestJava` | **BLOCKED** — attempted, exit 1 before compilation: Gradle 9.7.1 download reports `java.net.SocketException: Network is unreachable`. |
| Independent E2E-003 method filter | **BLOCKED** — attempted, exit 1 at the same Wrapper download before TestNG/Chrome. |
| Complete E2ETests class | **BLOCKED** — attempted, exit 1 before TestNG/Chrome. No live E2E-001/002 regression. |
| Login + Enrollment + Account + Cancellation regression | **BLOCKED** — attempted with four class filters, exit 1 before TestNG/Chrome. |
| Full suite `bash gradlew test` | **BLOCKED** — attempted, exit 1 before TestNG/Chrome. |
| Archive contents and integrity | **PASS** — all 50 entries match the inspected project, valid ZIP CRC, complete Wrapper and executable Unix launcher. Caches, generated reports and offline test doubles excluded. |

The environment has **OpenJDK 17.0.20**, not the configured JDK 26, and no installed
Chrome/Chromium or Gradle distribution. Configuration was preserved. There was no
live browser run, enrollment/cancellation write, Allure results or Allure report.
README contains the exact compile, isolated method, E2E class, regression and full
suite commands for PowerShell, with the Linux/macOS launcher equivalent.

The **112 offline checks** cover the complete simulated journey; original-tab
continuity; wrong return, wrong owner and unexpected responses; no guest ownership;
absence rechecks; partial writes; session loss and wrong-owner recovery; preserved
original Courses; duplicate ambiguity; post-refresh loss; cleanup-only failure;
primary failure preservation; evidence timing and failure; and existing Cancellation/
re-enrollment behavior. These injected faults are **not observed application defects**.

**New live application defects: none observed; live validation was blocked.**
The actual Login return, shared authentication in the observation tab, current course
availability, live popup, server persistence and real cleanup remain unverified.
The test fails rather than repairing or inventing any of these behaviors.

---

# Historical v1.1.23 report

# VLearningAutomation v1.1.23 — E2E-002

## Implementation baseline

Checked GitHub `johnsteve1091-dot/VLearningAutomation` on **2026-10-16**:
main is still commit `3d82310b87d4d42b59f41552efaa2acd92cd5605`, tree
`5fbbbf24b02c5fb4c26fb26ae54573c3cb8fde02`, **v1.1.21 / 61 TestNG methods**.
The 47 files at that immutable commit were compared with the complete local project.

Used the newer complete **v1.1.22 / 62-test** local project:
`VLearningAutomation-v1.1.22-E2ETC1.zip` (181067 bytes, SHA-256
`e586fb0364959e4c624d89e2faf7e31655aae97a3b335e1ab717aa1c7b019f38`).
All 49 entries matched the local project. Its E2E-001 and E2ESupport were preserved.
The current GitHub HomePage hover/logout implementation is already present locally
and remains unchanged. No older main-only baseline replaced this local work.

Also inspected the separate `E2E-001-login-fix.zip`. Its earlier intercepted-click
wait and conditional Register/Login overlay change were superseded by the later
**2026-10-16 06:45 UTC reload correction**. The implementation baseline carries
forward that exact simple sequence: wait for the registration success popup to
close, `driver.navigate().refresh()`, then existing
`AccountSupport.loginAndOpenAccount(...)`. No `LoginPage` change or intercepted-click
retry is applied. This correction was provided in the prior conversation; a live
browser confirmation or newer complete fixed ZIP was not available. All remaining
E2E-001 assertions and its support class are unchanged.

Version increments once from the complete local baseline: **1.1.22 → 1.1.23**.
No commit, push or merge was performed.

## Files added and modified

Paths are relative to the extracted `VLearningAutomation` project root.

| File | Change |
| --- | --- |
| `src/test/java/vn/cybersoft/vlearning/data/e2e/CrossAccountEnrollmentSupport.java` | **Added.** E2E-002 session transitions, two immutable baselines, shared frontend discovery, independent Account + Course ownership, redacted evidence and independent cleanup orchestration. Reuses AccountSupport and EnrollmentSupport. |
| `src/test/java/vn/cybersoft/vlearning/tests/E2ETests.java` | **Modified.** Added exactly one public scenario `courseEnrollmentShouldRemainIsolatedBetweenAccounts()`, group `e2e-tc2`, name `E2E-002 - Cross-Account Enrollment Isolation`, Feature `End-to-End`. Carries forward E2E-001's latest reload correction; its corrected method is preserved verbatim. No test-class helper methods or nested models. |
| `build.gradle` | **Modified.** Project version only, 1.1.23. |
| `README.md` | **Modified.** Current counts, E2E-002 purpose/flow, run commands, cleanup, preservation and actual verification results. Earlier department documentation retained. |
| `CHANGE_REPORT.md` | **Modified.** This release report prepended; previous v1.1.22 report retained below as historical evidence. |

**1 added + 4 modified; 45 baseline files unchanged.** The complete deliverable has
**50 project files, 37 Java sources and 63 TestNG methods**. There are two E2E tests;
Login 5, Register 14, Course List 10, Course Detail 5, Enrollment 8, Account 16 and
Cancellation 3 remain intact. Against the corrected local baseline, **all 62
existing test methods and every existing Java member are preserved verbatim**.
Relative to the previously packaged v1.1.22 E2E-001 method, only its specified
post-registration transition changes to the later reload correction.

`testng.xml` is byte-identical to v1.1.22: eight classes, E2ETests registered exactly
once, no parallel execution or dependency/order additions. No new Page Object,
locator, URL, credential, hardcoded Course identifier, popup expectation, API call,
JavaScript force-click, sleep or blind mutation retry was added. Existing
`E2ESupport`, `AccountSupport`, `EnrollmentSupport`, `CancellationSupport`,
`LoginSupport`, all Page Objects and BaseTest are unchanged.

The exact dependency/configuration baseline is retained: **Java 26, Selenium
4.49.0, TestNG 7.12.0, Gradle Wrapper 9.7.1, Log4j 2.26.1, Allure plugin 4.3.0,
report 3.14.3, adapter 2.35.5**, AspectJ disabled, automatic Allure report finalizer.
All four Wrapper files, settings, properties, resources and `.gitignore` are included.

## Business assertions in E2E-002

One TestNG method, one BaseTest WebDriver session, no registration:

1. Login as the existing EnrollInput account A; verify its loaded labeled Account.
   Open My Courses, refresh, and capture A's immutable original title/count map.
   Use application logout, Login as AccountInput.UPDATE_ACCOUNT B, verify B and
   its distinction from A, then capture B's independently refreshed original map.
   No enrollment mutation occurs before both baselines are captured.
2. Search actual frontend category URLs and the existing exclusion selector. Exclude
   the union of both original title sets and recheck candidate absence in refreshed
   loaded data for both accounts. A currently enrolled candidate is excluded and
   another rendered candidate is tried. Exhausted/unready categories give a clear
   setup failure; existing courses are never removed to make space. Capture one
   exact category/href/stikerCard-name identity for the accepted Course X.
3. Login as A, verify A, refresh and recheck X's exact absence. Open X through its
   captured category/card and verify the Detail URL. Mark A's ownership immediately
   before the click. Record the actual popup and assert `EnrollInput.SUCCESS_TITLE`.
   Require exactly one X and A's complete original map plus X under the loaded owner.
4. Logout/Login as B, verify B's actual identity, open and refresh B's courses.
   **Before B attempts enrollment**, require X=0 and B's entire original map unchanged.
   This is the explicit check that B has not inherited A's enrollment.
5. Recheck B's absence, open the same captured Course and verify the same Detail URL.
   Mark B's independent ownership immediately before its click. Require the existing
   exact success popup, one X, and B's original map plus X. A rejection is attached
   as the actual response and fails; there is no substitution or relaxed expectation.
6. Logout/Login as A, verify identity, refresh and require one X plus all A originals.
   Require the exact cancellation control, cancel A's X, verify X=0 and A's original
   map. Refresh and reassert absence and the complete original map.
7. Logout/Login as B, verify B, open courses and **refresh**. Require X=1 and B's
   complete original map plus X. This final assertion proves A's cancellation did
   not remove B's independent enrollment; a pre-refresh observation is insufficient.

All interactions reuse existing Page Objects. Exact normalized Course names gate
My Courses matching; the captured href/name/category identifies the same frontend
Course for both enrollment attempts. Duplicate exact matches are rejected.

## Independent cleanup and evidence

The support instance keeps two separate account states: each account has its own
password, immutable original map and attempted `EnrollmentCourse` identity. There
is no shared ownership flag, no ownership inferred merely from a visible Course,
and no use of fixed-account CancellationSupport for B. Both attempt markers are
set immediately before their respective clicks, so a persisted write followed by
an exception remains eligible for targeted cleanup.

The scenario always calls cleanup in `finally`. Its primary failure is retained
with its message, source stack and nested diagnostics; both account passwords are
redacted from messages/causes/suppressed errors and milestone attachments. Failure
evidence is captured before any recovery navigation. An observed unmasked Login
password field causes that screenshot to be omitted and the reason to be recorded.
No storage/token values are read or attached.

For **each attempted owner independently**, cleanup opens the known Homepage,
ends any authenticated session using the actual logout control, explicitly logs in
as the intended owner, verifies its actual loaded Account identity, and opens and
refreshes its My Courses. It checks X's exact count. Zero means no repeated
cancellation; multiple matches mean refusal; one unique entry is delegated to the
unchanged `EnrollmentSupport.finish` under the verified owner. The delegated
cleanup's errors are collected by the outer support rather than replacing the
business failure. The account is refreshed again and its complete original map
must be restored. Missing/changed originals are reported, never repaired by
unrequested deletion or enrollment.

A cleanup exception or unavailable identity cannot authorize mutation, and cannot
skip B's independent recovery attempt. Evidence errors do not prevent authorized
cleanup. Independent outcomes, captured identities, both original maps, actual
responses, isolation checkpoints and refreshed cleanup maps are logged/attached.
Cleanup/evidence/integrity failures become suppressed errors on the primary failure,
or fail the overall test if its business assertions passed. A business isolation
failure remains a failure even if cleanup subsequently restores visible state.

The existing default sequential TestNG/Gradle execution is retained. Runs sharing
either account must not overlap with E2E-002; no locks, parallel framework or extra
test cases were introduced.

## Actual verification results — 2026-10-16

| Verification | Actual result |
| --- | --- |
| Repository/local baseline comparison | **PASS** — current main commit checked; all 49 complete local ZIP entries match local source; later reload correction carried forward explicitly. |
| Java syntax, package paths and member preservation | **PASS** — all 37 Java files parsed with the available JDK compiler API; all 62 pre-existing methods/members preserved against the corrected baseline; test classes expose only public TestNG scenario methods. This is not dependency compilation. |
| Local import references, counts and configuration | **PASS** — project-local imports resolve to source types; 63 test methods; existing dependencies/build unchanged apart from version; valid XML with E2ETests registered once. |
| Focused offline compile and simulations | **PASS — 74 checks**. Actual E2ETests, CrossAccountEnrollmentSupport, E2ESupport, EnrollmentSupport, LoginSupport, fixtures/generators and selected verbatim AccountSupport methods compile against simulated Page Objects/framework APIs. The 74 checks execute E2E-002, not E2E-001. Not a complete project build or real TestNG/Selenium execution. |
| Complete `bash gradlew compileJava compileTestJava` | **BLOCKED** — attempted, exit 1 before compilation: Gradle 9.7.1 distribution download reports `java.net.SocketException: Network is unreachable`. |
| Independent E2E-002 via `--tests` method filter | **BLOCKED** — attempted, exit 1 at the same Wrapper download before TestNG/Chrome. |
| Both E2E methods via `--tests ...E2ETests` | **BLOCKED** — attempted, exit 1 before TestNG/Chrome. No live E2E-001 regression or reload-fix confirmation. |
| Account + Enrollment + Cancellation class regression | **BLOCKED** — attempted together with three `--tests` filters, exit 1 before TestNG/Chrome. |
| Full suite `bash gradlew test` | **BLOCKED** — attempted, exit 1 before TestNG/Chrome. |
| Archive contents and integrity | **PASS** — all 50 files match the inspected project; valid ZIP CRC; complete Wrapper; Unix launcher executable; generated caches/reports/offline test doubles excluded. |

The environment provides **OpenJDK 17.0.20**, not the project's required JDK 26;
Chrome/Chromium and an installed Gradle distribution are unavailable. Dependency
versions were preserved rather than changed to make this environment appear to pass.
No new browser session, live enrollment/cancellation, Allure results or report was
produced. The five commands and matching PowerShell commands are in README.

The offline checks specifically cover non-empty independent baselines, exact shared
identity, absence before B's attempt, partial writes by either account, session loss,
A-to-B leakage, B's global duplicate rejection, cross-account cancellation, UI-only
cancellation, collateral loss, duplicate-title ambiguity, wrong loaded owners,
logout/Login failure, wrong detail URL, exhausted categories, an already-enrolled
candidate, independent cleanup continuation, cleanup-only failure, evidence failure,
unready/malformed snapshots, pre-recovery screenshots and both-password redaction.
These are injected faults in simulation, **not observed application defects**.

**Discovered live application defects: none observed; live validation was blocked.**
Actual account login availability, current frontend courses, both accounts' same-course
enrollment behavior, cancellation isolation, server persistence, live cleanup and
the carried-forward reload fix remain unverified until the configured browser runs
complete. The implementation does not assert unverified cancellation popup text or
invent backend behavior.

---

# Historical v1.1.22 report (before the later local reload correction)

# VLearningAutomation v1.1.22 — E2E-001

## Verified baseline and preservation

On **2026-10-16**, GitHub main at
`https://github.com/johnsteve1091-dot/VLearningAutomation` was rechecked:

- Commit: `3d82310b87d4d42b59f41552efaa2acd92cd5605`.
- Tree: `5fbbbf24b02c5fb4c26fb26ae54573c3cb8fde02`.
- Version: **1.1.21**, 47 project files and 61 TestNG methods.
- Compared the complete previous local `VLearningAutomation-v1.1.21-CancelTC1-TC3-EnrollTC8.zip`
  (173450 bytes; SHA-256 `49790ee6935dfde6911cea0da2457c9d7f87e53bd89ab2727eb1dad47a9ecc95`).
  Its extracted contents match the local source, and **46/47 files match GitHub exactly**.
- GitHub's newer `HomePage.logoutIfAuthenticated()` uses explicit avatar/logout hover
  interactions. This latest version is preserved. All 47 implementation-baseline
  files were verified against GitHub blob hashes before code changes. No newer
  unpushed local work was discarded.

The requested lifecycle, Page Object, fixture, support, test, suite and build files
were inspected before reuse. Version increments once: **1.1.21 → 1.1.22**.

The completed source has **49 project files, 36 Java sources and 62 TestNG methods**.
Every existing Java file, all **61 previous tests**, all seven prior departments,
locators, fixtures and helper implementations are byte-identical to current main.
No GitHub commit, push or merge was performed.

## Exact files created or modified

Paths are relative to the extracted `VLearningAutomation` root.

| File | Change |
| --- | --- |
| `src/test/java/vn/cybersoft/vlearning/tests/E2ETests.java` | **Added** exactly one public TestNG scenario: `newStudentShouldCompleteCourseLifecycle()`, group `e2e-tc1`, test name `E2E-001 - Complete New Student Lifecycle`, Epic V Learning / Feature End-to-End. Visible business assertions, no helper methods or nested models. |
| `src/test/java/vn/cybersoft/vlearning/data/e2e/E2ESupport.java` | **Added** only generated-student baseline/selection/ownership, evidence and owner-verified cleanup orchestration. No locators or browser lifecycle creation. |
| `src/test/resources/testng.xml` | Added E2ETests exactly once, retained the seven previous class registrations/order and suite configuration, extended the display name. |
| `build.gradle` | Project version only, 1.1.22. |
| `README.md` | Updated version/counts, new lifecycle purpose, commands, shared infrastructure, cleanup and registration side effect; retained existing department documentation. |
| `CHANGE_REPORT.md` | Current baseline, implementation, precise validation results and limitations. |

**2 new + 4 modified; 43 baseline files unchanged.** No changes to existing Java,
Page Objects, BaseTest, BasePage, existing support classes or TestNG methods.
Dependencies remain **Java 26, Selenium 4.49.0, TestNG 7.12.0, Gradle Wrapper 9.7.1,
Log4j 2.26.1, Allure plugin 4.3.0, report runtime 3.14.3 and adapter 2.35.5**.
The existing automatic Allure finalizer and all four Wrapper files are retained.

## Implemented business assertions

One method, one BaseTest browser session, one newly generated student:

1. Navigate Homepage → Login → Register. Generate Account and Email with existing
   RegisterSupport functions; keep credentials in local variables. Use only the
   valid RegisterInput.Correct data. Require a native Group dropdown and expected
   option, explicitly select it and assert the selection. Require the existing
   registration-success popup and wait for it to close.
2. Follow ACCOUNT-002's post-registration navigation. If automatically authenticated,
   use application logout and assert authentication ended. Explicitly log in with
   the new credentials; require authenticated navigation and loaded account data.
   Assert all six profile fields: Account, Email, Full Name, Phone, Group and Học viên.
   No static registration result from another test is read or written.
3. Capture an immutable loaded My Courses identity/count baseline; do not assume it
   is empty. Discover categories through actual frontend links and reuse the existing
   exclusion selector. Capture category URL, href and exact stikerCard name; recheck
   exact absence on authenticated My Courses before returning through that category/card.
   Assert the expected Detail URL; reuse root/dialog/clickable-enrollment readiness.
4. Record ownership immediately before `clickEnroll()`. Require the existing
   `EnrollInput.SUCCESS_TITLE`, wait for the dialog to close, then require exactly
   one selected Course and the complete original baseline plus that entry.
5. Use the actual application logout, assert its result and ended authentication,
   explicitly log in again with the same new credentials, and verify the same six
   profile values. Require the original enrollment exactly once and all original
   Course identities/counts unchanged.
6. Require the selected Course's exact cancellation control, cancel only that owned
   Course, assert exact count zero and the original complete Course baseline.
7. Refresh using `AccountPage.refreshMyCourses(newAccount)` and capture a loaded,
   owner-gated snapshot. Assert the canceled title remains absent/count zero and the
   complete original identity/count baseline remains unchanged.

No TestNG test method invokes another test. No hardcoded Course ID/title/category,
new locator, invented cancellation popup, backend request, new wait/sleep, blind
mutation retry or additional framework is introduced.

## Reused infrastructure and new-account cleanup

- Registration uses HomePage/LoginPage/RegisterPage navigation, unique generators,
  confirmed registration inputs, native selection checks and popup-close wait.
- `AccountSupport.loginAndOpenAccount` provides the normal UI login, login completion,
  authenticated header and expected Account readiness checks. Its existing
  `recordAccountObservation` supplies Log4j/Allure observations with password redaction;
  `staleAttemptFailure` preserves business assertions and redacts WebDriver failure
  messages. Existing helper implementations are unchanged.
- Course discovery and detail opening reuse `getCourseCategoryUrls`,
  `goToCourseCategory`, `selectCourseForEnrollmentExcluding` and
  `openCourseForEnrollment`; CourseDetailPage owns all detail/popup operations.
- AccountPage owns exact-name matching, occurrence counts, cancellation availability,
  cancellation disappearance and authenticated refresh/snapshot readiness.
- Fixed-account CancellationSupport is inspected but **not used** for this student.
  EnrollInput is used only for its confirmed success-title constant, never credentials.

`E2ESupport` keeps its original map and one selected identity per test instance.
The ownership marker is set immediately before the single enrollment click, so
partial server success followed by a lost response still permits targeted cleanup.
Original titles cannot acquire ownership. Ownership remains set after scenario
cancellation so the final integrity check still runs.

Every scenario exit goes through `finally`. A primary failure screenshot is captured
before recovery/navigation changes the UI. If enrollment was attempted, cleanup
opens the known Homepage, reads any authenticated account's actual identity, and
uses application logout if it is a different student. It reuses
`LoginSupport.restoreSessionForCleanup` and **then verifies the generated student's
loaded Account identity again**. An authenticated header alone never authorizes
cancellation. Failed owner recovery stops mutation and reports the cleanup failure.

Only the generated Account and exact test-owned title reach unchanged
`EnrollmentSupport.finish`. Already-absent owned data requires no cancellation.
An unrelated snapshot/evidence failure is collected without skipping eligible
exact-title cleanup. Duplicate/ambiguous matches fail safely. The final refreshed
snapshot must contain zero owned occurrences and match the original complete map;
missing original Courses are reported, never recreated to disguise collateral loss.
Cleanup errors are suppressed on the primary business error, or fail an otherwise
successful scenario. BaseTest's final evidence/session cleanup/quit remains unchanged.

Milestone evidence records generated Account/Email, registration/profile, captured
Course identity, baseline, actual enrollment popup, both enrolled snapshots,
application logout, cancellation, refresh and final cleanup integrity. No password,
cookie or authentication token is intentionally logged or attached. No routine
success screenshots are added.

**Registration side effect:** a registered account can remain in the backend after
execution, including after later failures. Only its test-owned enrollment is cleaned
where recovery is possible. No verified account-deletion operation exists; none is
invented or claimed, and this side effect is not reported as an application defect.

## Actual validation — 2026-10-16

| Check | Actual result |
| --- | --- |
| GitHub/local comparison | **PASS**: main/commit/tree checked; latest HomePage retained; all 47 baseline blobs verified. |
| Existing source preservation | **PASS**: every existing Java file and all 61 tests byte-identical; all eight departments registered exactly once after adding E2E. |
| AST, packages and structure | **PASS**: 36 Java sources parse; package paths/local import references match; exactly 62 TestNG methods; E2ETests declares one public scenario, no helper methods/nested types/raw locators. This is not dependency/type compilation. |
| Suite and build configuration | **PASS**: XML parses, original seven class registrations retained; group `e2e-tc1` present; build.gradle changes only project version. |
| Focused offline checks | **PASS — 51 checks**: real E2ETests/E2ESupport and existing EnrollmentSupport/LoginSupport/RegisterSupport/fixtures; selected AccountSupport methods copied verbatim; Page Objects and framework APIs simulated. Covers both post-registration states, fresh credentials, all six profile mismatches, Group/registration gates, non-empty baseline, partial writes, expired/wrong sessions, wrong-owner recovery refusal, persistence failures, collateral loss, duplicate refusal, evidence/snapshot failure, password redaction and cleanup failure after business success. |
| Full project compilation | **BLOCKED**: real `./gradlew compileJava compileTestJava --no-daemon` attempted; exit 1 at Wrapper download, before compilation. |
| E2E-001 independent method | **BLOCKED / NOT RUN**: requested method filter attempted through the real Wrapper; exit 1 before TestNG. |
| E2ETests class | **BLOCKED / NOT RUN**: requested class filter attempted through the real Wrapper; exit 1 before TestNG. |
| Full suite | **BLOCKED / NOT RUN**: unavailable bootstrap/toolchain/browser prerequisites; `clean test` not attempted after the three identical bootstrap failures. |
| Allure/TestNG results | **NOT GENERATED**: checked output locations; no new raw Allure results, HTML report or Gradle test report exists. TestNG and its report finalizer never started. |
| Complete ZIP | **PASS**: 49 files, archive CRC and all-file byte equality, Wrapper files present, executable Unix gradlew; build/cache/IDE/offline-harness artifacts excluded. |

All three requested Wrapper attempts failed with **`java.net.SocketException:
Network is unreachable`**, downloading
`https://downloads.gradle.org/distributions/gradle-9.7.1-bin.zip`.
The available runtime is **OpenJDK 17.0.20**; **JDK 26, Chrome/Chromium and a standalone
Gradle installation are unavailable**. No dependency/toolchain downgrade was made.

The 51 offline checks compile selected actual sources against synthetic APIs on
Java 17 and directly invoke the scenario body. They are not 51 additional project
testcases, a complete dependency-backed compile, TestNG results or live Selenium
execution. No live registration, enrollment or cancellation occurred in this environment.

## Defects and unverified behavior

**No new live application defect was observed.** The offline fault injections verify
failure handling only. Existing defect-sensitive assertions are unchanged.
Full acceptance still requires the configured live run: registration availability,
current dropdown/popup behavior, authenticated profile mapping, available Course data,
real persistence, frontend synchronization and successful targeted cleanup are not
claimed verified here. Readiness uses the existing loaded-account/active-panel contract;
no unobserved loading state is invented. A shared account changed externally can cause
an integrity mismatch; cleanup never alters unrelated data to force a PASS.

## Run after extracting

Open `VLearningAutomation` directly in IntelliJ with **JDK 26** for Project SDK and
Gradle JVM, sync Gradle and install Google Chrome. Run sequentially:

```powershell
.\gradlew.bat compileJava compileTestJava
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.E2ETests.newStudentShouldCompleteCourseLifecycle"
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.E2ETests"
.\gradlew.bat clean test
```

Linux/macOS: use `bash gradlew` in place of `.\gradlew.bat`. After an actual test
run, inspect the existing Allure outputs; `allureReport` regenerates the report and
`allureServe` serves it. Do not clean between a run and report regeneration.

Suggested commit: `test: add new-student E2E lifecycle in v1.1.22`
