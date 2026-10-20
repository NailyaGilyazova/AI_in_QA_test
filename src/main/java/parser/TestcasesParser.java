package parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.TestSuite;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestcasesParser {

    public static TestSuite parse(String path) {
        try {
            String json = Files.readString(Path.of(path));
            ObjectMapper mapper = new ObjectMapper();
            TestSuite suite = mapper.readValue(json, TestSuite.class);
            validate(suite);
            return suite;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse testcases.json", e);
        }
    }

    private static void validate(TestSuite suite) {
        if (suite == null || suite.testcases == null || suite.testcases.isEmpty()) {
            throw new RuntimeException("Testcases must contain a non-empty testcases array");
        }
        for (var testCase : suite.testcases) {
            if (isBlank(testCase.id) || isBlank(testCase.title) || isBlank(testCase.type)
                    || testCase.steps == null || testCase.steps.isEmpty() || isBlank(testCase.expected)) {
                throw new RuntimeException("Testcase has missing required fields: " + testCase.id);
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
