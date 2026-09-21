package vn.cybersoft.vlearning.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
    public void tearDown() {
        if (driver != null) {
            try {
                LOGGER.info("Closing Chrome");
                driver.quit();
            } finally {
                driver = null;
            }
        }
    }
}
