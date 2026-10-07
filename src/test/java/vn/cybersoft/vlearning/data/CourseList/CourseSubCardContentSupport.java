package vn.cybersoft.vlearning.data.CourseList;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import vn.cybersoft.vlearning.pages.CourseCardSnapshot;
import vn.cybersoft.vlearning.pages.CourseCardSnapshot.ParentContent;
import vn.cybersoft.vlearning.pages.CourseCardSnapshot.SubCardContent;

public final class CourseSubCardContentSupport {

    private static final String EXPECTED_DETAIL_BUTTON =
            "XEM CHI TIẾT";

    private CourseSubCardContentSupport() {
    }
    private static void compareWhenParentProvidesValue(
            String field,
            String parentValue,
            String subCardValue,
            List<FieldIssue> issues
    ) {

        /*
         * Not every Course-card surface exposes Hours / Weeks / Level.
         *
         * When the parent does expose the field, the subCard must expose
         * the same value.
         *
         * When the parent does not expose it, there is no equivalent
         * parent value to compare against.
         */
        if (isBlank(
                parentValue
        )) {

            return;
        }

        if (isBlank(
                subCardValue
        )) {

            issues.add(
                    new FieldIssue(
                            field,
                            parentValue,
                            subCardValue,
                            "Parent Course card exposes this field, but subCard value is missing."
                    )
            );

            return;
        }

        if (!parentValue.equals(
                subCardValue
        )) {

            issues.add(
                    new FieldIssue(
                            field,
                            parentValue,
                            subCardValue,
                            "Parent Course card and subCard values do not match."
                    )
            );
        }
    }
    public record FieldIssue(
            String field,
            String parentValue,
            String subCardValue,
            String reason
    ) {

        public String describe() {

            return "Field: " + field
                    + "\nParent value: " + display(parentValue)
                    + "\nSubCard value: " + display(subCardValue)
                    + "\nReason: " + reason;
        }
    }

    public record CardProblem(
            String surface,
            String location,
            int cardIndex,
            String courseName,
            String href,
            List<FieldIssue> issues
    ) {

        public String fields() {

            Set<String> fieldNames =
                    new LinkedHashSet<>();

            for (FieldIssue issue : issues) {
                fieldNames.add(issue.field());
            }

            return String.join(
                    ", ",
                    fieldNames
            );
        }

        public String describe() {

            StringBuilder report =
                    new StringBuilder()
                            .append("Surface: ")
                            .append(surface)
                            .append('\n')
                            .append("Location: ")
                            .append(location)
                            .append('\n')
                            .append("Card position: ")
                            .append(cardIndex)
                            .append('\n')
                            .append("Course name: ")
                            .append(display(courseName))
                            .append('\n')
                            .append("Href: ")
                            .append(display(href))
                            .append('\n')
                            .append("Problem fields: ")
                            .append(fields())
                            .append('\n');

            for (int index = 0;
                 index < issues.size();
                 index++) {

                report.append("\n  Field failure ")
                        .append(index + 1)
                        .append('\n')
                        .append(indent(
                                issues.get(index)
                                        .describe()
                        ))
                        .append('\n');
            }

            return report.toString();
        }
    }

    public static List<FieldIssue> inspect(
            CourseCardSnapshot course
    ) {

        List<FieldIssue> issues =
                new ArrayList<>();

        /*
         * TC3 remains independently runnable.
         *
         * TC2 also catches this, but TC3 should not silently pass
         * a card whose content cannot be inspected at all.
         */
        if (!course.hasSubCard()
                || course.subCard() == null) {

            issues.add(
                    new FieldIssue(
                            "subCard",
                            "",
                            "",
                            "subCard is missing, so its content cannot be verified."
                    )
            );

            return List.copyOf(issues);
        }

        ParentContent parent =
                course.parent();

        SubCardContent subCard =
                course.subCard();

        compareRequired(
                "Course name",
                parent.courseName(),
                subCard.courseName(),
                true,
                issues
        );

        compareRequired(
                "Instructor name",
                parent.instructorName(),
                subCard.instructorName(),
                false,
                issues
        );


        compareWhenParentProvidesValue(
                "Hours",
                parent.hours(),
                subCard.hours(),
                issues
        );

        compareWhenParentProvidesValue(
                "Weeks",
                parent.weeks(),
                subCard.weeks(),
                issues
        );

        compareWhenParentProvidesValue(
                "Level",
                parent.level(),
                subCard.level(),
                issues
        );

        requireSubCardValue(
                "Instructor avatar",
                subCard.instructorAvatarSrc(),
                issues
        );

        requireSubCardValue(
                "Description",
                subCard.description(),
                issues
        );

        validateDetailButton(
                subCard.detailButtonText(),
                issues
        );

        return List.copyOf(issues);
    }

    private static void compareRequired(
            String field,
            String parentValue,
            String subCardValue,
            boolean allowTruncatedCourseName,
            List<FieldIssue> issues
    ) {

        if (isBlank(parentValue)) {

            issues.add(
                    new FieldIssue(
                            field,
                            parentValue,
                            subCardValue,
                            "Parent Course card value is missing."
                    )
            );

            return;
        }

        if (isBlank(subCardValue)) {

            issues.add(
                    new FieldIssue(
                            field,
                            parentValue,
                            subCardValue,
                            "SubCard value is missing."
                    )
            );

            return;
        }

        boolean matches =
                allowTruncatedCourseName
                        ? courseNamesMatch(
                        parentValue,
                        subCardValue
                )
                        : parentValue.equals(
                        subCardValue
                );

        if (!matches) {

            issues.add(
                    new FieldIssue(
                            field,
                            parentValue,
                            subCardValue,
                            "Parent Course card and subCard values do not match."
                    )
            );
        }
    }

    /*
     * Some frontend card titles may already contain an ellipsis.
     *
     * Exact equality is preferred.
     *
     * If either side ends in ... or …, the visible/DOM value may
     * represent a shortened title, so prefix equivalence is allowed.
     *
     * This avoids falsely failing an intentionally truncated title
     * while still catching completely unrelated course names.
     */
    public static boolean courseNamesMatch(
            String parentValue,
            String subCardValue
    ) {

        if (parentValue.equals(subCardValue)) {
            return true;
        }

        String parentPrefix =
                removeTrailingEllipsis(parentValue);

        if (!parentPrefix.equals(parentValue)
                && subCardValue.startsWith(
                parentPrefix
        )) {

            return true;
        }

        String subCardPrefix =
                removeTrailingEllipsis(subCardValue);

        return !subCardPrefix.equals(subCardValue)
                && parentValue.startsWith(
                subCardPrefix
        );
    }

    private static String removeTrailingEllipsis(
            String value
    ) {

        if (value.endsWith("...")) {

            return value.substring(
                            0,
                            value.length() - 3
                    )
                    .trim();
        }

        if (value.endsWith("…")) {

            return value.substring(
                            0,
                            value.length() - 1
                    )
                    .trim();
        }

        return value;
    }

    private static void requireSubCardValue(
            String field,
            String value,
            List<FieldIssue> issues
    ) {

        if (isBlank(value)) {

            issues.add(
                    new FieldIssue(
                            field,
                            "",
                            value,
                            "Required SubCard value is missing or blank."
                    )
            );
        }
    }

    private static void validateDetailButton(
            String buttonText,
            List<FieldIssue> issues
    ) {

        if (isBlank(buttonText)) {

            issues.add(
                    new FieldIssue(
                            "Detail button",
                            EXPECTED_DETAIL_BUTTON,
                            buttonText,
                            "SubCard detail button is missing or blank."
                    )
            );

            return;
        }

        if (!EXPECTED_DETAIL_BUTTON.equals(
                buttonText
        )) {

            issues.add(
                    new FieldIssue(
                            "Detail button",
                            EXPECTED_DETAIL_BUTTON,
                            buttonText,
                            "SubCard detail button text is incorrect."
                    )
            );
        }
    }

    public static String buildDetailedSummary(
            String testId,
            String surface,
            String coverageLabel,
            int coverageCount,
            int totalCardsChecked,
            List<CardProblem> problems
    ) {

        int fieldFailures = 0;

        for (CardProblem problem : problems) {
            fieldFailures +=
                    problem.issues().size();
        }

        StringBuilder report =
                new StringBuilder(testId)
                        .append(
                                " - SubCard Content Integrity\n"
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
                        .append("Cards with problems: ")
                        .append(problems.size())
                        .append('\n')
                        .append("Field failures: ")
                        .append(fieldFailures)
                        .append('\n');

        for (int index = 0;
             index < problems.size();
             index++) {

            report.append("\nCard failure ")
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

    private static boolean isBlank(
            String value
    ) {

        return value == null
                || value.isBlank();
    }

    private static String display(
            String value
    ) {

        return isBlank(value)
                ? "<missing>"
                : value;
    }

    private static String indent(
            String value
    ) {

        return value.replace(
                "\n",
                "\n    "
        );
    }
}
