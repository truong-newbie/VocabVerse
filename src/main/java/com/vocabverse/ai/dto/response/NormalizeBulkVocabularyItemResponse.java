package com.vocabverse.ai.dto.response;

public record NormalizeBulkVocabularyItemResponse(
        String term,
        String meaning,
        String vietnameseMeaning,
        String pronunciation,
        String partOfSpeech,
        String exampleSentence,
        java.util.List<String> synonyms,
        java.util.List<String> antonyms,
        String difficulty,
        String aiExplanation
) {
}
