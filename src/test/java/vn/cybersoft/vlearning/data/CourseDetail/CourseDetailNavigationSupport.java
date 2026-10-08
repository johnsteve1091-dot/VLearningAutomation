package vn.cybersoft.vlearning.data.CourseDetail;

import static vn.cybersoft.vlearning.data.CourseDetail.CourseDetailContractSupport.appendRelatedIssues;
import static vn.cybersoft.vlearning.data.CourseDetail.CourseDetailContractSupport.checkDisplayed;
import static vn.cybersoft.vlearning.data.CourseDetail.CourseDetailContractSupport.require;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import vn.cybersoft.vlearning.data.CourseList.CourseListSupport;
import vn.cybersoft.vlearning.pages.CourseDetailPage;
import vn.cybersoft.vlearning.pages.CourseDetailPage.NavigationSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.RelatedNavigationTarget;

/** Orchestrates existing Page Object navigation and evaluates only href/root outcomes. */
public final class CourseDetailNavigationSupport {

    private static final Logger LOGGER = LogManager.getLogger(CourseDetailNavigationSupport.class);

    private CourseDetailNavigationSupport() {
    }

    public record NavigationResult(List<String> issues, String summary) {
        public NavigationResult {
            issues = List.copyOf(issues);
        }
    }

    public static NavigationResult inspect(CourseDetailPage page, List<RelatedNavigationTarget> targets) {
        String testId = "COURSE-DETAIL-004";
        List<String> issues = new ArrayList<>();
        StringBuilder report = new StringBuilder(testId)
                .append(" - Related Course Navigation\nSource: ")
                .append(CourseDetailPage.KNOWN_COURSE_URL)
                .append("\nCards discovered: ").append(targets.size()).append('\n');

        for (RelatedNavigationTarget target : targets) {
            String urlProblem = CourseListSupport.findCourseUrlProblem(target.href());
            NavigationSnapshot main = null;
            NavigationSnapshot detail = null;
            List<String> mainIssues = new ArrayList<>();
            List<String> detailIssues = new ArrayList<>();

            if (urlProblem != null) {
                mainIssues.add("field=href | reason=" + urlProblem);
                detailIssues.add("field=href | reason=" + urlProblem);
            } else {
                page.openKnownCourse();
                main = page.navigateRelatedCard(target);
                mainIssues.addAll(inspectRelatedNavigation(main));

                page.openKnownCourse();
                detail = page.navigateRelatedDetailButton(target);
                detailIssues.addAll(inspectRelatedNavigation(detail));
            }

            addRelatedNavigationIssues(issues, target, "card navigation", mainIssues);
            addRelatedNavigationIssues(issues, target, "detail-button navigation", detailIssues);
            appendRelatedNavigation(report, "MAIN CARD", target, main, mainIssues);
            appendRelatedNavigation(report, "XEM CHI TIẾT", target, detail, detailIssues);

            LOGGER.info("RELATED CARD {} | cardNavigation={} | detailButtonNavigation={}",
                    target.cardIndex(), mainIssues.isEmpty() ? "PASS" : "FAIL",
                    detailIssues.isEmpty() ? "PASS" : "FAIL");
        }

        report.append("\nCOURSE-DETAIL-004 COMPLETE | cards=").append(targets.size())
                .append(" | issues=").append(issues.size()).append('\n');
        appendRelatedIssues(report, issues);
        return new NavigationResult(issues, report.toString());
    }

    private static List<String> inspectRelatedNavigation(NavigationSnapshot navigation) {
        List<String> issues = new ArrayList<>();
        if (!navigation.clicked()) {
            require(issues, false, "Click prerequisite",
                    navigation.problem().isBlank() ? "click was not performed" : navigation.problem());
            return issues;
        }
        if (!navigation.navigated()) {
            require(issues, false, "Navigation", "click produced no observable navigation");
            return issues;
        }
        if (!CourseDetailPage.sameBrowserUrl(navigation.source().href(), navigation.resultingUrl())) {
            require(issues, false, "Destination URL", "expected=" + navigation.source().href()
                    + " | actual=" + navigation.resultingUrl());
            return issues;
        }
        checkDisplayed(issues, "Destination Course Detail root", navigation.root());
        return issues;
    }

    private static void addRelatedNavigationIssues(
            List<String> issues,
            RelatedNavigationTarget target,
            String stage,
            List<String> found
    ) {
        for (String issue : found) {
            issues.add("card=" + target.cardIndex() + " | href=" + target.href()
                    + " | stage=" + stage + " | " + issue);
        }
    }

    private static void appendRelatedNavigation(
            StringBuilder report,
            String label,
            RelatedNavigationTarget target,
            NavigationSnapshot navigation,
            List<String> issues
    ) {
        report.append("\n").append(label)
                .append(" | card=").append(target.cardIndex())
                .append(" | expected href=").append(target.href()).append('\n');
        if (navigation == null) {
            report.append("  not attempted because href prerequisite failed\n");
        } else {
            report.append("  source URL=").append(navigation.sourceUrl())
                    .append("\n  clicked=").append(navigation.clicked())
                    .append(" | navigated=").append(navigation.navigated())
                    .append("\n  resulting URL=").append(navigation.resultingUrl())
                    .append("\n  URL match=").append(CourseDetailPage.sameBrowserUrl(target.href(), navigation.resultingUrl()))
                    .append("\n  Course Detail root displayed=").append(navigation.root().displayed())
                    .append("\n  problem=").append(navigation.problem()).append('\n');
        }
        if (!issues.isEmpty()) {
            for (String issue : issues) report.append("  FAIL | ").append(issue).append('\n');
        }
    }
}
