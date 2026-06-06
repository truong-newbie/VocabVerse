package com.vocabverse.admin.controller;

import com.vocabverse.admin.dto.request.AdminCreateYoutubeShadowingLessonRequest;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.dto.response.AdminShadowingLessonResponse;
import com.vocabverse.admin.service.AdminShadowingService;
import com.vocabverse.common.response.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/shadowing/lessons")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShadowingController {

    private final AdminShadowingService adminShadowingService;

    @PostMapping("/upload")
    public ApiResponse<AdminShadowingLessonResponse> uploadLesson(@RequestPart("file") MultipartFile file) {
        return ApiResponse.success(adminShadowingService.uploadLesson(file));
    }

    @PostMapping("/youtube")
    public ApiResponse<AdminShadowingLessonResponse> createYoutubeLesson(
            @Valid @RequestBody AdminCreateYoutubeShadowingLessonRequest request
    ) {
        return ApiResponse.success(adminShadowingService.createYoutubeLesson(request));
    }

    @GetMapping("/{lessonId}/status")
    public ApiResponse<AdminShadowingLessonResponse> getLessonStatus(@PathVariable UUID lessonId) {
        return ApiResponse.success(adminShadowingService.getLessonStatus(lessonId));
    }

    @GetMapping
    public ApiResponse<AdminPageResponse<AdminShadowingLessonResponse>> listLessons(
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ApiResponse.success(adminShadowingService.listLessons(pageable));
    }
}
