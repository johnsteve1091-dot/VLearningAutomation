package vn.cybersoft.vlearning.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import vn.cybersoft.vlearning.core.BasePage;

public class CourseListPage extends BasePage {

    /*
     * =============================================================
     * COURSE LIST PAGE CONTRACT
     * =============================================================
     */

    private static final By PAGE_TITLE =
            By.xpath(
                    "//div[contains(@class,'titleCourse')]//h3"
            );

    private static final By PAGE_SUBTITLE =
            By.xpath(
                    "//div[contains(@class,'titleCourse')]//p"
            );

    private static final By INSIGHT_ITEMS =
            By.xpath(
                    "//div[contains(@class,'coursesContainer')]"
                            + "//div[contains(@class,'coursesBoxItem')]"
            );

    private static final By INSIGHT_LABEL =
            By.xpath(
                    ".//h6"
            );

    private static final By INSIGHT_ICON =
            By.xpath(
                    ".//i"
            );

    private static final By INSIGHT_COUNT =
            By.xpath(
                    ".//p"
            );

    private static final By COURSE_LIST_HEADING =
            By.xpath(
                    "//div[contains(@class,'courseListPage')]"
                            + "//h6[normalize-space()='Danh sách khóa học']"
            );

    private static final By COURSE_CARD_CONTAINERS =
            By.xpath(
                    "//div[contains(@class,'courseListPage')]"
                            + "//div[contains(@class,'cardGlobalRes')]"
            );

    /*
     * =============================================================
     * COURSE CARDS
     * =============================================================
     */

    /*
     * Do not filter by /chitiet/.
     *
     * TC1 itself validates whether the href is valid.
     */
    private static final By COURSE_CARDS =
            By.xpath(
                    "//div[contains(@class,'courseListPage')]"
                            + "//div[contains(@class,'cardGlobalRes')]"
                            + "//a[contains(@class,'cardGlobal')]"
            );

    /*
     * =============================================================
     * PAGINATION
     * =============================================================
     */

    private static final By PAGINATION =
            By.xpath(
                    "//ul[contains(@class,'paginationPages')]"
            );

    private static final By PAGINATION_ITEMS =
            By.xpath(
                    "//ul[contains(@class,'paginationPages')]"
                            + "//li[contains(@class,'page-item')]"
            );

    private static final By ACTIVE_PAGE =
            By.xpath(
                    "//ul[contains(@class,'paginationPages')]"
                            + "//li[contains(@class,'page-item') "
                            + "and contains(@class,'active')]"
            );

    private static final By PREVIOUS_PAGE =
            By.xpath(
                    "//ul[contains(@class,'paginationPages')]"
                            + "//li[contains(@class,'page-item') "
                            + "and normalize-space(.)='< Trước']"
            );

    private static final By NEXT_PAGE =
            By.xpath(
                    "//ul[contains(@class,'paginationPages')]"
                            + "//li[contains(@class,'page-item') "
                            + "and normalize-space(.)='Sau >']"
            );

    private static final By ELLIPSIS =
            By.xpath(
                    "//ul[contains(@class,'paginationPages')]"
                            + "//li[contains(@class,'page-item') "
                            + "and normalize-space(.)='...']"
            );

    /*
     * Actual loading DOM captured from /khoahoc:
     *
     * <div id="preloader">
     *     <div id="loader"></div>
     * </div>
     */
    private static final By PRELOADER =
            By.xpath(
                    "//div[@id='preloader']"
            );

    public CourseListPage(WebDriver driver) {
        super(driver);
    }

    /*
     * =============================================================
     * DATA RECORDS
     * =============================================================
     */

    public record InsightItem(
            String label,
            String count,
            String iconClass
    ) {
    }

    public record CourseCandidate(
            String name,
            String href,
            int page,
            int cardIndex,
            boolean hasSubCard,
            CourseCardSnapshot snapshot
    ) {
    }

    public record CatalogPage(
            int number,
            List<CourseCandidate> courses,
            List<String> hrefs
    ) {
    }

    /*
     * =============================================================
     * TC5A - PAGE CONTRACT
     * =============================================================
     */

    public String getPageTitle() {

        return readNormalizedText(
                waitUntilVisible(
                        PAGE_TITLE
                )
        );
    }

    public String getPageSubtitle() {

        return readNormalizedText(
                waitUntilVisible(
                        PAGE_SUBTITLE
                )
        );
    }

    public String getCourseListHeading() {

        return readNormalizedText(
                waitUntilVisible(
                        COURSE_LIST_HEADING
                )
        );
    }

    public List<InsightItem> getInsightItems() {

        List<WebElement> elements =
                getWait().until(driver -> {

                    List<WebElement> found =
                            driver.findElements(
                                    INSIGHT_ITEMS
                            );

                    return found.isEmpty()
                            ? null
                            : found;
                });

        List<InsightItem> items =
                new ArrayList<>();

        for (WebElement element
                : elements) {

            String label =
                    readChildText(
                            element,
                            INSIGHT_LABEL
                    );

            String count =
                    readChildText(
                            element,
                            INSIGHT_COUNT
                    );

            String iconClass =
                    readChildAttribute(
                            element,
                            INSIGHT_ICON,
                            "class"
                    );

            items.add(
                    new InsightItem(
                            label,
                            count,
                            iconClass
                    )
            );
        }

        return List.copyOf(
                items
        );
    }

    public int getVisibleCourseCardCount() {

        int count =
                0;

        for (WebElement card
                : driver.findElements(
                COURSE_CARDS
        )) {

            if (card.isDisplayed()) {
                count++;
            }
        }

        return count;
    }

    public boolean areAllCourseCardContainersDisplayed() {

        List<WebElement> containers =
                driver.findElements(
                        COURSE_CARD_CONTAINERS
                );

        if (containers.isEmpty()) {
            return false;
        }

        for (WebElement container
                : containers) {

            if (!container.isDisplayed()) {
                return false;
            }

            if (container.getRect().getWidth() <= 0
                    || container.getRect().getHeight() <= 0) {

                return false;
            }
        }

        return true;
    }

    /*
     * =============================================================
     * CARD CAPTURE
     * =============================================================
     */

    public CatalogPage captureCurrentPage() {

        int pageNumber =
                getCurrentPageNumber();

        List<WebElement> courseElements =
                waitForCourseCards();

        List<CourseCandidate> courses =
                new ArrayList<>();

        List<String> hrefs =
                new ArrayList<>();

        for (int index = 0;
             index < courseElements.size();
             index++) {

            WebElement courseElement =
                    courseElements.get(index);

            CourseCardSnapshot snapshot =
                    CourseCardDomReader.capture(
                            courseElement,
                            index + 1
                    );

            CourseCandidate course =
                    new CourseCandidate(
                            snapshot.name(),
                            snapshot.href(),
                            pageNumber,
                            snapshot.cardIndex(),
                            snapshot.hasSubCard(),
                            snapshot
                    );

            courses.add(
                    course
            );

            hrefs.add(
                    snapshot.href()
            );
        }

        return new CatalogPage(
                pageNumber,
                List.copyOf(courses),
                List.copyOf(hrefs)
        );
    }

    /*
     * =============================================================
     * TC5B - COURSE DETAIL NAVIGATION
     * =============================================================
     */

    public CourseDetailPage openCourseAt(
            int cardIndex
    ) {

        List<WebElement> courseElements =
                waitForCourseCards();

        if (cardIndex < 1
                || cardIndex > courseElements.size()) {

            throw new IllegalArgumentException(
                    "Course card index "
                            + cardIndex
                            + " is outside the current page range 1.."
                            + courseElements.size()
            );
        }

        WebElement card =
                courseElements.get(
                        cardIndex - 1
                );

        card.click();

        return new CourseDetailPage(
                driver
        );
    }

    /*
     * =============================================================
     * TC5C - PAGINATION STATE
     * =============================================================
     */

    public boolean isPaginationDisplayed() {

        for (WebElement element
                : driver.findElements(
                PAGINATION
        )) {

            if (element.isDisplayed()) {
                return true;
            }
        }

        return false;
    }

    public boolean isPreviousControlDisplayed() {

        return isDisplayedWithoutWait(
                PREVIOUS_PAGE
        );
    }

    public boolean isNextControlDisplayed() {

        return isDisplayedWithoutWait(
                NEXT_PAGE
        );
    }

    public boolean isPreviousDisabled() {

        return isControlDisabled(
                PREVIOUS_PAGE
        );
    }

    public boolean isNextDisabled() {

        return isControlDisabled(
                NEXT_PAGE
        );
    }

    public int getCurrentPageNumber() {

        String pageText =
                readNormalizedText(
                        waitUntilVisible(
                                ACTIVE_PAGE
                        )
                );

        return Integer.parseInt(
                pageText
        );
    }

    public List<Integer> getVisiblePageNumbers() {

        List<Integer> numbers =
                new ArrayList<>();

        for (WebElement item
                : driver.findElements(
                PAGINATION_ITEMS
        )) {

            if (!item.isDisplayed()) {
                continue;
            }

            String text =
                    readNormalizedText(
                            item
                    );

            if (text.matches(
                    "\\d+"
            )) {

                numbers.add(
                        Integer.parseInt(
                                text
                        )
                );
            }
        }

        return List.copyOf(
                numbers
        );
    }

    public int getVisibleEllipsisCount() {

        int count =
                0;

        for (WebElement element
                : driver.findElements(
                ELLIPSIS
        )) {

            if (element.isDisplayed()) {
                count++;
            }
        }

        return count;
    }

    /*
     * =============================================================
     * PAGINATION ACTIONS
     * =============================================================
     */

    public void clickNextPage() {

        waitUntilClickable(
                NEXT_PAGE
        ).click();
    }

    public void clickPreviousPage() {

        waitUntilClickable(
                PREVIOUS_PAGE
        ).click();
    }

    public void clickPageNumber(
            int pageNumber
    ) {

        By page =
                By.xpath(
                        "//ul[contains(@class,'paginationPages')]"
                                + "//li[contains(@class,'page-item') "
                                + "and normalize-space(.)='"
                                + pageNumber
                                + "']"
                );

        waitUntilClickable(
                page
        ).click();
    }

    public void clickFirstEllipsis() {

        List<WebElement> ellipses =
                getVisibleEllipses();

        if (ellipses.isEmpty()) {

            throw new IllegalStateException(
                    "No visible pagination ellipsis is available."
            );
        }

        ellipses.getFirst()
                .click();
    }

    public void clickLastEllipsis() {

        List<WebElement> ellipses =
                getVisibleEllipses();

        if (ellipses.isEmpty()) {

            throw new IllegalStateException(
                    "No visible pagination ellipsis is available."
            );
        }

        ellipses.getLast()
                .click();
    }

    /*
     * =============================================================
     * LOADING INDICATOR
     * =============================================================
     */

    public boolean waitForLoadingIndicatorToAppear() {

        try {

            new WebDriverWait(
                    driver,
                    Duration.ofSeconds(2)
            )
                    .pollingEvery(
                            Duration.ofMillis(50)
                    )
                    .until(
                            ExpectedConditions
                                    .visibilityOfElementLocated(
                                            PRELOADER
                                    )
                    );

            return true;

        } catch (TimeoutException ignored) {

            return false;
        }
    }

    public void waitForLoadingIndicatorToDisappear() {

        new WebDriverWait(
                driver,
                Duration.ofSeconds(10)
        )
                .pollingEvery(
                        Duration.ofMillis(50)
                )
                .until(
                        ExpectedConditions
                                .invisibilityOfElementLocated(
                                        PRELOADER
                                )
                );
    }

    /*
     * =============================================================
     * PAGE CHANGE WAIT
     * =============================================================
     */

    public CatalogPage waitForPageChange(
            CatalogPage currentPage
    ) {

        WebDriverWait paginationWait =
                new WebDriverWait(
                        driver,
                        Duration.ofSeconds(10)
                );

        paginationWait.until(d -> {

            try {

                int newPageNumber =
                        getCurrentPageNumber();

                List<String> newHrefs =
                        readCurrentHrefs();

                return newPageNumber
                        != currentPage.number()
                        || !newHrefs.equals(
                        currentPage.hrefs()
                );

            } catch (StaleElementReferenceException ignored) {

                return false;
            }
        });

        return captureCurrentPage();
    }

    /*
     * Existing TC1/TC2/TC3/TC4 traversal keeps using this method.
     */
    public CatalogPage goToNextPage(
            CatalogPage currentPage
    ) {

        if (!isNextControlDisplayed()
                || isNextDisabled()) {

            return null;
        }

        clickNextPage();

        return waitForPageChange(
                currentPage
        );
    }

    /*
     * =============================================================
     * PRIVATE HELPERS
     * =============================================================
     */

    private List<WebElement> waitForCourseCards() {

        return getWait().until(driver -> {

            List<WebElement> elements =
                    driver.findElements(
                            COURSE_CARDS
                    );

            return elements.isEmpty()
                    ? null
                    : elements;
        });
    }

    private List<String> readCurrentHrefs() {

        List<String> hrefs =
                new ArrayList<>();

        for (WebElement courseElement
                : driver.findElements(
                COURSE_CARDS
        )) {

            hrefs.add(
                    courseElement.getAttribute(
                            "href"
                    )
            );
        }

        return hrefs;
    }

    private boolean isControlDisabled(
            By locator
    ) {

        List<WebElement> elements =
                driver.findElements(
                        locator
                );

        if (elements.isEmpty()) {
            return true;
        }

        String classes =
                elements.getFirst()
                        .getAttribute(
                                "class"
                        );

        return classes != null
                && classes.contains(
                "disabled"
        );
    }

    private boolean isDisplayedWithoutWait(
            By locator
    ) {

        for (WebElement element
                : driver.findElements(
                locator
        )) {

            if (element.isDisplayed()) {
                return true;
            }
        }

        return false;
    }

    private List<WebElement> getVisibleEllipses() {

        List<WebElement> visible =
                new ArrayList<>();

        for (WebElement element
                : driver.findElements(
                ELLIPSIS
        )) {

            if (element.isDisplayed()) {
                visible.add(
                        element
                );
            }
        }

        return visible;
    }

    private String readChildText(
            WebElement root,
            By locator
    ) {

        List<WebElement> elements =
                root.findElements(
                        locator
                );

        if (elements.isEmpty()) {
            return "";
        }

        return readNormalizedText(
                elements.getFirst()
        );
    }

    private String readChildAttribute(
            WebElement root,
            By locator,
            String attribute
    ) {

        List<WebElement> elements =
                root.findElements(
                        locator
                );

        if (elements.isEmpty()) {
            return "";
        }

        String value =
                elements.getFirst()
                        .getAttribute(
                                attribute
                        );

        return normalize(
                value
        );
    }

    private String readNormalizedText(
            WebElement element
    ) {

        return normalize(
                element.getDomProperty(
                        "textContent"
                )
        );
    }

    private String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace(
                        '\u00A0',
                        ' '
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();
    }
}