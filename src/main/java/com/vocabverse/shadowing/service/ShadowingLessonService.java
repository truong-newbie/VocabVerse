package com.vocabverse.shadowing.service;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.shadowing.dto.response.ShadowingLessonDetailResponse;
import com.vocabverse.shadowing.dto.response.ShadowingLessonPageResponse;
import com.vocabverse.shadowing.dto.response.ShadowingLessonSummaryResponse;
import com.vocabverse.shadowing.dto.response.ShadowingSubtitleResponse;
import com.vocabverse.shadowing.entity.ShadowingLessonEntity;
import com.vocabverse.shadowing.entity.ShadowingLessonSource;
import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import com.vocabverse.shadowing.entity.ShadowingLessonSubtitleEntity;
import com.vocabverse.shadowing.repository.ShadowingLessonRepository;
import com.vocabverse.shadowing.repository.ShadowingLessonSubtitleRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShadowingLessonService {

    private final ShadowingLessonRepository shadowingLessonRepository;
    private final ShadowingLessonSubtitleRepository subtitleRepository;

    @Transactional(readOnly = true)
    public ShadowingLessonPageResponse listLessons(Pageable pageable) {
        Page<ShadowingLessonSummaryResponse> page = shadowingLessonRepository
                .findBySourceInAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
                        List.of(ShadowingLessonSource.UPLOAD, ShadowingLessonSource.YOUTUBE),
                        ShadowingLessonStatus.COMPLETED,
                        pageable
                )
                .map(this::toSummaryResponse);
        return new ShadowingLessonPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ShadowingLessonDetailResponse getLesson(UUID lessonId) {
        ShadowingLessonEntity lesson = shadowingLessonRepository.findByIdAndDeletedAtIsNull(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND));
        if (lesson.getStatus() != ShadowingLessonStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND);
        }
        if (lesson.getSource() != ShadowingLessonSource.UPLOAD && lesson.getSource() != ShadowingLessonSource.YOUTUBE) {
            throw new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND);
        }

        List<ShadowingSubtitleResponse> subtitles = subtitleRepository
                .findByLessonIdOrderByOrderIndexAscStartTimeMsAsc(lessonId)
                .stream()
                .map(this::toSubtitleResponse)
                .toList();

        return new ShadowingLessonDetailResponse(
                lesson.getId(),
                lesson.getSource(),
                lesson.getStatus(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getVideoUrl(),
                lesson.getThumbnailUrl(),
                lesson.getDuration(),
                lesson.getProgress(),
                subtitles,
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }

    private ShadowingLessonSummaryResponse toSummaryResponse(ShadowingLessonEntity lesson) {
        return new ShadowingLessonSummaryResponse(
                lesson.getId(),
                lesson.getSource(),
                lesson.getStatus(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getVideoUrl(),
                lesson.getThumbnailUrl(),
                lesson.getDuration(),
                subtitleRepository.countByLessonId(lesson.getId()),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }

    private ShadowingSubtitleResponse toSubtitleResponse(ShadowingLessonSubtitleEntity subtitle) {
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
