# V Learning Automation | Known Defects and Test Exceptions

**Document version:** STAB-011, 18 October 2026  
**System under test:** https://demo2.cybersoft.edu.vn/  
**Repository:** https://github.com/johnsteve1091-dot/VLearningAutomation  
**Evidence:** `full-regression-results.zip` (Gradle XML, executed 18 October 2026); `COURSE-DETAIL-005-summary.txt` (earlier targeted run); and the **post-STAB-010 targeted rerun results** supplied in chat.  
**GitHub check:** `main` at `3abd78db04a5fd8a50446f5a1f709f41081e7076` (18 October 2026). This is **not guaranteed to be the same code** as the user's local working directory.

## 1. Executive status

The **pre-STAB-010 full regression** executed 65 tests: **51 passed, 14 failed, 0 skipped** (78.5% passing). This is an execution baseline, **not** the post-fix full-suite total.

After the local STAB-010 change, only three affected tests were rerun. All three still **failed**, with more focused evidence:

| Test | Post-STAB-010 result | Observed scope |
|---|---|---|
| COURSE-INTEGRITY-003A, Homepage SubCard content | FAIL | 12 cards; 20 field issues |
| COURSE-INTEGRITY-003B, Catalogue SubCard content | FAIL | 249 cards; 249 field issues |
| COURSE-INTEGRITY-003C, Category SubCard content | FAIL | 249 cards; 498 field issues, down from 747 |

The 249-field decrease in 003C aligns with removing the unsupported comparison between a **promotional main-card `h6`** and the SubCard's **actual course title**. This is evidence that the intended local test correction took effect, **not** evidence that other failures have disappeared.

**Counts are assertions/field occurrences, not unique bugs.** A course can appear on more than one surface, and one underlying issue can fail multiple test cases.

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

## 4. Automation and tooling exceptions

### VL-AUT-009 | Promotional text incorrectly compared to course title

- **Status:** **Corrected locally in STAB-010**, as verified by the user's local source check (`requireSubCardValue("Course name", ...)` replacing the old `compareRequired("Course name", ...)`) and the targeted reduction from 747 to 498 Category field failures.
- **Scope:** `CourseSubCardContentSupport.inspect()` requires a nonblank SubCard course name but no longer equates that title to the parent card's promotional text. Instructor, duration, level, description and button checks remain active.
- **Release caveat:** GitHub `main` at `3abd78d...`, checked on 18 October, **still contains** the old `compareRequired("Course name", ...)` call. The verified fix lives in the **local working tree** until committed and merged.
- **Outstanding documentation:** The three `CourseListTests` `@Description` strings on GitHub still claim the course-name equality is tested; update them when synchronizing local code. Do not overwrite newer local files with old GitHub copies.

### VL-ENV-010 | Selenium CDP minor-version warning

- **Observed:** Chrome 155 and Selenium's available CDP support do not exactly match; Selenium logged fallback/compatibility warnings.
- **Status:** Non-blocking in the audited run; all test classes executed.
- **Action:** Check a supported Selenium/Chrome CDP combination as a **separate maintenance change**, not an explanation for the above assertion mismatches.

## 5. Impact on release and acceptance

- **Automation execution:** Functional. Pre-STAB-010 full run: 51 passed / 14 failed / 0 skipped; Login, Enrollment, Cancellation and E2E were all passing in that run.
- **Product:** Has demonstrated content/link/message inconsistencies. A full-green test suite is **not** an honest acceptance criterion for the current demo site.
- **Test reliability:** Improved by STAB-010, but the SubCard presence and button-label contracts still require confirmation.
- **GitHub synchronization:** **Not finished.** Do not label `main` as containing STAB-010 merely because the local command ran successfully.
- **Documentation:** This register groups repeated test failures by underlying issue and explicitly marks uncertainty. The latest full-suite pass/fail total **has not been recalculated** after STAB-010.

## 6. Next release actions, in order

1. **Resolve the two remaining specification questions without a broad rerun:** Is SubCard mandatory on Homepage/Catalogue, and what exact Detail button text is required? Clarify uppercase/lowercase password wording from a product requirement.
2. **Synchronize local fixes to GitHub** using a branch/PR or a checked commit. Compare `git diff` against `main` first; never replace locally newer project files with the old repository snapshot.
3. **Update README** with the 65-test historical baseline, this known-defects document, and the distinction between confirmed product issues and requirement questions. Do not write an undocumented 'all tests passing' claim.
4. **Perform final verification on the merged code** and record new totals only if that final run occurs. Preserve genuine site failures and store the updated Allure/Gradle evidence for handover.

**Handover verdict:** Test framework functional; known product defects documented; clean product acceptance **not established**; release documentation and GitHub merge remain outstanding.
