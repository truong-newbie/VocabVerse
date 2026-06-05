package com.vocabverse.publiccollection.dto.response;

import java.util.UUID;

public record PublicVocabularyResponse(
        UUID id,
        String term,
        String meaning,
        String vietnameseMeaning,
        String pronunciation,
        String partOfSpeech,
        String exampleSentence
) {
}
