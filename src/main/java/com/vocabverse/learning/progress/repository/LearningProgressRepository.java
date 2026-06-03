package com.vocabverse.learning.progress.repository;

import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningProgressRepository extends JpaRepository<LearningProgressEntity, UUID> {

    Page<LearningProgressEntity> findAllByUserId(UUID userId, Pageable pageable);

    Optional<LearningProgressEntity> findByUserIdAndVocabularyId(UUID userId, UUID vocabularyId);

    Page<LearningProgressEntity> findAllByUserIdAndNextReviewAtLessThanEqual(
            UUID userId,
            LocalDateTime now,
            Pageable pageable
    );

    List<LearningProgressEntity> findAllByUserIdAndNextReviewAtLessThanEqual(UUID userId, LocalDateTime now);
}
