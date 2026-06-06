package com.vocabverse.notification.repository;

import com.vocabverse.notification.entity.NotificationEntity;
import com.vocabverse.notification.entity.NotificationStatus;
import com.vocabverse.notification.entity.NotificationType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    boolean existsByUserIdAndTypeAndCreatedAtBetween(
            UUID userId,
            NotificationType type,
            LocalDateTime start,
            LocalDateTime end
    );

    Page<NotificationEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<NotificationEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<NotificationEntity> findAllByUserIdAndStatusInOrderByCreatedAtDesc(
            UUID userId,
            Collection<NotificationStatus> statuses,
            Pageable pageable
    );

    long countByStatusAndCreatedAtBetween(NotificationStatus status, LocalDateTime start, LocalDateTime end);
}
