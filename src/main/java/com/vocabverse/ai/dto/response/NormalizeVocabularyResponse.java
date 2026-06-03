package com.vocabverse.ai.dto.response;

import java.util.List;

public record NormalizeVocabularyResponse(
        String term,
        String meaning,
        String pronunciation,
        String partOfSpeech,
        String exampleSentence,
        String vietnameseMeaning,
        List<String> synonyms,
        List<String> antonyms,
        String difficulty,
        String aiExplanation
) {
}
