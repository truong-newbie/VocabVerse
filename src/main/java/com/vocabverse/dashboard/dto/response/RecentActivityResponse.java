package com.vocabverse.dashboard.dto.response;

import com.vocabverse.notification.dto.response.NotificationResponse;
import com.vocabverse.review.dto.response.ReviewHistoryResponse;
import java.util.List;

public record RecentActivityResponse(
        List<ReviewHistoryResponse> recentReviewHistory,
        List<NotificationResponse> recentNotifications
) {
}
