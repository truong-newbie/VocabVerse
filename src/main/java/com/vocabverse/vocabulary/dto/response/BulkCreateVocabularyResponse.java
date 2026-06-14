package com.vocabverse.vocabulary.dto.response;

import java.util.List;

public record BulkCreateVocabularyResponse(
        int successCount,
        int failedCount,
        List<BulkCreateVocabularyFailedItemResponse> failedItems
) {
}
