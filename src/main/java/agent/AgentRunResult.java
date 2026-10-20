package agent;

import java.util.List;

public record AgentRunResult(
        String userGoal,
        boolean success,
        String finalReport,
        List<AgentStepResult> steps
) {
}
