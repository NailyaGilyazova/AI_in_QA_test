package agent;

import agent.skills.AllureResultsSkill;
import agent.skills.BugReportSkill;
import agent.skills.BuildScenariosPromptSkill;
import agent.skills.PiiScanSkill;
import agent.skills.ScenarioGenerationSkill;
import agent.skills.SummarySkill;
import agent.skills.TestRunSkill;
import agent.skills.TestcaseGenerationSkill;
import agent.skills.UiTestGenerationSkill;

import java.util.List;

public final class AgentSkillRegistry {

    private AgentSkillRegistry() {
    }

    public static List<AgentSkill> defaultSkills() {
        return List.of(
                new BuildScenariosPromptSkill(),
                new PiiScanSkill(),
                new ScenarioGenerationSkill(),
                new TestcaseGenerationSkill(),
                new UiTestGenerationSkill(),
                new TestRunSkill(),
                new AllureResultsSkill(),
                new BugReportSkill(),
                new SummarySkill()
        );
    }
}
