package com.vocabverse.review.repository;

import com.vocabverse.review.entity.ReviewHistoryEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistoryEntity, UUID> {

    Page<ReviewHistoryEntity> findAllByUserIdOrderByReviewedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndReviewedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);

    @Query("""
            select rh.reviewedAt
            from ReviewHistoryEntity rh
            where rh.user.id = :userId
            order by rh.reviewedAt desc
            """)
    List<LocalDateTime> findReviewedAtByUserIdOrderByReviewedAtDesc(@Param("userId") UUID userId);
}
