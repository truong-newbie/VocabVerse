package com.vocabverse.review.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.review.entity.ReviewResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class Sm2SchedulerTest {

    private final Sm2Scheduler scheduler = new Sm2Scheduler();

    @Test
    void newCardAgainResetsRepetitionAndSchedulesTomorrow() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress(0, 0, "2.50", 0, 0);

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.AGAIN, reviewedAt, null);

        assertThat(result.repetitionCount()).isZero();
        assertThat(result.lastIntervalDays()).isEqualTo(1);
        assertThat(result.status()).isEqualTo(LearningStatus.LEARNING);
        assertThat(result.easeFactor()).isEqualByComparingTo("2.18");
        assertThat(result.lapseCount()).isEqualTo(1);
        assertThat(result.reviewCount()).isEqualTo(1);
        assertThat(result.nextReviewAt()).isEqualTo(reviewedAt.plusDays(1));
    }

    @Test
    void newCardGoodSchedulesOneDayWithDefaultEaseFactor() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress(0, 0, "2.50", 0, 0);

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.GOOD, reviewedAt, null);

        assertThat(result.repetitionCount()).isEqualTo(1);
        assertThat(result.lastIntervalDays()).isEqualTo(1);
        assertThat(result.status()).isEqualTo(LearningStatus.LEARNING);
        assertThat(result.easeFactor()).isEqualByComparingTo("2.50");
        assertThat(result.nextReviewAt()).isEqualTo(reviewedAt.plusDays(1));
    }

    @Test
    void secondGoodReviewSchedulesSixDays() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress(1, 1, "2.50", 0, 1);

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.GOOD, reviewedAt, null);

        assertThat(result.repetitionCount()).isEqualTo(2);
        assertThat(result.lastIntervalDays()).isEqualTo(6);
        assertThat(result.status()).isEqualTo(LearningStatus.REVIEWING);
        assertThat(result.nextReviewAt()).isEqualTo(reviewedAt.plusDays(6));
    }

    @Test
    void matureEasyReviewIncreasesEaseFactorAndCanMasterVocabulary() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress(4, 14, "2.50", 0, 4);

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.EASY, reviewedAt, null);

        assertThat(result.repetitionCount()).isEqualTo(5);
        assertThat(result.easeFactor()).isEqualByComparingTo("2.60");
        assertThat(result.lastIntervalDays()).isEqualTo(36);
        assertThat(result.status()).isEqualTo(LearningStatus.MASTERED);
        assertThat(result.nextReviewAt()).isEqualTo(reviewedAt.plusDays(36));
    }

    @Test
    void hardReviewReducesEaseFactor() {
        LearningProgressEntity progress = progress(0, 0, "2.50", 0, 0);

        ReviewScheduleResult result = scheduler.schedule(
                progress,
                ReviewResult.HARD,
                LocalDateTime.of(2026, 6, 14, 9, 0),
                null
        );

        assertThat(result.easeFactor()).isEqualByComparingTo("2.36");
    }

    @Test
    void easeFactorNeverDropsBelowMinimum() {
        LearningProgressEntity progress = progress(0, 0, "1.31", 0, 0);

        ReviewScheduleResult result = scheduler.schedule(
                progress,
                ReviewResult.AGAIN,
                LocalDateTime.of(2026, 6, 14, 9, 0),
                null
        );

        assertThat(result.easeFactor()).isEqualByComparingTo("1.30");
    }

    @Test
    void intervalIsClampedToMaxInterval() {
        LearningProgressEntity progress = progress(4, 300, "2.50", 0, 4);

        ReviewScheduleResult result = scheduler.schedule(
                progress,
                ReviewResult.EASY,
                LocalDateTime.of(2026, 6, 14, 9, 0),
                null
        );

        assertThat(result.lastIntervalDays()).isEqualTo(365);
    }

    private LearningProgressEntity progress(
            int repetitionCount,
            int lastIntervalDays,
            String easeFactor,
            int lapseCount,
            int reviewCount
    ) {
        return LearningProgressEntity.builder()
                .repetitionCount(repetitionCount)
                .lastIntervalDays(lastIntervalDays)
                .easeFactor(new BigDecimal(easeFactor))
                .lapseCount(lapseCount)
                .reviewCount(reviewCount)
                .build();
    }
}
