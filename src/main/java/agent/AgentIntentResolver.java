package agent;

import java.util.Locale;

public final class AgentIntentResolver {

    private AgentIntentResolver() {
    }

    public static AgentIntent resolve(String userGoal) {
        String goal = normalize(userGoal);

        if (containsAny(goal, "pii", "персональ", "персональных", "чувствительн", "секрет", "маскир")) {
            return AgentIntent.PII_ONLY;
        }

        if (containsTestcaseRequest(goal)) {
            if (containsAny(goal, "без автомат", "не автомат", "не генерируй авто", "только")) {
                return AgentIntent.TESTCASES_ONLY;
            }
            if (!containsFullCycleRequest(goal)) {
                return AgentIntent.TESTCASES_ONLY;
            }
        }

        if (containsFullCycleRequest(goal)) {
            return AgentIntent.FULL_QA_CYCLE;
        }

        if (containsTestcaseRequest(goal)) {
            return AgentIntent.TESTCASES_ONLY;
        }

        if (containsAny(goal, "только сценар", "сценарии", "сценариев")) {
            return AgentIntent.SCENARIOS_ONLY;
        }

        return AgentIntent.FULL_QA_CYCLE;
    }

    private static boolean containsTestcaseRequest(String goal) {
        return containsAny(goal,
                "тест кейс",
                "тест-кейс",
                "тесткейс",
                "test case",
                "testcase",
                "test cases",
                "testcases");
    }

    private static boolean containsFullCycleRequest(String goal) {
        return containsAny(goal, "полный цикл", "full cycle", "полную провер", "qa провер", "автоматизац",
                "автотест", "ui тест", "selenium", "allure", "bug report", "баг репорт", "запусти тест");
    }

    private static boolean containsAny(String goal, String... markers) {
        for (String marker : markers) {
            if (goal.contains(marker)) {
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}
