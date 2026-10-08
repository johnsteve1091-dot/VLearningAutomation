package vn.cybersoft.vlearning.pages;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.ui.ExpectedConditions;

import vn.cybersoft.vlearning.core.BasePage;

public class CourseDetailPage extends BasePage {

    public static final String KNOWN_COURSE_URL =
            "https://demo2.cybersoft.edu.vn/chitiet/.100";

    // These spellings and structures come from the observed Course Detail DOM.
    private static final String ROOT_PATH = "//section[contains(@class,'detailCoures')]";
    private static final String HEADER_PATH = ROOT_PATH + "//div[contains(@class,'headDetailCourse')]";
    private static final String INSTRUCTOR_PATH = HEADER_PATH
            + "//div[contains(@class,'detailCourseIntro')]"
            + "[.//div[contains(@class,'instrutorTitle')]/p[normalize-space()='Giảng viên']]";
    private static final String CATEGORY_PATH = HEADER_PATH
            + "//div[contains(@class,'detailCourseIntro')]"
            + "[.//div[contains(@class,'instrutorTitle')]/p[normalize-space()='Lĩnh vực']]";
    private static final String REVIEW_PATH = HEADER_PATH + "//div[contains(@class,'reviewDetail')]";
    private static final String SIDEBAR_PATH = ROOT_PATH + "//div[contains(@class,'sideBarCourseDetail')]";
    private static final String SIDEBAR_CONTENT_PATH = SIDEBAR_PATH
            + "//div[contains(@class,'sideBarDetailContent')]";

    private static final By ROOT = By.xpath(ROOT_PATH);
    private static final By CONTENT = By.xpath(ROOT_PATH + "//div[contains(@class,'detailCouresContent')]");
    private static final By TITLE = By.xpath(ROOT_PATH
            + "//div[contains(@class,'titleCourse')]//h3[normalize-space()='Thông tin khóa học']");
    private static final By SUBTITLE = By.xpath(ROOT_PATH
            + "//div[contains(@class,'titleCourse')]//p[normalize-space()='Tiến lên và không chần chừ !!!']");
    private static final By COURSE_NAME = By.xpath(ROOT_PATH + "//h4[contains(@class,'titleDetailCourse')]");
    private static final By INSTRUCTOR_LABEL = By.xpath(INSTRUCTOR_PATH
            + "//div[contains(@class,'instrutorTitle')]/p[normalize-space()='Giảng viên']");
    private static final By INSTRUCTOR_NAME = By.xpath(INSTRUCTOR_PATH
            + "//div[contains(@class,'instrutorTitle')]/p[2]");
    private static final By INSTRUCTOR_AVATAR = By.xpath(INSTRUCTOR_PATH + "//img");
    private static final By CATEGORY_LABEL = By.xpath(CATEGORY_PATH
            + "//div[contains(@class,'instrutorTitle')]/p[normalize-space()='Lĩnh vực']");
    private static final By CATEGORY_VALUE = By.xpath(CATEGORY_PATH
            + "//div[contains(@class,'instrutorTitle')]/p[2]");
    private static final By REVIEW = By.xpath(REVIEW_PATH);
    private static final By STARS = By.xpath(REVIEW_PATH + "/span/i");
    private static final By RATING = By.xpath(REVIEW_PATH + "/span");
    private static final By REVIEW_COUNT = By.xpath(REVIEW_PATH + "/p");
    private static final By DESCRIPTION = By.xpath(ROOT_PATH + "//p[contains(@class,'textDiscripts')]");
    private static final By LEARNING_OBJECTIVES = By.xpath(ROOT_PATH + "//div[contains(@class,'boxCourseLearn')]");
    private static final By SIDEBAR = By.xpath(SIDEBAR_PATH);
    private static final By COURSE_IMAGE = By.xpath(SIDEBAR_PATH + "/img");
    private static final By PRICE = By.xpath(SIDEBAR_PATH + "//div[contains(@class,'coursePrice')]");
    private static final By STUDENTS = sidebarValue("Ghi danh:");
    private static final By DURATION = sidebarValue("Thời gian:");
    private static final By LESSONS = sidebarValue("Bài học:");
    private static final By VIDEOS = sidebarValue("Video:");
    private static final By LEVEL = sidebarValue("Trình độ:");
    private static final By REGISTER_BUTTON = By.xpath(SIDEBAR_PATH
            + "//button[contains(@class,'btnPreview') and normalize-space()='Đăng ký']");

    private static final By ENROLLMENT_DIALOG = By.cssSelector("div.swal-modal[role='dialog']");
    private static final By ENROLLMENT_DIALOG_TITLE = By.cssSelector("div.swal-modal[role='dialog'] .swal-title");

    private static final String CURRICULUM_PATH = ROOT_PATH + "//div[contains(@class,'courseContent')]";
    private static final By CURRICULUM = By.xpath(CURRICULUM_PATH);
    private static final By CURRICULUM_TITLE = By.xpath(CURRICULUM_PATH
            + "//h6[normalize-space()='Nội dung khóa học']");
    private static final By CURRICULUM_MODULES = By.xpath(CURRICULUM_PATH
            + "//div[contains(@class,'courseDetailItem')]");
    private static final By MODULE_CONTENT = By.xpath(".//div[contains(@class,'courseDetailContent')]");
    private static final By MODULE_HEADER = By.xpath(".//div[contains(@class,'sectionCourse')]");
    private static final By MODULE_TITLE = By.xpath(".//div[contains(@class,'sectionCourse')]/span");
    private static final By LESSON_CONTAINER = By.xpath(".//div[contains(@class,'lessonContainer')]");
    private static final By CURRICULUM_LESSONS = By.xpath(
            ".//div[contains(@class,'lessonContainer')]//div[contains(@class,'lessonContent')]");
    private static final By LESSON_TITLE = By.xpath(".//span[1]");

    private static final String RELATED_PATH = ROOT_PATH + "//div[contains(@class,'coursesRelated')]";
    private static final By RELATED = By.xpath(RELATED_PATH);
    // Direct h6 comes from the supplied coursesRelated DOM, not a card's nested h6.
    private static final By RELATED_HEADING = By.xpath(RELATED_PATH + "/h6");
    private static final By RELATED_CARDS = By.xpath(RELATED_PATH
            + "//div[contains(@class,'cardGlobalRes')]//a[contains(@class,'cardGlobal')]");
    // Interaction targets reuse the existing CourseCardDomReader's observed structure.
    private static final By RELATED_CARD_IMAGE = By.xpath("./img[1]");
    private static final By RELATED_DETAIL_BUTTON = By.xpath(
            ".//div[contains(@class,'subCard')]//button[contains(@class,'btnSubCard')]");

    public CourseDetailPage(WebDriver driver) {
        super(driver);
    }

    public CourseDetailPage openKnownCourse() {
        driver.get(KNOWN_COURSE_URL);
        return this;
    }

    public CourseDetailPage waitUntilEnrollmentReady() {
        waitUntilVisible(ROOT);
        waitUntilEnrollmentPopupClosed();
        waitUntilClickable(REGISTER_BUTTON);
        return this;
    }

    public void clickEnroll() {
        click(REGISTER_BUTTON);
    }

    public String waitForEnrollmentPopupTitle() {
        return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
            for (WebElement title : currentDriver.findElements(ENROLLMENT_DIALOG_TITLE)) {
                if (title.isDisplayed() && !title.getText().isBlank()) return title.getText().strip();
            }
            return null;
        });
    }

    public void waitUntilEnrollmentPopupClosed() {
        // The supplied success-dialog DOM has no dismiss button.
        getWait().until(ExpectedConditions.invisibilityOfElementLocated(ENROLLMENT_DIALOG));
    }

    /**
     * One shared explicit-wait budget covers the root, async content and images.
     * Missing fields survive the deadline as evidence for the final assertion;
     * session/command failures propagate instead of becoming product defects.
     */
    public CoreSnapshot captureCoreState() {
        CoreSnapshot[] latest = new CoreSnapshot[1];
        try {
            return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                try {
                    latest[0] = readCoreState();
                } catch (TimeoutException commandTimeout) {
                    throw new IllegalStateException(
                            "WebDriver command timed out while reading Course Detail", commandTimeout);
                }
                return latest[0].readyForInspection() ? latest[0] : null;
            });
        } catch (TimeoutException readinessTimeout) {
            if (latest[0] == null) {
                throw new IllegalStateException(
                        "Course Detail DOM could not be captured consistently", readinessTimeout);
            }
            return latest[0];
        }
    }

    public DetailIdentitySnapshot captureIdentityState() {
        DetailIdentitySnapshot[] latest = new DetailIdentitySnapshot[1];
        try {
            return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                try {
                    latest[0] = new DetailIdentitySnapshot(
                            driver.getCurrentUrl(),
                            readContainer(ROOT),
                            readElement(COURSE_NAME),
                            readElement(INSTRUCTOR_NAME)
                    );
                } catch (TimeoutException commandTimeout) {
                    throw new IllegalStateException(
                            "WebDriver command timed out while reading Course Detail identity", commandTimeout);
                }
                return latest[0].readyForInspection() ? latest[0] : null;
            });
        } catch (TimeoutException readinessTimeout) {
            if (latest[0] == null) {
                throw new IllegalStateException(
                        "Course Detail identity DOM could not be captured consistently", readinessTimeout);
            }
            return latest[0];
        }
    }

    public CurriculumSnapshot captureCurriculumState() {
        CurriculumSnapshot[] latest = new CurriculumSnapshot[1];
        try {
            return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                CurriculumSnapshot previous = latest[0];
                try {
                    latest[0] = readCurriculumState();
                } catch (TimeoutException commandTimeout) {
                    throw new IllegalStateException(
                            "WebDriver command timed out while reading the curriculum", commandTimeout);
                }
                // Confirm two matching observations; no fixed module/lesson counts or sleeps.
                return latest[0].readyForInspection() && latest[0].equals(previous) ? latest[0] : null;
            });
        } catch (TimeoutException readinessTimeout) {
            if (latest[0] == null) {
                throw new IllegalStateException("Curriculum DOM could not be captured consistently", readinessTimeout);
            }
            // Ordinary missing/blank fields remain in the evidence for one final assertion.
            return latest[0];
        }
    }

    private CurriculumSnapshot readCurriculumState() {
        ElementState section = readContainer(CURRICULUM);
        ElementState title = readElement(CURRICULUM_TITLE);
        List<ModuleSnapshot> modules = new ArrayList<>();
        for (WebElement module : driver.findElements(CURRICULUM_MODULES)) {
            List<LessonSnapshot> lessons = new ArrayList<>();
            for (WebElement lesson : module.findElements(CURRICULUM_LESSONS)) {
                lessons.add(new LessonSnapshot(lessons.size() + 1, readElement(lesson, LESSON_TITLE, true)));
            }
            modules.add(new ModuleSnapshot(modules.size() + 1, module.isDisplayed(),
                    !module.findElements(MODULE_CONTENT).isEmpty(),
                    !module.findElements(MODULE_HEADER).isEmpty(), readElement(module, MODULE_TITLE, true),
                    !module.findElements(LESSON_CONTAINER).isEmpty(), lessons));
        }
        return new CurriculumSnapshot(KNOWN_COURSE_URL, driver.getCurrentUrl(), section, title, modules);
    }

    public RelatedCoursesSnapshot captureRelatedCourses() {
        return awaitRelatedObservation(() -> {
            List<RelatedCardSnapshot> cards = new ArrayList<>();
            for (WebElement card : driver.findElements(RELATED_CARDS)) {
                cards.add(readRelatedCard(card, cards.size() + 1));
            }
            return new RelatedCoursesSnapshot(KNOWN_COURSE_URL, driver.getCurrentUrl(),
                    readContainer(RELATED), readElement(RELATED_HEADING), cards);
        }, RelatedCoursesSnapshot::ready);
    }

    private RelatedCardSnapshot readRelatedCard(WebElement card, int index) {
        return new RelatedCardSnapshot(CourseCardDomReader.captureStrict(card, index),
                card.isDisplayed(), CourseCardDomReader.hasDisplayedDiscount(card));
    }

    /** Capture only immutable navigation identity so no WebElement survives a navigation. */
    public List<RelatedNavigationTarget> captureRelatedNavigationTargets() {
        return awaitRelatedObservation(() -> {
            List<RelatedNavigationTarget> targets = new ArrayList<>();
            List<WebElement> cards = driver.findElements(RELATED_CARDS);
            for (int index = 0; index < cards.size(); index++) {
                String href = normalizeHref(cards.get(index).getAttribute("href"));
                targets.add(new RelatedNavigationTarget(index + 1, href));
            }
            return List.copyOf(targets);
        }, targets -> !targets.isEmpty());
    }

    /** Prefer the original position, then recover by the captured href only. */
    private WebElement rediscoverRelatedCard(RelatedNavigationTarget expected) {
        List<WebElement> cards = driver.findElements(RELATED_CARDS);
        int position = expected.cardIndex() - 1;
        if (position >= 0 && position < cards.size() && matchesRelatedCard(cards.get(position), expected)) {
            return cards.get(position);
        }
        for (int index = 0; index < cards.size(); index++) {
            if (index != position && matchesRelatedCard(cards.get(index), expected)) return cards.get(index);
        }
        return null;
    }

    private boolean matchesRelatedCard(WebElement card, RelatedNavigationTarget expected) {
        return normalizeHref(card.getAttribute("href")).equals(expected.href());
    }

    public NavigationSnapshot navigateRelatedCard(RelatedNavigationTarget expected) {
        return navigateRelated(expected, false);
    }

    public NavigationSnapshot navigateRelatedDetailButton(RelatedNavigationTarget expected) {
        return navigateRelated(expected, true);
    }

    private NavigationSnapshot navigateRelated(RelatedNavigationTarget expected, boolean detailButton) {
        boolean[] observedSource = {false};
        ClickEvidence click;
        try {
            click = getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                try {
                    WebElement card = rediscoverRelatedCard(expected);
                    observedSource[0] = true;
                    if (card == null || !card.isDisplayed()) return null;

                    WebElement target = card;
                    if (detailButton) {
                        try {
                            new Actions(driver).moveToElement(card).perform();
                        } catch (ElementNotInteractableException | MoveTargetOutOfBoundsException unusable) {
                            return new ClickEvidence(expected, driver.getCurrentUrl(),
                                    "hover unusable: " + unusable.getClass().getSimpleName());
                        }
                        List<WebElement> buttons = card.findElements(RELATED_DETAIL_BUTTON);
                        if (buttons.isEmpty()) return null;
                        target = buttons.getFirst();
                    } else {
                        List<WebElement> images = card.findElements(RELATED_CARD_IMAGE);
                        if (!images.isEmpty() && images.getFirst().isDisplayed()) target = images.getFirst();
                    }

                    if (!target.isDisplayed() || !target.isEnabled()) return null;
                    String sourceUrl = driver.getCurrentUrl();
                    try {
                        target.click();
                        return new ClickEvidence(expected, sourceUrl, "");
                    } catch (ElementNotInteractableException unusable) {
                        return new ClickEvidence(expected, sourceUrl,
                                "click unusable: " + unusable.getClass().getSimpleName());
                    }
                } catch (TimeoutException commandTimeout) {
                    throw new IllegalStateException("WebDriver command timed out during related-course click", commandTimeout);
                }
            });
        } catch (TimeoutException readinessTimeout) {
            if (!observedSource[0]) throw new IllegalStateException("Source DOM could not be observed", readinessTimeout);
            return blockedNavigation(expected, "source card or click target unavailable after explicit wait; not clicked");
        }
        if (!click.problem().isEmpty()) return blockedNavigation(click.source(), click.problem());

        return awaitRelatedObservation(() -> {
            String resultingUrl = driver.getCurrentUrl();
            boolean navigated = !sameBrowserUrl(click.sourceUrl(), resultingUrl);
            return new NavigationSnapshot(click.source(), click.sourceUrl(), true, navigated, resultingUrl,
                    readContainer(ROOT), "");
        }, state -> state.navigated() && sameBrowserUrl(state.source().href(), state.resultingUrl())
                && state.root().displayed());
    }

    private NavigationSnapshot blockedNavigation(RelatedNavigationTarget source, String problem) {
        ElementState absent = new ElementState(false, false, false, "");
        String url = driver.getCurrentUrl();
        return new NavigationSnapshot(source, url, false, false, url, absent, problem);
    }

    private static String normalizeHref(String value) {
        return value == null ? "" : value.strip();
    }

    public static boolean sameBrowserUrl(String expected, String actual) {
        if (expected == null || actual == null || expected.isBlank() || actual.isBlank()) return false;
        try {
            URI base = URI.create(KNOWN_COURSE_URL);
            return base.resolve(expected).normalize().equals(base.resolve(actual).normalize());
        } catch (IllegalArgumentException invalidUrl) {
            return false;
        }
    }

    /** Two equal ready observations, or the last complete evidence at the deadline. */
    private <T> T awaitRelatedObservation(Supplier<T> reader, Predicate<T> ready) {
        AtomicReference<T> latest = new AtomicReference<>();
        try {
            return getWait().ignoring(StaleElementReferenceException.class).until(currentDriver -> {
                T previous = latest.get();
                T current;
                try {
                    current = reader.get();
                } catch (TimeoutException commandTimeout) {
                    throw new IllegalStateException("WebDriver command timed out while reading related courses", commandTimeout);
                }
                latest.set(current);
                return ready.test(current) && Objects.equals(current, previous) ? current : null;
            });
        } catch (TimeoutException readinessTimeout) {
            if (latest.get() == null) throw new IllegalStateException("Related-course DOM could not be observed", readinessTimeout);
            return latest.get();
        }
    }

    public record RelatedCardSnapshot(CourseCardSnapshot content, boolean displayed, boolean discountedPriceRendered) {
    }

    public record RelatedCoursesSnapshot(String requestedUrl, String resultingUrl, ElementState section,
                                         ElementState heading, List<RelatedCardSnapshot> cards) {
        public RelatedCoursesSnapshot {
            cards = List.copyOf(cards);
        }

        private boolean ready() {
            return section.displayed() && heading.displayed() && !heading.text().isBlank() && !cards.isEmpty()
                    && cards.stream().allMatch(card -> {
                        var content = card.content();
                        var parent = content.parent();
                        return card.displayed() && !content.name().isBlank() && !content.href().isBlank()
                                && !parent.instructorName().isBlank() && !parent.originalPrice().isBlank()
                                && parent.courseImageNaturalWidth() > 0 && parent.instructorAvatarNaturalWidth() > 0
                                && (!card.discountedPriceRendered() || !parent.discountedPrice().isBlank())
                                && (!content.hasSubCard() || !content.subCard().description().isBlank());
                    });
        }
    }

    public record RelatedNavigationTarget(int cardIndex, String href) {
    }

    public record NavigationSnapshot(RelatedNavigationTarget source, String sourceUrl, boolean clicked,
                                     boolean navigated, String resultingUrl, ElementState root, String problem) {
    }

    private record ClickEvidence(RelatedNavigationTarget source, String sourceUrl, String problem) {
    }

    private CoreSnapshot readCoreState() {
        return new CoreSnapshot(
                KNOWN_COURSE_URL, driver.getCurrentUrl(),
                readContainer(ROOT), readContainer(CONTENT), readElement(TITLE), readElement(SUBTITLE),
                readElement(COURSE_NAME), readElement(INSTRUCTOR_LABEL), readElement(INSTRUCTOR_NAME),
                readImage(INSTRUCTOR_AVATAR), readElement(CATEGORY_LABEL), readElement(CATEGORY_VALUE),
                readContainer(REVIEW), countDisplayedStars(), readElement(RATING), readElement(REVIEW_COUNT),
                readElement(DESCRIPTION), readElement(LEARNING_OBJECTIVES), readContainer(SIDEBAR),
                readImage(COURSE_IMAGE), readElement(PRICE), readElement(STUDENTS), readElement(DURATION),
                readElement(LESSONS), readElement(VIDEOS), readElement(LEVEL), readElement(REGISTER_BUTTON)
        );
    }

    private ElementState readElement(By locator) {
        return readElement(locator, true);
    }

    private ElementState readContainer(By locator) {
        // Do not collect nested curriculum/related-course text through broad containers.
        return readElement(locator, false);
    }

    private ElementState readElement(By locator, boolean captureText) {
        return readElement(driver, locator, captureText);
    }

    private ElementState readElement(SearchContext context, By locator, boolean captureText) {
        List<WebElement> elements = context.findElements(locator);
        if (elements.isEmpty()) {
            return new ElementState(false, false, false, "");
        }
        WebElement element = elements.getFirst();
        return new ElementState(true, element.isDisplayed(), element.isEnabled(),
                captureText ? element.getText().replace('\u00a0', ' ').strip() : "");
    }

    private ImageState readImage(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        if (elements.isEmpty()) {
            return new ImageState(false, false, "", false, 0);
        }
        WebElement image = elements.getFirst();
        Map<?, ?> loading = (Map<?, ?>) ((JavascriptExecutor) driver).executeScript(
                "return {complete: arguments[0].complete, naturalWidth: arguments[0].naturalWidth};", image);
        String src = image.getDomAttribute("src");
        return new ImageState(true, image.isDisplayed(), src == null ? "" : src.strip(),
                Boolean.TRUE.equals(loading.get("complete")),
                ((Number) loading.get("naturalWidth")).longValue());
    }

    private int countDisplayedStars() {
        int count = 0;
        for (WebElement star : driver.findElements(STARS)) {
            if (star.isDisplayed()) {
                count++;
            }
        }
        return count;
    }

    private static By sidebarValue(String label) {
        return By.xpath(SIDEBAR_CONTENT_PATH
                + "//li[.//p[contains(normalize-space(),'" + label + "')]]"
                + "//p[contains(normalize-space(),'" + label + "')]/span");
    }

    public record ElementState(boolean present, boolean displayed, boolean enabled, String text) {
    }

    public record ImageState(boolean present, boolean displayed, String src, boolean complete, long naturalWidth) {
        public boolean loaded() {
            return complete && naturalWidth > 0;
        }
    }

    public record DetailIdentitySnapshot(String resultingUrl, ElementState root,
                                         ElementState courseName, ElementState instructorName) {
        private boolean readyForInspection() {
            return root.displayed()
                    && courseName.displayed() && !courseName.text().isBlank()
                    && instructorName.displayed() && !instructorName.text().isBlank();
        }
    }

    public record LessonSnapshot(int index, ElementState title) {
    }

    public record ModuleSnapshot(int index, boolean displayed, boolean contentPresent,
                                 boolean headerPresent, ElementState title, boolean lessonContainerPresent,
                                 List<LessonSnapshot> lessons) {
        public ModuleSnapshot {
            lessons = List.copyOf(lessons);
        }
    }

    public record CurriculumSnapshot(String requestedUrl, String resultingUrl, ElementState section,
                                     ElementState title, List<ModuleSnapshot> modules) {
        public CurriculumSnapshot {
            modules = List.copyOf(modules);
        }

        public int lessonCount() {
            return modules.stream().mapToInt(module -> module.lessons().size()).sum();
        }

        private boolean readyForInspection() {
            return section.displayed() && title.displayed() && !modules.isEmpty() && lessonCount() > 0
                    && modules.stream().allMatch(module -> module.displayed() && module.contentPresent()
                    && module.headerPresent() && module.title().displayed() && !module.title().text().isBlank()
                    && module.lessonContainerPresent() && module.lessons().stream().allMatch(lesson ->
                    lesson.title().displayed() && !lesson.title().text().isBlank()));
        }
    }

    public record CoreSnapshot(
            String requestedUrl, String resultingUrl,
            ElementState root, ElementState content, ElementState title, ElementState subtitle,
            ElementState courseName, ElementState instructorLabel, ElementState instructorName,
            ImageState instructorAvatar, ElementState categoryLabel, ElementState categoryValue,
            ElementState review, int displayedStars, ElementState rating, ElementState reviewCount,
            ElementState description, ElementState learningObjectives, ElementState sidebar,
            ImageState courseImage, ElementState price, ElementState students, ElementState duration,
            ElementState lessons, ElementState videos, ElementState level, ElementState registerButton
    ) {
        private boolean readyForInspection() {
            boolean containersReady = List.of(root, content, title, subtitle, learningObjectives,
                    sidebar, registerButton).stream().allMatch(ElementState::displayed);
            boolean valuesReady = List.of(courseName, instructorName, categoryValue, rating,
                    reviewCount, description, price, students, duration, lessons, videos, level)
                    .stream().allMatch(value -> value.present() && !value.text().isBlank());
            // A completed broken image is ready to inspect; loaded() is asserted by the test.
            return containersReady && valuesReady && !learningObjectives.text().isBlank()
                    && instructorLabel.present() && categoryLabel.present()
                    && review.present() && displayedStars > 0 && registerButton.enabled()
                    && instructorAvatar.present() && instructorAvatar.complete()
                    && courseImage.present() && courseImage.complete();
        }
    }
}
