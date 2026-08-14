package com.vocabverse;

import com.vocabverse.auth.repository.RefreshTokenRepository;
import com.vocabverse.admin.repository.PublicCollectionModerationRepository;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.collection.repository.CollectionReviewSettingRepository;
import com.vocabverse.learning.flashcard.repository.FlashcardSessionItemRepository;
import com.vocabverse.learning.flashcard.repository.FlashcardSessionRepository;
import com.vocabverse.learning.progress.repository.LearningProgressRepository;
import com.vocabverse.learning.quiz.repository.QuizQuestionRepository;
import com.vocabverse.learning.quiz.repository.QuizSessionRepository;
import com.vocabverse.learning.typing.repository.TypingQuestionRepository;
import com.vocabverse.learning.typing.repository.TypingSessionRepository;
import com.vocabverse.notification.repository.NotificationRepository;
import com.vocabverse.roleplay.repository.RoleplayMessageRepository;
import com.vocabverse.roleplay.repository.RoleplayReportRepository;
import com.vocabverse.roleplay.repository.RoleplaySessionRepository;
import com.vocabverse.review.repository.ReviewHistoryRepository;
import com.vocabverse.shadowing.repository.ShadowingLessonRepository;
import com.vocabverse.shadowing.repository.ShadowingLessonSubtitleRepository;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import com.vocabverse.vocabulary.repository.VocabularyRepository;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration",
        "notification.email.listener.enabled=false"
})
class VocabVerseApplicationTests {

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    @MockBean
    private CollectionRepository collectionRepository;

    @MockBean
    private CollectionReviewSettingRepository collectionReviewSettingRepository;

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

    @MockBean
    private TypingSessionRepository typingSessionRepository;

    @MockBean
    private TypingQuestionRepository typingQuestionRepository;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private RoleplaySessionRepository roleplaySessionRepository;

    @MockBean
    private RoleplayMessageRepository roleplayMessageRepository;

    @MockBean
    private RoleplayReportRepository roleplayReportRepository;

    @MockBean
    private PublicCollectionModerationRepository publicCollectionModerationRepository;

    @MockBean
    private ShadowingLessonRepository shadowingLessonRepository;

    @MockBean
    private ShadowingLessonSubtitleRepository shadowingLessonSubtitleRepository;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private ConnectionFactory connectionFactory;

    @MockBean
    private TransactionTemplate transactionTemplate;

    @Test
    void contextLoads() {
    }
}
