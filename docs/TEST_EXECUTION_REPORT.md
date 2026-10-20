# V Learning Automation | Test Execution Report

**Report ID:** TER-2026-10-18-01  
**Report type:** Historical full regression and subsequent targeted verification  
**Execution date:** 18 October 2026  
**System under test:** https://demo2.cybersoft.edu.vn/  
**Project version:** 1.1.25 (configured in `build.gradle`)  
**Test suite:** `src/test/resources/testng.xml` (8 TestNG classes)  
**Status:** Completed with failures; **not** an all-green regression  
**Scope of this document:** Test execution evidence. For product defects and unresolved requirements, see [Known Defects and Test Exceptions](KNOWN_DEFECTS.md).

> **Evidence boundary:** The full regression figures below come from the eight Gradle XML reports in `full-regression-results.zip`, supplied from the actual Windows execution on 18 October 2026. The archive is an external/local test artifact, **not a file tracked in this GitHub repository**. The precise Git commit checked out during that run was not recorded in the XML, so the tested source cannot be conclusively tied to a commit. The separate post-STAB-010 figures come from the user's subsequent targeted console/XML output. **No post-STAB-010 full-suite execution is claimed.**

## 1. Execution objective and environment

The run exercised the eight registered functional areas of the V Learning browser automation project: Login, Register, Course List, Course Detail, Enrollment, Account, Cancellation, and End-to-End.

- **Execution mode:** Full TestNG suite invoked through Gradle on Windows, with browser-based UI tests against the live demo site.
- **Configured stack:** Java 26 toolchain; Gradle Wrapper 9.7.1; Selenium 4.49.0; TestNG 7.12.0; Allure Gradle plugin 4.3.0; Allure report runtime 3.14.3; TestNG adapter 2.35.5; Log4j 2.26.1.
- **Browser:** Google Chrome via `ChromeDriver` in `BaseTest`. The run logged Chrome/CDP version 155/153 compatibility warnings; they were non-blocking.
- **Recorded evidence:** Gradle/JUnit XML with test methods, outcomes, durations, and failure messages; Gradle/Allure summaries and selected per-test attachments where available.
- **Not established by this evidence:** The exact local Git commit, a production-like environment, or the website's signed-off UX/validation specifications.

Dependency versions are verified from repository configuration. They should not be mistaken for an independent runtime-environment inventory.

## 2. Full regression summary (pre-STAB-010)

| Metric | Result |
| --- | ---: |
| TestNG test methods executed | **65** |
| Passed | **51** |
| Failed | **14** |
| Skipped | **0** |
| Test-runner error entries | **0** |
| Pass rate | **78.5%** |
| Test status | **FAIL: assertions failed** |

These are **test-method counts**, not unique website defect counts. Multiple tests can fail for the same issue. Likewise, a single test may report hundreds of field comparisons.

### Department results

| Department | Test class | Total | Passed | Failed | Skipped |
| --- | --- | ---: | ---: | ---: | ---: |
| Login | `LoginTests` | 5 | 5 | 0 | 0 |
| Register | `RegisterTests` | 14 | 11 | 3 | 0 |
| Course List | `CourseListTests` | 10 | 2 | 8 | 0 |
| Course Detail | `CourseDetailTests` | 5 | 4 | 1 | 0 |
| Enrollment | `EnrollTests` | 8 | 8 | 0 | 0 |
| Account | `AccountTests` | 16 | 14 | 2 | 0 |
| Cancellation | `CancelTests` | 3 | 3 | 0 | 0 |
| End-to-End | `E2ETests` | 4 | 4 | 0 | 0 |
| **Total** | **8 classes** | **65** | **51** | **14** | **0** |

**Observed strength:** All Login, Enrollment, Cancellation, and E2E tests passed in this historical execution. This does not guarantee that the live website will continue to behave identically.

## 3. Failed-test inventory (14 methods)

These are the actual failed **method names** from the XML files, not new or proposed test cases.

| Department | Failed method | Observed failure / disposition |
| --- | --- | --- |
| Register | `emptySubmissionShouldDisplayRequiredValidationMessages` | Empty-password warning refers to Account; confirmed message-label issue. |
| Register | `invalidSubmissionShouldDisplayFormatValidationMessages` | Phone warning typo; password case-wording expectation still requires a product requirement. |
| Register | `phoneShouldValidateLengthAndCharacters` | Repeated phone-format warning typo, not proof phone input was accepted. |
| Course List | `everyHomepageCourseShouldHaveCourseDetailId` | Malformed `/chitiet/` link without course ID. |
| Course List | `everyCourseListCourseShouldHaveCourseDetailId` | Same malformed-link pattern on catalogue. |
| Course List | `everyCategoryCourseShouldHaveCourseDetailId` | Same malformed-link pattern on category. |
| Course List | `everyHomepageCourseCardShouldContainSubCard` | 4 of 12 Homepage cards lacked SubCards in captured state; requirement pending confirmation. |
| Course List | `everyCourseListCardShouldContainSubCard` | 249 of 249 catalogue cards lacked SubCards in captured state; requirement pending confirmation. |
| Course List | `everyHomepageSubCardShouldContainCorrectCourseInformation` | 12 cards flagged, 28 field occurrences before STAB-010; mixed content/structure checks. |
| Course List | `everyCourseListSubCardShouldContainCorrectCourseInformation` | 249 content failures corresponding to missing catalogue SubCards. |
| Course List | `everyCategorySubCardShouldContainCorrectCourseInformation` | 249 cards, 747 field occurrences before STAB-010; contained one invalid promotional-text/title comparison per card. |
| Course Detail | `courseCardsShouldMatchTheirCourseDetailIdentity` | 262 field mismatches across 30 distinct destination URLs; cross-page content inconsistency, not 262 separate bugs. |
| Account | `requiredProfileWarningsShouldClearIncrementally` | Empty-password warning incorrectly refers to Account. |
| Account | `formatWarningsShouldClearIncrementally` | Phone typo and unverified uppercase/lowercase wording requirement. |

### Consolidated failure analysis

**Confirmed website-content or warning defects**

- **Malformed course links:** A link to `https://demo2.cybersoft.edu.vn/chitiet/` lacks a course ID and was observed on three surfaces. Treat as one repeated defect pattern, not three unrelated bugs.
- **Inconsistent course advertising:** Instructor names/avatars, prices, titles, and images differed between listings/SubCards and destination details. The full-run Course Detail summary counted **262 field occurrences**: instructor name 80, instructor avatar 80, price 44, course title 36, image 14, and SubCard absence 8.
- **Incorrect empty-password label:** The password-field warning displays `Tài khoản không được để trống` instead of identifying `Mật khẩu`.
- **Phone warning typo:** The site displays `định đạng` instead of `định dạng`.

**Observed or unverified assertions; not automatically confirmed product defects**

- **SubCard presence:** Missing on 4/12 Homepage cards and 249/249 catalogue cards; SubCards were present on the 249 observed Category cards. Whether every surface must have one needs an approved UI requirement.
- **Detail button text:** Tests require the exact string `XEM CHI TIẾT`. Confirm DOM text versus visually transformed CSS text and the intended wording before classifying all button failures.
- **Password rule wording:** Tests expect explicit uppercase/lowercase language, but the signed-off wording requirement was not established by these logs alone.

**Automation issue discovered after the historical run**

- `CourseSubCardContentSupport.inspect()` compared the main card's promotional `h6` content against the SubCard's actual course title. This was an invalid equality assumption. STAB-010 replaced it with a required nonblank SubCard title check; it did **not** suppress other content checks.

Refer to [KNOWN_DEFECTS.md](KNOWN_DEFECTS.md) for the separate issue register. Some observations may share an underlying frontend implementation cause.

## 4. Subsequent targeted verification (post-STAB-010)

After correcting the Course Card/SubCard title comparison, **only these three affected Course List tests were rerun**. The developer's original website defects and unresolved UI-contract assertions were intentionally left visible.

| Test ID | Surface | Targeted result | Remaining reported scope |
| --- | --- | --- | --- |
| COURSE-INTEGRITY-003A | Homepage SubCard content | **FAIL** | 12 cards, 20 field issues |
| COURSE-INTEGRITY-003B | Catalogue SubCard content | **FAIL** | 249 cards, 249 field issues |
| COURSE-INTEGRITY-003C | Category SubCard content | **FAIL** | 249 cards, 498 field issues (previously 747) |

The reduction in Category field issues from **747 to 498** is exactly **249**, consistent with removing one invalid course-name comparison from each of the 249 inspected cards. This verifies the intended correction at the aggregate level; it does **not** identify the exact two remaining fields per card in the latest result, and it does not imply the three tests passed.

**Do not combine or substitute these targeted results with the historical full-suite total.** There is no verified new full-run pass/fail count after STAB-010.

## 5. Report and evidence locations

The repository intentionally ignores generated `build/` outputs, so local Gradle/Allure reports are **not hidden GitHub documents**.

| Artifact | Location / availability |
| --- | --- |
| Gradle HTML test report | `build/reports/tests/test/index.html` on the execution computer |
| TestNG/JUnit XML | `build/test-results/test/TEST-*.xml` on the execution computer |
| Allure raw results/attachments | `build/allure-results/` on the execution computer |
| Generated Allure HTML | `build/reports/allure-report/allureReport/index.html` on the execution computer |
| Full regression XML archive | `full-regression-results.zip`, supplied to the project review conversation; **not committed to GitHub** |
| Course Detail field-comparison summary | `COURSE-DETAIL-005-summary.txt`, supplied separately; **not committed to GitHub** |
| Human-readable product issue register | [`docs/KNOWN_DEFECTS.md`](KNOWN_DEFECTS.md) |

Allure generation is configured as a finalizer of the Gradle `test` task, so genuinely failing tests can still produce a report. Screenshots are attempted on failures where a browser remains available. This Markdown document is **a human-authored summary of test results**, not an automatically generated Allure report or a substitute for preserving the underlying XML/attachments.

## 6. Execution conclusion and handover

- **Execution completed:** All 65 methods produced results. There is no evidence of a test-runner crash or suite-wide inability to execute.
- **Functional areas passing in the historical run:** Login, Enrollment, Cancellation, and End-to-End.
- **Unresolved findings:** Website URL/content/message defects, plus UI-contract assumptions that need explicit product confirmation.
- **Post-run action:** STAB-010 was separately applied and verified through three targeted runs; the correction was later merged into GitHub `main`. **The original full regression was not rerun after that correction.**
- **Final disposition:** The automation framework is demonstrably executable, but the live application's clean acceptance and a completely passing regression are **not established**.

**Reporting rule:** Keep this report as the historical 18 October execution record. If the suite is rerun later, add a dated new execution report or clearly labeled new-run section backed by new XML/Allure evidence; do not silently replace these counts.
