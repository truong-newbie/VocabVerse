package com.vocabverse.admin.dto.response;

import java.util.UUID;

public record AdminPublicVocabularyResponse(
        UUID id,
        String term,
        String meaning
) {
}
