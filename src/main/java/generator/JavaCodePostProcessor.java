package generator;

public final class JavaCodePostProcessor {

    private static final String GENERATED_TEST_PACKAGE = "org.demo.generated";
    private static final String GENERATED_PAGE_IMPORT =
            "import org.demo.generated.pages.GeneratedPageObject;\n";
    private static final String GENERATED_BASE_IMPORT =
            "import org.demo.generated.support.GeneratedBaseUiTest;\n";

    private JavaCodePostProcessor() {
    }

    public static String normalizePageObject(String javaCode) {
        String normalized = javaCode;
        if (normalized.contains("By.")
                && !normalized.contains("import org.openqa.selenium.By;")) {
            normalized = normalized.replace(
                    "import org.openqa.selenium.WebDriver;\n",
                    "import org.openqa.selenium.By;\nimport org.openqa.selenium.WebDriver;\n"
            );
        }
        return normalized;
    }

    public static String normalizeGeneratedTest(String javaCode) {
        String normalized = javaCode;
        if (normalized.contains("extends GeneratedBaseUiTest")
                && !normalized.contains(GENERATED_BASE_IMPORT.trim())) {
            normalized = insertImport(normalized, GENERATED_BASE_IMPORT);
        }
        if (normalized.contains("GeneratedPageObject")
                && !normalized.contains(GENERATED_PAGE_IMPORT.trim())) {
            normalized = insertImport(normalized, GENERATED_PAGE_IMPORT);
        }
        return normalized;
    }

    private static String insertImport(String javaCode, String importLine) {
        String packageLine = "package " + GENERATED_TEST_PACKAGE + ";\n";
        if (javaCode.startsWith(packageLine)) {
            return javaCode.replace(packageLine, packageLine + "\n" + importLine);
        }
        return javaCode;
    }
}
