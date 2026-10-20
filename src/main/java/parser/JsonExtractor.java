package parser;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonExtractor {

    public static String extractJson(String text) {

        if (text == null) {
            throw new RuntimeException("LLM returned null");
        }

        // remove markdown fences
        String cleaned = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        // try to cut everything before first {
        int start = cleaned.indexOf("{");
        int end = cleaned.lastIndexOf("}");

        if (start == -1 || end == -1) {
            throw new RuntimeException("No JSON object found in LLM output");
        }

        String json = cleaned.substring(start, end + 1).trim();
        json = stripJsonComments(json);
        try {
            new ObjectMapper().readTree(json);
            return json;
        } catch (Exception e) {
            throw new RuntimeException("LLM output does not contain valid JSON", e);
        }
    }

    private static String stripJsonComments(String json) {
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < json.length(); i++) {
            char current = json.charAt(i);

            if (inString) {
                result.append(current);
                if (escaped) {
                    escaped = false;
                } else if (current == '\\') {
                    escaped = true;
                } else if (current == '"') {
                    inString = false;
                }
                continue;
            }

            if (current == '"') {
                inString = true;
                result.append(current);
                continue;
            }

            if (current == '/' && i + 1 < json.length()) {
                char next = json.charAt(i + 1);
                if (next == '/') {
                    i += 2;
                    while (i < json.length() && json.charAt(i) != '\n' && json.charAt(i) != '\r') {
                        i++;
                    }
                    if (i < json.length()) {
                        result.append(json.charAt(i));
                    }
                    continue;
                }
                if (next == '*') {
                    i += 2;
                    while (i + 1 < json.length()
                            && !(json.charAt(i) == '*' && json.charAt(i + 1) == '/')) {
                        i++;
                    }
                    i++;
                    continue;
                }
            }

            result.append(current);
        }

        return result.toString().trim();
    }
}
