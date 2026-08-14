package com.vocabverse.shadowing.repository;

import com.vocabverse.shadowing.entity.ShadowingLessonSubtitleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShadowingLessonSubtitleRepository extends JpaRepository<ShadowingLessonSubtitleEntity, UUID> {

    List<ShadowingLessonSubtitleEntity> findByLessonIdOrderByOrderIndexAscStartTimeMsAsc(UUID lessonId);

    Optional<ShadowingLessonSubtitleEntity> findByIdAndLessonId(UUID id, UUID lessonId);

    int countByLessonId(UUID lessonId);

    @Query("""
            select subtitle.lesson.id as lessonId, count(subtitle.id) as subtitleCount
            from ShadowingLessonSubtitleEntity subtitle
            where subtitle.lesson.id in :lessonIds
            group by subtitle.lesson.id
            """)
    List<LessonSubtitleCount> countByLessonIds(@Param("lessonIds") List<UUID> lessonIds);

    void deleteByLessonId(UUID lessonId);

    interface LessonSubtitleCount {
        UUID getLessonId();

        long getSubtitleCount();
    }
}
