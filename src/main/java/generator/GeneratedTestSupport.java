package generator;

import util.FilesUtil;

public class GeneratedTestSupport {

    public static final String PATH =
            "src/test/java/org/demo/generated/support/GeneratedBaseUiTest.java";

    public static String create() {
        String source = """
                package org.demo.generated.support;

                import org.openqa.selenium.WebDriver;
                import org.openqa.selenium.chrome.ChromeDriver;
                import org.openqa.selenium.chrome.ChromeOptions;
                import org.testng.annotations.AfterMethod;
                import org.testng.annotations.BeforeMethod;

                public class GeneratedBaseUiTest {
                    protected WebDriver driver;

                    @BeforeMethod
                    public void setUp() {
                        ChromeOptions options = new ChromeOptions();
                        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
                        driver = new ChromeDriver(options);
                        driver.manage().window().maximize();
                    }

                    @AfterMethod
                    public void tearDown() {
                        if (driver != null) {
                            driver.quit();
                        }
                    }
                }
                """;
        FilesUtil.write(PATH, source);
        return source;
    }
}
