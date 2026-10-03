package vn.cybersoft.vlearning.data.CourseList;

import java.net.URI;
import java.util.List;

public final class CourseListSupport {

    private static final String COURSE_DETAIL_PATH_PREFIX =
            "/chitiet/";

    private CourseListSupport() {
    }

    public record CourseUrlProblem(
            String surface,
            String location,
            int cardIndex,
            String courseName,
            String href,
            String reason
    ) {

        public String describe() {

            return "Surface: " + surface
                    + "\nLocation: " + location
                    + "\nCard position: " + cardIndex
                    + "\nCourse name: " + display(courseName)
                    + "\nHref: " + display(href)
                    + "\nReason: " + reason;
        }
    }

    public static String findCourseUrlProblem(
            String href
    ) {

        if (href == null || href.isBlank()) {
            return "Course href is null or blank.";
        }

        try {

            URI uri =
                    URI.create(
                            href.trim()
                    );

            String path =
                    uri.getRawPath();

            if (path == null || path.isBlank()) {
                return "Course href has no URL path.";
            }

            if (!path.startsWith(
                    COURSE_DETAIL_PATH_PREFIX
            )) {
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
            String testId,
            String surface,
            String coverageLabel,
            int coverageCount,
            int totalCardsChecked,
            List<CourseUrlProblem> problems
    ) {

        StringBuilder report =
                new StringBuilder(testId)
                        .append(
                                " - Course Detail URL ID Integrity\n"
                        )
                        .append("Surface: ")
                        .append(surface)
                        .append('\n')
                        .append(coverageLabel)
                        .append(": ")
                        .append(coverageCount)
                        .append('\n')
                        .append("Course cards checked: ")
                        .append(totalCardsChecked)
                        .append('\n')
                        .append("Missing/invalid Course Detail IDs: ")
                        .append(problems.size())
                        .append('\n');

        for (int index = 0;
             index < problems.size();
             index++) {

            report.append("\nFailure ")
                    .append(index + 1)
                    .append('\n')
                    .append(
                            problems
                                    .get(index)
                                    .describe()
                    )
                    .append('\n');
        }

        return report.toString();
    }

    private static String display(
            String value
    ) {

        return value == null
                || value.isBlank()
                ? "<missing>"
                : value;
    }
}