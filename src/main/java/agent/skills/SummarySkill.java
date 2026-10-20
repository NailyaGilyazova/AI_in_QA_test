package agent.skills;

import agent.AgentContext;
import agent.AgentIntent;
import agent.AgentSkill;
import agent.AgentStepResult;
import client.LlmProfile;
import client.LlmClient;
import util.FilesUtil;

public class SummarySkill implements AgentSkill {

    @Override
    public String name() {
        return "GENERATE_AGENT_SUMMARY";
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        if (context.hasIntent(AgentIntent.PII_ONLY)) {
            String report = buildLocalPiiSummary(context);
            FilesUtil.write("generated/agent_summary.txt", report);
            context.finalReport(report);
            return AgentStepResult.passed(name(), "Local PII summary saved to generated/agent_summary.txt");
        }

        String prompt = FilesUtil.read("prompts/06_agent_summary.txt")
                .replace("{{USER_GOAL}}", context.userGoal())
                .replace("{{AGENT_INTENT}}", context.intent().name())
                .replace("{{AGENT_TRACE}}", context.formatTrace())
                .replace("{{TEST_RUN_LOG}}", nullToEmpty(context.testRunLog()))
                .replace("{{ALLURE_RESULTS}}", nullToEmpty(context.allureResults()))
                .replace("{{BUG_REPORT}}", nullToEmpty(context.bugReportJson()));
        FilesUtil.write("generated/agent_summary_prompt.txt", prompt);

        LlmClient.LlmResponse response = LlmClient.call(prompt, LlmProfile.SUMMARY);
        FilesUtil.write("generated/agent_summary_raw.json", response.rawBody());
        FilesUtil.write("generated/agent_summary.txt", response.content());
        context.finalReport(response.content());
        return AgentStepResult.passed(name(), "Summary saved to generated/agent_summary.txt");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String buildLocalPiiSummary(AgentContext context) {
        return """
                1. Что проверялось
                Проверялся входной checklist/prompt на поддерживаемые PII-паттерны.

                2. Какие шаги агент выполнил
                %s
                3. Результат
                PII-проверка завершена. Подробности сохранены в generated/pii_report.txt.
                """.formatted(context.formatTrace());
    }
}
