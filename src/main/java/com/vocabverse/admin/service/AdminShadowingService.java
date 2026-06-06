package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.request.AdminImportShadowingSubtitlesRequest;
import com.vocabverse.admin.dto.request.AdminUpsertShadowingSubtitleRequest;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.dto.response.AdminShadowingLessonResponse;
import com.vocabverse.admin.dto.response.AdminShadowingLessonStatusResponse;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.shadowing.dto.response.ShadowingSubtitleResponse;
import com.vocabverse.shadowing.entity.ShadowingLessonEntity;
import com.vocabverse.shadowing.entity.ShadowingLessonSource;
import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import com.vocabverse.shadowing.entity.ShadowingLessonSubtitleEntity;
import com.vocabverse.shadowing.repository.ShadowingLessonRepository;
import com.vocabverse.shadowing.repository.ShadowingLessonSubtitleRepository;
import com.vocabverse.shadowing.service.CloudinaryVideoStorageService;
import com.vocabverse.shadowing.service.CloudinaryVideoStorageService.CloudinaryUploadResult;
import com.vocabverse.shadowing.service.ShadowingAiSubtitleService;
import com.vocabverse.shadowing.service.ShadowingAiSubtitleService.GeneratedSubtitle;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminShadowingService {

    private static final int COMPLETED_PROGRESS = 100;

    private final ShadowingLessonRepository shadowingLessonRepository;
    private final ShadowingLessonSubtitleRepository subtitleRepository;
    private final UserRepository userRepository;
    private final CloudinaryVideoStorageService cloudinaryVideoStorageService;
    private final ShadowingAiSubtitleService shadowingAiSubtitleService;

    @Transactional
    public AdminShadowingLessonResponse uploadLesson(MultipartFile file, String title, String description) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "MP4 file is required");
        }
        if (!isMp4(file)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Only MP4 uploads are supported");
        }

        CloudinaryUploadResult uploadResult = cloudinaryVideoStorageService.uploadMp4(file);
        ShadowingLessonEntity lesson = ShadowingLessonEntity.builder()
                .source(ShadowingLessonSource.UPLOAD)
                .status(ShadowingLessonStatus.COMPLETED)
                .title(trimToNull(title) == null ? file.getOriginalFilename() : title.trim())
                .description(trimToNull(description))
                .originalFilename(file.getOriginalFilename())
                .cloudinaryPublicId(uploadResult.publicId())
                .videoUrl(uploadResult.videoUrl())
                .thumbnailUrl(uploadResult.thumbnailUrl())
                .storageProvider(uploadResult.storageProvider())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .duration(uploadResult.duration())
                .progress(COMPLETED_PROGRESS)
                .createdBy(getCurrentAdmin())
                .build();

        return toResponse(shadowingLessonRepository.save(lesson));
    }

    @Transactional(readOnly = true)
    public AdminShadowingLessonStatusResponse getLessonStatus(UUID lessonId) {
        ShadowingLessonEntity lesson = findUploadLesson(lessonId);
        return new AdminShadowingLessonStatusResponse(
                lesson.getId(),
                lesson.getStatus(),
                lesson.getProgress(),
                resolveStatusMessage(lesson)
        );
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminShadowingLessonResponse> listLessons(Pageable pageable) {
        Page<AdminShadowingLessonResponse> page = shadowingLessonRepository
                .findBySourceOrderByCreatedAtDesc(ShadowingLessonSource.UPLOAD, pageable)
                .map(this::toResponse);
        return new AdminPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public List<ShadowingSubtitleResponse> listSubtitles(UUID lessonId) {
        findUploadLesson(lessonId);
        return subtitleRepository.findByLessonIdOrderByOrderIndexAscStartTimeMsAsc(lessonId)
                .stream()
                .map(this::toSubtitleResponse)
                .toList();
    }

    @Transactional
    public ShadowingSubtitleResponse createSubtitle(UUID lessonId, AdminUpsertShadowingSubtitleRequest request) {
        ShadowingLessonEntity lesson = findUploadLesson(lessonId);
        validateSubtitleTime(request.startTimeMs(), request.endTimeMs());
        int orderIndex = request.orderIndex() == null
                ? subtitleRepository.countByLessonId(lessonId)
                : request.orderIndex();

        ShadowingLessonSubtitleEntity subtitle = ShadowingLessonSubtitleEntity.builder()
                .lesson(lesson)
                .startTimeMs(request.startTimeMs())
                .endTimeMs(request.endTimeMs())
                .englishText(request.englishText().trim())
                .vietnameseText(trimToNull(request.vietnameseText()))
                .orderIndex(orderIndex)
                .build();
        return toSubtitleResponse(subtitleRepository.save(subtitle));
    }

    @Transactional
    public List<ShadowingSubtitleResponse> importSubtitles(UUID lessonId, AdminImportShadowingSubtitlesRequest request) {
        ShadowingLessonEntity lesson = findUploadLesson(lessonId);
        if (Boolean.TRUE.equals(request.replaceExisting())) {
            subtitleRepository.deleteByLessonId(lessonId);
        }

        int nextOrderIndex = subtitleRepository.countByLessonId(lessonId);
        List<ShadowingLessonSubtitleEntity> subtitles = new ArrayList<>();
        for (int index = 0; index < request.items().size(); index++) {
            AdminUpsertShadowingSubtitleRequest item = request.items().get(index);
            validateSubtitleTime(item.startTimeMs(), item.endTimeMs());
            subtitles.add(ShadowingLessonSubtitleEntity.builder()
                    .lesson(lesson)
                    .startTimeMs(item.startTimeMs())
                    .endTimeMs(item.endTimeMs())
                    .englishText(item.englishText().trim())
                    .vietnameseText(trimToNull(item.vietnameseText()))
                    .orderIndex(item.orderIndex() == null ? nextOrderIndex + index : item.orderIndex())
                    .build());
        }

        return subtitleRepository.saveAll(subtitles)
                .stream()
                .map(this::toSubtitleResponse)
                .toList();
    }

    @Transactional
    public ShadowingSubtitleResponse updateSubtitle(
            UUID lessonId,
            UUID subtitleId,
            AdminUpsertShadowingSubtitleRequest request
    ) {
        findUploadLesson(lessonId);
        validateSubtitleTime(request.startTimeMs(), request.endTimeMs());
        ShadowingLessonSubtitleEntity subtitle = subtitleRepository.findByIdAndLessonId(subtitleId, lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "Subtitle not found"));

        subtitle.setStartTimeMs(request.startTimeMs());
        subtitle.setEndTimeMs(request.endTimeMs());
        subtitle.setEnglishText(request.englishText().trim());
        subtitle.setVietnameseText(trimToNull(request.vietnameseText()));
        if (request.orderIndex() != null) {
            subtitle.setOrderIndex(request.orderIndex());
        }
        return toSubtitleResponse(subtitleRepository.save(subtitle));
    }

    @Transactional
    public void deleteSubtitle(UUID lessonId, UUID subtitleId) {
        findUploadLesson(lessonId);
        ShadowingLessonSubtitleEntity subtitle = subtitleRepository.findByIdAndLessonId(subtitleId, lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "Subtitle not found"));
        subtitleRepository.delete(subtitle);
    }

    @Transactional
    public List<ShadowingSubtitleResponse> generateAiSubtitles(UUID lessonId) {
        ShadowingLessonEntity lesson = findUploadLesson(lessonId);
        lesson.setStatus(ShadowingLessonStatus.PROCESSING);
        lesson.setProgress(50);
        lesson.setErrorMessage(null);
        shadowingLessonRepository.save(lesson);

        try {
            List<GeneratedSubtitle> generatedSubtitles = shadowingAiSubtitleService.generateSubtitles(lesson);
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
            List<ShadowingSubtitleResponse> responses = subtitleRepository.saveAll(subtitles)
                    .stream()
                    .map(this::toSubtitleResponse)
                    .toList();

            lesson.setStatus(ShadowingLessonStatus.COMPLETED);
            lesson.setProgress(COMPLETED_PROGRESS);
            shadowingLessonRepository.save(lesson);
            return responses;
        } catch (BusinessException exception) {
            lesson.setStatus(ShadowingLessonStatus.COMPLETED);
            lesson.setProgress(COMPLETED_PROGRESS);
            lesson.setErrorMessage(exception.getMessage());
            shadowingLessonRepository.save(lesson);
            throw exception;
        }
    }

    private ShadowingLessonEntity findUploadLesson(UUID lessonId) {
        ShadowingLessonEntity lesson = shadowingLessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND));
        if (lesson.getSource() != ShadowingLessonSource.UPLOAD) {
            throw new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND);
        }
        return lesson;
    }

    private UserEntity getCurrentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private boolean isMp4(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        return "video/mp4".equalsIgnoreCase(contentType)
                || (StringUtils.hasText(filename) && filename.toLowerCase().endsWith(".mp4"));
    }

    private AdminShadowingLessonResponse toResponse(ShadowingLessonEntity lesson) {
        return new AdminShadowingLessonResponse(
                lesson.getId(),
                lesson.getSource(),
                lesson.getSource(),
                lesson.getStatus(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getOriginalFilename(),
                lesson.getVideoUrl(),
                lesson.getThumbnailUrl(),
                lesson.getCloudinaryPublicId(),
                lesson.getStorageProvider(),
                lesson.getContentType(),
                lesson.getFileSize(),
                lesson.getDuration(),
                lesson.getProgress(),
                subtitleRepository.countByLessonId(lesson.getId()),
                lesson.getErrorMessage(),
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

    private void validateSubtitleTime(int startTimeMs, int endTimeMs) {
        if (startTimeMs >= endTimeMs) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Subtitle endTimeMs must be greater than startTimeMs");
        }
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isBlank() ? null : value.trim();
    }

    private String resolveStatusMessage(ShadowingLessonEntity lesson) {
        if (lesson.getErrorMessage() != null && !lesson.getErrorMessage().isBlank()) {
            return lesson.getErrorMessage();
        }
        return switch (lesson.getStatus()) {
            case PENDING -> "Waiting for processing";
            case PROCESSING -> "Processing lesson";
            case COMPLETED -> "Lesson is ready";
            case FAILED -> "Lesson processing failed";
        };
    }
}
