package com.vocabverse.review.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.review.entity.ReviewResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class FixedIntervalSchedulerTest {

    private final FixedIntervalScheduler scheduler = new FixedIntervalScheduler();

    @Test
    void againUsesFirstCollectionInterval() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress(3);
        CollectionReviewSettingEntity setting = setting(List.of(2, 4, 8));

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.AGAIN, reviewedAt, setting);

        assertThat(result.repetitionCount()).isZero();
        assertThat(result.status()).isEqualTo(LearningStatus.LEARNING);
        assertThat(result.lastIntervalDays()).isEqualTo(2);
        assertThat(result.nextReviewAt()).isEqualTo(reviewedAt.plusDays(2));
    }

    @Test
    void goodUsesCollectionIntervalByNewRepetitionCount() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress(1);
        CollectionReviewSettingEntity setting = setting(List.of(1, 3, 7));

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.GOOD, reviewedAt, setting);

        assertThat(result.repetitionCount()).isEqualTo(2);
        assertThat(result.status()).isEqualTo(LearningStatus.REVIEWING);
        assertThat(result.lastIntervalDays()).isEqualTo(3);
        assertThat(result.nextReviewAt()).isEqualTo(reviewedAt.plusDays(3));
    }

    @Test
    void defaultRuleIsUsedWhenSettingIsMissing() {
        LocalDateTime reviewedAt = LocalDateTime.of(2026, 6, 14, 9, 0);
        LearningProgressEntity progress = progress(2);

        ReviewScheduleResult result = scheduler.schedule(progress, ReviewResult.EASY, reviewedAt, null);

        assertThat(result.repetitionCount()).isEqualTo(3);
        assertThat(result.lastIntervalDays()).isEqualTo(14);
        assertThat(result.nextReviewAt()).isEqualTo(reviewedAt.plusDays(14));
    }

    @Test
    void easyAtFifthRepetitionMarksVocabularyMastered() {
        LearningProgressEntity progress = progress(4);

        ReviewScheduleResult result = scheduler.schedule(
                progress,
                ReviewResult.EASY,
                LocalDateTime.of(2026, 6, 14, 9, 0),
                null
        );

        assertThat(result.repetitionCount()).isEqualTo(5);
        assertThat(result.status()).isEqualTo(LearningStatus.MASTERED);
    }

    private LearningProgressEntity progress(int repetitionCount) {
        return LearningProgressEntity.builder()
                .repetitionCount(repetitionCount)
                .easeFactor(BigDecimal.valueOf(2.50))
                .lastIntervalDays(0)
                .lapseCount(0)
                .reviewCount(0)
                .build();
    }

    private CollectionReviewSettingEntity setting(List<Integer> intervals) {
        return CollectionReviewSettingEntity.builder()
                .intervalsJson(intervals)
                .schedulerType(ReviewSchedulerType.FIXED_INTERVAL)
                .build();
    }
}
