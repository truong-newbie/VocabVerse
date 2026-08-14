package com.vocabverse.shadowing.service;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.shadowing.dto.response.ShadowingSubtitleResponse;
import com.vocabverse.shadowing.entity.ShadowingLessonEntity;
import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import com.vocabverse.shadowing.entity.ShadowingLessonSubtitleEntity;
import com.vocabverse.shadowing.repository.ShadowingLessonRepository;
import com.vocabverse.shadowing.repository.ShadowingLessonSubtitleRepository;
import com.vocabverse.shadowing.service.ShadowingAiSubtitleService.GeneratedSubtitle;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class ShadowingSubtitleGenerationService {

    private final ShadowingLessonRepository shadowingLessonRepository;
    private final ShadowingLessonSubtitleRepository subtitleRepository;
    private final ShadowingAiSubtitleService shadowingAiSubtitleService;
    private final TransactionTemplate transactionTemplate;

    @Async("shadowingTaskExecutor")
    public void generateSubtitlesAsync(UUID lessonId) {
        try {
            ShadowingLessonEntity lesson = loadLesson(lessonId);
            List<GeneratedSubtitle> generatedSubtitles = shadowingAiSubtitleService.generateSubtitles(lesson);
            saveSuccess(lessonId, generatedSubtitles);
        } catch (Exception exception) {
            saveFailure(lessonId, exception);
        }
    }

    private ShadowingLessonEntity loadLesson(UUID lessonId) {
        return shadowingLessonRepository.findByIdAndDeletedAtIsNull(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND));
    }

    private void saveSuccess(UUID lessonId, List<GeneratedSubtitle> generatedSubtitles) {
        transactionTemplate.executeWithoutResult(status -> {
            ShadowingLessonEntity lesson = loadLesson(lessonId);
            subtitleRepository.deleteByLessonId(lessonId);

            List<ShadowingLessonSubtitleEntity> subtitles = generatedSubtitles.stream()
                    .map(item -> ShadowingLessonSubtitleEntity.builder()
                            .lesson(lesson)
                            .startTimeMs(item.startTimeMs())
                            .endTimeMs(item.endTimeMs())
                            .englishText(item.englishText())
                            .vietnameseText(trimToNull(item.vietnameseText()))
                            .orderIndex(item.orderIndex())
                            .build())
                    .toList();
            subtitleRepository.saveAll(subtitles);

            lesson.setStatus(ShadowingLessonStatus.COMPLETED);
            lesson.setProgress(100);
            lesson.setErrorMessage(null);
            shadowingLessonRepository.save(lesson);
        });
    }

    private void saveFailure(UUID lessonId, Exception exception) {
        transactionTemplate.executeWithoutResult(status -> {
            shadowingLessonRepository.findByIdAndDeletedAtIsNull(lessonId).ifPresent(lesson -> {
                lesson.setStatus(ShadowingLessonStatus.FAILED);
                lesson.setProgress(0);
                lesson.setErrorMessage(resolveErrorMessage(exception));
                shadowingLessonRepository.save(lesson);
            });
        });
    }

    private String resolveErrorMessage(Exception exception) {
        if (exception instanceof BusinessException businessException) {
            return businessException.getMessage();
        }
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? ErrorCode.AI_PROCESSING_FAILED.getMessage()
                : message;
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isBlank() ? null : value.trim();
    }

    public ShadowingSubtitleResponse toSubtitleResponse(ShadowingLessonSubtitleEntity subtitle) {
        return new ShadowingSubtitleResponse(
                subtitle.getId(),
                subtitle.getStartTimeMs(),
                subtitle.getEndTimeMs(),
                subtitle.getEnglishText(),
                subtitle.getVietnameseText(),
                subtitle.getOrderIndex()
        );
    }
}
