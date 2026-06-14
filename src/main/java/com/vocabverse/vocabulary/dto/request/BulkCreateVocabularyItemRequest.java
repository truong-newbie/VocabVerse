package com.vocabverse.vocabulary.dto.request;

public record BulkCreateVocabularyItemRequest(
        String term,
        String meaning,
        String vietnameseMeaning,
        String pronunciation,
        String partOfSpeech,
        String exampleSentence,
        String note
) {
}
