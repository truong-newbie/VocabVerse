package com.vocabverse.learning.quiz.service;

import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.learning.quiz.dto.request.CreateQuizSessionRequest;
import com.vocabverse.learning.quiz.dto.request.SubmitQuizAnswerRequest;
import com.vocabverse.learning.quiz.dto.response.QuizAnswerResponse;
import com.vocabverse.learning.quiz.dto.response.QuizQuestionResponse;
import com.vocabverse.learning.quiz.dto.response.QuizSessionResponse;
import com.vocabverse.learning.quiz.entity.QuizQuestionEntity;
import com.vocabverse.learning.quiz.entity.QuizQuestionType;
import com.vocabverse.learning.quiz.entity.QuizSessionEntity;
import com.vocabverse.learning.quiz.entity.QuizSessionSource;
import com.vocabverse.learning.quiz.entity.QuizSessionStatus;
import com.vocabverse.learning.quiz.mapper.QuizMapper;
import com.vocabverse.learning.quiz.repository.QuizQuestionRepository;
import com.vocabverse.learning.quiz.repository.QuizSessionRepository;
import com.vocabverse.review.dto.response.ReviewSubmitResponse;
import com.vocabverse.review.entity.ReviewResult;
import com.vocabverse.review.service.ReviewService;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
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
public class QuizService {

    private final QuizSessionRepository quizSessionRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final VocabularyRepository vocabularyRepository;
    private final CollectionRepository collectionRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final LearningProgressRepository learningProgressRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final QuizMapper quizMapper;

    @Transactional
    public QuizSessionResponse createSession(CreateQuizSessionRequest request) {
        UserEntity user = getCurrentUser();
        CollectionEntity collection = resolveCollection(request, user.getId());
        List<VocabularyEntity> vocabularies = resolveSessionVocabularies(request, user.getId(), collection);
        List<VocabularyEntity> optionPool = vocabularyRepository.findAllByOwnerIdAndDeletedAtIsNull(user.getId());

        QuizSessionEntity session = QuizSessionEntity.builder()
                .user(user)
                .source(request.source())
                .collection(collection)
                .status(QuizSessionStatus.IN_PROGRESS)
                .totalQuestions(vocabularies.size())
                .correctAnswers(0)
                .completedQuestions(0)
                .startedAt(LocalDateTime.now())
                .build();

        QuizSessionEntity savedSession = quizSessionRepository.save(session);
        QuizSessionEntity sessionForQuestions = savedSession;
        List<QuizQuestionEntity> questions = vocabularies.stream()
                .map(vocabulary -> buildQuestion(sessionForQuestions, vocabulary, optionPool))
                .toList();
        quizQuestionRepository.saveAll(questions);

        if (vocabularies.isEmpty()) {
            savedSession.setStatus(QuizSessionStatus.COMPLETED);
            savedSession.setCompletedAt(LocalDateTime.now());
            savedSession = quizSessionRepository.save(savedSession);
        }

        return quizMapper.toSessionResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public QuizSessionResponse getSessionDetail(UUID sessionId) {
        return quizMapper.toSessionResponse(getOwnedSession(sessionId));
    }

    @Transactional(readOnly = true)
    public List<QuizQuestionResponse> getSessionQuestions(UUID sessionId) {
        QuizSessionEntity session = getOwnedSession(sessionId);
        return quizQuestionRepository.findAllBySessionIdOrderByCreatedAtAsc(session.getId())
                .stream()
                .map(quizMapper::toQuestionResponse)
                .toList();
    }

    @Transactional
    public QuizAnswerResponse submitAnswer(UUID sessionId, UUID questionId, SubmitQuizAnswerRequest request) {
        QuizSessionEntity session = getOwnedSession(sessionId);
        if (session.getStatus() == QuizSessionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.QUIZ_SESSION_COMPLETED);
        }

        QuizQuestionEntity question = quizQuestionRepository.findByIdAndSessionId(questionId, sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_QUESTION_NOT_FOUND));
        if (question.getAnsweredAt() != null) {
            throw new BusinessException(ErrorCode.QUIZ_QUESTION_ALREADY_ANSWERED);
        }

        boolean correct = normalizeAnswer(request.answer()).equals(normalizeAnswer(question.getCorrectAnswer()));
        ReviewSubmitResponse reviewResponse = reviewService.submitReview(
                question.getVocabulary().getId(),
                correct ? ReviewResult.GOOD : ReviewResult.AGAIN
        );

        question.setUserAnswer(request.answer());
        question.setCorrect(correct);
        question.setAnsweredAt(LocalDateTime.now());
        quizQuestionRepository.save(question);

        session.setCompletedQuestions(session.getCompletedQuestions() + 1);
        if (correct) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
        }
        if (session.getCompletedQuestions() >= session.getTotalQuestions()) {
            session.setStatus(QuizSessionStatus.COMPLETED);
            session.setCompletedAt(LocalDateTime.now());
        }
        quizSessionRepository.save(session);

        return new QuizAnswerResponse(
                session.getId(),
                question.getId(),
                question.getVocabulary().getId(),
                correct,
                question.getCorrectAnswer(),
                reviewResponse.status(),
                reviewResponse.nextReviewAt(),
                reviewResponse.repetitionCount(),
                session.getCorrectAnswers(),
                session.getCompletedQuestions(),
                session.getTotalQuestions(),
                session.getStatus()
        );
    }

    private QuizQuestionEntity buildQuestion(
            QuizSessionEntity session,
            VocabularyEntity vocabulary,
            List<VocabularyEntity> optionPool
    ) {
        QuizQuestionType type = Math.abs(vocabulary.getId().hashCode()) % 2 == 0
                ? QuizQuestionType.TERM_TO_MEANING
                : QuizQuestionType.MEANING_TO_TERM;
        String correctAnswer = resolveCorrectAnswer(vocabulary, type);

        return QuizQuestionEntity.builder()
                .session(session)
                .vocabulary(vocabulary)
                .questionType(type)
                .questionText(resolveQuestionText(vocabulary, type))
                .correctAnswer(correctAnswer)
                .options(buildOptions(vocabulary, type, optionPool))
                .build();
    }

    private List<String> buildOptions(
            VocabularyEntity vocabulary,
            QuizQuestionType type,
            List<VocabularyEntity> optionPool
    ) {
        LinkedHashSet<String> options = new LinkedHashSet<>();
        options.add(resolveCorrectAnswer(vocabulary, type));

        optionPool.stream()
                .filter(candidate -> !candidate.getId().equals(vocabulary.getId()))
                .sorted(Comparator.comparing(VocabularyEntity::getWord))
                .map(candidate -> resolveCorrectAnswer(candidate, type))
                .filter(option -> option != null && !option.isBlank())
                .forEach(option -> {
                    if (options.size() < 4) {
                        options.add(option);
                    }
                });

        return new ArrayList<>(options);
    }

    private String resolveQuestionText(VocabularyEntity vocabulary, QuizQuestionType type) {
        return switch (type) {
            case TERM_TO_MEANING -> "What does \"" + vocabulary.getWord() + "\" mean?";
            case MEANING_TO_TERM -> "Which term matches this meaning: " + safeMeaning(vocabulary) + "?";
        };
    }

    private String resolveCorrectAnswer(VocabularyEntity vocabulary, QuizQuestionType type) {
        return switch (type) {
            case TERM_TO_MEANING -> safeMeaning(vocabulary);
            case MEANING_TO_TERM -> vocabulary.getWord();
        };
    }

    private String safeMeaning(VocabularyEntity vocabulary) {
        if (vocabulary.getMeaningVi() != null && !vocabulary.getMeaningVi().isBlank()) {
            return vocabulary.getMeaningVi();
        }
        return vocabulary.getMeaningEn();
    }

    private CollectionEntity resolveCollection(CreateQuizSessionRequest request, UUID userId) {
        if (request.source() != QuizSessionSource.COLLECTION) {
            return null;
        }
        if (request.collectionId() == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return collectionRepository.findByIdAndOwnerIdAndDeletedAtIsNull(request.collectionId(), userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }

    private List<VocabularyEntity> resolveSessionVocabularies(
            CreateQuizSessionRequest request,
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

    private QuizSessionEntity getOwnedSession(UUID sessionId) {
        UUID userId = getCurrentUser().getId();
        return quizSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_SESSION_NOT_FOUND));
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
}
