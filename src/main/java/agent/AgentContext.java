package agent;

import util.FilesUtil;

import java.util.ArrayList;
import java.util.List;

public class AgentContext {

    public static final String CHECKLIST_PATH = "checklist.txt";
    public static final String TESTCASES_PATH = "generated/testcases.json";
    public static final String TRACE_PATH = "generated/agent_trace.txt";

    private final String userGoal;
    private final AgentIntent intent;
    private final List<AgentStepResult> steps = new ArrayList<>();

    private String scenariosPrompt;
    private String scenarios;
    private String testcasesJson;
    private String pageObject;
    private String testRunLog;
    private String allureResults;
    private String bugReportJson;
    private String finalReport;

    public AgentContext(String userGoal) {
        this.userGoal = userGoal;
        this.intent = AgentIntentResolver.resolve(userGoal);
    }

    public String userGoal() {
        return userGoal;
    }

    public AgentIntent intent() {
        return intent;
    }

    public boolean hasIntent(AgentIntent expectedIntent) {
        return intent == expectedIntent;
    }

    public List<AgentStepResult> steps() {
        return List.copyOf(steps);
    }

    public void addStep(AgentStepResult result) {
        steps.add(result);
        writeTrace();
    }

    public void writeTrace() {
        FilesUtil.write(TRACE_PATH, formatTrace());
    }

    public String formatTrace() {
        StringBuilder builder = new StringBuilder();
        for (AgentStepResult step : steps) {
            builder.append(step.status())
                    .append(" | ")
                    .append(step.name())
                    .append(" | ")
                    .append(step.details())
                    .append('\n');
        }
        return builder.toString();
    }

    public String scenariosPrompt() {
        return scenariosPrompt;
    }

    public void scenariosPrompt(String scenariosPrompt) {
        this.scenariosPrompt = scenariosPrompt;
    }

    public String scenarios() {
        return scenarios;
    }

    public void scenarios(String scenarios) {
        this.scenarios = scenarios;
    }

    public String testcasesJson() {
        return testcasesJson;
    }

    public void testcasesJson(String testcasesJson) {
        this.testcasesJson = testcasesJson;
    }

    public String pageObject() {
        return pageObject;
    }

    public void pageObject(String pageObject) {
        this.pageObject = pageObject;
    }

    public String testRunLog() {
        return testRunLog;
    }

    public void testRunLog(String testRunLog) {
        this.testRunLog = testRunLog;
    }

    public String allureResults() {
        return allureResults;
    }

    public void allureResults(String allureResults) {
        this.allureResults = allureResults;
    }

    public String bugReportJson() {
        return bugReportJson;
    }

    public void bugReportJson(String bugReportJson) {
        this.bugReportJson = bugReportJson;
    }

    public String finalReport() {
        return finalReport;
    }

    public void finalReport(String finalReport) {
        this.finalReport = finalReport;
    }
}
