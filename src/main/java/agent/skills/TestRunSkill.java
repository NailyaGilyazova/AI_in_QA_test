package agent.skills;

import agent.AgentContext;
import agent.AgentIntent;
import agent.AgentSkill;
import agent.AgentStepResult;
import util.FilesUtil;
import util.TestRunExecutor;

public class TestRunSkill implements AgentSkill {

    @Override
    public String name() {
        return "RUN_GENERATED_TESTS";
    }

    @Override
    public boolean canHandle(AgentContext context) {
        return context.hasIntent(AgentIntent.FULL_QA_CYCLE);
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        String testRunLog = TestRunExecutor.runTests();
        context.testRunLog(testRunLog);
        FilesUtil.write("generated/test_run.log", testRunLog);

        if (testRunLog.contains("Код завершения: 0")) {
            return AgentStepResult.passed(name(), "Generated UI tests finished successfully");
        }

        if (testRunLog.contains("COMPILATION ERROR")
                || testRunLog.contains("Compilation failure")) {
            throw new RuntimeException("Generated UI tests did not compile; see generated/test_run.log");
        }

        return AgentStepResult.passed(name(), "Generated UI tests finished with failures; continuing to bug report");
    }
}
