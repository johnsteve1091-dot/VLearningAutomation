package vn.cybersoft.vlearning.base;

import io.qameta.allure.Allure;
import java.io.ByteArrayInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {

    private static final Logger LOGGER = LogManager.getLogger(BaseTest.class);
    private static final String HOME_PAGE_URL = "https://demo2.cybersoft.edu.vn/";
    protected WebDriver driver;


    @BeforeMethod
    public void setUp() {
        LOGGER.info("Launching Chrome");
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        LOGGER.info("Opening Homepage: {}", HOME_PAGE_URL);
        driver.get(HOME_PAGE_URL);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        if (driver != null) {
            try {
                if (result.getStatus() == ITestResult.FAILURE
                        && driver instanceof TakesScreenshot screenshotDriver) {
                    try {
                        byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
                        Allure.addAttachment(
                                "Failure Screenshot",
                                "image/png",
                                new ByteArrayInputStream(screenshot),
                                ".png"
                        );
                    } catch (RuntimeException screenshotError) {
                        LOGGER.warn("Could not attach failure screenshot", screenshotError);
                    }
                }

                // Clear authentication/session state after evidence is captured.
                clearBrowserSession();

            } finally {
                try {
                    LOGGER.info("Closing Chrome");
                    driver.quit();
                } finally {
                    driver = null;
                }
            }
        }
    }
    private void clearBrowserSession() {
        try {
            if (driver instanceof JavascriptExecutor js) {
                js.executeScript("""
                    window.localStorage.clear();
                    window.sessionStorage.clear();
                    """);
            }
        } catch (RuntimeException storageError) {
            LOGGER.warn("Could not clear browser storage", storageError);
        }

        try {
            driver.manage().deleteAllCookies();
        } catch (RuntimeException cookieError) {
            LOGGER.warn("Could not clear browser cookies", cookieError);
        }
    }
}
