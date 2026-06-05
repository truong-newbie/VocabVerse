package com.vocabverse.vocabulary.dto.response;

public record BulkCreateVocabularyFailedItemResponse(
        int row,
        String term,
        String reason
) {
}
