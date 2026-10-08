# VLearningAutomation v1.1.12 — Enrollment TC1/TC2 audit

## Baselines and delivery scope

GitHub main was inspected first on **2026-10-08**. Its tree is
`411ea3f139739f72ec1b5011e73255a51de45f35`, and build.gradle reports **1.1.10**.
It contains ENROLL-001 and the existing Enrollment Page Objects/data/support.

The current local project is **1.1.11**, not 1.1.10. All 42 files matched the
previously delivered v1.1.11 ZIP. Compared with the current GitHub tree, 37 files
match and five differ: EnrollTests.java, LoginPage.java, build.gradle, README.md
and CHANGE_REPORT.md. There are no local-only or GitHub-only project files.
Those local differences already implement ENROLL-002 and its route wait.

The newer local source was retained. This delivery increments its patch version
exactly once: **1.1.11 → 1.1.12**. It audits both existing tests and records fresh
attempts to compile and run TC1 alone, TC2 alone and the complete Enrollment class.
No test was duplicated. No TC3 or new architecture was added. No push was made.

## ENROLL-001 source audit

**Result: no contract defect was identified in source review; no code fix was
required. ENROLL-001 and its supporting implementation were preserved unchanged.**
This is a source-audit conclusion, not a live execution PASS.

| Contract item | Inspected implementation |
| --- | --- |
| Session and login | BaseTest creates fresh Chrome per method and opens the Homepage. TC1 enters the confirmed account through LoginPage, waits for login and then the authenticated header. |
| Credentials | EnrollInput matches the supplied TranMinhDuc109 account/password pair. The registration-form password is not used. |
| One healthy course | Existing first-category navigation selects the first usable rendered card with a nonblank sticker name and valid same-site Detail href. Opening it verifies the advertised URL, Detail root and clickable enrollment button. No category/pagination sweep runs. |
| Dynamic identity | CourseCategoryPage captures `span.stikerCard` text into the selected-course object; TC1 retains that string. No Course name or ID is hardcoded. |
| Existing enroll button | CourseDetailPage.clickEnroll reuses the existing sidebar `btnPreview` button whose text is Đăng ký. |
| Business assertion A | The visible SweetAlert title is compared with exactly `Đăng kí thành công`, using `kí` and no period. |
| Business assertion B | AccountPage requires a unique visible My Courses item with a descendant h6 matching the captured name after whitespace/NBSP normalization. |
| Safe title handling | Java string equality preserves case, accents and quotes; course names are not concatenated into XPath. |
| Existing-state protection | A read-only pre-enrollment My Courses check refuses a selected name already present. It never cancels pre-existing account courses. |
| Targeted cancellation | AccountPage finds the unique exact-title item and clicks its own Hủy khóa học button. Ambiguous duplicate titles are refused; unrelated items are untouched. |
| Removal verification | Cleanup explicitly waits until the exact-title item is absent from the active My Courses panel. Hidden matching items are not silently treated as absent. |
| Cleanup after failed assertion | The attempt flag is set before the enrollment click. The finally block invokes EnrollmentSupport even when a popup/presence assertion fails after the request may have succeeded. |
| Original failure preserved | Cleanup/logout RuntimeExceptions and AssertionErrors are logged and suppressed onto the original failure. They do not replace it. |
| Cleanup failure after business success | EnrollmentSupport throws an AssertionError when cleanup/logout fails without an earlier business failure. |
| Application logout | A separate logout attempt follows cancellation, including after cancellation failure. HomePage clicks the observed avatar-associated logout control and waits for Login visibility and disappearance of the displayed account link. |
| Test-class architecture | TC1 contains orchestration and exactly two business assertions. EnrollTests has no helpers, nested types, raw By locators, XPath or DOM parsing. |
| Scope and waits | Enrollment uses explicit waits and frontend Page Objects; no API calls, hard sleeps, implicit waits or exhaustive Course List inspection were introduced. |

The extra read-only account visit before enrollment protects existing state and
permits targeted cleanup when the success-popup assertion fails. It does not
add a business testcase or perform startup cancellation.

## ENROLL-002 audit and inclusion

ENROLL-002 was already implemented in local v1.1.11 and is included unchanged:

Guest Homepage → first Danh mục → one selected Course → Course Detail → Đăng ký → `/login`.

`guestEnrollmentShouldRedirectToLogin()` reuses the existing category-selection
and Detail-click methods, staying on the same category Page Object to avoid
repeat navigation. It contains exactly one business assertion calling
`LoginPage.waitUntilAtLoginPage()`. That method uses the existing explicit wait
and compares only `URI.create(currentUrl).getPath()` with `/login`.

The guest test enters no credentials, makes no Login UI or success-popup
assertions, visits no AccountPage, invokes no EnrollmentSupport cleanup and
performs no cancellation or application logout. BaseTest supplies the fresh
guest session and normal browser teardown. Authentication-return behavior is
not implemented.

There are exactly **two Enrollment @Test methods**, zero test helpers and zero
nested types. TestNG still includes EnrollTests once. The active suite remains
**36 methods** across Login, Register, Course List, Course Detail and Enrollment.

## Fresh validation attempts — 2026-10-08

The requested real commands were attempted sequentially in the delivery copy:

```sh
bash gradlew compileJava compileTestJava --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.healthyCourseShouldEnrollAndAppearInMyCourses --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests.guestEnrollmentShouldRedirectToLogin --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests --console=plain --no-daemon
```

Every command exited **1 before any Gradle task started**. The Wrapper failed to
download `https://downloads.gradle.org/distributions/gradle-9.7.1-bin.zip` with
`java.net.SocketException: Network is unreachable`.
The available runtime is **Java 17.0.20**; the project's **Java 26** requirement
is unchanged. Chrome, TestNG and Allure execution were not reached.

| Required result | Actual outcome |
| --- | --- |
| Production/test compilation | Blocked at Gradle bootstrap; no build verdict |
| TC1 isolated execution | Command attempted; test not executed |
| TC1 success popup assertion | Not reached; unverified live |
| TC1 exact My Courses assertion | Not reached; unverified live |
| TC1 targeted cancellation/removal | Not reached; unverified live; this run created no enrollment |
| TC1 application logout | Not reached; no authenticated test session was created |
| TC2 isolated execution | Command attempted; test not executed |
| TC2 guest state, click and `/login` redirect | Not reached; unverified live |
| TC2 enrollment cleanup | None in the implementation; no test execution or account mutation occurred |
| Full EnrollTests execution | Command attempted; both tests blocked before TestNG |
| Genuine application defect | No verdict: neither test reached the application |
| Environment blocker | Gradle distribution download unavailable; environment JDK is also below the configured requirement |

No arbitrary account courses were inspected or cancelled during validation.
No live PASS, successful removal, logout or guest redirect is claimed.

Available source validation completed: all **29 Java files passed syntax
parsing**; the existing parser also compiled 34 Course Detail XPath expressions.
AST inspection confirmed exactly two test methods with two TC1 assertions and
one TC2 assertion. Hash checks establish that all Java files, testng.xml,
Enrollment fixtures/support, Page Objects and dependency declarations remain
unchanged from v1.1.11. Syntax/structure checks do not establish real dependency
type checking, Java 26 compilation or frontend behavior.

## Exact delivery changes

Relative to the actual local v1.1.11 baseline:

- **Created project files: none.**
- `build.gradle`: project version 1.1.11 → 1.1.12 only.
- `README.md`: version, requested TC1/TC2/full-class run sequence and audit status.
- `CHANGE_REPORT.md`: this audit and fresh execution results.
- **39 original files are byte-for-byte unchanged, including all 29 Java files.**
- **TC1 fixes: none. TC2 additions/modifications in this delivery: none required.**

Relative to GitHub v1.1.10, the ZIP also retains the existing local additions:
`EnrollTests.guestEnrollmentShouldRedirectToLogin()` and
`LoginPage.waitUntilAtLoginPage()` with its URI import, plus the updated
version/documentation. The original TC1 declaration and support flow are intact.

The ZIP contains **42 project files** under one VLearningAutomation folder.
Its CRC and every archived file were verified against the final delivery source.
Temporary validation logs/programs, build output and IDE caches are excluded.
The README provides the same four commands for a configured JDK 26/Chrome
environment with the required Gradle/dependency downloads available.
