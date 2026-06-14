package com.vocabverse.ai.dto.response;

import java.util.List;

public record NormalizeBulkVocabularyResponse(
        List<NormalizeBulkVocabularyItemResponse> items
) {
}
