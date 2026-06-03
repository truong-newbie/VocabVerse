package com.vocabverse.learning.flashcard.repository;

import com.vocabverse.learning.flashcard.entity.FlashcardSessionEntity;
import com.vocabverse.learning.flashcard.entity.FlashcardSessionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardSessionRepository extends JpaRepository<FlashcardSessionEntity, UUID> {

    Optional<FlashcardSessionEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, FlashcardSessionStatus status);
}
