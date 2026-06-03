package com.vocabverse.notification.dto.response;

import java.util.List;

public record NotificationPageResponse(
        List<NotificationResponse> items,
        int page,
        int size,
        long totalItems,
        int totalPages
) {
}
