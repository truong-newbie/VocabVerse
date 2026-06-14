package com.vocabverse.collection.dto.response;

import com.vocabverse.review.strategy.ReviewSchedulerType;
import java.math.BigDecimal;
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
        BigDecimal fsrsDesiredRetention,
        int fsrsMaxIntervalDays,
        LocalDateTime lastResetAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
