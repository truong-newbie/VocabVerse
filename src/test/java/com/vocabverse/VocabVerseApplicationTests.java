package com.vocabverse;

import com.vocabverse.auth.repository.RefreshTokenRepository;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.learning.flashcard.repository.FlashcardSessionItemRepository;
import com.vocabverse.learning.flashcard.repository.FlashcardSessionRepository;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.learning.quiz.repository.QuizQuestionRepository;
import com.vocabverse.learning.quiz.repository.QuizSessionRepository;
import com.vocabverse.review.repository.ReviewHistoryRepository;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
})
class VocabVerseApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private CollectionRepository collectionRepository;

    @MockBean
    private VocabularyRepository vocabularyRepository;

    @MockBean
    private CollectionVocabularyRepository collectionVocabularyRepository;

    @MockBean
    private LearningProgressRepository learningProgressRepository;

    @MockBean
    private ReviewHistoryRepository reviewHistoryRepository;

    @MockBean
    private FlashcardSessionRepository flashcardSessionRepository;

    @MockBean
    private FlashcardSessionItemRepository flashcardSessionItemRepository;

    @MockBean
    private QuizSessionRepository quizSessionRepository;

    @MockBean
    private QuizQuestionRepository quizQuestionRepository;

    @Test
    void contextLoads() {
    }
}
