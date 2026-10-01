package vn.cybersoft.vlearning.data.CourseList;

import java.util.List;

public final class CourseSubCardSupport {

    private CourseSubCardSupport() {
    }

    public record MissingSubCard(
            String surface,
            String location,
            int cardIndex,
            String courseName,
            String href
    ) {

        public String describe() {

            return "Surface: " + surface
                    + "\nLocation: " + location
                    + "\nCard position: " + cardIndex
                    + "\nCourse name: " + courseName
                    + "\nHref: " + href
                    + "\nReason: Missing subCard element.";
        }
    }

    public static String buildDetailedSummary(
            String testId,
            String surface,
            String coverageLabel,
            int coverageCount,
            int totalCardsChecked,
            List<MissingSubCard> missingSubCards
    ) {

        StringBuilder report =
                new StringBuilder(testId)
                        .append(
                                " - Course Card subCard Integrity\n"
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
                        .append("Missing subCards: ")
                        .append(missingSubCards.size())
                        .append('\n');

        for (int index = 0;
             index < missingSubCards.size();
             index++) {

            report.append("\nFailure ")
                    .append(index + 1)
                    .append('\n')
                    .append(
                            missingSubCards
                                    .get(index)
                                    .describe()
                    )
                    .append('\n');
        }

        return report.toString();
    }
}