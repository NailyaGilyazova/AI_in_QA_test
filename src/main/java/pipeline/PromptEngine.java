package pipeline;

import util.FilesUtil;

public class PromptEngine {

    public static String buildPrompt(String promptTemplatePath, String checklistPath) {
        String template = FilesUtil.read(promptTemplatePath);
        String checklist = FilesUtil.read(checklistPath);
        return template.replace("{{CHECKLIST}}", checklist);
    }

    public static String buildPrompt(String promptTemplatePath, String checklistPath, String scenarioPath) {
        String template = FilesUtil.read(promptTemplatePath);
        String checklist = FilesUtil.read(checklistPath);
        String scenario = FilesUtil.read(scenarioPath);
        String t1 = template.replace("{{SCENARIOS}}", scenario);
        return t1.replace("{{CHECKLIST}}", checklist);
    }
}
