package security;

import java.util.LinkedHashMap;
import java.util.Map;

public class PiiReport {

    private final Map<String, Integer> findings = new LinkedHashMap<>();

    public void add(String type) {
        findings.merge(type, 1, Integer::sum);
    }

    public boolean hasFindings() {
        return !findings.isEmpty();
    }

    public Map<String, Integer> getFindings() {
        return findings;
    }

    public String toText() {
        if (findings.isEmpty()) {
            return "No PII detected";
        }

        StringBuilder sb = new StringBuilder("PII DETECTED:\n");
        for (var finding : findings.entrySet()) {
            sb.append("- ")
                    .append(finding.getKey())
                    .append(": ")
                    .append(finding.getValue())
                    .append(" совпадений\n");
        }
        return sb.toString();
    }
}
