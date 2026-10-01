package vn.cybersoft.vlearning.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;

import vn.cybersoft.vlearning.core.BasePage;

public class CourseListPage extends BasePage {

    /*
     * All rendered Course cards.
     *
     * Do NOT filter by /chitiet/ here.
     * TC1 itself decides whether an href is valid.
     * TC2 checks the card structure.
     */
    private static final By COURSE_CARDS =
            By.xpath(
                    "//div[contains(@class,'courseListPage')]"
                            + "//div[contains(@class,'cardGlobalRes')]"
                            + "//a[contains(@class,'cardGlobal')]"
            );

    private static final By ACTIVE_PAGE =
            By.xpath(
                    "//ul[contains(@class,'paginationPages')]"
                            + "//li[contains(@class,'page-item') "
                            + "and contains(@class,'active')]"
            );

    private static final By NEXT_PAGE =
            By.xpath(
                    "//ul[contains(@class,'paginationPages')]"
                            + "//li[contains(@class,'page-item') "
                            + "and normalize-space(.)='Sau >']"
            );

    private static final By COURSE_NAME =
            By.xpath(
                    ".//div[contains(@class,'cardBodyGlobal')]//h6"
            );

    private static final By SUB_CARD =
            By.xpath(
                    ".//div[contains(@class,'subCard')]"
            );

    public CourseListPage(WebDriver driver) {
        super(driver);
    }

    public record CourseCandidate(
            String name,
            String href,
            int page,
            int cardIndex,
            boolean hasSubCard
    ) {
    }

    public record CatalogPage(
            int number,
            List<CourseCandidate> courses,
            List<String> hrefs
    ) {
    }

    public CatalogPage captureCurrentPage() {

        int pageNumber =
                getCurrentPageNumber();

        List<WebElement> courseElements =
                getWait().until(driver -> {

                    List<WebElement> elements =
                            driver.findElements(COURSE_CARDS);

                    return elements.isEmpty()
                            ? null
                            : elements;
                });

        List<CourseCandidate> courses =
                new ArrayList<>();

        List<String> hrefs =
                new ArrayList<>();

        for (int index = 0;
             index < courseElements.size();
             index++) {

            WebElement courseElement =
                    courseElements.get(index);

            String href =
                    courseElement.getAttribute("href");

            String name = "";

            List<WebElement> nameElements =
                    courseElement.findElements(COURSE_NAME);

            if (!nameElements.isEmpty()) {

                name = nameElements.getFirst()
                        .getText()
                        .trim();
            }

            boolean hasSubCard =
                    !courseElement.findElements(SUB_CARD)
                            .isEmpty();

            CourseCandidate course =
                    new CourseCandidate(
                            name,
                            href,
                            pageNumber,
                            index + 1,
                            hasSubCard
                    );

            courses.add(course);
            hrefs.add(href);
        }

        return new CatalogPage(
                pageNumber,
                List.copyOf(courses),
                List.copyOf(hrefs)
        );
    }

    public CatalogPage goToNextPage(
            CatalogPage currentPage
    ) {

        List<WebElement> nextElements =
                driver.findElements(NEXT_PAGE);

        if (nextElements.isEmpty()) {
            return null;
        }

        WebElement next =
                nextElements.getFirst();

        String nextClass =
                next.getAttribute("class");

        if (nextClass != null
                && nextClass.contains("disabled")) {

            return null;
        }

        next.click();

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

    private int getCurrentPageNumber() {

        String pageText =
                waitUntilVisible(ACTIVE_PAGE)
                        .getText()
                        .trim();

        return Integer.parseInt(pageText);
    }

    private List<String> readCurrentHrefs() {

        List<WebElement> courseElements =
                driver.findElements(COURSE_CARDS);

        List<String> hrefs =
                new ArrayList<>();

        for (WebElement courseElement
                : courseElements) {

            hrefs.add(
                    courseElement.getAttribute("href")
            );
        }

        return hrefs;
    }
}