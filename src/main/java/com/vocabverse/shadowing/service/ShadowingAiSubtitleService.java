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
import java.nio.file.Files;
import java.nio.file.Path;
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
    private static final int TRANSLATION_BATCH_SIZE = 40;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final AudioExtractorService audioExtractorService;

    @Value("${ai.groq.api-key:}")
    private String groqApiKey;

    @Value("${shadowing.ai.subtitle.enabled:true}")
    private boolean enabled;

    @Value("${shadowing.ai.subtitle.transcription-model:whisper-large-v3-turbo}")
    private String transcriptionModel;

    @Value("${shadowing.ai.subtitle.translation-model:openai/gpt-oss-20b}")
    private String translationModel;

    public ShadowingAiSubtitleService(ObjectMapper objectMapper, AudioExtractorService audioExtractorService) {
        this.objectMapper = objectMapper;
        this.audioExtractorService = audioExtractorService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public List<GeneratedSubtitle> generateSubtitles(ShadowingLessonEntity lesson) {
        assertAvailable(lesson);
        Path audioFile = audioExtractorService.downloadAndExtractAudio(lesson.getVideoUrl());
        try {
            List<TranscriptSegment> segments = transcribe(audioFile);
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
        } finally {
            try { Files.deleteIfExists(audioFile); } catch (IOException ignored) {}
        }
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

    private List<TranscriptSegment> transcribe(Path audioFile) {
        String boundary = "----vocabverse-" + UUID.randomUUID();

        HttpRequest request = HttpRequest.newBuilder(GROQ_TRANSCRIPTIONS_URI)
                .timeout(Duration.ofSeconds(120))
                .header("Authorization", "Bearer " + groqApiKey.trim())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(buildMultipartBodyPublisher(boundary, audioFile))
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
        Map<Integer, String> translations = new HashMap<>();
        for (int start = 0; start < segments.size(); start += TRANSLATION_BATCH_SIZE) {
            int end = Math.min(start + TRANSLATION_BATCH_SIZE, segments.size());
            translations.putAll(translateBatch(segments.subList(start, end), start));
        }
        return translations;
    }

    private Map<Integer, String> translateBatch(List<TranscriptSegment> segments, int offset) {
        try {
            List<Map<String, Object>> items = new ArrayList<>();
            for (int index = 0; index < segments.size(); index++) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("index", offset + index);
                item.put("englishText", segments.get(index).englishText());
                items.add(item);
            }

            String prompt = """
                    Translate the following English shadowing subtitle segments to Vietnamese.
                    Return a JSON object only with exactly this shape:
                    {"items":[{"index":0,"vietnameseText":"..."}]}
                    Keep the same index values. Do not add markdown.
                    Segments:
                    """ + objectMapper.writeValueAsString(items);

            String requestBody = objectMapper.writeValueAsString(Map.of(
                    "model", translationModel,
                    "temperature", 0.1,
                    "response_format", Map.of("type", "json_object"),
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

            return sendTranslationRequestWithRetry(request);
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

    private Map<Integer, String> sendTranslationRequestWithRetry(HttpRequest request) throws IOException, InterruptedException {
        try {
            return sendTranslationRequest(request);
        } catch (BusinessException exception) {
            if (exception.getErrorCode() != ErrorCode.AI_RESPONSE_INVALID) {
                throw exception;
            }
            return sendTranslationRequest(request);
        }
    }

    private Map<Integer, String> sendTranslationRequest(HttpRequest request) throws IOException, InterruptedException {
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
    }

    private BusinessException mapAiError(int statusCode, String message) {
        if (statusCode == 429) {
            return new BusinessException(ErrorCode.AI_RATE_LIMITED);
        }
        return new BusinessException(ErrorCode.AI_PROVIDER_NOT_AVAILABLE, message);
    }

    private HttpRequest.BodyPublisher buildMultipartBodyPublisher(String boundary, Path audioFile) {
        try {
            byte[] audioData = Files.readAllBytes(audioFile);
            byte[] fieldPart = ("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"audio.m4a\"\r\n"
                    + "Content-Type: audio/mp4\r\n\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] modelPart = ("\r\n--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"model\"\r\n\r\n"
                    + transcriptionModel + "\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] langPart = ("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"language\"\r\n\r\n"
                    + "en\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] formatPart = ("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"response_format\"\r\n\r\n"
                    + "verbose_json\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] tempPart = ("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"temperature\"\r\n\r\n"
                    + "0\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] granPart = ("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"timestamp_granularities[]\"\r\n\r\n"
                    + "segment\r\n").getBytes(StandardCharsets.UTF_8);
            byte[] closing = ("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

            int total = fieldPart.length + audioData.length + modelPart.length + langPart.length
                    + formatPart.length + tempPart.length + granPart.length + closing.length;
            byte[] body = new byte[total];
            int offset = 0;
            System.arraycopy(fieldPart, 0, body, offset, fieldPart.length);
            offset += fieldPart.length;
            System.arraycopy(audioData, 0, body, offset, audioData.length);
            offset += audioData.length;
            System.arraycopy(modelPart, 0, body, offset, modelPart.length);
            offset += modelPart.length;
            System.arraycopy(langPart, 0, body, offset, langPart.length);
            offset += langPart.length;
            System.arraycopy(formatPart, 0, body, offset, formatPart.length);
            offset += formatPart.length;
            System.arraycopy(tempPart, 0, body, offset, tempPart.length);
            offset += tempPart.length;
            System.arraycopy(granPart, 0, body, offset, granPart.length);
            offset += granPart.length;
            System.arraycopy(closing, 0, body, offset, closing.length);
            return HttpRequest.BodyPublishers.ofByteArrays(List.of(body));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.VIDEO_PROCESSING_FAILED,
                    "Failed to read audio file for transcription: " + exception.getMessage(), exception);
        }
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
