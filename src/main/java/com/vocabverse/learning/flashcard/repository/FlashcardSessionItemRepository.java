package com.vocabverse.learning.flashcard.repository;

import com.vocabverse.learning.flashcard.entity.FlashcardSessionItemEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlashcardSessionItemRepository extends JpaRepository<FlashcardSessionItemEntity, UUID> {

    List<FlashcardSessionItemEntity> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    Optional<FlashcardSessionItemEntity> findBySessionIdAndVocabularyId(UUID sessionId, UUID vocabularyId);
}
