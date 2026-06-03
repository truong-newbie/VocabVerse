package com.vocabverse.learning.quiz.repository;

import com.vocabverse.learning.quiz.entity.QuizQuestionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestionEntity, UUID> {

    List<QuizQuestionEntity> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    Optional<QuizQuestionEntity> findByIdAndSessionId(UUID id, UUID sessionId);
}
