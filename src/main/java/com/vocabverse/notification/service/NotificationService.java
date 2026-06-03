package com.vocabverse.notification.service;

import com.vocabverse.async.event.ReviewDueEvent;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.notification.dto.response.NotificationPageResponse;
import com.vocabverse.notification.email.EmailService;
import com.vocabverse.notification.entity.NotificationEntity;
import com.vocabverse.notification.entity.NotificationStatus;
import com.vocabverse.notification.entity.NotificationType;
import com.vocabverse.notification.mapper.NotificationMapper;
import com.vocabverse.notification.repository.NotificationRepository;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final EmailService emailService;

    @Transactional
    public Optional<ReviewDueEvent> createPendingReviewDueNotification(
            UserEntity user,
            long totalDueVocabularies,
            LocalDate reviewDate
    ) {
        if (totalDueVocabularies <= 0) {
            return Optional.empty();
        }
        if (existsReviewDueNotificationForDate(user.getId(), reviewDate)) {
            return Optional.empty();
        }

        NotificationEntity notification = NotificationEntity.builder()
                .user(user)
                .type(NotificationType.REVIEW_DUE)
                .status(NotificationStatus.PENDING)
                .build();

        try {
            notification = notificationRepository.saveAndFlush(notification);
        } catch (DataIntegrityViolationException exception) {
            return Optional.empty();
        }

        return Optional.of(new ReviewDueEvent(
                notification.getId(),
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                totalDueVocabularies,
                reviewDate
        ));
    }

    @Transactional
    public void sendReviewDueEmail(ReviewDueEvent event) {
        NotificationEntity notification = notificationRepository.findById(event.notificationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (notification.getStatus() == NotificationStatus.SENT) {
            return;
        }

        try {
            emailService.sendReviewDueEmail(event);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (RuntimeException exception) {
            notification.setStatus(NotificationStatus.FAILED);
        }
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public NotificationPageResponse getNotifications(Pageable pageable) {
        UUID userId = getCurrentUser().getId();
        Page<NotificationEntity> page = notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);
        return toPageResponse(page);
    }

    @Transactional(readOnly = true)
    public NotificationPageResponse getNotificationHistory(Pageable pageable) {
        UUID userId = getCurrentUser().getId();
        Page<NotificationEntity> page = notificationRepository.findAllByUserIdAndStatusInOrderByCreatedAtDesc(
                userId,
                List.of(NotificationStatus.SENT, NotificationStatus.FAILED),
                pageable
        );
        return toPageResponse(page);
    }

    private boolean existsReviewDueNotificationForDate(UUID userId, LocalDate reviewDate) {
        LocalDateTime start = reviewDate.atStartOfDay();
        LocalDateTime end = reviewDate.plusDays(1).atStartOfDay().minusNanos(1);
        return notificationRepository.existsByUserIdAndTypeAndCreatedAtBetween(
                userId,
                NotificationType.REVIEW_DUE,
                start,
                end
        );
    }

    private NotificationPageResponse toPageResponse(Page<NotificationEntity> page) {
        return new NotificationPageResponse(
                page.map(notificationMapper::toResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private UserEntity getCurrentUser() {
        String email = getAuthenticatedEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return authentication.getName();
    }
}
