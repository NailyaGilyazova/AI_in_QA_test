package agent;

public record AgentStepResult(
        String name,
        AgentStepStatus status,
        String details
) {
    public static AgentStepResult passed(String name, String details) {
        return new AgentStepResult(name, AgentStepStatus.PASSED, details);
    }

    public static AgentStepResult failed(String name, String details) {
        return new AgentStepResult(name, AgentStepStatus.FAILED, details);
    }

    public static AgentStepResult skipped(String name, String details) {
        return new AgentStepResult(name, AgentStepStatus.SKIPPED, details);
    }
}
