package com.vocabverse.learning.typing.service;

import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.learning.typing.dto.request.CreateTypingSessionRequest;
import com.vocabverse.learning.typing.dto.request.SubmitTypingAnswerRequest;
import com.vocabverse.learning.typing.dto.response.TypingAnswerResponse;
import com.vocabverse.learning.typing.dto.response.TypingQuestionResponse;
import com.vocabverse.learning.typing.dto.response.TypingSessionResponse;
import com.vocabverse.learning.typing.entity.TypingQuestionEntity;
import com.vocabverse.learning.typing.entity.TypingSessionEntity;
import com.vocabverse.learning.typing.entity.TypingSessionSource;
import com.vocabverse.learning.typing.entity.TypingSessionStatus;
import com.vocabverse.learning.typing.mapper.TypingMapper;
import com.vocabverse.learning.typing.repository.TypingQuestionRepository;
import com.vocabverse.learning.typing.repository.TypingSessionRepository;
import com.vocabverse.review.dto.response.ReviewSubmitResponse;
import com.vocabverse.review.entity.ReviewResult;
import com.vocabverse.review.service.ReviewService;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TypingService {

    private final TypingSessionRepository typingSessionRepository;
    private final TypingQuestionRepository typingQuestionRepository;
    private final VocabularyRepository vocabularyRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final TypingMapper typingMapper;

    @Transactional
    public TypingSessionResponse createSession(CreateTypingSessionRequest request) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = resolveCollection(request, user.getId());
        List<VocabularyEntity> vocabularies = resolveSessionVocabularies(request, user.getId(), collection);

        TypingSessionEntity session = TypingSessionEntity.builder()
                .user(user)
                .source(request.source())
                .collection(collection)
                .status(TypingSessionStatus.IN_PROGRESS)
                .totalQuestions(vocabularies.size())
                .correctAnswers(0)
                .completedQuestions(0)
                .startedAt(LocalDateTime.now())
                .build();

        TypingSessionEntity savedSession = typingSessionRepository.save(session);
        TypingSessionEntity sessionForQuestions = savedSession;
        List<TypingQuestionEntity> questions = vocabularies.stream()
                .map(vocabulary -> TypingQuestionEntity.builder()
                        .session(sessionForQuestions)
                        .vocabulary(vocabulary)
                        .promptText(resolvePromptText(vocabulary))
                        .correctAnswer(vocabulary.getWord())
                        .build())
                .toList();
        typingQuestionRepository.saveAll(questions);

        if (vocabularies.isEmpty()) {
            savedSession.setStatus(TypingSessionStatus.COMPLETED);
            savedSession.setCompletedAt(LocalDateTime.now());
            savedSession = typingSessionRepository.save(savedSession);
        }

        return typingMapper.toSessionResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public TypingSessionResponse getSessionDetail(UUID sessionId) {
        return typingMapper.toSessionResponse(getOwnedSession(sessionId));
    }

    @Transactional(readOnly = true)
    public List<TypingQuestionResponse> getSessionQuestions(UUID sessionId) {
        TypingSessionEntity session = getOwnedSession(sessionId);
        return typingQuestionRepository.findAllBySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .map(typingMapper::toQuestionResponse)
                .toList();
    }

    @Transactional
    public TypingAnswerResponse submitAnswer(UUID sessionId, UUID questionId, SubmitTypingAnswerRequest request) {
        TypingSessionEntity session = getOwnedSession(sessionId);
        if (session.getStatus() == TypingSessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.TYPING_SESSION_COMPLETED);
        }

        TypingQuestionEntity question = typingQuestionRepository.findByIdAndSessionId(questionId, sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TYPING_QUESTION_NOT_FOUND));
        if (question.getAnsweredAt() != null) {
            throw new BusinessException(ErrorCode.TYPING_QUESTION_ALREADY_ANSWERED);
        }

        boolean correct = normalizeAnswer(request.answer()).equals(normalizeAnswer(question.getCorrectAnswer()));
        BigDecimal similarityScore = calculateSimilarityScore(request.answer(), question.getCorrectAnswer());
        ReviewSubmitResponse reviewResponse = reviewService.submitReview(
                question.getVocabulary().getId(),
                correct ? ReviewResult.GOOD : ReviewResult.AGAIN
        );

        question.setUserAnswer(request.answer());
        question.setCorrect(correct);
        question.setSimilarityScore(similarityScore);
        question.setAnsweredAt(LocalDateTime.now());
        typingQuestionRepository.save(question);

        session.setCompletedQuestions(session.getCompletedQuestions() + 1);
        if (correct) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        }
        if (session.getCompletedQuestions() >= session.getTotalQuestions()) {
            session.setStatus(TypingSessionStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
        }
        typingSessionRepository.save(session);

        return new TypingAnswerResponse(
                session.getId(),
                question.getId(),
                question.getVocabulary().getId(),
                correct,
                question.getCorrectAnswer(),
                similarityScore,
                reviewResponse.status(),
                reviewResponse.nextReviewAt(),
                reviewResponse.repetitionCount(),
                session.getCorrectAnswers(),
                session.getCompletedQuestions(),
                session.getTotalQuestions(),
                session.getStatus()
        );
    }

    private String resolvePromptText(VocabularyEntity vocabulary) {
        if (vocabulary.getMeaningVi() != null && !vocabulary.getMeaningVi().isBlank()) {
            return vocabulary.getMeaningVi();
        }
        if (vocabulary.getMeaningEn() != null && !vocabulary.getMeaningEn().isBlank()) {
            return vocabulary.getMeaningEn();
        }
        return "Type the vocabulary term";
    }

    private CollectionEntity resolveCollection(CreateTypingSessionRequest request, UUID userId) {
        if (request.source() != TypingSessionSource.COLLECTION) {
            return null;
        }
        if (request.collectionId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return collectionRepository.findByIdAndOwnerIdAndDeletedAtIsNull(request.collectionId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }

    private List<VocabularyEntity> resolveSessionVocabularies(
            CreateTypingSessionRequest request,
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

    private TypingSessionEntity getOwnedSession(UUID sessionId) {
        UUID userId = getCurrentUser().getId();
        return typingSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.TYPING_SESSION_NOT_FOUND));
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

    private String normalizeAnswer(String answer) {
        return answer == null ? "" : answer.trim().toLowerCase(Locale.ROOT);
    }

    private BigDecimal calculateSimilarityScore(String answer, String correctAnswer) {
        String normalizedAnswer = normalizeAnswer(answer);
        String normalizedCorrectAnswer = normalizeAnswer(correctAnswer);
        if (normalizedCorrectAnswer.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        int distance = levenshteinDistance(normalizedAnswer, normalizedCorrectAnswer);
        int maxLength = Math.max(normalizedAnswer.length(), normalizedCorrectAnswer.length());
        double score = Math.max(0.0, 1.0 - ((double) distance / maxLength)) * 100.0;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private int levenshteinDistance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];

        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(
                        Math.min(current[j - 1] + 1, previous[j] + 1),
                        previous[j - 1] + cost
                );
            }
            int[] temp = previous;
            previous = current;
            current = temp;
        }

        return previous[right.length()];
    }
}
