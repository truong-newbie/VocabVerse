package com.vocabverse.admin.service;

import com.vocabverse.admin.dto.request.AdminCreateYoutubeShadowingLessonRequest;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.dto.response.AdminShadowingLessonResponse;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.shadowing.entity.ShadowingLessonEntity;
import com.vocabverse.shadowing.entity.ShadowingLessonSource;
import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import com.vocabverse.shadowing.repository.ShadowingLessonRepository;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
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

    private final ShadowingLessonRepository shadowingLessonRepository;
    private final UserRepository userRepository;

    @Transactional
    public AdminShadowingLessonResponse uploadLesson(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "MP4 file is required");
        }
        if (!isMp4(file)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Only MP4 uploads are supported");
        }

        ShadowingLessonEntity lesson = ShadowingLessonEntity.builder()
                .source(ShadowingLessonSource.UPLOAD)
                .status(ShadowingLessonStatus.PENDING)
                .title(file.getOriginalFilename())
                .originalFilename(file.getOriginalFilename())
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .createdBy(getCurrentAdmin())
                .build();

        return toResponse(shadowingLessonRepository.save(lesson));
    }

    @Transactional
    public AdminShadowingLessonResponse createYoutubeLesson(AdminCreateYoutubeShadowingLessonRequest request) {
        String youtubeUrl = request.youtubeUrl().trim();
        if (!isHttpUrl(youtubeUrl)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "youtubeUrl must be a valid URL");
        }

        ShadowingLessonEntity lesson = ShadowingLessonEntity.builder()
                .source(ShadowingLessonSource.YOUTUBE)
                .status(ShadowingLessonStatus.PENDING)
                .title(youtubeUrl)
                .youtubeUrl(youtubeUrl)
                .createdBy(getCurrentAdmin())
                .build();

        return toResponse(shadowingLessonRepository.save(lesson));
    }

    @Transactional(readOnly = true)
    public AdminShadowingLessonResponse getLessonStatus(UUID lessonId) {
        return toResponse(findLesson(lessonId));
    }

    @Transactional(readOnly = true)
    public AdminPageResponse<AdminShadowingLessonResponse> listLessons(Pageable pageable) {
        Page<AdminShadowingLessonResponse> page = shadowingLessonRepository
                .findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
        return new AdminPageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private ShadowingLessonEntity findLesson(UUID lessonId) {
        return shadowingLessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SHADOWING_LESSON_NOT_FOUND));
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

    private boolean isHttpUrl(String value) {
        return value.startsWith("https://") || value.startsWith("http://");
    }

    private AdminShadowingLessonResponse toResponse(ShadowingLessonEntity lesson) {
        return new AdminShadowingLessonResponse(
                lesson.getId(),
                lesson.getSource(),
                lesson.getStatus(),
                lesson.getTitle(),
                lesson.getOriginalFilename(),
                lesson.getYoutubeUrl(),
                lesson.getContentType(),
                lesson.getFileSize(),
                lesson.getErrorMessage(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
