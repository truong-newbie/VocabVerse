package com.vocabverse.notification.scheduler;

import com.vocabverse.async.event.ReviewDueEvent;
import com.vocabverse.async.producer.NotificationProducer;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.notification.service.NotificationService;
import com.vocabverse.user.entity.UserEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReviewDueScheduler {

    private final LearningProgressRepository learningProgressRepository;
    private final NotificationService notificationService;
    private final NotificationProducer notificationProducer;

    @Scheduled(cron = "${notification.review-due.cron:0 0 8 * * *}")
    public void publishReviewDueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate reviewDate = now.toLocalDate();
        List<UserEntity> users = learningProgressRepository.findDistinctUsersWithDueReviews(now);

        for (UserEntity user : users) {
            long totalDueVocabularies = learningProgressRepository.countDueReviewsByUserId(user.getId(), now);
            notificationService.createPendingReviewDueNotification(user, totalDueVocabularies, reviewDate)
                    .ifPresent(this::publish);
        }
    }

    private void publish(ReviewDueEvent event) {
        notificationProducer.publishReviewDueEvent(event);
    }
}
