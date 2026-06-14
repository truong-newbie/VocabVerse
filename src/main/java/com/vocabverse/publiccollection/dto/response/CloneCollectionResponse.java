package com.vocabverse.publiccollection.dto.response;

import com.vocabverse.collection.enums.CollectionVisibility;
import java.util.UUID;

public record CloneCollectionResponse(
        UUID id,
        String title,
        String description,
        CollectionVisibility visibility,
        int vocabularyCount
) {
}
