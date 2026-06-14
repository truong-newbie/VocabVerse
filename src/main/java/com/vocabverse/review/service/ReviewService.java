package com.vocabverse.review.service;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import com.vocabverse.collection.repository.CollectionReviewSettingRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.review.dto.response.ReviewDuePageResponse;
import com.vocabverse.review.dto.response.ReviewDueItemResponse;
import com.vocabverse.review.dto.response.ReviewHistoryResponse;
import com.vocabverse.review.dto.response.ReviewHistoryPageResponse;
import com.vocabverse.review.dto.response.ReviewSubmitResponse;
import com.vocabverse.review.entity.ReviewHistoryEntity;
import com.vocabverse.review.entity.ReviewResult;
import com.vocabverse.review.repository.ReviewHistoryRepository;
import com.vocabverse.review.strategy.ReviewIntervalStrategy;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final BigDecimal DEFAULT_EASE_FACTOR = BigDecimal.valueOf(2.50);

    private final LearningProgressRepository learningProgressRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final VocabularyRepository vocabularyRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final CollectionReviewSettingRepository collectionReviewSettingRepository;
    private final UserRepository userRepository;
    private final ReviewIntervalStrategy reviewIntervalStrategy;

    @Transactional
    public ReviewSubmitResponse submitReview(UUID vocabularyId, ReviewResult result) {
        UserEntity user = getCurrentUser();
        LearningProgressEntity progress = initializeProgress(user, vocabularyId);
        LearningStatus previousStatus = progress.getStatus();
        LocalDateTime reviewedAt = LocalDateTime.now();

        updateProgress(progress, result, reviewedAt);
        learningProgressRepository.save(progress);
        createReviewHistory(progress, result, reviewedAt, previousStatus);

        return new ReviewSubmitResponse(
                progress.getStatus(),
                progress.getNextReviewAt(),
                progress.getRepetitionCount()
        );
    }

    @Transactional(readOnly = true)
    public ReviewDuePageResponse getTodayReviews(Pageable pageable) {
        UUID userId = getCurrentUser().getId();
        Page<LearningProgressEntity> duePage = learningProgressRepository
                .findAllByUserIdAndNextReviewAtLessThanEqual(userId, LocalDateTime.now(), pageable);

        return new ReviewDuePageResponse(
                duePage.map(progress -> toDueItemResponse(userId, progress)).getContent(),
                duePage.getNumber(),
                duePage.getSize(),
                duePage.getTotalElements(),
                duePage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public ReviewHistoryPageResponse getReviewHistory(Pageable pageable) {
        UUID userId = getCurrentUser().getId();
        Page<ReviewHistoryEntity> historyPage = reviewHistoryRepository
                .findAllByUserIdOrderByReviewedAtDesc(userId, pageable);

        return new ReviewHistoryPageResponse(
                historyPage.map(this::toHistoryResponse).getContent(),
                historyPage.getNumber(),
                historyPage.getSize(),
                historyPage.getTotalElements(),
                historyPage.getTotalPages()
        );
    }

    private LearningProgressEntity initializeProgress(UserEntity user, UUID vocabularyId) {
        VocabularyEntity vocabulary = vocabularyRepository
                .findByIdAndOwnerIdAndDeletedAtIsNull(vocabularyId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VOCABULARY_NOT_FOUND));

        return learningProgressRepository
                .findByUserIdAndVocabularyId(user.getId(), vocabularyId)
                .orElseGet(() -> learningProgressRepository.save(LearningProgressEntity.builder()
                        .user(user)
                        .vocabulary(vocabulary)
                        .status(LearningStatus.NEW)
                        .repetitionCount(0)
                        .easeFactor(DEFAULT_EASE_FACTOR)
                        .build()));
    }

    private void updateProgress(LearningProgressEntity progress, ReviewResult result, LocalDateTime reviewedAt) {
        if (result == ReviewResult.AGAIN) {
            progress.setRepetitionCount(0);
            progress.setStatus(LearningStatus.LEARNING);
        } else {
            progress.setRepetitionCount(progress.getRepetitionCount() + 1);
            progress.setStatus(updateLearningStatus(progress, result));
        }

        progress.setLastReviewedAt(reviewedAt);
        progress.setNextReviewAt(calculateNextReviewDate(progress, result, reviewedAt));
    }

    private LocalDateTime calculateNextReviewDate(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt
    ) {
        List<UUID> collectionIds = collectionVocabularyRepository.findOwnedCollectionIdsByVocabularyId(
                progress.getUser().getId(),
                progress.getVocabulary().getId()
        );
        if (!collectionIds.isEmpty()) {
            return collectionReviewSettingRepository
                    .findFirstByUserIdAndCollectionIdInAndEnabledTrue(progress.getUser().getId(), collectionIds)
                    .map(setting -> reviewedAt.plusDays(resolveCollectionIntervalDays(setting, progress, result)))
                    .orElseGet(() -> reviewIntervalStrategy.calculateNextReviewDate(progress, result, reviewedAt));
        }
        return reviewIntervalStrategy.calculateNextReviewDate(progress, result, reviewedAt);
    }

    private int resolveCollectionIntervalDays(
            CollectionReviewSettingEntity setting,
            LearningProgressEntity progress,
            ReviewResult result
    ) {
        List<Integer> intervals = setting.getIntervalsJson();
        if (intervals == null || intervals.isEmpty()) {
            return 1;
        }
        if (result == ReviewResult.AGAIN) {
            return intervals.get(0);
        }
        int index = Math.max(0, progress.getRepetitionCount() - 1);
        return intervals.get(Math.min(index, intervals.size() - 1));
    }

    private LearningStatus updateLearningStatus(LearningProgressEntity progress, ReviewResult result) {
        if (result == ReviewResult.EASY && progress.getRepetitionCount() >= 5) {
            return LearningStatus.MASTERED;
        }
        if (progress.getRepetitionCount() >= 2) {
            return LearningStatus.REVIEWING;
        }
        return LearningStatus.LEARNING;
    }

    private void createReviewHistory(
            LearningProgressEntity progress,
            ReviewResult result,
            LocalDateTime reviewedAt,
            LearningStatus previousStatus
    ) {
        ReviewHistoryEntity history = ReviewHistoryEntity.builder()
                .user(progress.getUser())
                .vocabulary(progress.getVocabulary())
                .result(result)
                .reviewedAt(reviewedAt)
                .nextReviewAt(progress.getNextReviewAt())
                .previousStatus(previousStatus)
                .newStatus(progress.getStatus())
                .build();

        reviewHistoryRepository.save(history);
    }

    private ReviewDueItemResponse toDueItemResponse(UUID userId, LearningProgressEntity progress) {
        VocabularyEntity vocabulary = progress.getVocabulary();
        return new ReviewDueItemResponse(
                vocabulary.getId(),
                vocabulary.getWord(),
                vocabulary.getMeaningEn(),
                vocabulary.getMeaningVi(),
                vocabulary.getPartOfSpeech(),
                firstExampleSentence(vocabulary),
                progress.getStatus(),
                progress.getNextReviewAt(),
                progress.getRepetitionCount(),
                firstCollectionName(userId, vocabulary.getId())
        );
    }

    private ReviewHistoryResponse toHistoryResponse(ReviewHistoryEntity history) {
        VocabularyEntity vocabulary = history.getVocabulary();
        return new ReviewHistoryResponse(
                vocabulary.getId(),
                vocabulary.getWord(),
                history.getResult(),
                history.getReviewedAt(),
                history.getNextReviewAt(),
                history.getPreviousStatus(),
                history.getNewStatus()
        );
    }

    private String firstExampleSentence(VocabularyEntity vocabulary) {
        if (vocabulary.getExamples() == null || vocabulary.getExamples().isEmpty()) {
            return null;
        }
        return vocabulary.getExamples().get(0).getEn();
    }

    private String firstCollectionName(UUID userId, UUID vocabularyId) {
        List<String> titles = collectionVocabularyRepository.findOwnedCollectionTitlesByVocabularyId(userId, vocabularyId);
        return titles.isEmpty() ? null : titles.get(0);
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
