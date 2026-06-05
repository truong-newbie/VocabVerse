package com.vocabverse.ai.dto.response;

public record NormalizeBulkVocabularyItemResponse(
        String term,
        String meaning,
        String vietnameseMeaning,
        String pronunciation,
        String partOfSpeech,
        String exampleSentence,
        String note
) {
}
