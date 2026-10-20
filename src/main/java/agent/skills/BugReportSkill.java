package agent.skills;

import agent.AgentContext;
import agent.AgentIntent;
import agent.AgentSkill;
import agent.AgentStepResult;
import client.LlmProfile;
import client.LlmClient;
import parser.JsonExtractor;
import util.FilesUtil;

public class BugReportSkill implements AgentSkill {

    @Override
    public String name() {
        return "GENERATE_BUG_REPORT";
    }

    @Override
    public boolean canHandle(AgentContext context) {
        return context.hasIntent(AgentIntent.FULL_QA_CYCLE);
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        String bugPrompt = FilesUtil.read("prompts/05_bug_report.txt")
                .replace("{{TEST_RUN_LOG}}", nullToEmpty(context.testRunLog()))
                .replace("{{ALLURE_RESULTS}}", nullToEmpty(context.allureResults()));
        FilesUtil.write("generated/bug_report_prompt.txt", bugPrompt);

        LlmClient.LlmResponse response = LlmClient.call(bugPrompt, LlmProfile.BUG_REPORT);
        FilesUtil.write("generated/bug_report_raw.json", response.rawBody());
        FilesUtil.write("generated/bug_report_llm.txt", response.content());
        String bugReportJson = JsonExtractor.extractJson(response.content());
        context.bugReportJson(bugReportJson);
        FilesUtil.write("generated/bug_report.json", bugReportJson);
        return AgentStepResult.passed(name(), "Bug report saved to generated/bug_report.json");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
