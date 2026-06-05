package com.vocabverse.review.repository;

import com.vocabverse.review.entity.ReviewHistoryEntity;
import java.time.LocalDate;
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

    @Query(
            value = """
                    select distinct cast(reviewed_at as date)
                    from review_history
                    where user_id = :userId
                    order by cast(reviewed_at as date) desc
                    """,
            nativeQuery = true
    )
    List<LocalDate> findDistinctReviewDatesByUserId(@Param("userId") UUID userId);
}
