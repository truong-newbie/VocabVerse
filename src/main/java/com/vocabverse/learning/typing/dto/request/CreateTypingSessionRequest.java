package com.vocabverse.learning.typing.dto.request;

import com.vocabverse.learning.typing.entity.TypingSessionSource;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateTypingSessionRequest(
        @NotNull
        TypingSessionSource source,

        UUID collectionId
) {
}
