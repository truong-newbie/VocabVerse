package com.vocabverse.learning.progress.service;

import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.learning.progress.dto.LearningProgressPageResponse;
import com.vocabverse.learning.progress.dto.LearningProgressResponse;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.learning.progress.mapper.LearningProgressMapper;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.math.BigDecimal;
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
public class LearningProgressService {

    public static final List<Integer> DEFAULT_REVIEW_INTERVAL_DAYS = List.of(1, 3, 7, 14, 30);
    private static final BigDecimal DEFAULT_EASE_FACTOR = BigDecimal.valueOf(2.50);

    private final LearningProgressRepository learningProgressRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserRepository userRepository;
    private final LearningProgressMapper learningProgressMapper;

    @Transactional(readOnly = true)
    public LearningProgressPageResponse getCurrentUserProgress(Pageable pageable) {
        UUID userId = getCurrentUser().getId();
        Page<LearningProgressResponse> progressPage = learningProgressRepository
                .findAllByUserId(userId, pageable)
                .map(learningProgressMapper::toResponse);

        return new LearningProgressPageResponse(
                progressPage.getContent(),
                progressPage.getNumber(),
                progressPage.getSize(),
                progressPage.getTotalElements(),
                progressPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public LearningProgressResponse getProgressDetail(UUID vocabularyId) {
        UUID userId = getCurrentUser().getId();
        LearningProgressEntity progress = learningProgressRepository
                .findByUserIdAndVocabularyId(userId, vocabularyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.LEARNING_PROGRESS_NOT_FOUND));

        return learningProgressMapper.toResponse(progress);
    }

    @Transactional
    public LearningProgressResponse initializeProgress(UUID vocabularyId) {
        UserEntity user = getCurrentUser();
        VocabularyEntity vocabulary = vocabularyRepository
                .findByIdAndOwnerIdAndDeletedAtIsNull(vocabularyId, user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.VOCABULARY_NOT_FOUND));

        LearningProgressEntity progress = learningProgressRepository
                .findByUserIdAndVocabularyId(user.getId(), vocabularyId)
                .orElseGet(() -> createInitialProgress(user, vocabulary));

        return learningProgressMapper.toResponse(progress);
    }

    private LearningProgressEntity createInitialProgress(UserEntity user, VocabularyEntity vocabulary) {
        LearningProgressEntity progress = LearningProgressEntity.builder()
                .user(user)
                .vocabulary(vocabulary)
                .status(LearningStatus.NEW)
                .repetitionCount(0)
                .easeFactor(DEFAULT_EASE_FACTOR)
                .build();

        return learningProgressRepository.save(progress);
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
