package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.request.AdminImportFromYouTubeRequest;
import com.vocabverse.admin.dto.request.AdminImportShadowingSubtitlesRequest;
import com.vocabverse.admin.dto.request.AdminUpdateShadowingLessonRequest;
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
import com.vocabverse.shadowing.service.ShadowingSubtitleGenerationService;
import com.vocabverse.shadowing.service.YouTubeDownloadService;
import com.vocabverse.shadowing.service.YouTubeDownloadService.DownloadResult;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
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
    private final ShadowingSubtitleGenerationService shadowingSubtitleGenerationService;
    private final YouTubeDownloadService youTubeDownloadService;

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

    @Transactional
    public AdminShadowingLessonResponse importFromYouTube(AdminImportFromYouTubeRequest request) {
        DownloadResult download = youTubeDownloadService.downloadAudio(request.youtubeUrl());

        CloudinaryUploadResult uploadResult;
        try {
            java.nio.file.Path tempAudio = download.audioFile();
            uploadResult = cloudinaryVideoStorageService.uploadAudio(tempAudio.toFile(),
                    download.title() != null ? download.title() : "youtube-import");
        } finally {
            cleanupQuietly(download.audioFile());
        }

        ShadowingLessonEntity lesson = ShadowingLessonEntity.builder()
                .source(ShadowingLessonSource.YOUTUBE)
                .status(ShadowingLessonStatus.PROCESSING)
                .title(trimToNull(request.title()) != null ? request.title() : download.title())
                .description(trimToNull(request.description()))
                .youtubeUrl(request.youtubeUrl())
                .cloudinaryPublicId(uploadResult.publicId())
                .videoUrl(uploadResult.videoUrl())
                .thumbnailUrl(uploadResult.thumbnailUrl())
                .storageProvider(uploadResult.storageProvider())
                .contentType("audio/m4a")
                .duration(download.duration())
                .progress(25)
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
        Page<ShadowingLessonEntity> page = shadowingLessonRepository
                .findBySourceInAndDeletedAtIsNullOrderByCreatedAtDesc(
                        List.of(ShadowingLessonSource.UPLOAD, ShadowingLessonSource.YOUTUBE),
                        pageable
                );
        Map<UUID, Integer> subtitleCounts = loadSubtitleCounts(page.getContent());
        return new AdminPageResponse<>(
                page.getContent().stream()
                        .map(lesson -> toResponse(lesson, subtitleCounts.getOrDefault(lesson.getId(), 0)))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public AdminShadowingLessonResponse updateLesson(UUID lessonId, AdminUpdateShadowingLessonRequest request) {
        ShadowingLessonEntity lesson = findUploadLesson(lessonId);
        if (request.title() != null) {
            String title = trimToNull(request.title());
            if (title == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT, "Lesson title must not be blank");
            }
            lesson.setTitle(title);
        }
        if (request.description() != null) {
            lesson.setDescription(trimToNull(request.description()));
        }
        return toResponse(shadowingLessonRepository.save(lesson));
    }

    @Transactional
    public void deleteLesson(UUID lessonId) {
        ShadowingLessonEntity lesson = findUploadLesson(lessonId);
        lesson.setDeletedAt(LocalDateTime.now());
        shadowingLessonRepository.save(lesson);
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
    public AdminShadowingLessonStatusResponse generateAiSubtitles(UUID lessonId) {
        ShadowingLessonEntity lesson = findUploadLesson(lessonId);
        if (lesson.getStatus() == ShadowingLessonStatus.PROCESSING) {
            return new AdminShadowingLessonStatusResponse(
                    lesson.getId(),
                    lesson.getStatus(),
                    lesson.getProgress(),
                    resolveStatusMessage(lesson)
            );
        }

        lesson.setStatus(ShadowingLessonStatus.PROCESSING);
        lesson.setProgress(10);
        lesson.setErrorMessage(null);
        ShadowingLessonEntity savedLesson = shadowingLessonRepository.save(lesson);
        shadowingSubtitleGenerationService.generateSubtitlesAsync(savedLesson.getId());

        return new AdminShadowingLessonStatusResponse(
                savedLesson.getId(),
                savedLesson.getStatus(),
                savedLesson.getProgress(),
                "AI subtitle generation started"
        );
    }

    private ShadowingLessonEntity findUploadLesson(UUID lessonId) {
        ShadowingLessonEntity lesson = shadowingLessonRepository.findByIdAndDeletedAtIsNull(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND));
        if (lesson.getSource() != ShadowingLessonSource.UPLOAD && lesson.getSource() != ShadowingLessonSource.YOUTUBE) {
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
        return toResponse(lesson, subtitleRepository.countByLessonId(lesson.getId()));
    }

    private AdminShadowingLessonResponse toResponse(ShadowingLessonEntity lesson, int subtitleCount) {
        return new AdminShadowingLessonResponse(
                lesson.getId(),
                lesson.getSource(),
                lesson.getStatus(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getOriginalFilename(),
                resolveVideoUrl(lesson),
                resolveAudioUrl(lesson),
                lesson.getThumbnailUrl(),
                lesson.getCloudinaryPublicId(),
                lesson.getStorageProvider(),
                lesson.getContentType(),
                lesson.getFileSize(),
                lesson.getDuration(),
                lesson.getProgress(),
                subtitleCount,
                lesson.getErrorMessage(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }

    private Map<UUID, Integer> loadSubtitleCounts(List<ShadowingLessonEntity> lessons) {
        if (lessons.isEmpty()) {
            return Map.of();
        }
        List<UUID> lessonIds = lessons.stream()
                .map(ShadowingLessonEntity::getId)
                .toList();
        return subtitleRepository.countByLessonIds(lessonIds)
                .stream()
                .collect(Collectors.toMap(
                        ShadowingLessonSubtitleRepository.LessonSubtitleCount::getLessonId,
                        count -> safeSubtitleCount(count.getSubtitleCount())
                ));
    }

    private int safeSubtitleCount(long value) {
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) value;
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

    private String resolveVideoUrl(ShadowingLessonEntity lesson) {
        return lesson.getSource() == ShadowingLessonSource.UPLOAD ? lesson.getVideoUrl() : null;
    }

    private String resolveAudioUrl(ShadowingLessonEntity lesson) {
        return lesson.getSource() == ShadowingLessonSource.YOUTUBE ? lesson.getVideoUrl() : null;
    }

    private void cleanupQuietly(java.nio.file.Path file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file);
            } catch (java.io.IOException ignored) {
            }
        }
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
