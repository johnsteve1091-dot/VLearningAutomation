# V Learning Automation | Known Defects and Test Exceptions

**Evidence date:** 18 October 2026; status reviewed 20 October 2026  
**System under test:** https://demo2.cybersoft.edu.vn/  
**Repository:** https://github.com/johnsteve1091-dot/VLearningAutomation  
**Execution evidence:** See [Test Execution Report](TEST_EXECUTION_REPORT.md). Original Gradle XML and Allure evidence remain external/local artifacts; they are not committed to this repository.  
**Repository status:** STAB-010 was merged in PR #18, the README restored in PR #19, and the historical execution report was added to `main`. These merges do not constitute a new full regression.

## 1. Purpose and evidence

This document groups **confirmed website defects** and **observed discrepancies requiring requirements confirmation**. It is not a list of every failed test method. The separate [Test Execution Report](TEST_EXECUTION_REPORT.md) records the full regression (65 executed, 51 passed, 14 failed, 0 skipped on 18 October 2026), the failed-test inventory, and the three post-STAB-010 targeted results.

Counts such as 262 field mismatches and 249 affected card occurrences are **observations, not counts of distinct defects**. Repeated displays of the same course and shared frontend data may contribute to multiple assertions.

## 2. Confirmed website defects

### VL-DEF-001 | Course Detail links without course IDs

- **Status:** Confirmed by full-regression browser observations.
- **Impact:** Course-card navigation may lead to an invalid Detail URL rather than a specific course.
- **Surfaces:** Homepage, `/khoahoc`, and the mobile (`DiDong`) category.
- **Affected tests:** COURSE-INTEGRITY-001A, 001B, 001C.
- **Observed:** A card advertises `https://demo2.cybersoft.edu.vn/chitiet/` with **no course identifier**. The same malformed URL occurs on three surfaces.
- **Expected:** Every navigable course card should target its valid, course-specific Detail URL.
- **Evidence:** `CourseListTests.xml` browser logs with `INVALID COURSE URL`; one offending card reported per affected test.
- **Suggested disposition:** One issue, three reproductions. Do not report three unrelated URL defects.

### VL-DEF-002 | Advertised course information disagrees with Course Detail

- **Status:** Confirmed cross-page consistency differences; grouping is by field type, not root cause.
- **Impact:** Users are shown an instructor, course identity, or price that differs from the destination page.
- **Affected test:** COURSE-DETAIL-005.
- **Observed in pre-STAB-010 full regression:** **262 field-level mismatches**, 30 distinct Detail URLs, across nine source instances:

| Field | Mismatch occurrences |
|---|---:|
| Instructor name | 80 |
| Instructor avatar | 80 |
| Advertised selling price | 44 |
| Course title | 36 |
| Course image | 14 |
| SubCard presence | 8 |
| **Total** | **262** |

- **Concrete example:** A card for `/chitiet/.100` displays instructor **Elon Musk** and **400.000đ**, while the destination displays **Robert Ngô Ngọc** and **500.000đ**. On a Category SubCard, the advertised **BOOTCAMP - LẬP TRÌNH FULL STACK TỪ ZERO ĐẾN CÓ VIỆC** differs from the destination's **LẬP TRÌNH FRONT-END CHUYÊN NGHIỆP**. The course thumbnail for that example matches, so the automation is not simply failing every image check.
- **Expected:** Course-specific advertised facts should agree with the destination course; distinct display formatting and legitimate marketing text are not expected to be identical.
- **Evidence:** `CourseDetailTests.xml` from the full regression and the separate COURSE-DETAIL-005 Allure summary. Different runs recorded 260 and 262 field mismatches; they should **not** be added together.
- **Suggested disposition:** Group by source-data/rendering cause after developers investigate. Do not open 262 individual defects.

### VL-DEF-003 | Parent card and SubCard advertise different instructor identities

- **Status:** Confirmed visible-content inconsistency; may overlap VL-DEF-002's instructor issue.
- **Impact:** The same listing/card presents inconsistent instructor identities.
- **Affected test:** COURSE-INTEGRITY-003C; also relevant to 003A and COURSE-DETAIL-005.
- **Observed:** Main card shows **Elon Musk**; its SubCard shows **Elun Musk Ricard**; many Detail pages show **Robert Ngô Ngọc**.
- **Expected:** The instructor associated with the displayed course should be consistent across card, SubCard, and Detail, unless the UI explicitly distinguishes roles.
- **Evidence:** Course Detail field summary and the category sweep's `Instructor name` failures. After STAB-010, 003C still reported two issues per card. Based on the previous field labels, these are consistent with `Instructor name` and `Detail button`; the new summary did not separately list each field.
- **Suggested disposition:** Link to VL-DEF-002 rather than duplicate developer tickets if the same frontend placeholder/content mapping is responsible.

### VL-DEF-004 | Empty password warning incorrectly refers to Account

- **Status:** Confirmed warning-label defect.
- **Affected tests:** REG required-field validation; ACCOUNT-008.
- **Steps:** Submit an empty Password field with the other fields valid (Account) or submit the empty Register form.
- **Observed:** `Tài khoản không được để trống`.
- **Expected:** `Mật khẩu không được để trống`.
- **Evidence:** Both `RegisterTests.xml` and `AccountTests.xml` assertions show the exact messages. The observed failure is in the field-specific warning, not proof that the invalid submission was accepted.
- **Suggested disposition:** One shared validation-message issue if both screens reuse the same validation rules; otherwise link the two affected locations.

### VL-DEF-005 | Phone-format warning contains a spelling error

- **Status:** Confirmed warning-text defect.
- **Affected tests:** REG combined invalid submission; REG invalid phone boundaries/characters; ACCOUNT-010.
- **Steps:** Submit an invalid Phone number, e.g. fewer than 10 digits, more than 10 digits, a letter, or a special character.
- **Observed:** `Số điện thoại chưa đúng định đạng`.
- **Expected:** `Số điện thoại chưa đúng định dạng`.
- **Evidence:** `RegisterTests.xml` and `AccountTests.xml` exact mismatch text.
- **Suggested disposition:** One wording defect repeated in several tests. This is **not** evidence that phone-value rejection itself is broken.

## 3. Observed discrepancies requiring product/requirement confirmation

These must **not** be silently called confirmed website bugs or silently suppressed to achieve green tests.

### VL-OBS-006 | Missing SubCards on selected surfaces

- **Observed:** Four of 12 Homepage cards and all 249 Catalogue (`/khoahoc`) cards had no SubCard in their test snapshots. All 249 Category cards were reported as having SubCards.
- **Post-STAB-010 effects:** 003B reports 249 field issues on 249 cards, consistent with the same SubCard absence. 003A still reports 20 issues in 12 cards.
- **Affected tests:** COURSE-INTEGRITY-002A/002B, 003A/003B; COURSE-DETAIL-005 also notes missing SubCards on inspected cards.
- **Question before defect classification:** Are SubCards intended for **every** surface? Could `/khoahoc` intentionally use a different card design? Check the intended UI contract/design before assigning product-defect status.
- **Next verification:** Compare a single actual DOM card on each surface with the approved design. Avoid rerunning all 249 cases for this clarification.

### VL-REQ-007 | Detail button text contract may be too strict

- **Observed:** The **pre-STAB-010** category log names `Detail button` as one of three failed fields on each of 249 Category cards. After removing the promotional-title comparison, 003C fell from 747 to 498 failures, so the remaining count is **consistent with instructor + button** discrepancies.
- **Source expectation:** GitHub `CourseSubCardContentSupport.java` requires the exact text `XEM CHI TIẾT`.
- **Missing evidence:** Actual DOM button text, computed `text-transform` style, and the approved text requirement. Uppercase display due to CSS is not necessarily uppercase DOM text.
- **Next verification:** Capture **one** representative `button.getText()` and the rendered label; confirm the intended contract. Do not automatically relax or preserve the case-sensitive assertion without evidence.

### VL-REQ-008 | Uppercase/lowercase password-warning requirement is unverified

- **Observed:** Invalid Password warning: `Mật khẩu phải ít nhất 8 tự gồm chữ, số, và kí tự đặc biệt`.
- **Automation expects:** Explicit uppercase and lowercase wording, plus length, number and special character wording.
- **Affected tests:** REG combined invalid submission; ACCOUNT-010.
- **Missing evidence:** A signed-off product requirement that the warning **must explicitly mention both letter cases**. Also distinguish message-content requirements from actual password acceptance rules.
- **Next verification:** Inspect requirements or conduct focused password acceptance boundary tests if those rules are not otherwise covered. Do not declare the missing words a confirmed defect solely from these assertions.

## 4. Automation and tooling notes (not website defects)

- **VL-AUT-009, corrected:** STAB-010 removed the unsupported comparison between a promotional main-card `h6` and the SubCard's course title. The correction is **merged into GitHub `main`** (PR #18). A subsequent targeted run reduced Category SubCard field failures from **747 to 498**, but did **not** make those tests pass. Remaining failures must not be hidden.
- **VL-ENV-010, non-blocking:** The historical run logged a Chrome/Selenium CDP version-compatibility warning; all eight test classes nevertheless executed. Treat this as tooling maintenance, not an explanation for business assertion failures.
- **Source-description follow-up:** The `CourseListTests` descriptions for the three SubCard content tests may still describe comparing the main-card course name to the SubCard title. Their descriptions should be reviewed against the corrected assertion; this is a documentation/test-metadata issue, **not a website defect**.

## 5. Current disposition

The repository contains the corrected STAB-010 assertion, the README, and a separate [Test Execution Report](TEST_EXECUTION_REPORT.md). **No full regression after STAB-010 has been documented**, so the historical 51-pass/14-fail count remains historical.

The confirmed website findings above are not declared fixed. The SubCard presence requirement, exact Detail button wording, and password uppercase/lowercase warning wording remain **unverified**. Clarify those requirements before accepting or closing the related assertions.

**Handover assessment:** The automated suite was executable in the recorded full run; documented product inconsistencies remain; a clean product acceptance decision has not been established.
