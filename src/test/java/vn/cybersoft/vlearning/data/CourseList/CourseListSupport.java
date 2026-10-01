package vn.cybersoft.vlearning.data.CourseList;

import java.net.URI;
import java.util.List;

import vn.cybersoft.vlearning.pages.CourseListPage.CourseCandidate;

public final class CourseListSupport {

    private static final String COURSE_DETAIL_PATH_PREFIX =
            "/chitiet/";

    private CourseListSupport() {
    }

    public record MissingCourseId(
            CourseCandidate course,
            String reason
    ) {

        public String describe() {
            return "Name: " + course.name()
                    + "\nHref: " + course.href()
                    + "\nSource page: " + course.page()
                    + "\nCard position: " + course.cardIndex()
                    + "\nReason: " + reason;
        }
    }

    public static String findCourseUrlProblem(String href) {

        if (href == null || href.isBlank()) {
            return "Course href is null or blank.";
        }

        try {
            URI uri = URI.create(href.trim());

            String path = uri.getRawPath();

            if (path == null || path.isBlank()) {
                return "Course href has no URL path.";
            }

            if (!path.startsWith(COURSE_DETAIL_PATH_PREFIX)) {
                return "Course href does not target /chitiet/.";
            }

            String courseId =
                    path.substring(
                            COURSE_DETAIL_PATH_PREFIX.length()
                    );

            if (courseId.isBlank()) {
                return "Course Detail URL has no course ID after /chitiet/.";
            }

            return null;

        } catch (IllegalArgumentException invalidUrl) {
            return "Course href is not a valid URI.";
        }
    }

    public static String buildDetailedSummary(
            int pagesChecked,
            int totalCardsChecked,
            List<MissingCourseId> missingIds
    ) {

        StringBuilder report =
                new StringBuilder(
                        "COURSE-LIST-001 - Course Detail URL ID Integrity\n"
                )
                        .append("Pagination pages checked: ")
                        .append(pagesChecked)
                        .append('\n')
                        .append("Course cards checked: ")
                        .append(totalCardsChecked)
                        .append('\n')
                        .append("Missing/invalid Course Detail IDs: ")
                        .append(missingIds.size())
                        .append('\n');

        for (int index = 0; index < missingIds.size(); index++) {

            report.append("\nFailure ")
                    .append(index + 1)
                    .append('\n')
                    .append(missingIds.get(index).describe())
                    .append('\n');
        }

        return report.toString();
    }
}