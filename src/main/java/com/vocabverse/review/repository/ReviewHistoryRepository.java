package com.vocabverse.review.repository;

import com.vocabverse.review.entity.ReviewHistoryEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewHistoryRepository extends JpaRepository<ReviewHistoryEntity, UUID> {

    Page<ReviewHistoryEntity> findAllByUserIdOrderByReviewedAtDesc(UUID userId, Pageable pageable);
}
