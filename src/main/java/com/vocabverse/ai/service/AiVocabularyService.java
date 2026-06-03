package com.vocabverse.ai.service;

import com.vocabverse.ai.client.AiClient;
import com.vocabverse.ai.dto.request.NormalizeVocabularyRequest;
import com.vocabverse.ai.dto.response.NormalizeVocabularyResponse;
import com.vocabverse.ai.prompt.VocabularyNormalizePromptBuilder;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiVocabularyService {

    private final AiClient aiClient;
    private final VocabularyNormalizePromptBuilder promptBuilder;

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
