package vn.cybersoft.vlearning.core;

import java.time.Duration;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
    }

    protected WebDriverWait getWait() {
        return new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    protected WebElement waitUntilVisible(By locator) {
        return getWait().until(
                ExpectedConditions.visibilityOfElementLocated(locator)
        );
    }

    protected WebElement waitUntilClickable(By locator) {
        return getWait().until(
                ExpectedConditions.elementToBeClickable(locator)
        );
    }

    protected void click(By locator) {
        waitUntilClickable(locator).click();
    }

    protected boolean isDisplayed(By locator) {
        return waitUntilVisible(locator).isDisplayed();
    }

    protected void enterText(By locator, String text) {
        WebElement input = waitUntilVisible(locator);
        input.clear();
        input.sendKeys(text);
    }

    protected String getText(By locator) {
        return waitUntilVisible(locator).getText().trim();
    }


}
