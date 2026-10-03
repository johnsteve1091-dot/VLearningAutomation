package vn.cybersoft.vlearning.data.CourseList;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import vn.cybersoft.vlearning.pages.CourseCardSnapshot;
import vn.cybersoft.vlearning.pages.CourseCardSnapshot.ParentContent;

public final class CourseCardContentSupport {

    /*
     * Current system price format observed on /khoahoc:
     *
     * 800.000đ
     * 400.000đ
     * 1.200.000đ
     *
     * Thousands use "." and currency suffix is "đ".
     */
    private static final Pattern PRICE_PATTERN =
            Pattern.compile(
                    "^(?:0|[1-9]\\d{0,2}(?:\\.\\d{3})*)đ$"
            );

    private CourseCardContentSupport() {
    }

    public record FieldIssue(
            String field,
            String value,
            String reason
    ) {

        public String describe() {

            return "Field: " + field
                    + "\nValue: " + display(value)
                    + "\nReason: " + reason;
        }
    }

    public record CardProblem(
            int page,
            int cardIndex,
            String courseName,
            String href,
            List<FieldIssue> issues
    ) {

        public String fields() {

            Set<String> names =
                    new LinkedHashSet<>();

            for (FieldIssue issue
                    : issues) {

                names.add(
                        issue.field()
                );
            }

            return String.join(
                    ", ",
                    names
            );
        }

        public String describe() {

            StringBuilder report =
                    new StringBuilder()
                            .append("Page: ")
                            .append(page)
                            .append('\n')
                            .append("Card position: ")
                            .append(cardIndex)
                            .append('\n')
                            .append("Course name: ")
                            .append(
                                    display(
                                            courseName
                                    )
                            )
                            .append('\n')
                            .append("Href: ")
                            .append(
                                    display(
                                            href
                                    )
                            )
                            .append('\n')
                            .append("Problem fields: ")
                            .append(
                                    fields()
                            )
                            .append('\n');

            for (int index = 0;
                 index < issues.size();
                 index++) {

                report.append(
                                "\n  Field failure "
                        )
                        .append(
                                index + 1
                        )
                        .append('\n')
                        .append(
                                indent(
                                        issues
                                                .get(index)
                                                .describe()
                                )
                        )
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

        ParentContent parent =
                course.parent();

        if (parent == null) {

            issues.add(
                    new FieldIssue(
                            "Main card",
                            "",
                            "Main Course card content snapshot is missing."
                    )
            );

            return List.copyOf(
                    issues
            );
        }

        /*
         * TC_CL_005
         * Course image exists and is not broken/missing.
         */
        validateImage(
                "Course image",
                parent.courseImageSrc(),
                parent.courseImageDisplayed(),
                parent.courseImageNaturalWidth(),
                issues
        );

        /*
         * TC_CL_006
         * Course name is displayed.
         */
        requireValue(
                "Course name",
                parent.courseName(),
                issues
        );

        /*
         * TC_CL_007 is intentionally NOT duplicated here.
         *
         * In the current frontend implementation the Course description
         * lives in subCard and is already validated by TC3.
         */

        /*
         * TC_CL_008
         * Instructor avatar is displayed.
         */
        validateImage(
                "Instructor avatar",
                parent.instructorAvatarSrc(),
                parent.instructorAvatarDisplayed(),
                parent.instructorAvatarNaturalWidth(),
                issues
        );

        /*
         * TC_CL_009
         * Instructor name is displayed.
         */
        requireValue(
                "Instructor name",
                parent.instructorName(),
                issues
        );

        /*
         * TC_CL_010
         * Original price exists and follows the current system format.
         */
        validateRequiredPrice(
                "Original price",
                parent.originalPrice(),
                issues
        );

        /*
         * TC_CL_011
         *
         * Discount is conditional.
         *
         * We cannot invent which courses SHOULD be discounted without
         * an independent source of truth.
         *
         * Therefore:
         * - if the DOM exposes a discounted price, validate it
         * - and require the original price to be struck through.
         */
        if (!isBlank(
                parent.discountedPrice()
        )) {

            validatePriceFormat(
                    "Discounted price",
                    parent.discountedPrice(),
                    issues
            );

            if (!containsLineThrough(
                    parent.originalPriceDecoration()
            )) {

                issues.add(
                        new FieldIssue(
                                "Original price strikethrough",
                                parent.originalPriceDecoration(),
                                "A discounted price is displayed, but the original price is not struck through."
                        )
                );
            }
        }

        /*
         * TC_CL_012
         * Rating score and review count are displayed.
         *
         * The legacy Course List requirement only requires display,
         * so TC4 does not invent a rating range or review-count format.
         */
        requireValue(
                "Rating score",
                parent.ratingScore(),
                issues
        );

        requireValue(
                "Review count",
                parent.reviewCount(),
                issues
        );

        return List.copyOf(
                issues
        );
    }

    private static void validateImage(
            String field,
            String src,
            boolean displayed,
            int naturalWidth,
            List<FieldIssue> issues
    ) {

        if (isBlank(
                src
        )) {

            issues.add(
                    new FieldIssue(
                            field,
                            src,
                            "Image src is missing or blank."
                    )
            );

            return;
        }

        if (!displayed) {

            issues.add(
                    new FieldIssue(
                            field,
                            src,
                            "Image element exists but is not displayed."
                    )
            );

            return;
        }

        if (naturalWidth <= 0) {

            issues.add(
                    new FieldIssue(
                            field,
                            src,
                            "Image failed to load or is broken because naturalWidth is 0."
                    )
            );
        }
    }

    private static void requireValue(
            String field,
            String value,
            List<FieldIssue> issues
    ) {

        if (isBlank(
                value
        )) {

            issues.add(
                    new FieldIssue(
                            field,
                            value,
                            "Required Course card value is missing or blank."
                    )
            );
        }
    }

    private static void validateRequiredPrice(
            String field,
            String value,
            List<FieldIssue> issues
    ) {

        if (isBlank(
                value
        )) {

            issues.add(
                    new FieldIssue(
                            field,
                            value,
                            "Required Course price is missing or blank."
                    )
            );

            return;
        }

        validatePriceFormat(
                field,
                value,
                issues
        );
    }

    private static void validatePriceFormat(
            String field,
            String value,
            List<FieldIssue> issues
    ) {

        String normalizedPrice =
                normalizePrice(
                        value
                );

        if (!PRICE_PATTERN
                .matcher(
                        normalizedPrice
                )
                .matches()) {

            issues.add(
                    new FieldIssue(
                            field,
                            value,
                            "Price does not match the current system format, for example 400.000đ."
                    )
            );
        }
    }

    private static boolean containsLineThrough(
            String decoration
    ) {

        return decoration != null
                && decoration
                .toLowerCase()
                .contains(
                        "line-through"
                );
    }

    private static String normalizePrice(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replaceAll(
                        "\\s+",
                        ""
                )
                .trim();
    }

    public static String buildDetailedSummary(
            int pagesChecked,
            int totalCardsChecked,
            List<CardProblem> problems
    ) {

        int fieldFailures =
                0;

        for (CardProblem problem
                : problems) {

            fieldFailures +=
                    problem
                            .issues()
                            .size();
        }

        StringBuilder report =
                new StringBuilder(
                        "COURSE-INTEGRITY-004 - Main Course Card Content Integrity\n"
                )
                        .append(
                                "Surface: Khóa học\n"
                        )
                        .append(
                                "Mapped legacy cases: "
                                        + "TC_CL_005, TC_CL_006, "
                                        + "TC_CL_008, TC_CL_009, "
                                        + "TC_CL_010, TC_CL_011, TC_CL_012\n"
                        )
                        .append(
                                "TC_CL_007 description: covered by TC3 subCard content integrity.\n"
                        )
                        .append(
                                "Pagination pages checked: "
                        )
                        .append(
                                pagesChecked
                        )
                        .append('\n')
                        .append(
                                "Course cards checked: "
                        )
                        .append(
                                totalCardsChecked
                        )
                        .append('\n')
                        .append(
                                "Cards with problems: "
                        )
                        .append(
                                problems.size()
                        )
                        .append('\n')
                        .append(
                                "Field failures: "
                        )
                        .append(
                                fieldFailures
                        )
                        .append('\n');

        for (int index = 0;
             index < problems.size();
             index++) {

            report.append(
                            "\nCard failure "
                    )
                    .append(
                            index + 1
                    )
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

        return isBlank(
                value
        )
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