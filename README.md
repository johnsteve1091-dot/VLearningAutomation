# V Learning Automation

Java 26 · Gradle Wrapper 9.7.1 · Selenium 4.45.0 · TestNG 7.12.0 · Log4j 2.26.1

This first capstone source contains two independent tests. Each opens fresh
Chrome, starts at the Homepage, clicks **Đăng nhập**, verifies its Account or
Password input is displayed, and quits Chrome.

## Open and run on Windows

1. Install **JDK 26**, **Google Chrome**, and **IntelliJ IDEA**.
2. Extract the ZIP, then open the `VLearningAutomation` folder containing
   `build.gradle` directly in IntelliJ.
3. Set **Project SDK = JDK 26** and **Gradle JVM = JDK 26**.
4. Use the included **Gradle Wrapper 9.7.1** and allow Gradle to sync.
5. Run `LoginTests` using its green run icon, or run either method alone.

From PowerShell in the project root:

```powershell
.\gradlew.bat test
```

The wrapper removes the need for a globally installed Gradle. Internet access
is needed for the website, dependencies, and Selenium Manager's ChromeDriver
download. JDK auto-download is disabled: if Gradle cannot find Java 26, install
JDK 26 and select it as the Gradle JVM. No URL or XPath edits are needed.

Log4j writes browser lifecycle messages to the console. Gradle's basic HTML
results are available at `build/reports/tests/test/index.html`.
