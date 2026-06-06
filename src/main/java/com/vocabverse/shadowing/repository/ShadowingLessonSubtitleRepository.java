package com.vocabverse.shadowing.repository;

import com.vocabverse.shadowing.entity.ShadowingLessonSubtitleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShadowingLessonSubtitleRepository extends JpaRepository<ShadowingLessonSubtitleEntity, UUID> {

    List<ShadowingLessonSubtitleEntity> findByLessonIdOrderByOrderIndexAscStartTimeMsAsc(UUID lessonId);

    Optional<ShadowingLessonSubtitleEntity> findByIdAndLessonId(UUID id, UUID lessonId);

    int countByLessonId(UUID lessonId);

    void deleteByLessonId(UUID lessonId);
}
