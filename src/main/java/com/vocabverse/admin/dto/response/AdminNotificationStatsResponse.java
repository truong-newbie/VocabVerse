package com.vocabverse.admin.dto.response;

public record AdminNotificationStatsResponse(
        long emailsSentToday,
        long emailsFailedToday
) {
}
