package client;

import config.ApplicationConfig;

public final class LlmClient {

    private LlmClient() {
    }

    public static LlmResponse call(String prompt) {
        return call(prompt, LlmProfile.DEFAULT);
    }

    public static LlmResponse call(String prompt, String profile) {
        ApplicationConfig config = ApplicationConfig.load();
        return switch (config.llmProvider()) {
            case "gigachat" -> GigaChatLlmClient.call(prompt, profile, config);
            case "ollama" -> OllamaLlmClient.call(prompt, profile, config);
            default -> throw new IllegalStateException("Unsupported LLM provider: " + config.llmProvider());
        };
    }

    public record LlmResponse(String rawBody, String content) {
    }
}
