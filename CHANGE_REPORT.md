# VLearningAutomation v1.1.10 — Enrollment TC1

## Source of truth and version

GitHub main was inspected before implementation. Commit/tree
`853db432d2a49c8b75b683b7e4aab9774b2a6596` reports **1.1.4** and lacks the
current Course Detail implementation. The current local project reports **1.1.9**.
All 38 local files matched the previously delivered v1.1.9 ZIP before work began;
an untouched baseline and SHA-256 manifest were retained for comparison.

The GitHub/local comparison found 27 identical shared files, six different shared
files, and five local-only Course Detail files. The local-only files are
CourseDetailPage, CourseDetailTests and the three CourseDetail support classes.
The different shared files are build.gradle, README.md, CHANGE_REPORT.md,
CourseCardDomReader, CourseSubCardContentSupport and testng.xml.
The newer local implementation is the source of truth; it was not rebuilt from GitHub.

**Exactly one patch increment: 1.1.9 → 1.1.10.** Dependency versions and the Java
requirement are unchanged. No GitHub push was made.

## Functional scope

Exactly one functional TestNG method was added:
**ENROLL-001 — Successful course enrollment**, group `enroll-tc1`.
The suite now contains **35 methods**: Login 5, Register 14, Course List 10,
Course Detail 5 and Enrollment 1. Cancellation is cleanup within ENROLL-001;
there is no Cancel Course suite or additional Enrollment scenario.

The test uses the existing confirmed Login account through the existing Login
Page Object. Those credentials are currently inline in LoginTests, so the small
EnrollInput holder supplies them for Enrollment without changing Login/Register
behavior. RegisterInput's registration-form sample password is not used.

The flow is:

1. Log in and wait for the authenticated account header.
2. Use existing category discovery and normal Danh mục menu navigation to open
   the first category. Stop at the first usable rendered card with a nonblank
   `span.stikerCard` name and a same-site Detail href containing a course path.
3. Retain its category URL, href and displayed name as strings. Make a read-only
   My Courses visit to establish that this exact name is absent before enrollment.
   If it already exists, fail setup and leave it untouched.
4. Return through the same category menu, find the captured card by href/name and
   click its main image. Wait for the advertised Detail URL, visible Detail root
   and clickable existing enrollment button. No course or category ID is hardcoded.
5. Mark the enrollment attempt before clicking Đăng ký, then capture the visible
   SweetAlert title. Business assertion A checks exactly **`Đăng kí thành công`**,
   with `kí` and no period.
6. Wait for the popup to close, open the account through its avatar link, and
   open Khóa học. Business assertion B requires one visible My Courses item whose
   h6 matches the captured `stikerCard` name after whitespace normalization.
7. In `finally`, cancel only that exact-title item if present, verify its removal,
   and perform real application logout.

The pre-enrollment absence guard allows cleanup after a failed popup assertion:
the server may have accepted the request even when the success message is wrong.
It also prevents cleanup from taking ownership of a pre-existing enrollment.
There is no startup mass-cancellation, pagination, category sweep, API call,
course crawling or fallback loop that conceals a broken candidate.

## Architecture and DOM grounding

EnrollTests is **84 source lines**, with **one directly declared method**, one
`@Test`, zero helper methods and zero nested types. It contains two business
assertions, high-level orchestration and `try/catch/finally`; it contains no By
locators, XPath, DOM parsing or browser element operations.

| Class | Responsibility |
| --- | --- |
| EnrollTests | One business flow, two assertions and delegation to final cleanup |
| EnrollInput | Minimal existing-account credentials and exact success-title expectation |
| EnrollmentSupport | Cleanup/logout orchestration, evidence and preservation of the primary failure |
| AccountPage | Account readiness, My Courses tab, exact-title matching, scoped cancellation and removal wait |
| HomePage additions | Normal category navigation, authenticated account navigation and application logout |
| CourseCategoryPage additions | Capture one `stikerCard` identity and reopen that same card |
| CourseDetailPage additions | Reuse the existing enroll button; wait/click/capture the SweetAlert lifecycle |

The supplied DOM and original screenshots ground the Detail enrollment button,
SweetAlert, account link, My Courses structures and scoped cancellation button.
The authenticated-header screenshot shows `img.avatar` and `span.logout` beneath
`a.infoHeader[href='/thongtincanhan']`; logout uses that observed control.
The account screenshot also grounds the `#infoPersonal` account field used as a
data-readiness check before accepting an empty My Courses tab.

Public browser inspection confirmed `li.courseCate`, the existing category links,
the direct card image, `span.stikerCard`, and the selected card's Detail root and
Đăng ký button. Only the public category → card → Detail path was inspected;
no live account login, enrollment, cancellation or logout was performed.

The existing CourseCardDomReader remains unchanged. Enrollment uses the observed
`stikerCard` title separately instead of reinterpreting the legacy h6 content.
Course names are compared as normalized Java strings; accents, case, apostrophes
and quotation marks are preserved. No name is interpolated into XPath.

All waits use the existing explicit-wait infrastructure. No sleeps, implicit
waits, custom retry frameworks, listeners or DataProviders were added.
The supplied success popup has no dismiss button; its disappearance is awaited.

## Cleanup and failure handling

Cleanup runs after an enrollment attempt even if a later assertion fails. It
reacquires the selected course by exact normalized title, clicks Hủy khóa học
inside that item only, and waits until no matching item remains in the active
My Courses panel. An already-absent item causes no cancellation. Multiple matches
are an explicit ambiguity failure; an existing but hidden match is not reported
as successful removal. Similarly named or unrelated items are left untouched.

Logout is attempted independently even if cancellation fails. The Page Object
clicks the observed logout control and waits until Login is visible and the
authenticated account link is no longer displayed. BaseTest's existing storage
cleanup does not replace application logout.

When a business/setup failure already exists, cleanup/logout errors are logged
and added as suppressed exceptions to that original failure. When business
assertions pass but cleanup or logout fails, the test fails. Failure evidence is
captured before cleanup when screenshots are available, so later navigation does
not erase the original failing screen.

Allure calls attach the selected category/href/name, actual popup title,
cleanup/logout summary and optional pre-cleanup failure screenshot. These calls
are implemented; this environment produced no new live TestNG/Allure results.

## Exact file scope

Created:

1. `src/main/java/vn/cybersoft/vlearning/pages/AccountPage.java`
2. `src/test/java/vn/cybersoft/vlearning/data/enroll/EnrollInput.java`
3. `src/test/java/vn/cybersoft/vlearning/data/enroll/EnrollmentSupport.java`
4. `src/test/java/vn/cybersoft/vlearning/tests/EnrollTests.java`

Modified:

1. `src/main/java/vn/cybersoft/vlearning/pages/HomePage.java`
2. `src/main/java/vn/cybersoft/vlearning/pages/CourseCategoryPage.java`
3. `src/main/java/vn/cybersoft/vlearning/pages/CourseDetailPage.java`
4. `src/test/resources/testng.xml` — include EnrollTests once and update the label.
5. `build.gradle` — project version only.
6. `README.md` — version, run instructions, Enrollment behavior and responsibilities.
7. `CHANGE_REPORT.md` — this release report.

The other **31 original files are byte-for-byte unchanged**, including all four
existing test classes, all three CourseDetail support classes, Login/Register
Page Objects, shared card readers, base classes, Wrapper files and logging
configuration. AST comparison confirms that all original declared members of
HomePage (11), CourseCategoryPage (8) and CourseDetailPage (87) are unchanged;
their Enrollment behavior is additive. Existing Course Detail locators and
TC1–TC5 contracts are preserved.

The ZIP contains **42 project files, including 29 Java files**, under one
`VLearningAutomation` folder. Temporary verification programs, fixture stubs,
reconnaissance images, crash logs, build output and IDE caches are excluded.

## Validation performed — 2026-10-07

The requested commands were attempted in this order, with EnrollTests as the
only selected functional test class:

```sh
bash gradlew compileJava compileTestJava --console=plain --no-daemon
bash gradlew test --tests vn.cybersoft.vlearning.tests.EnrollTests --console=plain --no-daemon
```

Both exited **1 before any Gradle task started**. The Wrapper could not download
`https://downloads.gradle.org/distributions/gradle-9.7.1-bin.zip` and reported
`java.net.SocketException: Network is unreachable`.
The available environment is Java **17.0.20**; the project's requirement remains
Java **26**. No dependency or Java version was downgraded to work around this.

| Requested result | Actual outcome |
| --- | --- |
| Production/test compilation | Blocked during Gradle bootstrap; no real build verdict |
| ENROLL-001 | Not executed; no PASS/FAIL application result |
| Runtime TestNG discovery | Not reached |
| Selected course removed afterward | Not verified live; no enrollment was made here |
| Application logout completed | Not verified live; no authenticated test session ran |
| Genuine application defect | No verdict from ENROLL-001 because it did not execute |
| Allure runtime report | No new report/evidence generated by ENROLL-001 |

Available offline checks completed:

- All 29 Java sources parsed successfully; 34 existing direct Course Detail XPath
  expressions also compiled. This is syntax validation, not dependency resolution
  or Java 26 compilation.
- An isolated Java fixture compiled the new Enrollment slice against temporary
  Selenium/TestNG/Allure/logging API stubs. Existing dependencies were stubbed;
  new CourseDetail method bodies were extracted into an isolated facade. This
  does not establish compatibility with the real dependency versions or compile
  the complete production/test project.
- Thirteen isolated safety assertions passed: quoted/NBSP name matching; exact
  target-only cancellation; removal detection; refusal of pre-existing and
  ambiguous matches; absent-target safety; failed-removal detection; successful
  cleanup/logout orchestration; preservation of the primary assertion with
  suppressed cleanup/logout errors; failure after otherwise successful business
  assertions when cleanup fails; logout failure reporting; no cancellation before
  an enrollment attempt; and evidence-failure preservation of the primary error.
  Fixtures use fake DOM objects and do not verify real browser wait timing.
- File hashes, AST member comparisons, method counts, the two-assertion structure,
  suite inclusion and the final baseline diff were checked.

These offline results are not an ENROLL-001 PASS. Full validation still requires
JDK 26, Chrome and access to the configured Gradle distribution/dependencies or
a compatible populated cache. Run only EnrollTests first and inspect its cleanup
and logout evidence before reporting a successful end-to-end result.
