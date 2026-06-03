package com.vocabverse.publiccollection.dto.response;

import java.util.List;

public record PublicVocabularyPageResponse(
        List<PublicVocabularyResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
