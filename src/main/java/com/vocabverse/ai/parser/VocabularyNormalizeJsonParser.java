package com.vocabverse.ai.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.ai.dto.response.NormalizeBulkVocabularyItemResponse;
import com.vocabverse.ai.dto.response.NormalizeVocabularyResponse;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Component;

@Component
public class VocabularyNormalizeJsonParser {

    private final ObjectMapper objectMapper;

    public VocabularyNormalizeJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public NormalizeVocabularyResponse parseSingle(String content) {
        try {
            JsonNode node = objectMapper.readTree(extractJson(content));
            if (!node.isObject()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
            }
            return toSingleResponse(node);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, ErrorCode.AI_RESPONSE_INVALID.getMessage(), exception);
        }
    }

    public List<NormalizeBulkVocabularyItemResponse> parseBulk(String content) {
        try {
            JsonNode node = objectMapper.readTree(extractJson(content));
            JsonNode itemsNode = node.isArray() ? node : node.path("items");
            if (!itemsNode.isArray()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
            }

            List<NormalizeBulkVocabularyItemResponse> items = new ArrayList<>();
            for (JsonNode itemNode : itemsNode) {
                NormalizeVocabularyResponse single = toSingleResponse(itemNode);
                items.add(new NormalizeBulkVocabularyItemResponse(
                        single.term(),
                        single.meaning(),
                        single.vietnameseMeaning(),
                        single.pronunciation(),
                        single.partOfSpeech(),
                        single.exampleSentence(),
                        single.synonyms(),
                        single.antonyms(),
                        single.difficulty(),
                        single.aiExplanation()
                ));
            }
            if (items.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
            }
            return items;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID, ErrorCode.AI_RESPONSE_INVALID.getMessage(), exception);
        }
    }

    private NormalizeVocabularyResponse toSingleResponse(JsonNode node) {
        if (!node.isObject()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }

        String term = requiredText(node, "term");
        String meaning = requiredText(node, "meaning");
        String vietnameseMeaning = requiredText(node, "vietnameseMeaning");
        String pronunciation = requiredText(node, "pronunciation");
        String partOfSpeech = requiredText(node, "partOfSpeech");
        String exampleSentence = requiredText(node, "exampleSentence");
        String difficulty = normalizeDifficulty(requiredText(node, "difficulty"));
        String aiExplanation = requiredText(node, "aiExplanation");

        return new NormalizeVocabularyResponse(
                term,
                meaning,
                vietnameseMeaning,
                pronunciation,
                partOfSpeech,
                exampleSentence,
                textArray(node.path("synonyms")),
                textArray(node.path("antonyms")),
                difficulty,
                aiExplanation
        );
    }

    private String extractJson(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }

        String candidate = content.trim();
        if (candidate.startsWith("```")) {
            int firstNewline = candidate.indexOf('\n');
            int closingFence = candidate.lastIndexOf("```");
            if (firstNewline >= 0 && closingFence > firstNewline) {
                candidate = candidate.substring(firstNewline + 1, closingFence).trim();
            }
        }

        if (candidate.startsWith("{") || candidate.startsWith("[")) {
            return trimToBalancedJson(candidate);
        }

        int objectStart = candidate.indexOf('{');
        int arrayStart = candidate.indexOf('[');
        if (objectStart < 0 && arrayStart < 0) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }

        int start = objectStart >= 0 && (arrayStart < 0 || objectStart < arrayStart) ? objectStart : arrayStart;
        return trimToBalancedJson(candidate.substring(start));
    }

    private String trimToBalancedJson(String candidate) {
        char open = candidate.charAt(0);
        char close = open == '{' ? '}' : ']';
        int depth = 0;
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < candidate.length(); i++) {
            char current = candidate.charAt(i);
            if (escaped) {
                escaped = false;
                continue;
            }
            if (current == '\\' && inString) {
                escaped = true;
                continue;
            }
            if (current == '"') {
                inString = !inString;
                continue;
            }
            if (inString) {
                continue;
            }
            if (current == open) {
                depth++;
            } else if (current == close) {
                depth--;
                if (depth == 0) {
                    return candidate.substring(0, i + 1);
                }
            }
        }
        throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
    }

    private String requiredText(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (!value.isTextual() || value.asText().trim().isBlank()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
        return value.asText().trim();
    }

    private List<String> textArray(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return List.of();
        }
        if (!node.isArray()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
        List<String> values = new ArrayList<>();
        for (JsonNode item : node) {
            if (!item.isTextual()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
            }
            String text = item.asText().trim();
            if (!text.isBlank()) {
                values.add(text);
            }
        }
        return List.copyOf(values);
    }

    private String normalizeDifficulty(String difficulty) {
        return switch (difficulty.trim().toUpperCase(Locale.ROOT)) {
            case "BEGINNER", "EASY" -> "BEGINNER";
            case "INTERMEDIATE", "MEDIUM" -> "INTERMEDIATE";
            case "ADVANCED", "HARD" -> "ADVANCED";
            default -> throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        };
    }
}
