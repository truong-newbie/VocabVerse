package com.vocabverse.ai.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.ai.dto.response.NormalizeBulkVocabularyItemResponse;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BulkVocabularyJsonParser {

    private final ObjectMapper objectMapper;

    public BulkVocabularyJsonParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<NormalizeBulkVocabularyItemResponse> parse(String content) {
        String json = extractJson(content);
        try {
            JsonNode node = objectMapper.readTree(json);
            JsonNode itemsNode = node.isArray() ? node : node.path("items");
            if (!itemsNode.isArray()) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
            }
            List<NormalizeBulkVocabularyItemResponse> items = objectMapper.convertValue(
                    itemsNode,
                    new TypeReference<>() {
                    }
            );
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

        if (candidate.startsWith("[") || candidate.startsWith("{")) {
            return candidate;
        }

        int arrayStart = candidate.indexOf('[');
        int arrayEnd = candidate.lastIndexOf(']');
        if (arrayStart >= 0 && arrayEnd > arrayStart) {
            return candidate.substring(arrayStart, arrayEnd + 1);
        }

        int objectStart = candidate.indexOf('{');
        int objectEnd = candidate.lastIndexOf('}');
        if (objectStart >= 0 && objectEnd > objectStart) {
            return candidate.substring(objectStart, objectEnd + 1);
        }

        throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
    }
}
