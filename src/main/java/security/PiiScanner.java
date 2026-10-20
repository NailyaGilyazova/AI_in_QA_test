package security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PiiScanner {

    public static PiiReport scan(String text) {

        PiiReport report = new PiiReport();

        find(text, PiiPatterns.EMAIL, "EMAIL", report);
        find(text, PiiPatterns.PHONE, "PHONE", report);
        find(text, PiiPatterns.PASSWORD, "PASSWORD", report);
        find(text, PiiPatterns.API_SECRET, "API_SECRET", report);
        find(text, PiiPatterns.BEARER_TOKEN, "BEARER_TOKEN", report);
        find(text, PiiPatterns.IBAN, "IBAN", report);
        find(text, PiiPatterns.SNILS, "SNILS", report);
        findBankCards(text, report);

        return report;
    }

    private static void find(String text, Pattern pattern, String type, PiiReport report) {
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            if (!"PHONE".equals(type) || !isPartOfCardOrSnils(text, m.start(), m.end())) {
                report.add(type);
            }
        }
    }

    private static boolean isPartOfCardOrSnils(String text, int start, int end) {
        Matcher cardMatcher = PiiPatterns.CARD_CANDIDATE.matcher(text);
        while (cardMatcher.find()) {
            if (contains(cardMatcher.start(), cardMatcher.end(), start, end)
                    && PiiPatterns.isBankCardNumber(cardMatcher.group())) {
                return true;
            }
        }

        Matcher snilsMatcher = PiiPatterns.SNILS.matcher(text);
        while (snilsMatcher.find()) {
            if (contains(snilsMatcher.start(), snilsMatcher.end(), start, end)) {
                return true;
            }
        }
        return false;
    }

    private static boolean contains(int containerStart, int containerEnd, int start, int end) {
        return containerStart <= start && end <= containerEnd;
    }

    private static void findBankCards(String text, PiiReport report) {
        Matcher matcher = PiiPatterns.CARD_CANDIDATE.matcher(text);
        while (matcher.find()) {
            if (PiiPatterns.isBankCardNumber(matcher.group())) {
                report.add("BANK_CARD");
            }
        }
    }
}
