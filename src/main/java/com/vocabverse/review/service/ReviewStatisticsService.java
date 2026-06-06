package com.vocabverse.review.service;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.collection.repository.CollectionReviewSettingRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.review.dto.response.ReviewStatisticsResponse;
import com.vocabverse.review.repository.ReviewHistoryRepository;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewStatisticsService {

    private final LearningProgressRepository learningProgressRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final VocabularyRepository vocabularyRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final CollectionReviewSettingRepository collectionReviewSettingRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ReviewStatisticsResponse getCurrentUserStats() {
        UserEntity user = getCurrentUser();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay().minusNanos(1);

        return new ReviewStatisticsResponse(
                countEnabledDueWords(user.getId(), endOfToday),
                reviewHistoryRepository.countByUserIdAndReviewedAtBetween(user.getId(), startOfToday, endOfToday),
                calculateCurrentStreak(user.getId(), today),
                vocabularyRepository.countByOwnerIdAndDeletedAtIsNull(user.getId())
        );
    }

    private long countEnabledDueWords(UUID userId, LocalDateTime endOfToday) {
        return learningProgressRepository.findAllByUserIdAndNextReviewAtLessThanEqual(userId, endOfToday)
                .stream()
                .filter(this::isReviewEnabled)
                .count();
    }

    private boolean isReviewEnabled(LearningProgressEntity progress) {
        List<UUID> collectionIds = collectionVocabularyRepository.findOwnedCollectionIdsByVocabularyId(
                progress.getUser().getId(),
                progress.getVocabulary().getId()
        );
        if (collectionIds.isEmpty()) {
            return true;
        }
        List<CollectionReviewSettingEntity> settings = collectionReviewSettingRepository.findAllByUserIdAndCollectionIdIn(
                progress.getUser().getId(),
                collectionIds
        );
        return settings.isEmpty() || settings.stream().anyMatch(CollectionReviewSettingEntity::isEnabled);
    }

    private long calculateCurrentStreak(java.util.UUID userId, LocalDate today) {
        Set<LocalDate> reviewDates = new HashSet<>(reviewHistoryRepository
                .findReviewedAtByUserIdOrderByReviewedAtDesc(userId)
                .stream()
                .map(LocalDateTime::toLocalDate)
                .toList());
        LocalDate cursor = reviewDates.contains(today) ? today : today.minusDays(1);
        long streak = 0;
        while (reviewDates.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
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
