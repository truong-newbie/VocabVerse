package com.vocabverse.ai.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GroqVocabularyClient {

    private static final URI CHAT_COMPLETIONS_URI = URI.create("https://api.groq.com/openai/v1/chat/completions");

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${ai.groq.model:llama-3.1-8b-instant}")
    private String model;

    public GroqVocabularyClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public String completeJson(String prompt, String apiKey) {
        String requestId = UUID.randomUUID().toString();
        long startedAt = System.nanoTime();
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "temperature", 0.1,
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", prompt
                    ))
            ));

            HttpRequest request = HttpRequest.newBuilder(CHAT_COMPLETIONS_URI)
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.info("AI normalize provider=GROQ requestId={} durationMs={} status={}",
                    requestId,
                    durationMs,
                    response.statusCode());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                if (response.statusCode() == 429) {
                    throw new BusinessException(ErrorCode.AI_RATE_LIMITED);
                }
                throw new BusinessException(ErrorCode.AI_PROVIDER_NOT_AVAILABLE);
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode content = root.path("choices").path(0).path("message").path("content");
            if (!content.isTextual() || content.asText().isBlank()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
            }
            return content.asText();
        } catch (BusinessException exception) {
            long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.warn("AI normalize failed provider=GROQ requestId={} durationMs={} errorCode={}",
                    requestId,
                    durationMs,
                    exception.getErrorCode().getCode());
            throw exception;
        } catch (HttpTimeoutException exception) {
            long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.warn("AI normalize timeout provider=GROQ requestId={} durationMs={}", requestId, durationMs);
            throw new BusinessException(ErrorCode.AI_TIMEOUT, ErrorCode.AI_TIMEOUT.getMessage(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.AI_NORMALIZE_FAILED, ErrorCode.AI_NORMALIZE_FAILED.getMessage(), exception);
        } catch (Exception exception) {
            long durationMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();
            log.warn("AI normalize failed provider=GROQ requestId={} durationMs={} errorCode={}",
                    requestId,
                    durationMs,
                    ErrorCode.AI_NORMALIZE_FAILED.getCode());
            throw new BusinessException(ErrorCode.AI_NORMALIZE_FAILED, ErrorCode.AI_NORMALIZE_FAILED.getMessage(), exception);
        }
    }
}
