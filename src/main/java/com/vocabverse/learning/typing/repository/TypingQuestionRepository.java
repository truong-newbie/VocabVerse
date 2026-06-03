package com.vocabverse.learning.typing.repository;

import com.vocabverse.learning.typing.entity.TypingQuestionEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypingQuestionRepository extends JpaRepository<TypingQuestionEntity, UUID> {

    List<TypingQuestionEntity> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);

    Optional<TypingQuestionEntity> findByIdAndSessionId(UUID id, UUID sessionId);
}
