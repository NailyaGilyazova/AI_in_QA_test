package agent;

import java.util.Scanner;

public class QaAgentMain {

    private static final String DEFAULT_GOAL =
            "Провести AI QA проверку целевого приложения по checklist.txt";

    public static void main(String[] args) {
        String userGoal = args.length == 0 ? readGoalFromTerminal() : String.join(" ", args);
        AgentRunResult result = new QaAgent().run(userGoal);

        System.out.println("=== AI QA AGENT RESULT ===");
        System.out.println("Goal: " + result.userGoal());
        System.out.println("Success: " + result.success());
        System.out.println();
        System.out.println(result.finalReport());
    }

    private static String readGoalFromTerminal() {
        System.out.println("Введите задачу для AI QA Agent и нажмите Enter:");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();
        return input.isBlank() ? DEFAULT_GOAL : input;
    }
}
