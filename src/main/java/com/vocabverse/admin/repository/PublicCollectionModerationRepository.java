package com.vocabverse.admin.repository;

import com.vocabverse.admin.entity.PublicCollectionModerationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublicCollectionModerationRepository extends JpaRepository<PublicCollectionModerationEntity, UUID> {
}
