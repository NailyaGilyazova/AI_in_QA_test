package generator;

import client.LlmClient;
import client.LlmProfile;
import util.FilesUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class TestGenerator {

    private static final String OUTPUT_PATH =
            "src/test/java/org/demo/generated/GeneratedUiTest.java";
    private static final String EXPECTED_PACKAGE = "org.demo.generated";
    private static final int MAX_ATTEMPTS = 3;
    private static final Pattern SUCCESS_TEST_ASSERTS_LOGIN_PAGE = Pattern.compile(
            "(?s)void\\s+\\w*Successful\\w*\\s*\\([^)]*\\)\\s*\\{.*Assert\\.assertTrue\\s*\\(\\s*page\\.isOnLoginPage\\s*\\(");
    private static final Pattern STRICT_ERROR_TEXT_ASSERTION = Pattern.compile(
            "Assert\\.assertEquals\\s*\\(\\s*page\\.getErrorMessage\\s*\\(");
    private static final Pattern DIRECT_PAGE_FIELD_ACCESS = Pattern.compile(
            "\\bpage\\.\\w+\\b(?!\\s*\\()");

    public static String generate(String testcasesJson, String baseUiTest, String pageObject) {
        Set<String> availableMethods = PageObjectContractAnalyzer.extractPublicMethods(pageObject);
        String basePrompt = FilesUtil.read("prompts/04_autotests_java.txt")
                .replace("{{TESTCASES}}", testcasesJson)
                .replace("{{BASE_UI_TEST}}", baseUiTest)
                .replace("{{PAGE_OBJECT}}", pageObject)
                .replace("{{AVAILABLE_PAGE_OBJECT_METHODS}}",
                        PageObjectContractAnalyzer.formatMethodsForPrompt(availableMethods));
        String prompt = basePrompt;
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            FilesUtil.write("generated/autotests_prompt.txt", prompt);
            LlmClient.LlmResponse response = LlmClient.call(prompt, LlmProfile.AUTOTESTS);
            FilesUtil.write("generated/autotests_raw.json", response.rawBody());
            FilesUtil.write("generated/autotests_llm.txt", response.content());

            try {
                String javaCode = JavaCodeExtractor.extract(response.content(), EXPECTED_PACKAGE);
                javaCode = JavaCodePostProcessor.normalizeGeneratedTest(javaCode);
                validate(javaCode, availableMethods);
                FilesUtil.write(OUTPUT_PATH, javaCode);
                return javaCode;
            } catch (RuntimeException e) {
                lastError = e;
                if (attempt == MAX_ATTEMPTS) {
                    break;
                }
                prompt = buildRetryPrompt(basePrompt, response.content(), e.getMessage(), attempt + 1);
            }
        }
        throw new RuntimeException("Не удалось сгенерировать валидный GeneratedUiTest за "
                + MAX_ATTEMPTS + " попытки", lastError);
    }

    private static void validate(String javaCode, Set<String> availableMethods) {
        if (!javaCode.contains("package " + EXPECTED_PACKAGE + ";")
                || !javaCode.contains("class GeneratedUiTest")
                || !javaCode.contains("extends GeneratedBaseUiTest")
                || !javaCode.contains("@Test")) {
            throw new RuntimeException("Generated test does not satisfy the required Java test contract");
        }

        Set<String> calledMethods = PageObjectContractAnalyzer.extractCalledPageMethods(javaCode);
        LinkedHashSet<String> missingMethods = new LinkedHashSet<>(calledMethods);
        missingMethods.removeAll(availableMethods);
        if (!missingMethods.isEmpty()) {
            throw new RuntimeException(
                    "Generated test calls methods absent in GeneratedPageObject: " + missingMethods
                            + ". Available methods: " + availableMethods);
        }

        String codeWithoutAllowedConstructor = javaCode.replace("new GeneratedPageObject(driver)", "");
        if (codeWithoutAllowedConstructor.contains("driver.")) {
            throw new RuntimeException("Generated test must not access WebDriver directly; use GeneratedPageObject methods");
        }
        if (javaCode.contains("By.") || javaCode.contains("import org.openqa.selenium.By;")) {
            throw new RuntimeException("Generated test must not use Selenium By directly; use GeneratedPageObject methods");
        }
        if (DIRECT_PAGE_FIELD_ACCESS.matcher(javaCode).find()) {
            throw new RuntimeException(
                    "Generated test must not access Page Object fields directly; call public Page Object methods");
        }
        int testCount = countOccurrences(javaCode, "@Test");
        int assertionCount = countOccurrences(javaCode, "Assert.");
        if (assertionCount < testCount) {
            throw new RuntimeException(
                    "Every generated @Test must contain a TestNG Assert using a Page Object query method");
        }
        if (javaCode.contains("ExpectedConditions")) {
            throw new RuntimeException("Generated test must not use ExpectedConditions directly; waits belong in GeneratedPageObject");
        }
        if (SUCCESS_TEST_ASSERTS_LOGIN_PAGE.matcher(javaCode).find()) {
            throw new RuntimeException(
                    "Successful login tests must not assert that the user remains on the login page");
        }
        if (STRICT_ERROR_TEXT_ASSERTION.matcher(javaCode).find()) {
            throw new RuntimeException(
                    "Do not use strict equality for generated error messages; assert visibility, non-empty text, or contains only when exact text is explicitly required");
        }
    }

    private static int countOccurrences(String value, String token) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(token, offset)) >= 0) {
            count++;
            offset += token.length();
        }
        return count;
    }

    private static String buildRetryPrompt(String basePrompt, String previousAnswer, String error, int attempt) {
        return basePrompt + "\n\n"
                + "ПРЕДЫДУЩАЯ ПОПЫТКА #" + (attempt - 1) + " НЕ ПРОШЛА ВАЛИДАЦИЮ.\n"
                + "Ошибка валидации: " + error + "\n"
                + "Исправь предыдущий ответ и верни заново только Java-код без Markdown.\n"
                + "Особенно важно:\n"
                + "- класс должен называться GeneratedUiTest\n"
                + "- должен быть package " + EXPECTED_PACKAGE + ";\n"
                + "- класс должен наследоваться от GeneratedBaseUiTest\n"
                + "- использовать можно только методы Page Object из списка доступных\n"
                + "- нельзя обращаться к driver напрямую\n\n"
                + "ПРЕДЫДУЩИЙ ОТВЕТ:\n"
                + previousAnswer;
    }
}
