package vn.cybersoft.vlearning.data.CourseDetail;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

import vn.cybersoft.vlearning.data.CourseList.CourseListSupport;
import vn.cybersoft.vlearning.data.CourseList.CourseSubCardContentSupport;
import vn.cybersoft.vlearning.pages.CourseCardSnapshot;
import vn.cybersoft.vlearning.pages.CourseCategoryPage;
import vn.cybersoft.vlearning.pages.CourseDetailPage;
import vn.cybersoft.vlearning.pages.CourseDetailPage.DetailIdentitySnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.RelatedCardSnapshot;
import vn.cybersoft.vlearning.pages.CourseDetailPage.RelatedCoursesSnapshot;
import vn.cybersoft.vlearning.pages.CourseListPage;
import vn.cybersoft.vlearning.pages.HomePage;
import vn.cybersoft.vlearning.pages.CourseDetailPage.ImageState;
import vn.cybersoft.vlearning.pages.CourseDetailPage.ElementState;
import vn.cybersoft.vlearning.pages.CourseCardSnapshot.ParentContent;

/** Deterministic source sampling, cached Detail comparison, defects and TC5 evidence. */
public final class CourseDetailConsistencySupport {

    private static final Logger LOGGER = LogManager.getLogger(CourseDetailConsistencySupport.class);
    private static final String HOME_PAGE_URL = "https://demo2.cybersoft.edu.vn/";
    private static final String COURSE_LIST_URL = "https://demo2.cybersoft.edu.vn/khoahoc";

    public static final int PRIMARY_SAMPLE_SIZE = 1;
    public static final int DIAGNOSTIC_MAX_PER_SOURCE = 5;

    private CourseDetailConsistencySupport() {
    }

    public static List<CourseCardSnapshot> firstValidCandidates(List<CourseCardSnapshot> cards) {
        List<CourseCardSnapshot> candidates = new ArrayList<>();
        for (CourseCardSnapshot card : cards) {
            if (CourseListSupport.findCourseUrlProblem(card.href()) == null) {
                candidates.add(card);
                if (candidates.size() == DIAGNOSTIC_MAX_PER_SOURCE) break;
            }
        }
        return List.copyOf(candidates);
    }


    public static ComparisonResult compare(
            CourseCardSnapshot card,
            DetailIdentitySnapshot detail
    ) {
        List<FieldComparison> fields = new ArrayList<>();

        if (!detail.root().present() || !detail.root().displayed()) {
            fields.add(new FieldComparison(
                    "destination",
                    "Course Detail root",
                    "displayed Course Detail",
                    detail.root().present()
                            ? "present but hidden"
                            : "missing",
                    false,
                    "Course Detail root did not load/display."
            ));
            return new ComparisonResult(card, detail, fields);
        }

        ParentContent parent = card.parent();

        if (parent == null) {
            fields.add(new FieldComparison(
                    "main card", "Card content",
                    "present", "missing", false,
                    "Main Course Card snapshot is missing."
            ));
            return new ComparisonResult(card, detail, fields);
        }

        // Course thumbnail
        compareImage(
                fields, "main card", "Course image",
                parent.courseImageSrc(),
                parent.courseImageDisplayed(),
                parent.courseImageNaturalWidth(),
                detail.courseImage(),
                detail.resultingUrl()
        );

        // Main card instructor
        compareInstructor(
                fields, "main card",
                parent.instructorName(),
                detail.instructorName().text()
        );

        // Main card instructor avatar
        compareImage(
                fields, "main card", "Instructor avatar",
                parent.instructorAvatarSrc(),
                parent.instructorAvatarDisplayed(),
                parent.instructorAvatarNaturalWidth(),
                detail.instructorAvatar(),
                detail.resultingUrl()
        );

        // Advertised price
        comparePrice(fields, parent, detail.price());

        // Actual SubCard course identity
        if (card.hasSubCard() && card.subCard() != null) {
            compareCourseName(
                    fields, "subCard",
                    card.subCard().courseName(),
                    detail.courseName().text()
            );

            compareInstructor(
                    fields, "subCard",
                    card.subCard().instructorName(),
                    detail.instructorName().text()
            );

            compareImageReference(
                    fields, "subCard", "Instructor avatar",
                    card.subCard().instructorAvatarSrc(),
                    detail.instructorAvatar(),
                    detail.resultingUrl()
            );
        } else {
            fields.add(new FieldComparison(
                    "subCard", "SubCard presence",
                    "present", "missing", false,
                    "Expected Course SubCard is absent."
            ));
        }

        return new ComparisonResult(card, detail, fields);
    }

    private static void compareImage(
            List<FieldComparison> fields,
            String source,
            String fieldName,
            String advertisedSrc,
            boolean advertisedDisplayed,
            long advertisedNaturalWidth,
            ImageState detailImage,
            String detailUrl
    ) {
        String advertised = resolveImageUrl(advertisedSrc, detailUrl);
        String actual = resolveImageUrl(detailImage.src(), detailUrl);

        boolean sourceLoaded = advertisedDisplayed
                && advertisedNaturalWidth > 0;
        boolean destinationLoaded = detailImage.present()
                && detailImage.displayed()
                && detailImage.loaded();

        boolean matches = sourceLoaded
                && destinationLoaded
                && !advertised.isBlank()
                && advertised.equals(actual);

        fields.add(new FieldComparison(
                source,
                fieldName,
                advertised + " [loaded=" + sourceLoaded + "]",
                actual + " [loaded=" + destinationLoaded + "]",
                matches,
                matches ? "" :
                        "Image missing, broken, or asset URLs differ."
        ));
    }

    private static void compareImageReference(
            List<FieldComparison> fields,
            String source,
            String fieldName,
            String advertisedSrc,
            ImageState detailImage,
            String detailUrl
    ) {
        String advertised = resolveImageUrl(advertisedSrc, detailUrl);
        String actual = resolveImageUrl(detailImage.src(), detailUrl);

        boolean matches = !advertised.isBlank()
                && detailImage.loaded()
                && detailImage.displayed()
                && advertised.equals(actual);

        fields.add(new FieldComparison(
                source, fieldName,
                advertised, actual, matches,
                matches ? "" :
                        "Instructor avatar URL differs or destination image is broken."
        ));
    }

    private static String resolveImageUrl(
            String src,
            String detailUrl
    ) {
        if (src == null || src.isBlank()) {
            return "";
        }

        try {
            return URI.create(detailUrl)
                    .resolve(src.strip())
                    .normalize()
                    .toString();
        } catch (IllegalArgumentException invalidUrl) {
            return src.strip();
        }
    }

    private static final Pattern PRICE_PATTERN = Pattern.compile(
            "(?<!\\d)(?:\\d{1,3}(?:\\.\\d{3})+|\\d+)"
                    + "\\s*(?:đ|₫|VND)(?!\\p{L})",
            Pattern.CASE_INSENSITIVE
    );

    private static List<String> extractPrices(String text) {
        List<String> prices = new ArrayList<>();
        Matcher matcher = PRICE_PATTERN.matcher(normalize(text));

        while (matcher.find()) {
            prices.add(matcher.group().replaceAll("[^0-9]", ""));
        }
        return prices;
    }

    private static void comparePrice(
            List<FieldComparison> fields,
            ParentContent parent,
            ElementState detailPrice
    ) {
        String advertised = !normalize(parent.discountedPrice()).isBlank()
                ? parent.discountedPrice()
                : parent.originalPrice();

        String actual = detailPrice.text();

        List<String> expectedValues = extractPrices(advertised);
        List<String> actualValues = extractPrices(actual);

        boolean singlePrice = expectedValues.size() == 1
                && actualValues.size() == 1;

        boolean matches = singlePrice
                && expectedValues.getFirst().equals(actualValues.getFirst());

        String reason = "";

        if (!singlePrice) {
            reason = "Price cannot be compared reliably from current text; "
                    + "inspect the original/discounted price DOM.";
        } else if (!matches) {
            reason = "Advertised selling price differs from Course Detail price.";
        }

        fields.add(new FieldComparison(
                "main card",
                "Price",
                advertised,
                actual,
                matches,
                reason
        ));
    }


    private static void compareCourseName(List<FieldComparison> fields, String source,
                                          String advertisedValue, String detailValue) {
        String advertised = normalize(advertisedValue);
        String detail = normalize(detailValue);
        String reason = "";
        boolean matches = false;

        if (advertised.isBlank()) {
            reason = "Advertised Course name is missing/blank.";
        } else if (detail.isBlank()) {
            reason = "Course Detail Course name is missing/blank.";
        } else {
            matches = CourseSubCardContentSupport.courseNamesMatch(advertised, detail);
            if (!matches) reason = "Advertised Course name does not correspond to Course Detail Course name.";
        }

        fields.add(new FieldComparison(source, "Course name", advertised, detail, matches, reason));
    }

    private static void compareInstructor(List<FieldComparison> fields, String source,
                                          String advertisedValue, String detailValue) {
        String advertised = normalize(advertisedValue);
        String detail = normalize(detailValue);
        String reason = "";
        boolean matches = false;

        if (advertised.isBlank()) {
            reason = "Advertised instructor name is missing/blank.";
        } else if (detail.isBlank()) {
            reason = "Course Detail instructor name is missing/blank.";
        } else {
            matches = advertised.equals(detail);
            if (!matches) reason = "Advertised instructor name does not match Course Detail instructor name.";
        }

        fields.add(new FieldComparison(source, "Instructor name", advertised, detail, matches, reason));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace('\u00a0', ' ').replaceAll("\\s+", " ").strip();
    }

    public record FieldComparison(String comparisonSource, String field,
                                  String advertisedValue, String detailValue,
                                  boolean matches, String reason) {
    }

    public record ComparisonResult(CourseCardSnapshot card, DetailIdentitySnapshot detail,
                                   List<FieldComparison> fields) {
        public ComparisonResult {
            fields = List.copyOf(fields);
        }

        public boolean passed() {
            return fields.stream().allMatch(FieldComparison::matches);
        }

        public List<FieldComparison> mismatches() {
            return fields.stream().filter(field -> !field.matches()).toList();
        }
    }

    public record ConsistencyResult(List<ConsistencySourceResult> sources, List<ConsistencyDefect> defects,
                                    int detailPagesInspected, int categoryCount, String summary) {
        public ConsistencyResult {
            sources = List.copyOf(sources);
            defects = List.copyOf(defects);
        }
    }

    public static ConsistencyResult inspectSources(WebDriver driver) {
        String testId = "COURSE-DETAIL-005";
        List<ConsistencyDefect> defects = new ArrayList<>();
        List<ConsistencySourceResult> sourceResults = new ArrayList<>();
        Map<String, DetailIdentitySnapshot> detailCache = new LinkedHashMap<>();

        driver.get(HOME_PAGE_URL);
        HomePage homePage = new HomePage(driver);
        List<CourseCardSnapshot> homepageCards = homePage.captureHomepageCourseCards();
        List<String> categoryUrls = homePage.getCourseCategoryUrls();
        sourceResults.add(checkConsistencySource(driver,
                "Homepage", HOME_PAGE_URL, "", homepageCards, detailCache, defects));

        driver.get(HOME_PAGE_URL);
        CourseListPage.CatalogPage courseList = new HomePage(driver).goToCourseListPage().captureCurrentPage();
        List<CourseCardSnapshot> courseListCards = courseList.courses().stream()
                .map(CourseListPage.CourseCandidate::snapshot)
                .toList();
        sourceResults.add(checkConsistencySource(driver,
                "Khóa học", COURSE_LIST_URL, "", courseListCards, detailCache, defects));

        if (categoryUrls.isEmpty()) {
            sourceResults.add(checkConsistencySource(driver,
                    "Danh mục", HOME_PAGE_URL, "Category discovery", List.of(), detailCache, defects));
        }

        CourseCategoryPage categoryPage = new CourseCategoryPage(driver);
        for (String categoryUrl : categoryUrls) {
            categoryPage.open(categoryUrl);
            String categoryName = categoryPage.getCategoryName();
            List<CourseCardSnapshot> categoryCards = categoryPage.captureCourseCards();
            sourceResults.add(checkConsistencySource(driver,
                    "Danh mục", categoryUrl, categoryName, categoryCards, detailCache, defects));
        }

        RelatedCoursesSnapshot relatedState = new CourseDetailPage(driver).openKnownCourse().captureRelatedCourses();
        List<CourseCardSnapshot> relatedCards = relatedState.cards().stream()
                .map(RelatedCardSnapshot::content)
                .toList();
        sourceResults.add(checkConsistencySource(driver,
                "Related Courses", CourseDetailPage.KNOWN_COURSE_URL, "", relatedCards, detailCache, defects));

        String summary = buildConsistencySummary(testId, sourceResults, defects, detailCache.size(), categoryUrls.size());
        return new ConsistencyResult(sourceResults, defects, detailCache.size(), categoryUrls.size(), summary);
    }

    private static ConsistencySourceResult checkConsistencySource(
            WebDriver driver,
            String surfaceType,
            String sourceLocation,
            String categoryName,
            List<CourseCardSnapshot> renderedCards,
            Map<String, DetailIdentitySnapshot> detailCache,
            List<ConsistencyDefect> defects
    ) {
        List<CourseCardSnapshot> candidates = CourseDetailConsistencySupport.firstValidCandidates(renderedCards);
        if (candidates.isEmpty()) {
            ConsistencyDefect defect = new ConsistencyDefect(
                    surfaceType, sourceLocation, categoryName, 0, "", "source",
                    "Representative Course", "<none>", "<none>",
                    "No valid Course-card candidate was available on this frontend source."
            );
            defects.add(defect);
            LOGGER.warn("{} | no valid representative Course", sourceLabel(surfaceType, categoryName));
            return new ConsistencySourceResult(surfaceType, sourceLocation, categoryName,
                    "", "", false, false, 0, 0, "no valid representative", List.of());
        }

        List<ComparisonResult> comparisons = new ArrayList<>();
        CourseCardSnapshot representative = candidates.getFirst();
        ComparisonResult first = compareCandidate(driver, representative, detailCache);
        comparisons.add(first);
        addConsistencyDefects(surfaceType, sourceLocation, categoryName, first, defects);

        boolean expanded = false;
        if (!first.passed()) {
            int limit = Math.min(CourseDetailConsistencySupport.DIAGNOSTIC_MAX_PER_SOURCE, candidates.size());
            if (limit > CourseDetailConsistencySupport.PRIMARY_SAMPLE_SIZE) {
                expanded = true;
                LOGGER.info("{} | representative FAIL | expanding diagnostics up to {}",
                        sourceLabel(surfaceType, categoryName), limit);
            }
            for (int index = CourseDetailConsistencySupport.PRIMARY_SAMPLE_SIZE; index < limit; index++) {
                ComparisonResult diagnostic = compareCandidate(driver, candidates.get(index), detailCache);
                comparisons.add(diagnostic);
                addConsistencyDefects(surfaceType, sourceLocation, categoryName, diagnostic, defects);
            }
        }

        int failedCourses = (int) comparisons.stream().filter(result -> !result.passed()).count();
        String assessment;
        if (first.passed()) {
            assessment = "healthy representative";
        } else if (comparisons.size() == 1) {
            assessment = "representative failed; no additional valid candidate available";
        } else if (failedCourses == 1) {
            assessment = "first representative mismatch; additional diagnostics healthy; likely Course-specific";
        } else {
            assessment = failedCourses + "/" + comparisons.size()
                    + " checked Courses mismatched; mapping problem appears repeated on this source";
        }

        LOGGER.info("{} | representative=\"{}\" | {}",
                sourceLabel(surfaceType, categoryName), representative.href(), first.passed() ? "PASS" : "FAIL");

        return new ConsistencySourceResult(
                surfaceType,
                sourceLocation,
                categoryName,
                representative.href(),
                representative.name(),
                first.passed(),
                expanded,
                comparisons.size(),
                failedCourses,
                assessment,
                comparisons
        );
    }

    private static ComparisonResult compareCandidate(
            WebDriver driver,
            CourseCardSnapshot candidate,
            Map<String, DetailIdentitySnapshot> detailCache
    ) {
        DetailIdentitySnapshot detail = detailCache.get(candidate.href());
        if (detail == null) {
            driver.get(candidate.href());
            detail = new CourseDetailPage(driver).captureIdentityState();
            detailCache.put(candidate.href(), detail);
        }
        return CourseDetailConsistencySupport.compare(candidate, detail);
    }

    private static void addConsistencyDefects(
            String surfaceType,
            String sourceLocation,
            String categoryName,
            ComparisonResult result,
            List<ConsistencyDefect> defects
    ) {
        for (FieldComparison mismatch : result.mismatches()) {
            defects.add(new ConsistencyDefect(
                    surfaceType,
                    sourceLocation,
                    categoryName,
                    result.card().cardIndex(),
                    result.card().href(),
                    mismatch.comparisonSource(),
                    mismatch.field(),
                    mismatch.advertisedValue(),
                    mismatch.detailValue(),
                    mismatch.reason()
            ));
        }
    }

    private static String buildConsistencySummary(
            String testId,
            List<ConsistencySourceResult> sourceResults,
            List<ConsistencyDefect> defects,
            int detailPagesInspected,
            int categoryCount
    ) {
        long sourcesRequiringExpansion = sourceResults.stream()
                .filter(result -> !result.representativePassed() && result.checkedCourses() > 0)
                .count();
        long normalChecks = sourceResults.stream()
                .filter(result -> result.representativePassed() && result.checkedCourses() == 1)
                .count();
        StringBuilder report = new StringBuilder(testId)
                .append(" - Course Card ↔ Course Detail Frontend Consistency\n")
                .append("This was representative frontend consistency testing, not an exhaustive Course database audit.\n\n")
                .append("Coverage:\n")
                .append("  Homepage visited: yes\n")
                .append("  Khóa học visited: yes (initial rendered page only; no full pagination)\n")
                .append("  Danh mục count visited: ").append(categoryCount).append('\n')
                .append("  Related Course source visited: yes (one depth only; no recursion)\n\n");

        for (ConsistencySourceResult source : sourceResults) {
            report.append("SOURCE | type=").append(source.surfaceType())
                    .append(" | location=").append(source.sourceLocation());
            if (!source.categoryName().isBlank()) report.append(" | category=\"").append(source.categoryName()).append('"');
            report.append("\n  representative href=").append(display(source.representativeHref()))
                    .append("\n  representative Course name=").append(display(source.representativeName()))
                    .append("\n  result=").append(source.representativePassed() ? "PASS" : "FAIL")
                    .append("\n  Courses checked=").append(source.checkedCourses())
                    .append(" | expanded=").append(source.expanded())
                    .append(" | failed Courses=").append(source.failedCourses())
                    .append("\n  assessment=").append(source.assessment()).append('\n');

            for (ComparisonResult comparison : source.comparisons()) {
                report.append("  COURSE | card=").append(comparison.card().cardIndex())
                        .append(" | href=").append(comparison.card().href())
                        .append(" | result=").append(comparison.passed() ? "PASS" : "FAIL").append('\n')
                        .append("    Detail URL: ").append(comparison.detail().resultingUrl()).append('\n');
                for (FieldComparison field : comparison.fields()) {
                    report.append("    ").append(field.comparisonSource()).append(" | ")
                            .append(field.field()).append(" | advertised=").append(display(field.advertisedValue()))
                            .append(" | detail=").append(display(field.detailValue()))
                            .append(" | ").append(field.matches() ? "MATCH" : "MISMATCH");
                    if (!field.matches()) report.append(" | reason=").append(field.reason());
                    report.append('\n');
                }
            }
            report.append('\n');
        }

        report.append("Totals:\n")
                .append("  source instances checked: ").append(sourceResults.size()).append('\n')
                .append("  normal one-Course checks: ").append(normalChecks).append('\n')
                .append("  sources requiring expansion: ").append(sourcesRequiringExpansion).append('\n')
                .append("  total Course Detail pages inspected: ").append(detailPagesInspected).append('\n')
                .append("  total mismatches: ").append(defects.size()).append('\n');

        if (!defects.isEmpty()) {
            report.append("\nMismatches:\n");
            for (ConsistencyDefect defect : defects) report.append("- ").append(defect.logLine()).append('\n');
        }
        return report.toString();
    }

    private static String sourceLabel(String surfaceType, String categoryName) {
        return categoryName.isBlank() ? surfaceType : surfaceType + " \"" + categoryName + "\"";
    }

    private static String display(String value) {
        return value == null || value.isBlank() ? "<missing>" : "\"" + value + "\"";
    }

    public record ConsistencySourceResult(
            String surfaceType,
            String sourceLocation,
            String categoryName,
            String representativeHref,
            String representativeName,
            boolean representativePassed,
            boolean expanded,
            int checkedCourses,
            int failedCourses,
            String assessment,
            List<ComparisonResult> comparisons
    ) {
        public ConsistencySourceResult {
            comparisons = List.copyOf(comparisons);
        }
    }

    public record ConsistencyDefect(
            String surfaceType,
            String sourceLocation,
            String categoryName,
            int cardIndex,
            String href,
            String comparisonSource,
            String field,
            String advertisedValue,
            String detailValue,
            String reason
    ) {
        public String logLine() {
            StringBuilder line = new StringBuilder("surface=").append(surfaceType)
                    .append(" | location=").append(sourceLocation);
            if (!categoryName.isBlank()) line.append(" | category=\"").append(categoryName).append('"');
            return line.append(" | card=").append(cardIndex)
                    .append(" | href=").append(display(href))
                    .append(" | source=").append(comparisonSource)
                    .append(" | field=").append(field)
                    .append(" | cardValue=").append(display(advertisedValue))
                    .append(" | detailValue=").append(display(detailValue))
                    .append(" | reason=").append(reason)
                    .toString();
        }
    }
}

