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

    private static final String MISSING_MEDIA_MESSAGE = "Shadowing lesson media URL is missing";

    private final ShadowingLessonRepository shadowingLessonRepository;
    private final ShadowingLessonSubtitleRepository subtitleRepository;
    private final CloudinaryVideoStorageService cloudinaryVideoStorageService;

    @Transactional(readOnly = true)
    public ShadowingLessonPageResponse listLessons(Pageable pageable) {
        Page<ShadowingLessonSummaryResponse> page = shadowingLessonRepository
                .findBySourceAndStatusOrderByCreatedAtDesc(
                        ShadowingLessonSource.UPLOAD,
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
        ShadowingLessonEntity lesson = shadowingLessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND));
        if (lesson.getSource() != ShadowingLessonSource.UPLOAD || lesson.getStatus() != ShadowingLessonStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND);
        }

        List<ShadowingSubtitleResponse> subtitles = subtitleRepository
                .findByLessonIdOrderByOrderIndexAscStartTimeMsAsc(lessonId)
                .stream()
                .map(this::toSubtitleResponse)
                .toList();
        String mediaUrl = resolveMediaUrl(lesson);
        if (!hasText(mediaUrl)) {
            throw new BusinessException(
                    ErrorCode.VIDEO_PROCESSING_FAILED,
                    MISSING_MEDIA_MESSAGE
            );
        }
        String thumbnailUrl = resolveThumbnailUrl(lesson);

        return new ShadowingLessonDetailResponse(
                lesson.getId(),
                lesson.getSource(),
                resolvePublicStatus(lesson),
                lesson.getTitle(),
                lesson.getDescription(),
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                thumbnailUrl,
                lesson.getDuration(),
                lesson.getProgress(),
                lesson.getErrorMessage(),
                subtitles,
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }

    private ShadowingLessonSummaryResponse toSummaryResponse(ShadowingLessonEntity lesson) {
        String mediaUrl = resolveMediaUrl(lesson);
        return new ShadowingLessonSummaryResponse(
                lesson.getId(),
                lesson.getSource(),
                resolvePublicStatus(lesson),
                lesson.getTitle(),
                lesson.getDescription(),
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                mediaUrl,
                resolveThumbnailUrl(lesson),
                lesson.getDuration(),
                subtitleRepository.countByLessonId(lesson.getId()),
                resolveErrorMessage(lesson),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }

    private String resolveMediaUrl(ShadowingLessonEntity lesson) {
        if (hasText(lesson.getVideoUrl())) {
            return lesson.getVideoUrl();
        }
        if (hasText(lesson.getYoutubeUrl())) {
            return lesson.getYoutubeUrl();
        }
        if (hasText(lesson.getCloudinaryPublicId())) {
            return cloudinaryVideoStorageService.buildVideoUrl(lesson.getCloudinaryPublicId());
        }
        return null;
    }

    private String resolveThumbnailUrl(ShadowingLessonEntity lesson) {
        if (hasText(lesson.getThumbnailUrl())) {
            return lesson.getThumbnailUrl();
        }
        if (hasText(lesson.getCloudinaryPublicId())) {
            return cloudinaryVideoStorageService.buildThumbnailUrl(lesson.getCloudinaryPublicId());
        }
        return null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ShadowingLessonStatus resolvePublicStatus(ShadowingLessonEntity lesson) {
        if (lesson.getStatus() == ShadowingLessonStatus.COMPLETED && !hasText(resolveMediaUrl(lesson))) {
            return ShadowingLessonStatus.FAILED;
        }
        return lesson.getStatus();
    }

    private String resolveErrorMessage(ShadowingLessonEntity lesson) {
        if (hasText(lesson.getErrorMessage())) {
            return lesson.getErrorMessage();
        }
        if (lesson.getStatus() == ShadowingLessonStatus.COMPLETED && !hasText(resolveMediaUrl(lesson))) {
            return MISSING_MEDIA_MESSAGE;
        }
        return null;
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
