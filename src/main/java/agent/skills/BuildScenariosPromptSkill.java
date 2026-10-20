package agent.skills;

import agent.AgentContext;
import agent.AgentSkill;
import agent.AgentStepResult;
import util.FilesUtil;

public class BuildScenariosPromptSkill implements AgentSkill {

    @Override
    public String name() {
        return "BUILD_SCENARIOS_PROMPT";
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        String prompt = FilesUtil.read("prompts/01_scenarios_from_checklist.txt")
                .replace("{{CHECKLIST}}", FilesUtil.read(AgentContext.CHECKLIST_PATH));
        context.scenariosPrompt(prompt);
        FilesUtil.write("generated/scenarios_prompt.txt", prompt);
        return AgentStepResult.passed(name(), "Prompt saved to generated/scenarios_prompt.txt");
    }
}
