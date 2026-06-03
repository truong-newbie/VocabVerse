package com.vocabverse.publiccollection.dto.response;

import com.vocabverse.collection.enums.CollectionVisibility;
import java.util.UUID;

public record CloneCollectionResponse(
        UUID sourceCollectionId,
        UUID clonedCollectionId,
        String title,
        CollectionVisibility visibility,
        int totalWords
) {
}
