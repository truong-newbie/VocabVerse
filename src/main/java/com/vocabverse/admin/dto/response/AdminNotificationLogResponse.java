package com.vocabverse.admin.dto.response;

import com.vocabverse.notification.entity.NotificationStatus;
import com.vocabverse.notification.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminNotificationLogResponse(
        UUID id,
        String recipient,
        NotificationType type,
        NotificationStatus status,
        String message,
        LocalDateTime createdAt
) {
}
