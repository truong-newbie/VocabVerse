package com.vocabverse.collection.dto.request;

import java.time.LocalTime;
import java.util.List;

public record UpdateCollectionReviewSettingRequest(
        Boolean enabled,
        Boolean emailEnabled,
        List<Integer> intervals,
        LocalTime reminderTime,
        String timezone
) {
}
