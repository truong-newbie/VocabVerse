package com.vocabverse.notification.dto.response;

import com.vocabverse.notification.entity.NotificationStatus;
import com.vocabverse.notification.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        NotificationType type,
        NotificationStatus status,
        LocalDateTime sentAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
