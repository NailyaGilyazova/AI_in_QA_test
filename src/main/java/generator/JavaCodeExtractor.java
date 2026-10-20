package generator;

public class JavaCodeExtractor {

    public static String extract(String text) {
        return extract(text, null);
    }

    public static String extract(String text, String expectedPackage) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("LLM returned empty Java code");
        }

        String cleaned = text
                .replaceFirst("(?s)^\\s*```(?:java)?\\s*", "")
                .replaceFirst("(?s)\\s*```\\s*$", "")
                .trim();

        if (expectedPackage != null && !expectedPackage.isBlank()
                && !cleaned.contains("package " + expectedPackage + ";")) {
            cleaned = "package " + expectedPackage + ";\n\n" + cleaned;
        }

        int packageStart = cleaned.indexOf("package ");
        if (packageStart < 0) {
            throw new RuntimeException("Generated code must start with a package declaration");
        }
        return cleaned.substring(packageStart);
    }
}
