package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.response.AdminNotificationLogResponse;
import com.vocabverse.admin.dto.response.AdminNotificationPageResponse;
import com.vocabverse.admin.dto.response.AdminNotificationStatsResponse;
import com.vocabverse.notification.entity.NotificationEntity;
import com.vocabverse.notification.entity.NotificationStatus;
import com.vocabverse.notification.repository.NotificationRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public AdminNotificationPageResponse listNotifications(Pageable pageable) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        Page<AdminNotificationLogResponse> page = notificationRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toLogResponse);

        return new AdminNotificationPageResponse(
                notificationRepository.countByStatusAndCreatedAtBetween(NotificationStatus.SENT, startOfDay, endOfDay),
                notificationRepository.countByStatusAndCreatedAtBetween(NotificationStatus.FAILED, startOfDay, endOfDay),
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize()
        );
    }

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

    private AdminNotificationLogResponse toLogResponse(NotificationEntity notification) {
        return new AdminNotificationLogResponse(
                notification.getId(),
                notification.getUser() == null ? null : notification.getUser().getEmail(),
                notification.getType(),
                notification.getStatus(),
                notification.getStatus().name(),
                notification.getCreatedAt()
        );
    }
}
