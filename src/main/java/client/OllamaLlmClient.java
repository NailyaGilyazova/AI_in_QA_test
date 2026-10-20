package client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import config.ApplicationConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class OllamaLlmClient {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private OllamaLlmClient() {
    }

    static LlmClient.LlmResponse call(String prompt, String profile, ApplicationConfig config) {
        try {
            ObjectNode body = OBJECT_MAPPER.createObjectNode();
            body.put("model", config.llmModel(profile));
            body.put("stream", false);

            ArrayNode messages = body.putArray("messages");
            addMessage(messages, "system", config.llmSystemPrompt(profile));
            addMessage(messages, "user", prompt);

            ObjectNode options = body.putObject("options");
            options.put("temperature", config.llmTemperature(profile));
            Integer maxTokens = config.llmMaxTokens(profile);
            if (maxTokens != null) {
                options.put("num_predict", maxTokens);
            }
            Double topP = config.llmTopP(profile);
            if (topP != null) {
                options.put("top_p", topP);
            }

            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(config.ollamaConnectTimeoutSeconds()))
                    .build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(config.ollamaBaseUrl() + "/api/chat"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(config.ollamaReadTimeoutSeconds()))
                    .POST(HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new RuntimeException(
                        "Ollama API returned HTTP " + response.statusCode() + ": " + response.body());
            }

            JsonNode responseJson = OBJECT_MAPPER.readTree(response.body());
            String content = responseJson.path("message").path("content").asText();
            if (content.isBlank()) {
                throw new RuntimeException("Ollama API response has no assistant content: " + response.body());
            }
            return new LlmClient.LlmResponse(response.body(), content);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Вызов локальной модели Ollama был прерван", e);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка вызова локальной модели Ollama", e);
        }
    }

    private static void addMessage(ArrayNode messages, String role, String content) {
        ObjectNode message = messages.addObject();
        message.put("role", role);
        message.put("content", content);
    }
}
