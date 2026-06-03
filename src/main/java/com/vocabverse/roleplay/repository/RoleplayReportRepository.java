package com.vocabverse.roleplay.repository;

import com.vocabverse.roleplay.entity.RoleplayReportEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleplayReportRepository extends JpaRepository<RoleplayReportEntity, UUID> {

    Optional<RoleplayReportEntity> findBySessionId(UUID sessionId);
}
