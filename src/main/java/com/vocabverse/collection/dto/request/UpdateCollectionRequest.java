package com.vocabverse.collection.dto.request;

import com.vocabverse.collection.enums.CollectionVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCollectionRequest(
        @NotBlank
        @Size(max = 150)
        String title,

        @Size(max = 2000)
        String description,

        @NotNull
        CollectionVisibility visibility,

        @Size(max = 2000)
        String thumbnailUrl
) {
}
