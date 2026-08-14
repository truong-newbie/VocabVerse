package com.vocabverse.roleplay.repository;

import com.vocabverse.roleplay.entity.RoleplaySessionEntity;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleplaySessionRepository extends JpaRepository<RoleplaySessionEntity, UUID> {

    Optional<RoleplaySessionEntity> findByIdAndUserId(UUID id, UUID userId);

    Page<RoleplaySessionEntity> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndCreatedAtGreaterThanEqual(UUID userId, LocalDateTime from);
}
