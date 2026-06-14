package com.vocabverse.admin.dto.response;

import java.util.List;

public record AdminNotificationPageResponse(
        long emailsSent,
        long emailsFailed,
        List<AdminNotificationLogResponse> logs,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
}
