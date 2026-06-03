package com.vocabverse.publiccollection.dto.response;

import com.vocabverse.vocabulary.entity.VocabularyExample;
import java.util.List;
import java.util.UUID;

public record PublicVocabularyResponse(
        UUID id,
        String word,
        String normalizedWord,
        String phonetic,
        String audioUrl,
        String partOfSpeech,
        String meaningVi,
        String meaningEn,
        List<String> synonyms,
        List<String> antonyms,
        List<VocabularyExample> examples
) {
}
