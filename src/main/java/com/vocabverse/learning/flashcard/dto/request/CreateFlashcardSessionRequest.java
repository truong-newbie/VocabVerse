package com.vocabverse.learning.flashcard.dto.request;

import com.vocabverse.learning.flashcard.entity.FlashcardSessionSource;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateFlashcardSessionRequest(
        @NotNull
        FlashcardSessionSource source,

        UUID collectionId
) {
}
