package com.vocabverse.shadowing.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.shadowing.dto.response.ShadowingLessonDetailResponse;
import com.vocabverse.shadowing.dto.response.ShadowingLessonPageResponse;
import com.vocabverse.shadowing.service.ShadowingLessonService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shadowing/lessons")
public class ShadowingLessonController {

    private final ShadowingLessonService shadowingLessonService;

    @GetMapping
    public ApiResponse<ShadowingLessonPageResponse> listLessons(
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return ApiResponse.success(shadowingLessonService.listLessons(pageable));
    }

    @GetMapping("/{lessonId}")
    public ApiResponse<ShadowingLessonDetailResponse> getLesson(@PathVariable UUID lessonId) {
        return ApiResponse.success(shadowingLessonService.getLesson(lessonId));
    }
}
