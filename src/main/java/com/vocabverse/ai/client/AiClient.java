package com.vocabverse.ai.client;

import com.vocabverse.ai.dto.response.NormalizeVocabularyResponse;

public interface AiClient {

    NormalizeVocabularyResponse normalizeVocabulary(String rawText, String prompt);
}
