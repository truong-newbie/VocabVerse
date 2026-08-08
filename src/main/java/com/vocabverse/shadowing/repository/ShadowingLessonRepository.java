package com.vocabverse.shadowing.repository;

import com.vocabverse.shadowing.entity.ShadowingLessonEntity;
import com.vocabverse.shadowing.entity.ShadowingLessonSource;
import com.vocabverse.shadowing.entity.ShadowingLessonStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShadowingLessonRepository extends JpaRepository<ShadowingLessonEntity, UUID> {

    Page<ShadowingLessonEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<ShadowingLessonEntity> findBySourceOrderByCreatedAtDesc(ShadowingLessonSource source, Pageable pageable);

    Page<ShadowingLessonEntity> findBySourceAndStatusOrderByCreatedAtDesc(
            ShadowingLessonSource source,
            ShadowingLessonStatus status,
            Pageable pageable
    );

    Page<ShadowingLessonEntity> findBySourceInAndStatusOrderByCreatedAtDesc(
            List<ShadowingLessonSource> sources,
            ShadowingLessonStatus status,
            Pageable pageable
    );

    Page<ShadowingLessonEntity> findBySourceInOrderByCreatedAtDesc(
            List<ShadowingLessonSource> sources,
            Pageable pageable
    );

    Optional<ShadowingLessonEntity> findByIdAndDeletedAtIsNull(UUID id);

    Page<ShadowingLessonEntity> findBySourceInAndDeletedAtIsNullOrderByCreatedAtDesc(
            List<ShadowingLessonSource> sources,
            Pageable pageable
    );

    Page<ShadowingLessonEntity> findBySourceInAndStatusAndDeletedAtIsNullOrderByCreatedAtDesc(
            List<ShadowingLessonSource> sources,
            ShadowingLessonStatus status,
            Pageable pageable
    );

    long countByDeletedAtIsNull();
}
