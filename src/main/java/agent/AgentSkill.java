package agent;

public interface AgentSkill {

    String name();

    default boolean canHandle(AgentContext context) {
        return true;
    }

    AgentStepResult execute(AgentContext context);
}
