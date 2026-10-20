package security;

import java.util.regex.Pattern;

public final class PiiPatterns {

    public static final Pattern EMAIL =
            Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    public static final Pattern PHONE =
            Pattern.compile("(?<![\\dA-Za-z])\\+?\\d[\\d\\s\\-()]{7,}\\d(?![\\dA-Za-z])");
    public static final Pattern PASSWORD =
            Pattern.compile("(?i)(password\\s*[:=]\\s*)\\S+");
    public static final Pattern API_SECRET = Pattern.compile(
            "(?i)((?:api[_-]?key|access[_-]?token|auth[_-]?token|client[_-]?secret|secret)\\s*[:=]\\s*[\\\"']?)[A-Za-z0-9._~+/=-]{16,}[\\\"']?");
    public static final Pattern BEARER_TOKEN =
            Pattern.compile("(?i)\\bBearer\\s+[A-Za-z0-9._~+/=-]{16,}");
    public static final Pattern IBAN =
            Pattern.compile("\\b[A-Z]{2}\\d{2}[A-Z0-9]{11,30}\\b");
    public static final Pattern SNILS =
            Pattern.compile("(?<!\\d)\\d{3}-\\d{3}-\\d{3}\\s?\\d{2}(?!\\d)");
    public static final Pattern CARD_CANDIDATE =
            Pattern.compile("(?<!\\d)(?:\\d[ -]?){13,19}(?!\\d)");

    private PiiPatterns() {
    }

    public static boolean isBankCardNumber(String value) {
        String digits = value.replaceAll("\\D", "");
        if (digits.length() < 13 || digits.length() > 19) {
            return false;
        }

        int sum = 0;
        boolean doubleDigit = false;
        for (int index = digits.length() - 1; index >= 0; index--) {
            int digit = digits.charAt(index) - '0';
            if (doubleDigit) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            doubleDigit = !doubleDigit;
        }
        return sum % 10 == 0;
    }
}
