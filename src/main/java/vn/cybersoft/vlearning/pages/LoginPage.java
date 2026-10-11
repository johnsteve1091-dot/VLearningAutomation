package vn.cybersoft.vlearning.pages;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import vn.cybersoft.vlearning.core.BasePage;

public class LoginPage extends BasePage {

    private static final By ACCOUNT_INPUT =
            By.xpath("//form[.//h1[normalize-space()='Đăng nhập']]//input[@name='taiKhoan']");

    private static final By PASSWORD_INPUT =
            By.xpath("//form[.//h1[normalize-space()='Đăng nhập']]//input[@name='matKhau']");

    private static final By LOGIN_BUTTON =
            By.xpath("//form[.//h1[normalize-space()='Đăng nhập']]//button[@type='submit' and normalize-space()='Đăng nhập']");

    private static final By FORGOT_PASSWORD_LINK =
            By.xpath("//form[.//h1[normalize-space()='Đăng nhập']]//a[normalize-space()='Quên mật khẩu?']");

    private static final By INVALID_CREDENTIALS_POPUP =
            By.xpath("//div[contains(@class,'swal-modal') and @role='dialog'][.//div[normalize-space()='Tài khoản hoặc mật khẩu không đúng!']]");

    private static final By REGISTER_BUTTON =
            By.xpath("//div[contains(@class,'overlay-panel') and contains(@class,'overlay-right')]//button[@id='signUp' and normalize-space()='Đăng ký']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public boolean isAccountInputDisplayed() {
        return isVisible(ACCOUNT_INPUT);
    }

    public boolean isPasswordInputDisplayed() {
        return isVisible(PASSWORD_INPUT);
    }

    public boolean isLoginButtonDisplayed() {
        return isVisible(LOGIN_BUTTON);
    }

    public boolean isForgotPasswordLinkDisplayed() {
        return isVisible(FORGOT_PASSWORD_LINK);
    }

    public void enterAccount(String account) {
        enterText(ACCOUNT_INPUT, account);
    }

    public void enterPassword(String password) {
        enterText(PASSWORD_INPUT, password);
    }

    public void clickLogin() {
        click(LOGIN_BUTTON);
    }

    public boolean isPasswordMasked() {
        return "password".equals(waitUntilVisible(PASSWORD_INPUT).getDomProperty("type"));
    }

    public boolean isPasswordClipboardCopyBlocked() {
        WebElement password = waitUntilVisible(PASSWORD_INPUT);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable previousContents = getWait().ignoring(IllegalStateException.class)
                .until(ignored -> Optional.ofNullable(clipboard.getContents(null))).orElse(null);
        String sentinel = "VLearningClipboardSentinel-" + UUID.randomUUID();

        try {
            new Actions(driver).click(password)
                    .keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).perform();
            boolean fullySelected = Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript("""
                    const input = arguments[0];
                    return document.activeElement === input && input.value.length > 0
                        && input.selectionStart === 0 && input.selectionEnd === input.value.length;
                    """, password));
            if (!fullySelected) {
                throw new IllegalStateException("Copy check requires the entire dummy password to be selected.");
            }

            setClipboardContents(clipboard, new StringSelection(sentinel));
            getWait().until(ignored -> sentinel.equals(readClipboardText(clipboard)));
            new Actions(driver).keyDown(Keys.CONTROL).sendKeys("c").keyUp(Keys.CONTROL).perform();

            // Observe the clipboard for the entire interval; an immediate unchanged
            // value alone would not prove that the copy operation has finished.
            try {
                getWait().withTimeout(Duration.ofSeconds(1)).pollingEvery(Duration.ofMillis(50))
                        .until(ignored -> !sentinel.equals(readClipboardText(clipboard)));
                return false;
            } catch (TimeoutException unchangedClipboard) {
                return sentinel.equals(readClipboardText(clipboard));
            }
        } finally {
            setClipboardContents(clipboard,
                    previousContents == null ? new StringSelection("") : previousContents);
        }
    }

    public boolean isPasswordCopyEventPrevented() {
        WebElement password = waitUntilVisible(PASSWORD_INPUT);
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript("""
                const input = arguments[0];
                if (!input.value) {
                    throw new Error('Copy check requires a non-empty dummy password field.');
                }
                const event = new ClipboardEvent('copy', {
                    bubbles: true,
                    cancelable: true,
                    clipboardData: new DataTransfer()
                });
                input.dispatchEvent(event);
                const exposesPassword = Array.from(event.clipboardData.types)
                    .some(type => event.clipboardData.getData(type).includes(input.value));
                return event.defaultPrevented && !exposesPassword;
                """, password));
    }

    public boolean isInvalidCredentialsPopupDisplayed() {
        return isVisible(INVALID_CREDENTIALS_POPUP);
    }

    public String getInvalidCredentialsPopupTitle() {
        // SweetAlert's first visible line is its title; the later OK button text
        // belongs to the same supplied dialog locator and is not part of the title.
        return getText(INVALID_CREDENTIALS_POPUP).lines()
                .map(String::strip).filter(line -> !line.isEmpty()).findFirst().orElse("");
    }

    private boolean isVisible(By locator) {
        try {
            return isDisplayed(locator);
        } catch (TimeoutException notVisible) {
            return false;
        }
    }

    private void setClipboardContents(Clipboard clipboard, Transferable contents) {
        getWait().ignoring(IllegalStateException.class).until(ignored -> {
            clipboard.setContents(contents, null);
            return true;
        });
    }

    private String readClipboardText(Clipboard clipboard) {
        try {
            if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                return "";
            }
            return (String) clipboard.getData(DataFlavor.stringFlavor);
        } catch (UnsupportedFlavorException | IOException clipboardError) {
            throw new IllegalStateException("Cannot read the local desktop clipboard.", clipboardError);
        }
    }

    public RegisterPage goToRegisterPage() {
        click(REGISTER_BUTTON);
        return new RegisterPage(driver);
    }

    public boolean waitUntilAtLoginPage() {
        try {
            return getWait().until(currentDriver ->
                    "/login".equals(URI.create(currentDriver.getCurrentUrl()).getPath())
            );
        } catch (TimeoutException loginRouteNotReached) {
            return false;
        }
    }

    public boolean waitUntilLoggedIn() {
        try {
            return getWait().until(currentDriver ->
                    !currentDriver.getCurrentUrl().contains("/login")
            );
        } catch (TimeoutException loginDidNotComplete) {
            return false;
        }
    }
}
