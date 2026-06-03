package com.vocabverse.vocabulary.dto.response;

import java.util.List;

public record VocabularyPageResponse(
        List<VocabularyResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
