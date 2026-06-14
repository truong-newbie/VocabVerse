package com.vocabverse.vocabulary.dto.request;

import java.util.List;

public record BulkCreateVocabularyRequest(
        List<BulkCreateVocabularyItemRequest> items
) {
}
