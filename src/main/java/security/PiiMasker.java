package security;

import java.util.regex.Matcher;

public class PiiMasker {

    public static String mask(String text) {
        String masked = maskBankCards(text);
        masked = PiiPatterns.EMAIL.matcher(masked).replaceAll("[EMAIL]");
        masked = PiiPatterns.PHONE.matcher(masked).replaceAll("[PHONE]");
        masked = PiiPatterns.PASSWORD.matcher(masked).replaceAll("$1[SECRET]");
        masked = PiiPatterns.API_SECRET.matcher(masked).replaceAll("$1[SECRET]");
        masked = PiiPatterns.BEARER_TOKEN.matcher(masked).replaceAll("[BEARER_TOKEN]");
        masked = PiiPatterns.IBAN.matcher(masked).replaceAll("[IBAN]");
        return PiiPatterns.SNILS.matcher(masked).replaceAll("[SNILS]");
    }

    private static String maskBankCards(String text) {
        Matcher matcher = PiiPatterns.CARD_CANDIDATE.matcher(text);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String replacement = PiiPatterns.isBankCardNumber(matcher.group())
                    ? "[BANK_CARD]"
                    : matcher.group();
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
