package ui;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class UiSnapshotClient {

    private static final int MAX_HTML_LENGTH = 100_000;

    public static String loadHtml(String url) {
        try {
            return truncate(loadRenderedHtml(url));
        } catch (Exception e) {
            return truncate(loadStaticHtml(url));
        }
    }

    private static String loadRenderedHtml(String url) {
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.get(url);
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(webDriver -> !webDriver.getPageSource().contains("<div id=\"root\"></div>"));
            return driver.getPageSource();
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private static String loadStaticHtml(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .header("User-Agent", "AI-QA-Pipeline/1.0")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();
            HttpResponse<String> response = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build()
                    .send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException("Страница вернула HTTP " + response.statusCode());
            }
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить HTML страницы: " + url, e);
        }
    }

    private static String truncate(String html) {
        return html.length() > MAX_HTML_LENGTH ? html.substring(0, MAX_HTML_LENGTH) : html;
    }
}
