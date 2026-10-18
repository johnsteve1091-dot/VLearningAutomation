# STAB-010: Course Card / SubCard comparison correction

Baseline checked: GitHub `johnsteve1091-dot/VLearningAutomation` main at `bfbe5938f35bf63c68992de8da4a0d389576d3c0`.

## What changes
- `CourseSubCardContentSupport.inspect()` requires a nonblank SubCard course title instead of comparing the SubCard title to the parent card's promotional `h6`.
- Keeps all instructor-name, duration/week/level, avatar, description, detail-button, and missing-SubCard checks.
- Keeps the public `courseNamesMatch()` helper used by COURSE-DETAIL-005 for actual course-title comparisons.
- Updates TestNG @Story and @Description for COURSE-INTEGRITY-003A/B/C. Test names, TestNG groups, methods, reporting and Allure attachments stay unchanged.

## Apply from your existing VLearningAutomation project directory (PowerShell)

Extract the ZIP into the project root (replace `$HOME\Downloads` if you saved it elsewhere):

```powershell
Expand-Archive -Path "$HOME\Downloads\STAB010-CourseSubCard-Fix.zip" -DestinationPath . -Force
git apply --check .\STAB010-CourseSubCard-Contract.patch
git apply .\STAB010-CourseSubCard-Contract.patch
```

The check is deliberate: if your local source has changed in those exact sections, stop and do not force the patch. The patch makes no changes to the remote GitHub repository.

## One targeted run

```powershell
.\gradlew.bat test --tests "vn.cybersoft.vlearning.tests.CourseListTests.everyHomepageSubCardShouldContainCorrectCourseInformation" --tests "vn.cybersoft.vlearning.tests.CourseListTests.everyCourseListSubCardShouldContainCorrectCourseInformation" --tests "vn.cybersoft.vlearning.tests.CourseListTests.everyCategorySubCardShouldContainCorrectCourseInformation"
```

Expected: The invalid parent-description/title equality failure no longer exists. Actual mismatched instructors, incorrect Detail button content or missing SubCards still fail. This has NOT been run in a real Selenium browser here.
