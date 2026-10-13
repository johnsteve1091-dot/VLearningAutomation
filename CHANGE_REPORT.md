# VLearningAutomation v1.1.19 — ACCOUNT-011 through ACCOUNT-013

## Baseline and preserved corrections

GitHub main was checked on 2026-10-13: still v1.1.15 at
`964b5d9095d0b1a6bf539918ef82119e2efd9dd9`. It predates the local Account work.
The latest complete accessible archive is v1.1.18, with SHA-256
`7107140dabdea3aad95cfec23929b30ed4753a2fcd891bba36aedc74e05c4189`.
Its extracted source was verified byte-for-byte against that archive.

That archive predates two subsequent corrections. Both exact source files were
recovered and integrated verbatim before creating the working baseline:

- `ACCOUNT-008-fixed-method.java.txt`: four independent single-empty-field submissions.
- `ACCOUNT-010-fixed-method.java.txt`: four independent single-invalid-field submissions.

The correction methods were not reconstructed from descriptions. Their existing
names, assertions, close/refresh workflows and failure behavior remain intact.
ACCOUNT-001–007 and ACCOUNT-009 remain as in v1.1.18; the supplied corrected
ACCOUNT-008/010 remain unchanged. The new work adds exactly three methods.

Version increments once: **1.1.18 → 1.1.19**. There are **13 Account tests** and
**54 TestNG methods overall**. Java 26, Selenium 4.49.0, TestNG 7.12.0, Gradle 9.7.1,
Log4j and Allure dependencies remain unchanged. No push or merge was performed.

## Implemented tests

| Test / group | Method | Implemented behavior |
| --- | --- | --- |
| ACCOUNT-011 / account-tc11 | `unsavedProfileChangesShouldBeDiscarded` | Capture originals; generate different valid Name/Email/Phone; enter all three and the existing password; verify inputs/masking; click Đóng; observe closure/no update response/Account route; reopen and assert discarded inputs; Đóng again; refresh and compare originals. |
| ACCOUNT-012 / account-tc12 | `individualUnsavedProfileChangesShouldBeDiscarded` | Four independent Name, Password, Email and Phone attempts. Verify fresh baseline, change only the selected field, compare the other three with their captured states, Đóng/reopen/inspect, then Đóng and refresh before the next attempt. Aggregate retention failures while safe. |
| ACCOUNT-013 / account-tc13 | `guestShouldNotAccessPersonalInformation` | Confirm the fresh browser is guest, navigate directly to the supplied Account URL, wait for a conclusive loaded state, and inspect actual protected data and editing controls. No authentication or account edit action is performed. |

TC11/12 use only `EnrollInput.ACCOUNT` / `EnrollInput.PASSWORD` and the three
original profile values read at runtime. Replacement values reuse
`AccountInput.differentValidProfile`, including the existing Register email generator.
No new generator, password, credentials or hardcoded original profile was added.
Empty reopened profile inputs or restored original values are accepted; replacement
values or other unexpected values fail. Unsaved password retention fails; no
intentional prefill exception has been verified or silently added.

TC12 verifies every fresh form after refresh. Password equality against a captured
baseline is computed inside AccountPage; there is no password value accessor or
password text in snapshot string representations. If the Password baseline already
equals the intended input, the test fails its prerequisite rather than claiming an
actual change was tested. Retention defects do not stop the other field attempts
when closing and refresh still establish safe, original state. Stored mutation or
another unsafe prerequisite stops execution with prior failures preserved.

## Minimal Page Object and support changes

The supplied footer Đóng locator is reused. New close operations never substitute
header X, Escape, refresh, JavaScript hiding or Hoàn thành. Before clicking, the
Page Object checks that Đóng is not an associated native form-submit control.
If it is, the non-submitting scenario fails before an unsafe click. Normal close
uses WebDriver click and explicit waits for hidden modal plus an interactable
Cập nhật control. It observes at least two seconds, with 500 ms stable state and
100 ms polling within the existing 15-second limit; popup titles are retained.
The existing read-only hit-test logic was extracted for reuse without changing
its previous behavior. Refresh is refused while the modal is still visible.

No normal TC11/12 path calls the update-submit method. Button semantics, observed
popup state and stored-data comparisons provide UI evidence; HTTP update requests
are not intercepted or counted. A custom application handler that silently sends
a request cannot be ruled out solely by this DOM observation.

Cleanup first checks persisted values. It calls the existing UI restoration mechanism
only after detecting a difference from the original snapshot. Successful restoration
does not turn the mutation into a PASS. A broken Đóng does not trigger an alternate
dismissal or blind update. Cleanup/restoration failures remain separate from the
primary assertion failure. No course or unrelated account operation was changed.

Guest checks reuse Account locators and the Login form's existing vault locators.
LoginPage gains one nonblocking read-only observation method; no existing Login
method or test changes. A visible Login form is accepted at any URL. An actual
HTTP 401/403 document response is an alternative denial signal, provided no protected
content is accessible. The browser's existing navigation entry supplies this status;
no second HTTP/API request is sent. Missing/zero status, blank content, loading state
or an unrecognized denial screen is not accepted as proof of access protection.
The code retains protected-data/editing exposure observed during polling, even if
later navigation hides it. This is bounded browser observation, not a security audit
of every server endpoint or every possible denial UI.

Browser API reference for the optional HTTP-denial observation:
[PerformanceResourceTiming.responseStatus](https://developer.mozilla.org/en-US/docs/Web/API/PerformanceResourceTiming/responseStatus),
inherited by PerformanceNavigationTiming. Unsupported/hidden status is treated as unknown.

## Evidence and privacy

TC11 records originals, intended/entered values, both Đóng results, reopened values
and refreshed stored values. TC12 records the same observations separately for each
of its four attempts, including untouched-field comparisons. TC13 records initial
guest state, requested/final URLs, Login/Account visibility, personal-panel access,
protected-value exposure, editing availability and settlement/denial evidence.

Password comparisons are boolean; snapshots never print password text. New evidence
and exceptions redact the existing password. Account and Login implementation retains
its masking checks. No runtime screenshot or Allure result is included in this ZIP
because no live browser test was executed here.

## Exact changed-file inventory

| File | Change |
| --- | --- |
| `build.gradle` | Project version only, to 1.1.19. |
| `README.md` | Current counts/scope, corrected TC8/TC10 descriptions, TC11–13 commands and execution limits. |
| `CHANGE_REPORT.md` | This report and concise commit message. |
| `src/main/java/vn/cybersoft/vlearning/pages/AccountPage.java` | Safe input snapshots/comparisons, footer Đóng observation, protected refresh, guest navigation/content checks, shared interactability helper. |
| `src/main/java/vn/cybersoft/vlearning/pages/LoginPage.java` | One additive nonblocking Login-form visibility reader using existing locators. |
| `src/test/java/vn/cybersoft/vlearning/data/account/AccountSupport.java` | Password-safe observations and mutation-detected unsaved-profile cleanup. |
| `src/test/java/vn/cybersoft/vlearning/tests/AccountTests.java` | Integrate the two exact existing correction files, then add exactly TC11–13. |

No project files were added or removed: 45 files total, seven changed and 38 unchanged
relative to the corrected baseline (the changed-file names are also the same relative
to the original v1.1.18 ZIP). Java AST checks preserve all ten baseline test methods
and annotations verbatim. All existing AccountPage members are preserved except the
behavior-equivalent extraction of the existing interactability predicate. All prior
LoginPage members, Enrollment/My Courses methods and other test classes are preserved.
`testng.xml` is unchanged and contains AccountTests exactly once. Test classes have
no new reusable helper methods, raw locators, generators or DOM parsing.

## Actual verification and observed behavior

| Check | Actual outcome |
| --- | --- |
| Syntax/package/AST checks | PASS: 32 Java files parsed; matching package paths; exactly 13 Account tests and 54 total tests. This is not full dependency/type compilation. |
| Preservation | PASS: ten Account methods preserved against the corrected baseline; supplied TC8/TC10 source preserved; existing Page Object members compared with the documented predicate extraction. |
| Offline support checks | PASS: 45 checks against actual AccountInput/AccountSupport using fake UI/framework dependencies. Includes no blind writes, Đóng cleanup, failure preservation, detected-mutation restoration and evidence redaction. This does not execute real Page Object DOM interactions or live TestNG/Selenium scenarios. |
| Full Gradle compilation | BLOCKED / NOT EXECUTED. Current environment has OpenJDK 17.0.20, no Java 26, no Gradle installation/cached distribution and no installed Chrome found. The prior Wrapper download failed with `Network is unreachable`; unchanged blocked commands were not retried. |
| TC11, TC12 and TC13 individually | NOT EXECUTED due to the same environment blockers. |
| Full Account suite/regression | NOT EXECUTED due to the same blockers. |
| Actual unsaved-input retention | NOT OBSERVED in a live browser. No PASS or product-defect finding is claimed. |
| Actual guest-access behavior | NOT OBSERVED in a live browser. No redirect, denial or exposure finding is claimed. |
| New bugs discovered | None established by live execution. Assertions retain failures if the application violates the expected behavior. |

Use JDK 26 and Chrome, allow Gradle to sync, then run the compile and independent
commands in README, followed by AccountTests. The archive has one ready-to-open
`VLearningAutomation/` root and the complete Gradle Wrapper. Build outputs, caches
and the temporary verification harness are excluded.

## Suggested Git commit

```text
test(account): add unsaved-edit and guest-access coverage
```
