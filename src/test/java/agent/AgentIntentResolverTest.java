package agent;

import org.testng.Assert;
import org.testng.annotations.Test;

public class AgentIntentResolverTest {

    @Test
    public void shouldResolveTestcasesOnlyRequest() {
        AgentIntent intent = AgentIntentResolver.resolve(
                "Сгенерируй только тесткейсы без автоматизации");

        Assert.assertEquals(intent, AgentIntent.TESTCASES_ONLY);
    }

    @Test
    public void shouldResolvePiiOnlyRequest() {
        AgentIntent intent = AgentIntentResolver.resolve(
                "Проверь checklist.txt на PII и чувствительные данные");
        Assert.assertEquals(intent, AgentIntent.PII_ONLY);
    }

    @Test
    public void shouldResolveFullCycleWhenAutomationIsRequested() {
        AgentIntent intent = AgentIntentResolver.resolve(
                "Проведи полный QA цикл с автотестами и Allure");

        Assert.assertEquals(intent, AgentIntent.FULL_QA_CYCLE);
    }
}
