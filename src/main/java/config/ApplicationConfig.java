package config;

import util.FilesUtil;

import java.util.Properties;

public record ApplicationConfig(
        String url,
        String llmModel,
        String llmSystemPrompt,
        String gigaChatAuthKey,
        String gigaChatScope,
        boolean gigaChatVerifySslCerts,
        Properties properties
) {

    private static final String DEFAULT_GIGACHAT_MODEL = "GigaChat-3-Ultra";
    private static final String DEFAULT_OLLAMA_MODEL = "qwen2.5-coder:7b";
    private static final String DEFAULT_GIGACHAT_API_URL = "https://api.giga.chat/v1";
    private static final String DEFAULT_GIGACHAT_SCOPE = "GIGACHAT_API_PERS";
    private static final double DEFAULT_TEMPERATURE = 0.2;
    private static final int DEFAULT_GIGACHAT_READ_TIMEOUT_SECONDS = 120;
    private static final int DEFAULT_GIGACHAT_CONNECT_TIMEOUT_SECONDS = 30;
    private static final String DEFAULT_LLM_SYSTEM_PROMPT =
            "You are a QA engineer. Return structured output.";

    public static ApplicationConfig load() {
        try {
            Properties properties = new Properties();
            properties.load(new java.io.StringReader(FilesUtil.read("application.properties")));
            String url = properties.getProperty("application.url", "").trim();
            if (url.isBlank()) {
                throw new RuntimeException("Не задано свойство application.url");
            }
            String provider = properties.getProperty("llm.provider", "gigachat").trim().toLowerCase();
            if (!provider.equals("gigachat") && !provider.equals("ollama")) {
                throw new RuntimeException("llm.provider должен быть gigachat или ollama: " + provider);
            }
            String defaultModel = provider.equals("ollama") ? DEFAULT_OLLAMA_MODEL : DEFAULT_GIGACHAT_MODEL;
            String providerModel = properties.getProperty(provider + ".model", defaultModel);
            String llmModel = properties.getProperty("llm.model", providerModel).trim();
            String llmSystemPrompt = properties.getProperty("llm.systemPrompt", DEFAULT_LLM_SYSTEM_PROMPT).trim();
            String gigaChatAuthKey = System.getenv("GIGACHAT_AUTH_KEY");
            String gigaChatScope = properties.getProperty("gigachat.scope", DEFAULT_GIGACHAT_SCOPE).trim();
            boolean gigaChatVerifySslCerts = Boolean.parseBoolean(
                    properties.getProperty("gigachat.verifySslCerts", "false").trim());
            if (llmModel.isBlank()) {
                throw new RuntimeException("Не задано свойство llm.model");
            }
            if (llmSystemPrompt.isBlank()) {
                throw new RuntimeException("Не задано свойство llm.systemPrompt");
            }
            if (provider.equals("gigachat") && (gigaChatAuthKey == null || gigaChatAuthKey.isBlank())) {
                throw new RuntimeException("Должна быть установлена переменная GIGACHAT_AUTH_KEY");
            }
            return new ApplicationConfig(
                    url,
                    llmModel,
                    llmSystemPrompt,
                    gigaChatAuthKey,
                    gigaChatScope,
                    gigaChatVerifySslCerts,
                    properties
            );
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить application.properties", e);
        }
    }

    public String llmModel(String profile) {
        return profileProperty(profile, "model", llmModel).trim();
    }

    public String llmProvider() {
        return properties.getProperty("llm.provider", "gigachat").trim().toLowerCase();
    }

    public String ollamaBaseUrl() {
        String value = properties.getProperty("ollama.baseUrl", "http://localhost:11434").trim();
        if (value.isBlank()) {
            throw new RuntimeException("Не задано свойство ollama.baseUrl");
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    public int ollamaConnectTimeoutSeconds() {
        return positiveIntProperty("ollama.connectTimeoutSeconds", 10);
    }

    public int ollamaReadTimeoutSeconds() {
        return positiveIntProperty("ollama.readTimeoutSeconds", 300);
    }

    public String llmSystemPrompt(String profile) {
        return profileProperty(profile, "systemPrompt", llmSystemPrompt).trim();
    }

    public double llmTemperature(String profile) {
        return parseDouble(profileProperty(profile, "temperature", String.valueOf(DEFAULT_TEMPERATURE)),
                "temperature", 0.0, 2.0);
    }

    public Integer llmMaxTokens(String profile) {
        String value = profileProperty(profile, "maxTokens", "").trim();
        if (value.isBlank()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new RuntimeException("maxTokens должен быть больше 0");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Некорректное значение maxTokens: " + value, e);
        }
    }

    public Double llmTopP(String profile) {
        String value = profileProperty(profile, "topP", "").trim();
        if (value.isBlank()) {
            return null;
        }
        return parseDouble(value, "topP", 0.0, 1.0);
    }

    public int gigaChatReadTimeoutSeconds() {
        return positiveIntProperty(
                "gigachat.readTimeoutSeconds",
                DEFAULT_GIGACHAT_READ_TIMEOUT_SECONDS
        );
    }

    public int gigaChatConnectTimeoutSeconds() {
        return positiveIntProperty(
                "gigachat.connectTimeoutSeconds",
                DEFAULT_GIGACHAT_CONNECT_TIMEOUT_SECONDS
        );
    }

    public String gigaChatApiUrl() {
        String value = properties.getProperty("gigachat.apiUrl", DEFAULT_GIGACHAT_API_URL).trim();
        if (value.isBlank()) {
            throw new RuntimeException("Не задано свойство gigachat.apiUrl");
        }
        return value;
    }

    private String profileProperty(String profile, String name, String fallback) {
        String normalizedProfile = profile == null || profile.isBlank() ? "default" : profile.trim();
        String profileKey = "llm." + normalizedProfile + "." + name;
        String defaultKey = "llm.default." + name;
        String legacyKey = "llm." + name;

        return properties.getProperty(
                profileKey,
                properties.getProperty(defaultKey, properties.getProperty(legacyKey, fallback))
        );
    }

    private double parseDouble(String value, String name, double min, double max) {
        try {
            double parsed = Double.parseDouble(value.trim());
            if (parsed < min || parsed > max) {
                throw new RuntimeException(name + " должен быть в диапазоне " + min + ".." + max);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Некорректное значение " + name + ": " + value, e);
        }
    }

    private int positiveIntProperty(String name, int fallback) {
        String value = properties.getProperty(name, String.valueOf(fallback)).trim();
        try {
            int parsed = Integer.parseInt(value);
            if (parsed <= 0) {
                throw new RuntimeException(name + " должен быть больше 0");
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new RuntimeException("Некорректное значение " + name + ": " + value, e);
        }
    }
}
