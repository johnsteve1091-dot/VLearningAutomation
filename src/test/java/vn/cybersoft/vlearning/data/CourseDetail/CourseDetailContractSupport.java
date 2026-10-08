package vn.cybersoft.vlearning.data.CourseDetail;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vn.cybersoft.vlearning.data.CourseList.CourseListSupport;
import vn.cybersoft.vlearning.pages.CourseCardSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.CoreSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.CurriculumSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.ElementState;
import vn.cybersoft.vlearning.pages.CourseDetailPage.ImageState;
import vn.cybersoft.vlearning.pages.CourseDetailPage.LessonSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.ModuleSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.RelatedCardSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.RelatedCoursesSnapshot;

/** Snapshot-only validation and evidence for the core, curriculum and related-card contracts. */
public final class CourseDetailContractSupport {

    private static final Logger LOGGER = LogManager.getLogger(CourseDetailContractSupport.class);
    private static final String TEST_ID = "COURSE-DETAIL-001";
    private static final String LEARNING_HEADING = "Những gì bạn sẽ học";

    private CourseDetailContractSupport() {
    }

    public record ContractResult(List<String> issues, String summary) {
        public ContractResult {
            issues = List.copyOf(issues);
        }
    }

    public record RelatedContractResult(List<String> issues, String summary, int problemCards) {
        public RelatedContractResult {
            issues = List.copyOf(issues);
        }
    }

    public static ContractResult inspectCore(CoreSnapshot state) {
        List<String> issues = new ArrayList<>();

        checkDisplayed(issues, "Course Detail root", state.root());
        checkDisplayed(issues, "Main detail content", state.content());
        checkDisplayed(issues, "Thông tin khóa học", state.title());
        checkDisplayed(issues, "Tiến lên và không chần chừ !!!", state.subtitle());
        checkText(issues, "Course name", state.courseName());
        require(issues, state.instructorLabel().present(), "Giảng viên label", "missing");
        checkText(issues, "Instructor name", state.instructorName());
        checkImage(issues, "Instructor avatar", state.instructorAvatar());
        require(issues, state.categoryLabel().present(), "Lĩnh vực label", "missing");
        checkText(issues, "Field/category", state.categoryValue());
        require(issues, state.review().present(), "Rating/review container", "missing");
        require(issues, state.displayedStars() > 0, "Rating stars", "no displayed star icons");
        checkText(issues, "Rating score", state.rating());
        checkText(issues, "Review count", state.reviewCount());
        checkText(issues, "Course description", state.description());
        checkDisplayed(issues, "Learning objectives", state.learningObjectives());
        String learningText = state.learningObjectives().text().replace('\u00a0', ' ')
                .replaceAll("\\s+", " ").strip();
        require(issues, learningText.contains(LEARNING_HEADING), "Learning objectives heading",
                "rendered text does not contain \"" + LEARNING_HEADING + "\"");
        require(issues, learningText.replace(LEARNING_HEADING, "").codePoints()
                        .anyMatch(Character::isLetterOrDigit),
                "Learning objectives content", "no meaningful content beyond the heading");
        checkDisplayed(issues, "Course sidebar", state.sidebar());
        checkImage(issues, "Course image", state.courseImage());
        checkText(issues, "Price", state.price());
        checkText(issues, "Ghi danh", state.students());
        checkText(issues, "Thời gian", state.duration());
        checkText(issues, "Bài học", state.lessons());
        checkText(issues, "Video", state.videos());
        checkText(issues, "Trình độ", state.level());
        checkDisplayed(issues, "Đăng ký button", state.registerButton());
        if (state.registerButton().present()) {
            require(issues, state.registerButton().enabled(), "Đăng ký button", "disabled");
        }

        return new ContractResult(issues, buildSummary(state, issues));
    }

    public static ContractResult inspectCurriculum(CurriculumSnapshot state) {
        List<String> issues = new ArrayList<>();

        checkDisplayed(issues, "Curriculum", state.section());
        // An absent/hidden section is one defect, not a cascade of invisible children.
        if (state.section().displayed()) {
            checkDisplayed(issues, "Nội dung khóa học title", state.title());
            require(issues, !state.modules().isEmpty(), "Curriculum", "no modules discovered");
            for (ModuleSnapshot module : state.modules()) {
                String scope = "Module " + module.index();
                LOGGER.info("MODULE {} | title=\"{}\" | lessons={}",
                        module.index(), module.title().text(), module.lessons().size());
                require(issues, module.displayed(), scope, "not displayed");
                if (!module.displayed()) continue;

                require(issues, module.contentPresent(), scope + " content", "courseDetailContent missing");
                if (module.headerPresent()) {
                    checkReadable(issues, scope + " title", module.title());
                } else if (module.contentPresent()) {
                    require(issues, false, scope + " header", "sectionCourse missing");
                }
                if (!module.lessonContainerPresent() && module.contentPresent()) {
                    require(issues, false, scope + " lessons", "lessonContainer missing");
                }
                // Inspect available lessons even if this module's other structure is defective.
                for (LessonSnapshot lesson : module.lessons()) {
                    checkReadable(issues, scope + ", lesson " + lesson.index() + " title", lesson.title());
                }
            }
            if (state.lessonCount() == 0 && state.modules().stream()
                    .anyMatch(module -> module.displayed() && module.lessonContainerPresent())) {
                require(issues, false, "Curriculum", "no lessons across the complete curriculum");
            }
        }

        return new ContractResult(issues, buildCurriculumSummary(state, issues));
    }

    public static RelatedContractResult inspectRelated(RelatedCoursesSnapshot state) {
        String testId = "COURSE-DETAIL-003";
        List<String> issues = new ArrayList<>();
        int problemCards = 0;
        checkDisplayed(issues, "Related Courses", state.section());
        if (state.section().displayed()) {
            checkReadable(issues, "Related Courses heading", state.heading());
            if (state.heading().displayed() && !state.heading().text().isBlank()) {
                require(issues, "Khóa học tham khảo".equals(state.heading().text()),
                        "Related Courses heading", "expected Khóa học tham khảo, actual=" + state.heading().text());
            }
            require(issues, !state.cards().isEmpty(), "Related Courses", "no recommendation cards discovered");
            require(issues, state.cards().size() <= 4, "Related Courses", "more than four recommendation cards");
            for (RelatedCardSnapshot card : state.cards()) {
                List<String> cardIssues = inspectRelatedCard(card);
                if (!cardIssues.isEmpty()) problemCards++;
                addRelatedIssues(issues, card.content(), "content", cardIssues);
                LOGGER.info("RELATED CARD {} | course=\"{}\" | issues={}",
                        card.content().cardIndex(), card.content().name(), cardIssues.size());
            }
        }
        StringBuilder report = relatedSummaryHeader(testId, "TC_CD_021-TC_CD_030", state);
        for (RelatedCardSnapshot card : state.cards()) appendRelatedCard(report, card);
        report.append("Total problem cards: ").append(problemCards).append('\n');
        appendRelatedIssues(report, issues);
        return new RelatedContractResult(issues, report.toString(), problemCards);
    }

    static void require(List<String> issues, boolean condition, String field, String reason) {
        if (!condition) {
            issues.add("field=" + field + " | reason=" + reason);
        }
    }

    static void checkDisplayed(List<String> issues, String field, ElementState value) {
        require(issues, value.present() && value.displayed(), field,
                value.present() ? "not displayed" : "missing");
    }

    private static void checkReadable(List<String> issues, String field, ElementState value) {
        if (!value.displayed()) checkDisplayed(issues, field, value);
        else checkText(issues, field, value);
    }

    private static void checkText(List<String> issues, String field, ElementState value) {
        require(issues, value.present() && !value.text().isBlank(), field,
                value.present() ? "rendered text is blank" : "missing");
    }

    private static void checkImage(List<String> issues, String field, ImageState image) {
        if (!image.present()) {
            require(issues, false, field, "missing");
            return;
        }
        List<String> reasons = new ArrayList<>();
        if (!image.displayed()) reasons.add("not displayed");
        if (image.src().isBlank()) reasons.add("src is blank");
        if (!image.complete()) reasons.add("loading incomplete");
        if (image.naturalWidth() <= 0) reasons.add("naturalWidth is zero");
        require(issues, reasons.isEmpty(), field, String.join("; ", reasons));
    }

    private static void checkRelatedImage(List<String> issues, String field, String src, boolean displayed, int width) {
        List<String> reasons = new ArrayList<>();
        if (src.isBlank()) reasons.add("image/src missing or blank");
        if (!displayed) reasons.add("not displayed");
        if (width <= 0) reasons.add("naturalWidth is 0");
        require(issues, reasons.isEmpty(), field, String.join("; ", reasons));
    }

    private static List<String> inspectRelatedCard(RelatedCardSnapshot card) {
        List<String> issues = new ArrayList<>();
        require(issues, card.displayed(), "Card", "not displayed");
        if (!card.displayed()) return issues;
        CourseCardSnapshot content = card.content();
        var parent = content.parent();
        String urlProblem = CourseListSupport.findCourseUrlProblem(content.href());
        require(issues, urlProblem == null, "href", urlProblem);
        require(issues, !content.name().isBlank(), "Course name", "missing/blank");
        checkRelatedImage(issues, "Course image", parent.courseImageSrc(),
                parent.courseImageDisplayed(), parent.courseImageNaturalWidth());
        require(issues, !parent.instructorName().isBlank(), "Instructor name", "missing/blank");
        checkRelatedImage(issues, "Instructor avatar", parent.instructorAvatarSrc(),
                parent.instructorAvatarDisplayed(), parent.instructorAvatarNaturalWidth());
        require(issues, !parent.originalPrice().isBlank(), "Original/current price", "missing/blank");
        if (card.discountedPriceRendered()) {
            require(issues, !parent.discountedPrice().isBlank(), "Discounted price", "rendered but blank");
            if (!parent.originalPrice().isBlank()) {
                require(issues, parent.originalPriceDecoration().contains("line-through"),
                        "Original price decoration", "rendered discount requires line-through");
            }
        }
        if (content.hasSubCard()) {
            require(issues, !content.subCard().description().isBlank(), "subCard description", "missing/blank DOM text");
        }
        return issues;
    }

    private static void addRelatedIssues(List<String> issues, CourseCardSnapshot card, String stage, List<String> found) {
        for (String issue : found) issues.add("card=" + card.cardIndex() + " | course=\"" + card.name()
                + "\" | href=" + card.href() + " | stage=" + stage + " | " + issue);
    }

    private static String buildSummary(CoreSnapshot state, List<String> issues) {
        StringBuilder report = new StringBuilder(TEST_ID + " - Course Detail Core Contract\n")
                .append("Legacy coverage: TC_CD_001–TC_CD_017\n")
                .append("Requested URL: ").append(state.requestedUrl()).append('\n')
                .append("Resulting browser URL: ").append(state.resultingUrl()).append('\n');
        appendField(report, "Course Detail root", state.root());
        appendField(report, "Main detail content", state.content());
        appendField(report, "Page title", state.title());
        appendField(report, "Page subtitle", state.subtitle());
        appendField(report, "Course name", state.courseName());
        appendField(report, "Instructor label", state.instructorLabel());
        appendField(report, "Instructor name", state.instructorName());
        appendImage(report, "Instructor avatar", state.instructorAvatar());
        appendField(report, "Field/category label", state.categoryLabel());
        appendField(report, "Field/category value", state.categoryValue());
        appendField(report, "Rating/review container", state.review());
        report.append("Displayed star icons: ").append(state.displayedStars()).append('\n');
        appendField(report, "Rating score", state.rating());
        appendField(report, "Review count", state.reviewCount());
        appendField(report, "Course description", state.description());
        appendField(report, "Learning objectives", state.learningObjectives());
        appendField(report, "Course sidebar", state.sidebar());
        appendImage(report, "Course image", state.courseImage());
        appendField(report, "Price", state.price());
        appendField(report, "Ghi danh value", state.students());
        appendField(report, "Thời gian value", state.duration());
        appendField(report, "Bài học value", state.lessons());
        appendField(report, "Video value", state.videos());
        appendField(report, "Trình độ value", state.level());
        appendField(report, "Đăng ký button", state.registerButton());
        report.append("Đăng ký enabled: ").append(state.registerButton().enabled()).append('\n')
                .append("Total issues: ").append(issues.size()).append('\n');
        if (issues.isEmpty()) report.append("No core-contract issues detected.\n");
        else issues.forEach(issue -> report.append("- ").append(issue).append('\n'));
        return report.toString();
    }

    private static String buildCurriculumSummary(CurriculumSnapshot state, List<String> issues) {
        StringBuilder report = new StringBuilder("COURSE-DETAIL-002 - Course Curriculum Contract\n")
                .append("Legacy coverage: TC_CD_019, TC_CD_020\n")
                .append("Requested URL: ").append(state.requestedUrl()).append('\n')
                .append("Resulting URL: ").append(state.resultingUrl()).append('\n');
        appendField(report, "Curriculum section", state.section());
        appendField(report, "Curriculum title", state.title());
        report.append("Total modules discovered: ").append(state.modules().size()).append('\n')
                .append("Total lessons discovered: ").append(state.lessonCount()).append('\n');
        for (ModuleSnapshot module : state.modules()) {
            report.append("Module ").append(module.index()).append(" | title=\"").append(module.title().text())
                    .append("\" | displayed=").append(module.displayed())
                    .append(" | contentPresent=").append(module.contentPresent())
                    .append(" | headerPresent=").append(module.headerPresent())
                    .append(" | lessonContainerPresent=").append(module.lessonContainerPresent())
                    .append(" | lessons=").append(module.lessons().size()).append('\n');
            appendField(report, "  Module title", module.title());
            for (LessonSnapshot lesson : module.lessons()) {
                appendField(report, "  Lesson " + lesson.index(), lesson.title());
            }
        }
        report.append("Total issues: ").append(issues.size()).append('\n');
        if (issues.isEmpty()) report.append("No curriculum-contract issues detected.\n");
        else issues.forEach(issue -> report.append("- ").append(issue).append('\n'));
        return report.toString();
    }

    private static StringBuilder relatedSummaryHeader(String testId, String legacy, RelatedCoursesSnapshot state) {
        StringBuilder report = new StringBuilder(testId).append("\nLegacy coverage: ").append(legacy)
                .append("\nRequested URL: ").append(state.requestedUrl())
                .append("\nResulting URL: ").append(state.resultingUrl()).append('\n');
        appendField(report, "Related Courses section", state.section());
        appendField(report, "Section heading", state.heading());
        return report.append("Total related cards discovered: ").append(state.cards().size()).append('\n');
    }

    private static void appendRelatedCard(StringBuilder report, RelatedCardSnapshot card) {
        var content = card.content();
        var parent = content.parent();
        report.append("\nCard ").append(content.cardIndex()).append(" | course=\"").append(content.name())
                .append("\" | href=").append(content.href()).append(" | displayed=").append(card.displayed())
                .append("\nCourse image: src=").append(parent.courseImageSrc())
                .append(" | displayed=").append(parent.courseImageDisplayed())
                .append(" | naturalWidth=").append(parent.courseImageNaturalWidth())
                .append("\nInstructor: ").append(parent.instructorName())
                .append("\nInstructor avatar: src=").append(parent.instructorAvatarSrc())
                .append(" | displayed=").append(parent.instructorAvatarDisplayed())
                .append(" | naturalWidth=").append(parent.instructorAvatarNaturalWidth())
                .append("\nOriginal/current price: ").append(parent.originalPrice())
                .append(" | decoration=").append(parent.originalPriceDecoration())
                .append("\nDiscounted price: ").append(parent.discountedPrice())
                .append(" | rendered=").append(card.discountedPriceRendered())
                .append("\nMain specifications: hours=").append(parent.hours()).append(" | weeks=").append(parent.weeks())
                .append(" | level=").append(parent.level()).append("\nsubCard present: ").append(content.hasSubCard());
        if (content.hasSubCard()) {
            var sub = content.subCard();
            report.append("\nDescription: ").append(sub.description()).append("\nsubCard specifications: hours=")
                    .append(sub.hours()).append(" | weeks=").append(sub.weeks()).append(" | level=").append(sub.level());
        }
        report.append('\n');
    }

    static void appendRelatedIssues(StringBuilder report, List<String> issues) {
        report.append("Total field failures/issues: ").append(issues.size()).append('\n');
        issues.forEach(issue -> report.append("- ").append(issue).append('\n'));
    }

    private static void appendField(StringBuilder report, String name, ElementState value) {
        report.append(name).append(": present=").append(value.present())
                .append(", displayed=").append(value.displayed())
                .append(", text=\"").append(value.text()).append("\"\n");
    }

    private static void appendImage(StringBuilder report, String name, ImageState image) {
        report.append(name).append(": present=").append(image.present())
                .append(", displayed=").append(image.displayed()).append(", src=\"").append(image.src())
                .append("\", complete=").append(image.complete()).append(", naturalWidth=")
                .append(image.naturalWidth()).append(", loaded=").append(image.loaded()).append('\n');
    }
}
