package com.vocabverse.roleplay.repository;

import com.vocabverse.roleplay.entity.RoleplayMessageEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleplayMessageRepository extends JpaRepository<RoleplayMessageEntity, UUID> {

    List<RoleplayMessageEntity> findAllBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
