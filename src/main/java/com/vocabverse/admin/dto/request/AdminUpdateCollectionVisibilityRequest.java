package com.vocabverse.admin.dto.request;

import com.vocabverse.collection.enums.CollectionVisibility;
import jakarta.validation.constraints.NotNull;

public record AdminUpdateCollectionVisibilityRequest(
        @NotNull
        CollectionVisibility visibility
) {
}
