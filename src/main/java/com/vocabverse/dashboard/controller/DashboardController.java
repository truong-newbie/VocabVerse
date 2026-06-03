package com.vocabverse.dashboard.controller;

import com.vocabverse.common.response.ApiResponse;
import com.vocabverse.dashboard.dto.response.DashboardSummaryResponse;
import com.vocabverse.dashboard.dto.response.LearningStatusStatsResponse;
import com.vocabverse.dashboard.dto.response.RecentActivityResponse;
import com.vocabverse.dashboard.dto.response.ReviewDueResponse;
import com.vocabverse.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    @GetMapping("/learning-status")
    public ApiResponse<LearningStatusStatsResponse> getLearningStatus() {
        return ApiResponse.success(dashboardService.getLearningStatus());
    }

    @GetMapping("/review-due")
    public ApiResponse<ReviewDueResponse> getReviewDue(
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ApiResponse.success(dashboardService.getReviewDue(pageable));
    }

    @GetMapping("/recent-activity")
    public ApiResponse<RecentActivityResponse> getRecentActivity() {
        return ApiResponse.success(dashboardService.getRecentActivity());
    }
}
