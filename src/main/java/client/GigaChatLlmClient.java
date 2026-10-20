package client;

import chat.giga.client.GigaChatClient;
import chat.giga.client.auth.AuthClient;
import chat.giga.client.auth.AuthClientBuilder.OAuthBuilder;
import chat.giga.model.Scope;
import chat.giga.model.completion.ChatMessage;
import chat.giga.model.completion.ChatMessageRole;
import chat.giga.model.completion.CompletionRequest;
import chat.giga.model.completion.CompletionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.ApplicationConfig;

public final class GigaChatLlmClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private GigaChatLlmClient() {
    }

    static LlmClient.LlmResponse call(String prompt, String profile, ApplicationConfig config) {
        try {
            GigaChatClient client = createClient(config);
            CompletionRequest.CompletionRequestBuilder request = CompletionRequest.builder()
                    .model(config.llmModel(profile))
                    .message(message(ChatMessageRole.SYSTEM, config.llmSystemPrompt(profile)))
                    .message(message(ChatMessageRole.USER, prompt))
                    .temperature((float) config.llmTemperature(profile));

            Integer maxTokens = config.llmMaxTokens(profile);
            if (maxTokens != null) {
                request.maxTokens(maxTokens);
            }
            Double topP = config.llmTopP(profile);
            if (topP != null) {
                request.topP(topP.floatValue());
            }

            CompletionResponse response = client.completions(request.build());
            String content = response.choices().isEmpty()
                    ? null
                    : response.choices().get(0).message().content();
            String rawBody = OBJECT_MAPPER.writeValueAsString(response);
            if (content == null || content.isBlank()) {
                throw new RuntimeException("GigaChat API response has no assistant content: " + rawBody);
            }
            return new LlmClient.LlmResponse(rawBody, content);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка вызова GigaChat API", e);
        }
    }

    private static GigaChatClient createClient(ApplicationConfig config) {
        return GigaChatClient.builder()
                .apiUrl(config.gigaChatApiUrl())
                .verifySslCerts(config.gigaChatVerifySslCerts())
                .readTimeout(config.gigaChatReadTimeoutSeconds())
                .connectTimeout(config.gigaChatConnectTimeoutSeconds())
                .authClient(AuthClient.builder()
                        .withOAuth(OAuthBuilder.builder()
                                .scope(parseScope(config.gigaChatScope()))
                                .authKey(config.gigaChatAuthKey())
                                .build())
                        .build())
                .build();
    }

    private static Scope parseScope(String value) {
        try {
            return Scope.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Некорректный gigachat.scope: " + value, e);
        }
    }

    private static ChatMessage message(ChatMessageRole role, String content) {
        return ChatMessage.builder().role(role).content(content).build();
    }
}
