package agent;

import util.FilesUtil;

import java.util.List;

public class QaAgent {

    private final List<AgentSkill> skills;

    public QaAgent() {
        this(AgentSkillRegistry.defaultSkills());
    }

    public QaAgent(List<AgentSkill> skills) {
        this.skills = List.copyOf(skills);
    }

    public AgentRunResult run(String userGoal) {
        AgentContext context = new AgentContext(userGoal);
        context.addStep(AgentStepResult.passed(
                "RESOLVE_AGENT_INTENT",
                "Resolved intent: " + context.intent()
        ));

        try {
            for (AgentSkill skill : skills) {
                if (!skill.canHandle(context)) {
                    context.addStep(AgentStepResult.skipped(skill.name(), "Skill skipped by canHandle"));
                    continue;
                }
                context.addStep(skill.execute(context));
            }

            context.writeTrace();
            return new AgentRunResult(
                    context.userGoal(),
                    true,
                    context.finalReport(),
                    context.steps()
            );
        } catch (RuntimeException e) {
            context.addStep(AgentStepResult.failed("AGENT_STOPPED", rootMessage(e)));
            String fallbackReport = buildFallbackReport(context, e);
            context.finalReport(fallbackReport);
            FilesUtil.write("generated/agent_summary.txt", fallbackReport);
            context.writeTrace();
            return new AgentRunResult(context.userGoal(), false, fallbackReport, context.steps());
        }
    }

    private String buildFallbackReport(AgentContext context, RuntimeException e) {
        return """
                AI QA Agent stopped before completion.

                Goal:
                %s

                Reason:
                %s

                Completed steps:
                %s
                """.formatted(context.userGoal(), rootMessage(e), context.formatTrace());
    }

    private String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() == null ? current.toString() : current.getMessage();
    }
}
