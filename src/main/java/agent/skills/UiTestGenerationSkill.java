package agent.skills;

import agent.AgentContext;
import agent.AgentIntent;
import agent.AgentSkill;
import agent.AgentStepResult;
import config.ApplicationConfig;
import generator.GeneratedTestSupport;
import generator.PageObjectGenerator;
import generator.TestGenerator;
import ui.UiSnapshotClient;
import util.FilesUtil;

public class UiTestGenerationSkill implements AgentSkill {

    @Override
    public String name() {
        return "GENERATE_UI_TESTS";
    }

    @Override
    public boolean canHandle(AgentContext context) {
        return context.hasIntent(AgentIntent.FULL_QA_CYCLE);
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        ApplicationConfig config = ApplicationConfig.load();
        String html = UiSnapshotClient.loadHtml(config.url());
        FilesUtil.write("generated/page_snapshot.html", html);

        String baseUiTest = GeneratedTestSupport.create();
        String pageObject = PageObjectGenerator.generate(config.url(), html);
        context.pageObject(pageObject);
        TestGenerator.generate(context.testcasesJson(), baseUiTest, pageObject);
        return AgentStepResult.passed(name(), "Page Object and UI test classes generated");
    }
}
