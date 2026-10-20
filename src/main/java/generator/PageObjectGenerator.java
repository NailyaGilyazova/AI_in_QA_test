package generator;

import client.LlmClient;
import client.LlmProfile;
import util.FilesUtil;

import java.util.regex.Pattern;

public class PageObjectGenerator {

    public static final String PATH =
            "src/test/java/org/demo/generated/pages/GeneratedPageObject.java";
    private static final String EXPECTED_PACKAGE = "org.demo.generated.pages";
    private static final int MAX_ATTEMPTS = 3;
    private static final Pattern PUBLIC_QUERY_METHOD = Pattern.compile(
            "(?m)public\\s+(?:boolean|String)\\s+\\w+\\s*\\(");

    public static String generate(String url, String html) {
        String basePrompt = FilesUtil.read("prompts/03_page_object_java.txt")
                .replace("{{APPLICATION_URL}}", url)
                .replace("{{HTML}}", html);
        String prompt = basePrompt;
        RuntimeException lastError = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            FilesUtil.write("generated/page_object_prompt.txt", prompt);

            LlmClient.LlmResponse response = LlmClient.call(prompt, LlmProfile.PAGE_OBJECT);
            FilesUtil.write("generated/page_object_raw.json", response.rawBody());
            FilesUtil.write("generated/page_object_llm.txt", response.content());

            try {
                String javaCode = JavaCodeExtractor.extract(response.content(), EXPECTED_PACKAGE);
                javaCode = JavaCodePostProcessor.normalizePageObject(javaCode);
                validate(javaCode);
                FilesUtil.write(PATH, javaCode);
                return javaCode;
            } catch (RuntimeException e) {
                lastError = e;
                if (attempt == MAX_ATTEMPTS) {
                    break;
                }
                prompt = buildRetryPrompt(basePrompt, response.content(), e.getMessage(), attempt + 1);
            }
        }
        throw new RuntimeException("Не удалось сгенерировать валидный Page Object за "
                + MAX_ATTEMPTS + " попытки", lastError);
    }

    private static void validate(String javaCode) {
        if (!javaCode.contains("package " + EXPECTED_PACKAGE + ";")
                || !javaCode.contains("class GeneratedPageObject")
                || !javaCode.contains("GeneratedPageObject(WebDriver driver)")
                || !javaCode.contains("void open()")) {
            throw new RuntimeException("Page Object does not satisfy the required Java contract");
        }
        if (javaCode.contains("public By ")) {
            throw new RuntimeException("Page Object must expose behavior methods, not public By fields");
        }
        if (javaCode.contains("@FindBy(dataTestId")) {
            throw new RuntimeException(
                    "Selenium @FindBy has no dataTestId attribute; use css = \"[data-test='value']\"");
        }
        if (PageObjectContractAnalyzer.extractPublicMethods(javaCode).size() <= 1) {
            throw new RuntimeException("Page Object must expose public methods in addition to open()");
        }
        if (!PUBLIC_QUERY_METHOD.matcher(javaCode).find()) {
            throw new RuntimeException(
                    "Page Object must expose at least one public boolean/String query method for assertions");
        }
    }

    private static String buildRetryPrompt(String basePrompt, String previousAnswer, String error, int attempt) {
        return basePrompt + "\n\n"
                + "ПРЕДЫДУЩАЯ ПОПЫТКА #" + (attempt - 1) + " НЕ ПРОШЛА ВАЛИДАЦИЮ.\n"
                + "Ошибка валидации: " + error + "\n"
                + "Исправь предыдущий ответ и верни заново только Java-код без Markdown.\n"
                + "Особенно важно:\n"
                + "- класс должен называться GeneratedPageObject\n"
                + "- должен быть package " + EXPECTED_PACKAGE + ";\n"
                + "- должен быть конструктор GeneratedPageObject(WebDriver driver)\n"
                + "- должен быть метод open()\n\n"
                + "ПРЕДЫДУЩИЙ ОТВЕТ:\n"
                + previousAnswer;
    }
}
