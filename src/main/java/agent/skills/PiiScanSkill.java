package agent.skills;

import agent.AgentContext;
import agent.AgentSkill;
import agent.AgentStepResult;
import security.PiiMasker;
import security.PiiReport;
import security.PiiScanner;
import util.FilesUtil;

public class PiiScanSkill implements AgentSkill {

    @Override
    public String name() {
        return "SCAN_AND_MASK_PII";
    }

    @Override
    public AgentStepResult execute(AgentContext context) {
        PiiReport report = PiiScanner.scan(context.scenariosPrompt());
        FilesUtil.write("generated/pii_report.txt", report.toText());

        if (report.hasFindings()) {
            String maskedPrompt = PiiMasker.mask(context.scenariosPrompt());
            context.scenariosPrompt(maskedPrompt);
            FilesUtil.write("generated/scenarios_prompt_masked.txt", maskedPrompt);
            return AgentStepResult.passed(name(), "PII found and masked before LLM call");
        }

        return AgentStepResult.passed(name(), "No supported PII found");
    }
}
