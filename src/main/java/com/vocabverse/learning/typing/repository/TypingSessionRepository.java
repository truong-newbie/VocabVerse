package com.vocabverse.learning.typing.repository;

import com.vocabverse.learning.typing.entity.TypingSessionEntity;
import com.vocabverse.learning.typing.entity.TypingSessionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypingSessionRepository extends JpaRepository<TypingSessionEntity, UUID> {

    Optional<TypingSessionEntity> findByIdAndUserId(UUID id, UUID userId);

    long countByUserIdAndStatus(UUID userId, TypingSessionStatus status);
}
