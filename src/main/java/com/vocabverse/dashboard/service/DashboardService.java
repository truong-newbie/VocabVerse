package com.vocabverse.dashboard.service;

import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.dashboard.dto.response.DashboardSummaryResponse;
import com.vocabverse.dashboard.dto.response.LearningStatusStatsResponse;
import com.vocabverse.dashboard.dto.response.RecentActivityResponse;
import com.vocabverse.dashboard.dto.response.ReviewDueResponse;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionStatus;
import com.vocabverse.learning.flashcard.repository.FlashcardSessionRepository;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.learning.quiz.entity.QuizSessionStatus;
import com.vocabverse.learning.quiz.repository.QuizSessionRepository;
import com.vocabverse.learning.typing.entity.TypingSessionStatus;
import com.vocabverse.learning.typing.repository.TypingSessionRepository;
import com.vocabverse.notification.mapper.NotificationMapper;
import com.vocabverse.notification.repository.NotificationRepository;
import com.vocabverse.review.entity.ReviewHistoryEntity;
import com.vocabverse.review.mapper.ReviewMapper;
import com.vocabverse.review.repository.ReviewHistoryRepository;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int RECENT_ACTIVITY_LIMIT = 5;

    private final VocabularyRepository vocabularyRepository;
    private final CollectionRepository collectionRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final FlashcardSessionRepository flashcardSessionRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final TypingSessionRepository typingSessionRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;
    private final NotificationMapper notificationMapper;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        UUID userId = getCurrentUser().getId();
        LearningStatusStatsResponse learningStatusStats = getLearningStatusStats(userId);
        LocalDateTime now = LocalDateTime.now();

        return new DashboardSummaryResponse(
                vocabularyRepository.countByOwnerIdAndDeletedAtIsNull(userId),
                collectionRepository.countByOwnerIdAndDeletedAtIsNull(userId),
                learningStatusStats.newCount(),
                learningStatusStats.learningCount(),
                learningStatusStats.reviewingCount(),
                learningStatusStats.masteredCount(),
                learningProgressRepository.countDueReviewsByUserId(userId, now),
                flashcardSessionRepository.countByUserIdAndStatus(userId, FlashcardSessionStatus.COMPLETED),
                quizSessionRepository.countByUserIdAndStatus(userId, QuizSessionStatus.COMPLETED),
                typingSessionRepository.countByUserIdAndStatus(userId, TypingSessionStatus.COMPLETED)
        );
    }

    @Transactional(readOnly = true)
    public LearningStatusStatsResponse getLearningStatus() {
        return getLearningStatusStats(getCurrentUser().getId());
    }

    @Transactional(readOnly = true)
    public ReviewDueResponse getReviewDue(Pageable pageable) {
        UUID userId = getCurrentUser().getId();
        LocalDateTime now = LocalDateTime.now();
        Page<LearningProgressEntity> duePage = learningProgressRepository
                .findAllByUserIdAndNextReviewAtLessThanEqual(userId, now, pageable);

        return new ReviewDueResponse(
                learningProgressRepository.countDueReviewsByUserId(userId, now),
                duePage.map(reviewMapper::toDueItemResponse).getContent(),
                duePage.getNumber(),
                duePage.getSize(),
                duePage.getTotalElements(),
                duePage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public RecentActivityResponse getRecentActivity() {
        UUID userId = getCurrentUser().getId();
        Pageable recentPage = PageRequest.of(0, RECENT_ACTIVITY_LIMIT);
        Page<ReviewHistoryEntity> reviewHistory = reviewHistoryRepository
                .findAllByUserIdOrderByReviewedAtDesc(userId, recentPage);

        return new RecentActivityResponse(
                reviewHistory.map(reviewMapper::toHistoryResponse).getContent(),
                notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId, recentPage)
                        .map(notificationMapper::toResponse)
                        .getContent()
        );
    }

    private LearningStatusStatsResponse getLearningStatusStats(UUID userId) {
        return new LearningStatusStatsResponse(
                learningProgressRepository.countByUserIdAndStatus(userId, LearningStatus.NEW),
                learningProgressRepository.countByUserIdAndStatus(userId, LearningStatus.LEARNING),
                learningProgressRepository.countByUserIdAndStatus(userId, LearningStatus.REVIEWING),
                learningProgressRepository.countByUserIdAndStatus(userId, LearningStatus.MASTERED)
        );
    }

    private UserEntity getCurrentUser() {
        String email = getAuthenticatedEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return authentication.getName();
    }
}
