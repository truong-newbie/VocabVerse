package com.vocabverse.learning.progress.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.learning.progress.dto.LearningProgressPageResponse;
import com.vocabverse.learning.progress.dto.LearningProgressResponse;
import com.vocabverse.learning.progress.dto.LearningProgressSummaryResponse;
import com.vocabverse.learning.progress.service.LearningProgressService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/learning/progress")
public class LearningProgressController {

    private final LearningProgressService learningProgressService;

    @GetMapping
    public ApiResponse<LearningProgressSummaryResponse> getCurrentUserProgressSummary() {
        return ApiResponse.success(learningProgressService.getCurrentUserProgressSummary());
    }

    @GetMapping("/items")
    public ApiResponse<LearningProgressPageResponse> getCurrentUserProgress(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(learningProgressService.getCurrentUserProgress(pageable));
    }

    @GetMapping("/{vocabularyId}")
    public ApiResponse<LearningProgressResponse> getProgressDetail(@PathVariable UUID vocabularyId) {
        return ApiResponse.success(learningProgressService.getProgressDetail(vocabularyId));
    }

    @PostMapping("/{vocabularyId}/initialize")
    public ApiResponse<LearningProgressResponse> initializeProgress(@PathVariable UUID vocabularyId) {
        return ApiResponse.success(learningProgressService.initializeProgress(vocabularyId));
    }
}
