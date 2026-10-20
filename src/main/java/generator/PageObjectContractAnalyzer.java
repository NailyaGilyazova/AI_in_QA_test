package generator;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PageObjectContractAnalyzer {

    private static final Pattern PUBLIC_METHOD_PATTERN = Pattern.compile(
            "(?m)public\\s+(?!class\\b)(?!interface\\b)(?!enum\\b)(?!record\\b)(?:static\\s+)?[\\w<>\\[\\], ?]+\\s+(\\w+)\\s*\\(");
    private static final Pattern PAGE_METHOD_CALL_PATTERN = Pattern.compile("\\bpage\\.(\\w+)\\s*\\(");

    private PageObjectContractAnalyzer() {
    }

    public static Set<String> extractPublicMethods(String pageObjectCode) {
        LinkedHashSet<String> methods = new LinkedHashSet<>();
        Matcher matcher = PUBLIC_METHOD_PATTERN.matcher(pageObjectCode);
        while (matcher.find()) {
            String methodName = matcher.group(1);
            if (!"GeneratedPageObject".equals(methodName)) {
                methods.add(methodName);
            }
        }
        return methods;
    }

    public static Set<String> extractCalledPageMethods(String testCode) {
        LinkedHashSet<String> methods = new LinkedHashSet<>();
        Matcher matcher = PAGE_METHOD_CALL_PATTERN.matcher(testCode);
        while (matcher.find()) {
            methods.add(matcher.group(1));
        }
        return methods;
    }

    public static String formatMethodsForPrompt(Set<String> methods) {
        if (methods.isEmpty()) {
            return "- open()";
        }

        StringBuilder builder = new StringBuilder();
        for (String method : methods) {
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append("- ").append(method).append("()");
        }
        return builder.toString();
    }
}
