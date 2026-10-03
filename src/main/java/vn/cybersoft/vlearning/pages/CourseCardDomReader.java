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

    /*
     * Main Course image is the direct image child of a.cardGlobal.
     *
     * This deliberately avoids instructor/subCard images.
     */
    private static final By COURSE_IMAGE =
            By.xpath(
                    "./img[1]"
            );

    private static final By COURSE_NAME =
            By.xpath(
                    ".//div[contains(@class,'cardBodyGlobal')]//h6"
            );

    /*
     * IMPORTANT:
     *
     * /khoahoc places titleMaker inside cardBodyGlobal.
     * Other Course-card surfaces may place it elsewhere.
     *
     * Therefore we anchor on titleMaker itself instead of
     * assuming it is inside cardFooter.
     */
    private static final By INSTRUCTOR_NAME =
            By.xpath(
                    ".//div[contains(@class,'titleMaker')]"
                            + "//span[contains(@class,'colorCardTitle')]"
            );

    private static final By INSTRUCTOR_AVATAR =
            By.xpath(
                    ".//div[contains(@class,'titleMaker')]//img"
            );

    /*
     * These fields are present on some Course-card surfaces such
     * as Homepage. They may legitimately be absent on /khoahoc.
     */
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
     * Price DOM observed on /khoahoc:
     *
     * div.cardFooter
     *   div
     *     p -> original price
     *     p -> discounted price
     */
    private static final By ORIGINAL_PRICE =
            By.xpath(
                    "(.//div[contains(@class,'cardFooter')]"
                            + "//p[.//sup])[1]"
            );

    private static final By DISCOUNTED_PRICE =
            By.xpath(
                    "(.//div[contains(@class,'cardFooter')]"
                            + "//p[.//sup])[2]"
            );

    /*
     * Rating container is identified using the star icon so we do
     * not confuse review count with instructor information.
     */
    private static final By RATING_SCORE =
            By.xpath(
                    ".//div[contains(@class,'cardFooter')]"
                            + "//div[.//i[contains(@class,'fa-star')]]"
                            + "//span[contains(@class,'textStar')]"
            );

    private static final By REVIEW_COUNT =
            By.xpath(
                    ".//div[contains(@class,'cardFooter')]"
                            + "//div[.//i[contains(@class,'fa-star')]]"
                            + "//span[contains(@class,'colorCardTitle')]"
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

    private record ImageState(
            String src,
            boolean displayed,
            int naturalWidth
    ) {
    }

    public static CourseCardSnapshot capture(
            WebElement courseElement,
            int cardIndex
    ) {

        String href =
                normalize(
                        courseElement.getAttribute("href")
                );

        ImageState courseImage =
                readImageState(
                        courseElement,
                        COURSE_IMAGE
                );

        ImageState instructorAvatar =
                readImageState(
                        courseElement,
                        INSTRUCTOR_AVATAR
                );

        ParentContent parent =
                new ParentContent(
                        readText(
                                courseElement,
                                COURSE_NAME
                        ),
                        readText(
                                courseElement,
                                INSTRUCTOR_NAME
                        ),
                        instructorAvatar.src(),

                        readText(
                                courseElement,
                                HOURS
                        ),
                        readText(
                                courseElement,
                                WEEKS
                        ),
                        readText(
                                courseElement,
                                LEVEL
                        ),

                        courseImage.src(),
                        courseImage.displayed(),
                        courseImage.naturalWidth(),

                        instructorAvatar.displayed(),
                        instructorAvatar.naturalWidth(),

                        readText(
                                courseElement,
                                ORIGINAL_PRICE
                        ),
                        readText(
                                courseElement,
                                DISCOUNTED_PRICE
                        ),
                        readTextDecoration(
                                courseElement,
                                ORIGINAL_PRICE
                        ),

                        readText(
                                courseElement,
                                RATING_SCORE
                        ),
                        readText(
                                courseElement,
                                REVIEW_COUNT
                        )
                );

        List<WebElement> subCards =
                courseElement.findElements(
                        SUB_CARD
                );

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
     * =============================================================
     * LOW-LEVEL DOM READERS
     * =============================================================
     */

    private static String readText(
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

        String text =
                elements.getFirst()
                        .getDomProperty(
                                "textContent"
                        );

        return normalize(
                text
        );
    }

    private static String readAttribute(
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

        return normalize(
                elements.getFirst()
                        .getAttribute(
                                attribute
                        )
        );
    }

    private static ImageState readImageState(
            WebElement root,
            By locator
    ) {

        List<WebElement> elements =
                root.findElements(
                        locator
                );

        if (elements.isEmpty()) {

            return new ImageState(
                    "",
                    false,
                    0
            );
        }

        WebElement image =
                elements.getFirst();

        String src =
                normalize(
                        image.getAttribute(
                                "src"
                        )
                );

        boolean displayed;

        try {

            displayed =
                    image.isDisplayed();

        } catch (RuntimeException ignored) {

            displayed =
                    false;
        }

        int naturalWidth =
                readNaturalWidth(
                        image
                );

        return new ImageState(
                src,
                displayed,
                naturalWidth
        );
    }

    private static int readNaturalWidth(
            WebElement image
    ) {

        String value =
                image.getDomProperty(
                        "naturalWidth"
                );

        if (value == null
                || value.isBlank()) {

            return 0;
        }

        try {

            return Integer.parseInt(
                    value
            );

        } catch (NumberFormatException ignored) {

            return 0;
        }
    }

    private static String readTextDecoration(
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

        WebElement element =
                elements.getFirst();

        String line =
                normalize(
                        element.getCssValue(
                                "text-decoration-line"
                        )
                );

        if (line.contains(
                "line-through"
        )) {

            return line;
        }

        String full =
                normalize(
                        element.getCssValue(
                                "text-decoration"
                        )
                );

        return full.isBlank()
                ? line
                : full;
    }

    private static String normalize(
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