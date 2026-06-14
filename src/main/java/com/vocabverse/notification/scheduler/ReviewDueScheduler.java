package com.vocabverse.notification.scheduler;

import com.vocabverse.async.event.ReviewDueEvent;
import com.vocabverse.async.producer.NotificationProducer;
import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.collection.repository.CollectionReviewSettingRepository;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.notification.service.NotificationService;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewDueScheduler {

    private final LearningProgressRepository learningProgressRepository;
    private final CollectionReviewSettingRepository collectionReviewSettingRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;

    @Scheduled(cron = "${notification.review-due.cron:0 0 8 * * *}")
    public void publishReviewDueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reviewDate = now.toLocalDate();
        List<UserEntity> users = learningProgressRepository.findDistinctUsersWithDueReviews(now);

        for (UserEntity user : users) {
            long totalDueVocabularies = countDueVocabulariesAllowedForNotification(user.getId(), now);
            notificationService.createPendingReviewDueNotification(user, totalDueVocabularies, reviewDate)
                    .ifPresent(this::publish);
        }
    }

    private long countDueVocabulariesAllowedForNotification(UUID userId, LocalDateTime now) {
        return learningProgressRepository.findAllByUserIdAndNextReviewAtLessThanEqual(userId, now)
                .stream()
                .filter(progress -> isNotificationAllowed(progress, now))
                .count();
    }

    private boolean isNotificationAllowed(LearningProgressEntity progress, LocalDateTime now) {
        List<UUID> collectionIds = collectionVocabularyRepository.findOwnedCollectionIdsByVocabularyId(
                progress.getUser().getId(),
                progress.getVocabulary().getId()
        );
        if (collectionIds.isEmpty()) {
            return true;
        }

        List<CollectionReviewSettingEntity> settings = collectionReviewSettingRepository.findAllByUserIdAndCollectionIdIn(
                progress.getUser().getId(),
                collectionIds
        );
        if (settings.isEmpty()) {
            return true;
        }

        return settings.stream().anyMatch(setting -> setting.isEnabled()
                && setting.isEmailEnabled()
                && isReminderTimeMatched(setting, now));
    }

    private boolean isReminderTimeMatched(CollectionReviewSettingEntity setting, LocalDateTime now) {
        LocalTime reminderTime = setting.getReminderTime();
        if (reminderTime == null) {
            return true;
        }

        ZoneId zoneId = resolveZoneId(setting.getTimezone());
        LocalTime currentTime = now.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(zoneId)
                .toLocalTime()
                .withSecond(0)
                .withNano(0);
        return currentTime.equals(reminderTime.withSecond(0).withNano(0));
    }

    private ZoneId resolveZoneId(String timezone) {
        if (timezone == null || timezone.isBlank()) {
            return ZoneId.systemDefault();
        }
        try {
            return ZoneId.of(timezone);
        } catch (RuntimeException exception) {
            return ZoneId.systemDefault();
        }
    }

    private void publish(ReviewDueEvent event) {
        notificationProducer.publishReviewDueEvent(event);
    }
}
