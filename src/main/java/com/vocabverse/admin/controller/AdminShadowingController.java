package com.vocabverse.admin.controller;

import com.vocabverse.admin.dto.request.AdminImportFromYouTubeRequest;
import com.vocabverse.admin.dto.request.AdminImportShadowingSubtitlesRequest;
import com.vocabverse.admin.dto.request.AdminUpsertShadowingSubtitleRequest;
import com.vocabverse.admin.dto.response.AdminPageResponse;
import com.vocabverse.admin.dto.response.AdminShadowingLessonResponse;
import com.vocabverse.admin.dto.response.AdminShadowingLessonStatusResponse;
import com.vocabverse.admin.service.AdminShadowingService;
import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.shadowing.dto.response.ShadowingSubtitleResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/shadowing/lessons")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShadowingController {

    private final AdminShadowingService adminShadowingService;

    @PostMapping("/upload")
    public ApiResponse<AdminShadowingLessonResponse> uploadLesson(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description
    ) {
        return ApiResponse.success(adminShadowingService.uploadLesson(file, title, description));
    }

    @PostMapping("/youtube")
    public ApiResponse<AdminShadowingLessonResponse> importFromYouTube(
            @Valid @RequestBody AdminImportFromYouTubeRequest request
    ) {
        return ApiResponse.success(adminShadowingService.importFromYouTube(request));
    }

    @GetMapping("/{lessonId}/status")
    public ApiResponse<AdminShadowingLessonStatusResponse> getLessonStatus(@PathVariable UUID lessonId) {
        return ApiResponse.success(adminShadowingService.getLessonStatus(lessonId));
    }

    @GetMapping
    public ApiResponse<AdminPageResponse<AdminShadowingLessonResponse>> listLessons(
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ApiResponse.success(adminShadowingService.listLessons(pageable));
    }

    @GetMapping("/{lessonId}/subtitles")
    public ApiResponse<List<ShadowingSubtitleResponse>> listSubtitles(@PathVariable UUID lessonId) {
        return ApiResponse.success(adminShadowingService.listSubtitles(lessonId));
    }

    @PostMapping("/{lessonId}/subtitles")
    public ApiResponse<ShadowingSubtitleResponse> createSubtitle(
            @PathVariable UUID lessonId,
            @Valid @RequestBody AdminUpsertShadowingSubtitleRequest request
    ) {
        return ApiResponse.success(adminShadowingService.createSubtitle(lessonId, request));
    }

    @PostMapping("/{lessonId}/subtitles/import")
    public ApiResponse<List<ShadowingSubtitleResponse>> importSubtitles(
            @PathVariable UUID lessonId,
            @Valid @RequestBody AdminImportShadowingSubtitlesRequest request
    ) {
        return ApiResponse.success(adminShadowingService.importSubtitles(lessonId, request));
    }

    @PostMapping("/{lessonId}/subtitles/generate-ai")
    public ApiResponse<List<ShadowingSubtitleResponse>> generateAiSubtitles(@PathVariable UUID lessonId) {
        return ApiResponse.success(adminShadowingService.generateAiSubtitles(lessonId));
    }

    @PutMapping("/{lessonId}/subtitles/{subtitleId}")
    public ApiResponse<ShadowingSubtitleResponse> updateSubtitle(
            @PathVariable UUID lessonId,
            @PathVariable UUID subtitleId,
            @Valid @RequestBody AdminUpsertShadowingSubtitleRequest request
    ) {
        return ApiResponse.success(adminShadowingService.updateSubtitle(lessonId, subtitleId, request));
    }

    @DeleteMapping("/{lessonId}/subtitles/{subtitleId}")
    public ApiResponse<Void> deleteSubtitle(
            @PathVariable UUID lessonId,
            @PathVariable UUID subtitleId
    ) {
        adminShadowingService.deleteSubtitle(lessonId, subtitleId);
        return ApiResponse.success("Delete subtitle successfully", null);
    }
}
