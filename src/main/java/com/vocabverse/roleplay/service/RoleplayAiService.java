package com.vocabverse.roleplay.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.roleplay.dto.response.RoleplayAiReply;
import com.vocabverse.roleplay.dto.response.RoleplayAiReport;
import com.vocabverse.roleplay.dto.response.RoleplayAiScenario;
import com.vocabverse.roleplay.entity.RoleplayCorrection;
import com.vocabverse.roleplay.entity.RoleplayMessageEntity;
import com.vocabverse.roleplay.entity.RoleplaySessionEntity;
import com.vocabverse.roleplay.enums.RoleplayMessageSender;
import com.vocabverse.roleplay.prompt.RoleplayPromptBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RoleplayAiService {

    private static final URI GROQ_CHAT_COMPLETIONS_URI = URI.create("https://api.groq.com/openai/v1/chat/completions");

    private final RoleplayPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${ai.groq.api-key:}")
    private String groqApiKey;

    @Value("${roleplay.ai.model:${ai.groq.model:llama-3.1-8b-instant}}")
    private String model;

    public RoleplayAiScenario startSession(String topic, String difficulty, String persona) {
        String prompt = promptBuilder.buildScenarioPrompt(topic, difficulty, persona);
        if (!isAiAvailable()) {
            return fallbackScenario(topic, difficulty, persona);
        }

        try {
            JsonNode root = readJsonCompletionWithRetry(prompt, 0.5);
            String scenario = requiredText(root, "scenario");
            String firstMessage = requiredText(root, "firstMessage");
            return new RoleplayAiScenario(scenario, firstMessage);
        } catch (BusinessException exception) {
            return fallbackScenario(topic, difficulty, persona);
        }
    }

    public RoleplayAiReply reply(RoleplaySessionEntity session, List<RoleplayMessageEntity> messages, String userMessage) {
        String prompt = promptBuilder.buildReplyPrompt(session, messages, userMessage);
        if (!isAiAvailable()) {
            return fallbackReply(session, userMessage);
        }

        try {
            JsonNode root = readJsonCompletionWithRetry(prompt, 0.4);
            String reply = requiredText(root, "reply");
            JsonNode correctionNode = root.path("correction");
            RoleplayCorrection correction = new RoleplayCorrection(
                    textOrDefault(correctionNode, "original", userMessage),
                    textOrDefault(correctionNode, "corrected", userMessage),
                    textOrDefault(correctionNode, "betterExpression", userMessage),
                    textOrDefault(correctionNode, "explanation", "Keep practicing clear, complete English sentences.")
            );
            return new RoleplayAiReply(reply, correction);
        } catch (BusinessException exception) {
            return fallbackReply(session, userMessage);
        }
    }

    public RoleplayAiReport report(RoleplaySessionEntity session, List<RoleplayMessageEntity> messages) {
        String prompt = promptBuilder.buildReportPrompt(session, messages);
        if (!isAiAvailable()) {
            return fallbackReport(session, messages);
        }

        try {
            JsonNode root = readJsonCompletionWithRetry(prompt, 0.2);
            return new RoleplayAiReport(
                    requiredText(root, "summary"),
                    textArray(root.path("strengths")),
                    textArray(root.path("weaknesses")),
                    textArray(root.path("suggestedVocabulary")),
                    requiredText(root, "grammarFeedback"),
                    clampScore(root.path("overallScore").asInt(0)),
                    clampRubricScore(root.path("grammarScore").asInt(0), 25),
                    clampRubricScore(root.path("vocabularyScore").asInt(0), 20),
                    clampRubricScore(root.path("relevanceScore").asInt(0), 20),
                    clampRubricScore(root.path("fluencyScore").asInt(0), 20),
                    clampRubricScore(root.path("interactionScore").asInt(0), 15)
            );
        } catch (BusinessException exception) {
            return fallbackReport(session, messages);
        }
    }

    private JsonNode readJsonCompletionWithRetry(String prompt, double temperature) {
        try {
            return readJsonCompletion(prompt, temperature);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() != ErrorCode.AI_RESPONSE_INVALID) {
                throw exception;
            }
            return readJsonCompletion(prompt, temperature);
        }
    }

    private JsonNode readJsonCompletion(String prompt, double temperature) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "temperature", temperature,
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", prompt
                    ))
            ));

            HttpRequest request = HttpRequest.newBuilder(GROQ_CHAT_COMPLETIONS_URI)
                    .timeout(Duration.ofSeconds(45))
                    .header("Authorization", "Bearer " + groqApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                if (response.statusCode() == 429) {
                    throw new BusinessException(ErrorCode.AI_RATE_LIMITED);
                }
                throw new BusinessException(ErrorCode.AI_PROVIDER_NOT_AVAILABLE);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content.isBlank()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
            }
            return objectMapper.readTree(stripCodeFence(content));
        } catch (BusinessException exception) {
            throw exception;
        } catch (HttpTimeoutException exception) {
            throw new BusinessException(ErrorCode.AI_TIMEOUT, ErrorCode.AI_TIMEOUT.getMessage(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.AI_PROCESSING_FAILED, ErrorCode.AI_PROCESSING_FAILED.getMessage(), exception);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, ErrorCode.AI_RESPONSE_INVALID.getMessage(), exception);
        }
    }

    private boolean isAiAvailable() {
        return StringUtils.hasText(groqApiKey);
    }

    private RoleplayAiScenario fallbackScenario(String topic, String difficulty, String persona) {
        String scenario = "You are practicing a realistic English conversation about " + topic + " with " + persona + ".";
        String firstMessage = switch (difficulty) {
            case "EASY" -> "Hi, let's practice " + topic + ". Please answer in English.";
            case "HARD" -> "Let's begin. Give a detailed English response about " + topic + ".";
            default -> "Hi, let's start our English conversation about " + topic + ".";
        };
        return new RoleplayAiScenario(scenario, firstMessage);
    }

    private RoleplayAiReply fallbackReply(RoleplaySessionEntity session, String userMessage) {
        String reply = userMessage.endsWith("?")
                ? "Good question. Please answer in English with one more detail about " + session.getTopic() + "."
                : "I understand. Can you expand your answer in English for this " + session.getTopic() + " situation?";
        return new RoleplayAiReply(reply, buildCorrection(userMessage));
    }

    private RoleplayAiReport fallbackReport(RoleplaySessionEntity session, List<RoleplayMessageEntity> messages) {
        long userMessageCount = messages.stream()
                .filter(message -> message.getSender() == RoleplayMessageSender.USER)
                .count();
        int grammarScore = 12;
        int vocabularyScore = 10;
        int relevanceScore = 12;
        int fluencyScore = 10;
        int interactionScore = Math.min(15, (int) userMessageCount * 3);
        int score = grammarScore + vocabularyScore + relevanceScore + fluencyScore + interactionScore;

        return new RoleplayAiReport(
                "You practiced an English roleplay session about " + session.getTopic() + ". AI scoring was unavailable, so this is a conservative practice summary.",
                List.of("Stayed in the conversation", "Practiced responding in English"),
                List.of("Use more complete sentences", "Add more specific details", "Review grammar and connectors"),
                List.of(session.getTopic(), "Could you clarify?", "I would prefer...", "That sounds useful."),
                "Focus on clear sentence structure, correct tense, and natural connectors.",
                score,
                grammarScore,
                vocabularyScore,
                relevanceScore,
                fluencyScore,
                interactionScore
        );
    }

    private RoleplayCorrection buildCorrection(String userMessage) {
        String trimmed = userMessage.trim();
        String corrected = trimmed.isEmpty()
                ? trimmed
                : Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
        if (!corrected.endsWith(".") && !corrected.endsWith("?") && !corrected.endsWith("!")) {
            corrected = corrected + ".";
        }

        return new RoleplayCorrection(
                userMessage,
                corrected,
                corrected,
                "Use a complete English sentence with clear punctuation."
        );
    }

    private String requiredText(JsonNode root, String field) {
        String value = root.path(field).asText("").trim();
        if (value.isBlank()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
        return value;
    }

    private String textOrDefault(JsonNode root, String field, String fallback) {
        String value = root.path(field).asText("").trim();
        return value.isBlank() ? fallback : value;
    }

    private List<String> textArray(JsonNode node) {
        if (!node.isArray()) {
            return List.of();
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            String value = item.asText("").trim();
            if (!value.isBlank()) {
                values.add(value);
            }
        }
        return values;
    }

    private int clampScore(int score) {
        return Math.max(0, Math.min(100, score));
    }

    private int clampRubricScore(int score, int maxScore) {
        return Math.max(0, Math.min(maxScore, score));
    }

    private String stripCodeFence(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }
}
