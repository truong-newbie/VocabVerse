package com.vocabverse.learning.quiz.repository;

import com.vocabverse.learning.quiz.entity.QuizSessionEntity;
import com.vocabverse.learning.quiz.entity.QuizSessionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSessionRepository extends JpaRepository<QuizSessionEntity, UUID> {

    Optional<QuizSessionEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, QuizSessionStatus status);
}
