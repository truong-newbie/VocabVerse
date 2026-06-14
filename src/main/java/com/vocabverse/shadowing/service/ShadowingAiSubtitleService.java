package com.vocabverse.shadowing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.shadowing.entity.ShadowingLessonEntity;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ShadowingAiSubtitleService {

    private static final URI GROQ_TRANSCRIPTIONS_URI = URI.create("https://api.groq.com/openai/v1/audio/transcriptions");
    private static final URI GROQ_CHAT_COMPLETIONS_URI = URI.create("https://api.groq.com/openai/v1/chat/completions");

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    @Value("${ai.groq.api-key:}")
    private String groqApiKey;

    @Value("${shadowing.ai.subtitle.enabled:true}")
    private boolean enabled;

    @Value("${shadowing.ai.subtitle.transcription-model:whisper-large-v3-turbo}")
    private String transcriptionModel;

    @Value("${shadowing.ai.subtitle.translation-model:llama-3.1-8b-instant}")
    private String translationModel;

    public ShadowingAiSubtitleService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<GeneratedSubtitle> generateSubtitles(ShadowingLessonEntity lesson) {
        assertAvailable(lesson);
        List<TranscriptSegment> segments = transcribe(lesson.getVideoUrl());
        Map<Integer, String> translations = translate(segments);
        List<GeneratedSubtitle> subtitles = new ArrayList<>();
        for (int index = 0; index < segments.size(); index++) {
            TranscriptSegment segment = segments.get(index);
            subtitles.add(new GeneratedSubtitle(
                    segment.startTimeMs(),
                    segment.endTimeMs(),
                    segment.englishText(),
                    translations.get(index),
                    index
            ));
        }
        return subtitles;
    }

    private void assertAvailable(ShadowingLessonEntity lesson) {
        if (!enabled) {
            throw new BusinessException(ErrorCode.AI_PROVIDER_NOT_AVAILABLE, "AI subtitle generation is disabled");
        }
        if (!StringUtils.hasText(groqApiKey)) {
            throw new BusinessException(ErrorCode.AI_PROVIDER_NOT_AVAILABLE, "GROQ_API_KEY is required for AI subtitles");
        }
        if (!StringUtils.hasText(lesson.getVideoUrl())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Lesson videoUrl is required for AI subtitles");
        }
    }

    private List<TranscriptSegment> transcribe(String videoUrl) {
        String boundary = "----vocabverse-" + UUID.randomUUID();
        String body = multipartBody(boundary, Map.of(
                "model", transcriptionModel,
                "url", videoUrl,
                "language", "en",
                "response_format", "verbose_json",
                "temperature", "0",
                "timestamp_granularities[]", "segment"
        ));

        HttpRequest request = HttpRequest.newBuilder(GROQ_TRANSCRIPTIONS_URI)
                .timeout(Duration.ofSeconds(120))
                .header("Authorization", "Bearer " + groqApiKey.trim())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw mapAiError(response.statusCode(), "Groq transcription failed");
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode segmentsNode = root.path("segments");
            if (!segmentsNode.isArray() || segmentsNode.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "Groq transcription response has no segments");
            }

            List<TranscriptSegment> segments = new ArrayList<>();
            for (JsonNode segmentNode : segmentsNode) {
                String text = segmentNode.path("text").asText("").trim();
                if (!text.isBlank()) {
                    segments.add(new TranscriptSegment(
                            secondsToMs(segmentNode.path("start").asDouble(0)),
                            secondsToMs(segmentNode.path("end").asDouble(0)),
                            text
                    ));
                }
            }
            if (segments.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "Groq transcription returned empty text");
            }
            return segments;
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

    private Map<Integer, String> translate(List<TranscriptSegment> segments) {
        try {
            List<Map<String, Object>> items = new ArrayList<>();
            for (int index = 0; index < segments.size(); index++) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("index", index);
                item.put("englishText", segments.get(index).englishText());
                items.add(item);
            }

            String prompt = """
                    Translate the following English shadowing subtitle segments to Vietnamese.
                    Return JSON only as an array of objects with exactly:
                    index: number
                    vietnameseText: string
                    Keep the same index values. Do not add markdown.
                    Segments:
                    """ + objectMapper.writeValueAsString(items);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", translationModel,
                    "temperature", 0.1,
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", prompt
                    ))
            ));

            HttpRequest request = HttpRequest.newBuilder(GROQ_CHAT_COMPLETIONS_URI)
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + groqApiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw mapAiError(response.statusCode(), "Groq translation failed");
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            JsonNode translationRoot = objectMapper.readTree(stripCodeFence(content));
            JsonNode itemsNode = translationRoot.isArray() ? translationRoot : translationRoot.path("items");
            if (!itemsNode.isArray()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, "Groq translation response is not a JSON array");
            }

            Map<Integer, String> translations = new HashMap<>();
            for (JsonNode itemNode : itemsNode) {
                translations.put(
                        itemNode.path("index").asInt(),
                        itemNode.path("vietnameseText").asText(null)
                );
            }
            return translations;
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

    private BusinessException mapAiError(int statusCode, String message) {
        if (statusCode == 429) {
            return new BusinessException(ErrorCode.AI_RATE_LIMITED);
        }
        return new BusinessException(ErrorCode.AI_PROVIDER_NOT_AVAILABLE, message);
    }

    private String multipartBody(String boundary, Map<String, String> fields) {
        StringBuilder builder = new StringBuilder();
        fields.forEach((name, value) -> builder
                .append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"").append(name).append("\"\r\n\r\n")
                .append(value).append("\r\n"));
        builder.append("--").append(boundary).append("--\r\n");
        return builder.toString();
    }

    private int secondsToMs(double seconds) {
        return (int) Math.max(0, Math.round(seconds * 1000));
    }

    private String stripCodeFence(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    private record TranscriptSegment(
            int startTimeMs,
            int endTimeMs,
            String englishText
    ) {
    }

    public record GeneratedSubtitle(
            int startTimeMs,
            int endTimeMs,
            String englishText,
            String vietnameseText,
            int orderIndex
    ) {
    }
}
