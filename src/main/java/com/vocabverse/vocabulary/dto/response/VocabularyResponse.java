package com.vocabverse.vocabulary.dto.response;

import com.vocabverse.vocabulary.entity.VocabularyExample;
import com.vocabverse.vocabulary.entity.VocabularySource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record VocabularyResponse(
        UUID id,
        UUID ownerId,
        String word,
        String normalizedWord,
        String phonetic,
        String audioUrl,
        String partOfSpeech,
        String meaningVi,
        String meaningEn,
        List<String> synonyms,
        List<String> antonyms,
        List<VocabularyExample> examples,
        VocabularySource source,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
