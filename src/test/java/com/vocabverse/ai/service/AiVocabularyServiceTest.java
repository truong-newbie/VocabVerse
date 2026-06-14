package com.vocabverse.ai.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.vocabverse.ai.client.AiClient;
import com.vocabverse.ai.dto.request.NormalizeVocabularyRequest;
import com.vocabverse.ai.dto.response.NormalizeVocabularyResponse;
import com.vocabverse.ai.prompt.VocabularyNormalizePromptBuilder;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import java.util.List;
import org.junit.jupiter.api.Test;

class AiVocabularyServiceTest {

    private final VocabularyNormalizePromptBuilder promptBuilder = new VocabularyNormalizePromptBuilder();

    @Test
    void normalizeReturnsProviderResult() {
        NormalizeVocabularyResponse providerResponse = new NormalizeVocabularyResponse(
                "abandon",
                "to leave permanently",
                "tu bo",
                "/uh-BAN-duhn/",
                "verb",
                "He abandoned the project.",
                List.of("leave"),
                List.of("continue"),
                "INTERMEDIATE",
                "Normalized by provider"
        );
        AiClient aiClient = (rawText, prompt) -> providerResponse;
        AiVocabularyService service = new AiVocabularyService(aiClient, promptBuilder);

        NormalizeVocabularyResponse response = service.normalize(new NormalizeVocabularyRequest("abandon"));

        assertThat(response).isEqualTo(providerResponse);
    }

    @Test
    void normalizeRejectsBlankRawText() {
        AiClient aiClient = (rawText, prompt) -> {
            throw new AssertionError("AI client should not be called");
        };
        AiVocabularyService service = new AiVocabularyService(aiClient, promptBuilder);

        assertThatThrownBy(() -> service.normalize(new NormalizeVocabularyRequest("   ")))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    void normalizeFallsBackWhenProviderFails() {
        AiClient aiClient = (rawText, prompt) -> {
            throw new IllegalStateException("provider failed");
        };
        AiVocabularyService service = new AiVocabularyService(aiClient, promptBuilder);

        NormalizeVocabularyResponse response = service.normalize(new NormalizeVocabularyRequest("abandon"));

        assertThat(response.term()).isEqualTo("abandon");
        assertThat(response.difficulty()).isEqualTo("INTERMEDIATE");
        assertThat(response.aiExplanation()).contains("Fallback normalization");
    }
}
