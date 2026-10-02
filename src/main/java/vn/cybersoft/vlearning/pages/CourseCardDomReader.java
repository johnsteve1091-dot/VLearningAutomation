package vn.cybersoft.vlearning.pages;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import vn.cybersoft.vlearning.pages.CourseCardSnapshot.ParentContent;
import vn.cybersoft.vlearning.pages.CourseCardSnapshot.SubCardContent;

public final class CourseCardDomReader {

    /*
     * =============================================================
     * MAIN COURSE CARD
     * =============================================================
     */

    private static final By COURSE_NAME =
            By.xpath(
                    ".//div[contains(@class,'cardBodyGlobal')]//h6"
            );

    private static final By INSTRUCTOR_NAME =
            By.xpath(
                    ".//div[contains(@class,'cardFooter')]"
                            + "//span[contains(@class,'colorCardTitle')]"
            );

    private static final By INSTRUCTOR_AVATAR =
            By.xpath(
                    ".//div[contains(@class,'cardFooter')]"
                            + "//div[contains(@class,'titleMaker')]//img"
            );

    private static final By HOURS =
            By.xpath(
                    ".//div[contains(@class,'cardBodyGlobal')]"
                            + "//span[.//i[contains(@class,'iconOclock')]]"
            );

    private static final By WEEKS =
            By.xpath(
                    ".//div[contains(@class,'cardBodyGlobal')]"
                            + "//span[.//i[contains(@class,'iconCalendar')]]"
            );

    private static final By LEVEL =
            By.xpath(
                    ".//div[contains(@class,'cardBodyGlobal')]"
                            + "//span[.//i[contains(@class,'iconLevel')]]"
            );

    /*
     * =============================================================
     * SUBCARD
     * =============================================================
     */

    private static final By SUB_CARD =
            By.xpath(
                    ".//div[contains(@class,'subCard')]"
            );

    private static final By SUB_CARD_INSTRUCTOR_AVATAR =
            By.xpath(
                    ".//div[contains(@class,'subCardHead')]//img"
            );

    private static final By SUB_CARD_INSTRUCTOR_NAME =
            By.xpath(
                    ".//div[contains(@class,'subCardHead')]"
                            + "//span[contains(@class,'colorCardTitle')]"
            );

    private static final By SUB_CARD_COURSE_NAME =
            By.xpath(
                    ".//h6"
            );

    private static final By SUB_CARD_DESCRIPTION =
            By.xpath(
                    ".//p[contains(@class,'colorCardTitle')]"
            );

    private static final By SUB_CARD_HOURS =
            By.xpath(
                    ".//span[.//i[contains(@class,'iconOclock')]]"
            );

    private static final By SUB_CARD_WEEKS =
            By.xpath(
                    ".//span[.//i[contains(@class,'iconCalendar')]]"
            );

    private static final By SUB_CARD_LEVEL =
            By.xpath(
                    ".//span[.//i[contains(@class,'iconLevel')]]"
            );

    private static final By SUB_CARD_DETAIL_BUTTON =
            By.xpath(
                    ".//button[contains(@class,'btnSubCard')]"
            );

    private CourseCardDomReader() {
    }

    public static CourseCardSnapshot capture(
            WebElement courseElement,
            int cardIndex
    ) {

        String href =
                normalize(
                        courseElement.getAttribute("href")
                );

        ParentContent parent =
                new ParentContent(
                        readText(courseElement, COURSE_NAME),
                        readText(courseElement, INSTRUCTOR_NAME),
                        readAttribute(
                                courseElement,
                                INSTRUCTOR_AVATAR,
                                "src"
                        ),
                        readText(courseElement, HOURS),
                        readText(courseElement, WEEKS),
                        readText(courseElement, LEVEL)
                );

        List<WebElement> subCards =
                courseElement.findElements(SUB_CARD);

        boolean hasSubCard =
                !subCards.isEmpty();

        SubCardContent subCard =
                null;

        if (hasSubCard) {

            WebElement subCardElement =
                    subCards.getFirst();

            subCard =
                    new SubCardContent(
                            readText(
                                    subCardElement,
                                    SUB_CARD_COURSE_NAME
                            ),
                            readText(
                                    subCardElement,
                                    SUB_CARD_INSTRUCTOR_NAME
                            ),
                            readAttribute(
                                    subCardElement,
                                    SUB_CARD_INSTRUCTOR_AVATAR,
                                    "src"
                            ),
                            readText(
                                    subCardElement,
                                    SUB_CARD_DESCRIPTION
                            ),
                            readText(
                                    subCardElement,
                                    SUB_CARD_HOURS
                            ),
                            readText(
                                    subCardElement,
                                    SUB_CARD_WEEKS
                            ),
                            readText(
                                    subCardElement,
                                    SUB_CARD_LEVEL
                            ),
                            readText(
                                    subCardElement,
                                    SUB_CARD_DETAIL_BUTTON
                            )
                    );
        }

        return new CourseCardSnapshot(
                parent.courseName(),
                href,
                cardIndex,
                hasSubCard,
                parent,
                subCard
        );
    }

    /*
     * getText() is intentionally NOT used here.
     *
     * subCard is hidden until hover, therefore Selenium getText()
     * can return blank for perfectly valid hidden DOM content.
     *
     * textContent reads the actual DOM text without needing hover.
     */
    private static String readText(
            WebElement root,
            By locator
    ) {

        List<WebElement> elements =
                root.findElements(locator);

        if (elements.isEmpty()) {
            return "";
        }

        String text =
                elements.getFirst()
                        .getDomProperty("textContent");

        return normalize(text);
    }

    private static String readAttribute(
            WebElement root,
            By locator,
            String attribute
    ) {

        List<WebElement> elements =
                root.findElements(locator);

        if (elements.isEmpty()) {
            return "";
        }

        return normalize(
                elements.getFirst()
                        .getAttribute(attribute)
        );
    }

    private static String normalize(
            String value
    ) {

        if (value == null) {
            return "";
        }

        return value
                .replace('\u00A0', ' ')
                .replaceAll("\\s+", " ")
                .trim();
    }
}