package com.vocabverse.async.event;

import java.time.LocalDate;
import java.util.UUID;

public record ReviewDueEvent(
        UUID notificationId,
        UUID userId,
        String email,
        String fullName,
        long totalDueVocabularies,
        LocalDate reviewDate
) {
}
