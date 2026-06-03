package com.vocabverse.vocabulary.dto.request;

import com.vocabverse.vocabulary.entity.VocabularyExample;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record CreateVocabularyRequest(
        @NotBlank
        @Size(max = 150)
        String word,

        @Size(max = 100)
        String phonetic,

        String audioUrl,

        @Size(max = 50)
        String partOfSpeech,

        @NotBlank
        @Size(max = 2000)
        String meaningVi,

        @Size(max = 2000)
        String meaningEn,

        List<String> synonyms,

        List<String> antonyms,

        List<VocabularyExample> examples,

        Set<UUID> collectionIds
) {
}
