package com.vocabverse.review.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.review.dto.request.SubmitReviewRequest;
import com.vocabverse.review.dto.response.ReviewDueCountResponse;
import com.vocabverse.review.dto.response.ReviewDuePageResponse;
import com.vocabverse.review.dto.response.ReviewHistoryPageResponse;
import com.vocabverse.review.dto.response.ReviewStatisticsResponse;
import com.vocabverse.review.dto.response.ReviewSubmitResponse;
import com.vocabverse.review.service.ReviewService;
import com.vocabverse.review.service.ReviewStatisticsService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewStatisticsService reviewStatisticsService;

    @PostMapping("/{vocabularyId}")
    public ApiResponse<ReviewSubmitResponse> submitReview(
            @PathVariable UUID vocabularyId,
            @Valid @RequestBody SubmitReviewRequest request
    ) {
        return ApiResponse.success(reviewService.submitReview(vocabularyId, request.result()));
    }

    @GetMapping("/today")
    public ApiResponse<ReviewDuePageResponse> getTodayReviews(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(reviewService.getTodayReviews(pageable));
    }

    @GetMapping("/history")
    public ApiResponse<ReviewHistoryPageResponse> getReviewHistory(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(reviewService.getReviewHistory(pageable));
    }

    @GetMapping("/stats")
    public ApiResponse<ReviewStatisticsResponse> getReviewStats() {
        return ApiResponse.success(reviewStatisticsService.getCurrentUserStats());
    }

    @GetMapping("/due-count")
    public ApiResponse<ReviewDueCountResponse> getDueCount() {
        return ApiResponse.success(reviewService.getDueCount());
    }
}
