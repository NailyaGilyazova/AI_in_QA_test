package security;

import org.testng.Assert;
import org.testng.annotations.Test;

public class PiiScannerTest {

    @Test
    public void shouldDetectAndMaskSupportedSensitiveData() {
        String source = """
                email: student@example.com
                phone: +375 29 123-45-67
                password: qwerty
                api_key: 1234567890abcdef1234567890abcdef
                Authorization: Bearer abcdefghijklmnopqrstuvwxyz.123456
                card: 4111 1111 1111 1111
                iban: DE89370400440532013000
                snils: 112-233-445 95
                """;

        PiiReport report = PiiScanner.scan(source);
        String masked = PiiMasker.mask(source);

        Assert.assertEquals(report.getFindings().get("EMAIL"), Integer.valueOf(1));
        Assert.assertEquals(report.getFindings().get("PHONE"), Integer.valueOf(1));
        Assert.assertEquals(report.getFindings().get("PASSWORD"), Integer.valueOf(1));
        Assert.assertEquals(report.getFindings().get("API_SECRET"), Integer.valueOf(1));
        Assert.assertEquals(report.getFindings().get("BEARER_TOKEN"), Integer.valueOf(1));
        Assert.assertEquals(report.getFindings().get("BANK_CARD"), Integer.valueOf(1));
        Assert.assertEquals(report.getFindings().get("IBAN"), Integer.valueOf(1));
        Assert.assertEquals(report.getFindings().get("SNILS"), Integer.valueOf(1));
        Assert.assertFalse(report.toText().contains("student@example.com"));
        Assert.assertFalse(masked.contains("qwerty"));
        Assert.assertFalse(masked.contains("4111 1111 1111 1111"));
        Assert.assertTrue(masked.contains("[BANK_CARD]"));
    }
}
