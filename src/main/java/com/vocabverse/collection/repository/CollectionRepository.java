package com.vocabverse.collection.repository;

import com.vocabverse.collection.entity.CollectionEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<CollectionEntity, UUID> {

    Optional<CollectionEntity> findByIdAndOwnerIdAndDeletedAtIsNull(UUID id, UUID ownerId);

    List<CollectionEntity> findAllByIdInAndOwnerIdAndDeletedAtIsNull(Collection<UUID> ids, UUID ownerId);

    long countByOwnerIdAndDeletedAtIsNull(UUID ownerId);
}
