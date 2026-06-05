package com.vocabverse.collection.repository;

import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.enums.CollectionVisibility;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<CollectionEntity, UUID> {

    Optional<CollectionEntity> findByIdAndOwnerIdAndDeletedAtIsNull(UUID id, UUID ownerId);

    Page<CollectionEntity> findAllByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);

    Optional<CollectionEntity> findByIdAndOwnerIdAndVisibilityInAndDeletedAtIsNull(
            UUID id,
            UUID ownerId,
            Collection<CollectionVisibility> visibilities
    );

    List<CollectionEntity> findAllByIdInAndOwnerIdAndDeletedAtIsNull(Collection<UUID> ids, UUID ownerId);

    long countByOwnerIdAndDeletedAtIsNull(UUID ownerId);

    Page<CollectionEntity> findAllByVisibilityAndOwnerIdNotAndDeletedAtIsNull(
            CollectionVisibility visibility,
            UUID ownerId,
            Pageable pageable
    );

    Page<CollectionEntity> findAllByVisibilityAndDeletedAtIsNull(CollectionVisibility visibility, Pageable pageable);

    Optional<CollectionEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<CollectionEntity> findByIdAndVisibilityAndDeletedAtIsNull(UUID id, CollectionVisibility visibility);
}
