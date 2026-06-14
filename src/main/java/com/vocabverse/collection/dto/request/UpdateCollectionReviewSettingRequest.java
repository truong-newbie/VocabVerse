package com.vocabverse.collection.dto.request;

import com.vocabverse.review.strategy.ReviewSchedulerType;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record UpdateCollectionReviewSettingRequest(
        Boolean enabled,
        Boolean emailEnabled,
        ReviewSchedulerType schedulerType,
        List<Integer> intervals,
        LocalTime reminderTime,
        String timezone,
        BigDecimal fsrsDesiredRetention,
        Integer fsrsMaxIntervalDays
) {
}
