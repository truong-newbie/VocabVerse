package com.vocabverse.collection.dto.response;

import com.vocabverse.review.strategy.ReviewSchedulerType;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record CollectionReviewSettingResponse(
        UUID id,
        UUID collectionId,
        boolean enabled,
        boolean emailEnabled,
        ReviewSchedulerType schedulerType,
        List<Integer> intervals,
        LocalTime reminderTime,
        String timezone,
        LocalDateTime lastResetAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
