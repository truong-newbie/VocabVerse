package com.vocabverse.learning.progress.repository;

import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.learning.progress.entity.LearningStatus;
import com.vocabverse.user.entity.UserEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LearningProgressRepository extends JpaRepository<LearningProgressEntity, UUID> {

    Page<LearningProgressEntity> findAllByUserId(UUID userId, Pageable pageable);

    Optional<LearningProgressEntity> findByUserIdAndVocabularyId(UUID userId, UUID vocabularyId);

    List<LearningProgressEntity> findAllByUserIdAndVocabularyIdIn(UUID userId, List<UUID> vocabularyIds);

    @Query("""
            select count(lp)
            from LearningProgressEntity lp
            where lp.user.id = :userId
              and lp.status = :status
              and lp.vocabulary.deletedAt is null
            """)
    long countByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") LearningStatus status);

    @Query("""
            select lp
            from LearningProgressEntity lp
            where lp.user.id = :userId
              and lp.nextReviewAt is not null
              and lp.nextReviewAt <= :now
              and lp.vocabulary.deletedAt is null
            """)
    Page<LearningProgressEntity> findAllByUserIdAndNextReviewAtLessThanEqual(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now,
            Pageable pageable
    );

    @Query("""
            select lp
            from LearningProgressEntity lp
            where lp.user.id = :userId
              and lp.nextReviewAt is not null
              and lp.nextReviewAt <= :now
              and lp.vocabulary.deletedAt is null
            """)
    List<LearningProgressEntity> findAllByUserIdAndNextReviewAtLessThanEqual(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now
    );

    @Query("""
            select distinct lp.user
            from LearningProgressEntity lp
            where lp.nextReviewAt is not null
              and lp.nextReviewAt <= :now
              and lp.vocabulary.deletedAt is null
            """)
    List<UserEntity> findDistinctUsersWithDueReviews(@Param("now") LocalDateTime now);

    @Query("""
            select count(lp)
            from LearningProgressEntity lp
            where lp.user.id = :userId
              and lp.nextReviewAt is not null
              and lp.nextReviewAt <= :now
              and lp.vocabulary.deletedAt is null
            """)
    long countDueReviewsByUserId(@Param("userId") UUID userId, @Param("now") LocalDateTime now);

    @Query("""
            select count(lp)
            from LearningProgressEntity lp
            where lp.user.id = :userId
              and lp.nextReviewAt is not null
              and lp.nextReviewAt <= :endOfDay
              and lp.vocabulary.deletedAt is null
            """)
    long countDueReviewsByUserIdUntil(
            @Param("userId") UUID userId,
            @Param("endOfDay") LocalDateTime endOfDay
    );
}
