package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.response.AdminNotificationStatsResponse;
import com.vocabverse.notification.entity.NotificationStatus;
import com.vocabverse.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public AdminNotificationStatsResponse getStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return new AdminNotificationStatsResponse(
                notificationRepository.countByStatusAndCreatedAtBetween(
                        NotificationStatus.SENT,
                        startOfDay,
                        endOfDay
                ),
                notificationRepository.countByStatusAndCreatedAtBetween(
                        NotificationStatus.FAILED,
                        startOfDay,
                        endOfDay
                )
        );
    }
}
