package agent.skills;

import agent.AgentContext;
import agent.AgentIntent;
import agent.AgentSkill;
import agent.AgentStepResult;
import client.LlmProfile;
import client.LlmClient;
import util.FilesUtil;

public class ScenarioGenerationSkill implements AgentSkill {

    @Override
    public String name() {
        return "GENERATE_SCENARIOS";
    }

    @Override
    public boolean canHandle(AgentContext context) {
        return context.hasIntent(AgentIntent.FULL_QA_CYCLE)
                || context.hasIntent(AgentIntent.TESTCASES_ONLY)
                || context.hasIntent(AgentIntent.SCENARIOS_ONLY);
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        LlmClient.LlmResponse response = LlmClient.call(context.scenariosPrompt(), LlmProfile.SCENARIOS);
        FilesUtil.write("generated/scenarios_raw.json", response.rawBody());
        context.scenarios(response.content());
        FilesUtil.write("generated/scenarios.txt", response.content());
        return AgentStepResult.passed(name(), "Scenarios saved to generated/scenarios.txt");
    }
}
