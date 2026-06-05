package com.vocabverse.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vocabverse.ai.client.AiClient;
import com.vocabverse.ai.client.GroqVocabularyClient;
import com.vocabverse.ai.dto.request.NormalizeBulkVocabularyRequest;
import com.vocabverse.ai.dto.request.NormalizeVocabularyRequest;
import com.vocabverse.ai.dto.response.NormalizeBulkVocabularyResponse;
import com.vocabverse.ai.dto.response.NormalizeVocabularyResponse;
import com.vocabverse.ai.parser.BulkVocabularyJsonParser;
import com.vocabverse.ai.prompt.BulkVocabularyNormalizePromptBuilder;
import com.vocabverse.ai.prompt.VocabularyNormalizePromptBuilder;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AiVocabularyService {

    private final AiClient aiClient;
    private final GroqVocabularyClient groqVocabularyClient;
    private final VocabularyNormalizePromptBuilder promptBuilder;
    private final BulkVocabularyNormalizePromptBuilder bulkPromptBuilder;
    private final BulkVocabularyJsonParser bulkVocabularyJsonParser;

    @Value("${ai.groq.api-key:}")
    private String systemGroqApiKey;

    @Autowired
    public AiVocabularyService(
            AiClient aiClient,
            GroqVocabularyClient groqVocabularyClient,
            VocabularyNormalizePromptBuilder promptBuilder,
            BulkVocabularyNormalizePromptBuilder bulkPromptBuilder,
            BulkVocabularyJsonParser bulkVocabularyJsonParser
    ) {
        this.aiClient = aiClient;
        this.groqVocabularyClient = groqVocabularyClient;
        this.promptBuilder = promptBuilder;
        this.bulkPromptBuilder = bulkPromptBuilder;
        this.bulkVocabularyJsonParser = bulkVocabularyJsonParser;
    }

    public AiVocabularyService(AiClient aiClient, VocabularyNormalizePromptBuilder promptBuilder) {
        ObjectMapper objectMapper = new ObjectMapper();
        this.aiClient = aiClient;
        this.groqVocabularyClient = new GroqVocabularyClient(objectMapper);
        this.promptBuilder = promptBuilder;
        this.bulkPromptBuilder = new BulkVocabularyNormalizePromptBuilder();
        this.bulkVocabularyJsonParser = new BulkVocabularyJsonParser(objectMapper);
    }

    public NormalizeVocabularyResponse normalize(NormalizeVocabularyRequest request) {
        String rawText = request.rawText().trim();
        if (rawText.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        try {
            String prompt = promptBuilder.build(rawText);
            return aiClient.normalizeVocabulary(rawText, prompt);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            return fallbackNormalize(rawText);
        }
    }

    public NormalizeBulkVocabularyResponse normalizeBulk(NormalizeBulkVocabularyRequest request) {
        List<String> terms = normalizeRawTerms(request.rawText());
        if (terms.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }

        String provider = request.provider() == null || request.provider().isBlank()
                ? "GROQ"
                : request.provider().trim().toUpperCase(Locale.ROOT);
        if (!"GROQ".equals(provider)) {
            throw new BusinessException(ErrorCode.AI_PROVIDER_NOT_AVAILABLE);
        }

        String apiKey = resolveApiKey(request.userApiKey());
        if (apiKey == null) {
            throw new BusinessException(ErrorCode.AI_PROVIDER_NOT_AVAILABLE);
        }

        String prompt = bulkPromptBuilder.build(terms);
        String aiContent = groqVocabularyClient.completeJson(prompt, apiKey);
        return new NormalizeBulkVocabularyResponse(bulkVocabularyJsonParser.parse(aiContent));
    }

    private List<String> normalizeRawTerms(String rawText) {
        if (rawText == null || rawText.trim().isBlank()) {
            return List.of();
        }
        Set<String> uniqueTerms = new LinkedHashSet<>();
        Arrays.stream(rawText.split("[,\\r\\n]+"))
                .map(String::trim)
                .filter(term -> !term.isBlank())
                .forEach(uniqueTerms::add);
        return List.copyOf(uniqueTerms);
    }

    private String resolveApiKey(String userApiKey) {
        if (userApiKey != null && !userApiKey.trim().isBlank()) {
            return userApiKey.trim();
        }
        if (systemGroqApiKey != null && !systemGroqApiKey.trim().isBlank()) {
            return systemGroqApiKey.trim();
        }
        return null;
    }

    private NormalizeVocabularyResponse fallbackNormalize(String rawText) {
        return new NormalizeVocabularyResponse(
                rawText,
                "Suggested meaning for \"" + rawText + "\". AI enrichment is temporarily unavailable.",
                "",
                "",
                "Example usage for \"" + rawText + "\" should be reviewed before saving.",
                "",
                List.of(),
                List.of(),
                "MEDIUM",
                "Fallback normalization was used because the AI provider failed."
        );
    }
}
