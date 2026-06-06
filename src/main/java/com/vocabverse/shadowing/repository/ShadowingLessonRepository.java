package com.vocabverse.shadowing.repository;

import com.vocabverse.shadowing.entity.ShadowingLessonEntity;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShadowingLessonRepository extends JpaRepository<ShadowingLessonEntity, UUID> {

    Page<ShadowingLessonEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
