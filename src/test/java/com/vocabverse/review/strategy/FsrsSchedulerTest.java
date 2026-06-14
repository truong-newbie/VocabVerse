package com.vocabverse.review.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.review.entity.ReviewResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class FsrsSchedulerTest {

    private final FsrsScheduler scheduler = new FsrsScheduler();

    @Test
    void newCardGoodInitializesMemoryStateAndSchedulesReview() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress(null, null, 0, 0, null);

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.GOOD, reviewedAt, setting("0.900", 3650));

        assertThat(result.status()).isEqualTo(LearningStatus.LEARNING);
        assertThat(result.repetitionCount()).isEqualTo(1);
        assertThat(result.fsrsDifficulty()).isEqualByComparingTo("4.800");
        assertThat(result.fsrsStability()).isEqualByComparingTo("3.150");
        assertThat(result.fsrsRetrievability()).isEqualByComparingTo("1.0000");
        assertThat(result.lastIntervalDays()).isEqualTo(3);
        assertThat(result.nextReviewAt()).isEqualTo(reviewedAt.plusDays(3));
    }

    @Test
    void existingEasyReviewIncreasesStabilityAndCanMasterVocabulary() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress("5.000", "10.000", 4, 4, reviewedAt.minusDays(10));

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.EASY, reviewedAt, setting("0.900", 3650));

        assertThat(result.fsrsDifficulty()).isLessThan(progress.getFsrsDifficulty());
        assertThat(result.fsrsStability()).isGreaterThan(progress.getFsrsStability());
        assertThat(result.fsrsRetrievability()).isEqualByComparingTo("0.9000");
        assertThat(result.status()).isEqualTo(LearningStatus.MASTERED);
    }

    @Test
    void againIncrementsLapseAndResetsRepetition() {
        LearningProgressEntity progress = progress("5.000", "10.000", 3, 2, LocalDateTime.of(2026, 6, 1, 9, 0));

        ReviewScheduleResult result = scheduler.schedule(
                progress,
                ReviewResult.AGAIN,
                LocalDateTime.of(2026, 6, 14, 9, 0),
                setting("0.900", 3650)
        );

        assertThat(result.repetitionCount()).isZero();
        assertThat(result.lapseCount()).isEqualTo(3);
        assertThat(result.fsrsDifficulty()).isGreaterThan(progress.getFsrsDifficulty());
        assertThat(result.fsrsStability()).isLessThan(progress.getFsrsStability());
    }

    @Test
    void desiredRetentionAndMaxIntervalAffectInterval() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress("5.000", "10.000", 2, 0, null);

        ReviewScheduleResult lowRetention = scheduler.schedule(
                progress,
                ReviewResult.GOOD,
                reviewedAt,
                setting("0.800", 3650)
        );
        ReviewScheduleResult highRetention = scheduler.schedule(
                progress,
                ReviewResult.GOOD,
                reviewedAt,
                setting("0.950", 3650)
        );
        ReviewScheduleResult clamped = scheduler.schedule(
                progress,
                ReviewResult.GOOD,
                reviewedAt,
                setting("0.800", 3)
        );

        assertThat(lowRetention.lastIntervalDays()).isGreaterThan(highRetention.lastIntervalDays());
        assertThat(clamped.lastIntervalDays()).isEqualTo(3);
    }

    private CollectionReviewSettingEntity setting(String desiredRetention, int maxIntervalDays) {
        return CollectionReviewSettingEntity.builder()
                .schedulerType(ReviewSchedulerType.FSRS)
                .fsrsDesiredRetention(new BigDecimal(desiredRetention))
                .fsrsMaxIntervalDays(maxIntervalDays)
                .build();
    }

    private LearningProgressEntity progress(
            String difficulty,
            String stability,
            int repetitionCount,
            int lapseCount,
            LocalDateTime lastReviewedAt
    ) {
        return LearningProgressEntity.builder()
                .repetitionCount(repetitionCount)
                .easeFactor(BigDecimal.valueOf(2.50))
                .lastIntervalDays(0)
                .lapseCount(lapseCount)
                .reviewCount(repetitionCount)
                .fsrsDifficulty(difficulty == null ? null : new BigDecimal(difficulty))
                .fsrsStability(stability == null ? null : new BigDecimal(stability))
                .lastReviewedAt(lastReviewedAt)
                .build();
    }
}
