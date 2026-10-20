package agent.skills;

import agent.AgentContext;
import agent.AgentIntent;
import agent.AgentSkill;
import agent.AgentStepResult;
import client.LlmProfile;
import client.LlmClient;
import parser.JsonExtractor;
import parser.TestcasesParser;
import util.FilesUtil;

public class TestcaseGenerationSkill implements AgentSkill {

    @Override
    public String name() {
        return "GENERATE_AND_VALIDATE_TESTCASES";
    }

    @Override
    public boolean canHandle(AgentContext context) {
        return context.hasIntent(AgentIntent.FULL_QA_CYCLE)
                || context.hasIntent(AgentIntent.TESTCASES_ONLY);
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        String testcasesPrompt = FilesUtil.read("prompts/02_testcases_json.txt")
                .replace("{{SCENARIOS}}", context.scenarios());
        FilesUtil.write("generated/testcases_prompt.txt", testcasesPrompt);

        LlmClient.LlmResponse response = LlmClient.call(testcasesPrompt, LlmProfile.TESTCASES);
        FilesUtil.write("generated/testcases_raw.json", response.rawBody());
        FilesUtil.write("generated/testcases_llm.txt", response.content());

        String testcasesJson = JsonExtractor.extractJson(response.content());
        context.testcasesJson(testcasesJson);
        FilesUtil.write(AgentContext.TESTCASES_PATH, testcasesJson);
        TestcasesParser.parse(AgentContext.TESTCASES_PATH);
        return AgentStepResult.passed(name(), "Validated JSON saved to " + AgentContext.TESTCASES_PATH);
    }
}
