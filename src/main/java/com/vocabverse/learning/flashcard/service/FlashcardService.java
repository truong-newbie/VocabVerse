package com.vocabverse.learning.flashcard.service;

import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.learning.flashcard.dto.request.CreateFlashcardSessionRequest;
import com.vocabverse.learning.flashcard.dto.request.SubmitFlashcardAnswerRequest;
import com.vocabverse.learning.flashcard.dto.response.FlashcardAnswerResponse;
import com.vocabverse.learning.flashcard.dto.response.FlashcardCardResponse;
import com.vocabverse.learning.flashcard.dto.response.FlashcardSessionResponse;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionEntity;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionItemEntity;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionSource;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionStatus;
import com.vocabverse.learning.flashcard.mapper.FlashcardMapper;
import com.vocabverse.learning.flashcard.repository.FlashcardSessionItemRepository;
import com.vocabverse.learning.flashcard.repository.FlashcardSessionRepository;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.review.dto.response.ReviewSubmitResponse;
import com.vocabverse.review.service.ReviewService;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final FlashcardSessionRepository flashcardSessionRepository;
    private final FlashcardSessionItemRepository flashcardSessionItemRepository;
    private final VocabularyRepository vocabularyRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final FlashcardMapper flashcardMapper;

    @Transactional
    public FlashcardSessionResponse createSession(CreateFlashcardSessionRequest request) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = resolveCollection(request, user.getId());
        List<VocabularyEntity> vocabularies = resolveSessionVocabularies(request, user.getId(), collection);

        FlashcardSessionEntity session = FlashcardSessionEntity.builder()
                .user(user)
                .source(request.source())
                .collection(collection)
                .status(FlashcardSessionStatus.IN_PROGRESS)
                .totalCards(vocabularies.size())
                .completedCards(0)
                .startedAt(LocalDateTime.now())
                .build();

        FlashcardSessionEntity savedSession = flashcardSessionRepository.save(session);
        FlashcardSessionEntity sessionForItems = savedSession;
        List<FlashcardSessionItemEntity> items = vocabularies.stream()
                .map(vocabulary -> FlashcardSessionItemEntity.builder()
                        .session(sessionForItems)
                        .vocabulary(vocabulary)
                        .build())
                .toList();
        flashcardSessionItemRepository.saveAll(items);

        if (vocabularies.isEmpty()) {
            savedSession.setStatus(FlashcardSessionStatus.COMPLETED);
            savedSession.setCompletedAt(LocalDateTime.now());
            savedSession = flashcardSessionRepository.save(savedSession);
        }

        return flashcardMapper.toSessionResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public FlashcardSessionResponse getSessionDetail(UUID sessionId) {
        FlashcardSessionEntity session = getOwnedSession(sessionId);
        return flashcardMapper.toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public List<FlashcardCardResponse> getSessionCards(UUID sessionId) {
        FlashcardSessionEntity session = getOwnedSession(sessionId);
        return flashcardSessionItemRepository.findAllBySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .map(flashcardMapper::toCardResponse)
                .toList();
    }

    @Transactional
    public FlashcardAnswerResponse submitAnswer(
            UUID sessionId,
            UUID vocabularyId,
            SubmitFlashcardAnswerRequest request
    ) {
        FlashcardSessionEntity session = getOwnedSession(sessionId);
        if (session.getStatus() == FlashcardSessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.FLASHCARD_SESSION_COMPLETED);
        }

        FlashcardSessionItemEntity item = flashcardSessionItemRepository
                .findBySessionIdAndVocabularyId(sessionId, vocabularyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLASHCARD_CARD_NOT_FOUND));

        if (item.getAnsweredAt() != null) {
            throw new BusinessException(ErrorCode.FLASHCARD_CARD_ALREADY_ANSWERED);
        }

        ReviewSubmitResponse reviewResponse = reviewService.submitReview(vocabularyId, request.result());
        item.setResult(request.result());
        item.setAnsweredAt(LocalDateTime.now());
        flashcardSessionItemRepository.save(item);

        session.setCompletedCards(session.getCompletedCards() + 1);
        if (session.getCompletedCards() >= session.getTotalCards()) {
            session.setStatus(FlashcardSessionStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
        }
        flashcardSessionRepository.save(session);

        return new FlashcardAnswerResponse(
                session.getId(),
                vocabularyId,
                reviewResponse.status(),
                reviewResponse.nextReviewAt(),
                reviewResponse.repetitionCount(),
                session.getCompletedCards(),
                session.getTotalCards(),
                session.getStatus()
        );
    }

    private CollectionEntity resolveCollection(CreateFlashcardSessionRequest request, UUID userId) {
        if (request.source() != FlashcardSessionSource.COLLECTION) {
            return null;
        }
        if (request.collectionId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return collectionRepository.findByIdAndOwnerIdAndDeletedAtIsNull(request.collectionId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }

    private List<VocabularyEntity> resolveSessionVocabularies(
            CreateFlashcardSessionRequest request,
            UUID userId,
            CollectionEntity collection
    ) {
        return switch (request.source()) {
            case ALL -> vocabularyRepository.findAllByOwnerIdAndDeletedAtIsNull(userId);
            case COLLECTION -> collectionVocabularyRepository
                    .findAllByCollectionIdAndVocabularyDeletedAtIsNull(collection.getId())
                    .stream()
                    .map(CollectionVocabularyEntity::getVocabulary)
                    .toList();
            case REVIEW_DUE -> learningProgressRepository
                    .findAllByUserIdAndNextReviewAtLessThanEqual(userId, LocalDateTime.now())
                    .stream()
                    .map(LearningProgressEntity::getVocabulary)
                    .filter(vocabulary -> vocabulary.getDeletedAt() == null)
                    .toList();
        };
    }

    private FlashcardSessionEntity getOwnedSession(UUID sessionId) {
        UUID userId = getCurrentUser().getId();
        return flashcardSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLASHCARD_SESSION_NOT_FOUND));
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
