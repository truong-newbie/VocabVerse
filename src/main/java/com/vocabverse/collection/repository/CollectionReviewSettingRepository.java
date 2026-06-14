package com.vocabverse.collection.repository;

import com.vocabverse.collection.entity.CollectionReviewSettingEntity;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionReviewSettingRepository extends JpaRepository<CollectionReviewSettingEntity, UUID> {

    Optional<CollectionReviewSettingEntity> findByUserIdAndCollectionId(UUID userId, UUID collectionId);

    Optional<CollectionReviewSettingEntity> findFirstByUserIdAndCollectionIdInAndEnabledTrue(
            UUID userId,
            Collection<UUID> collectionIds
    );

    List<CollectionReviewSettingEntity> findAllByUserIdAndCollectionIdIn(UUID userId, Collection<UUID> collectionIds);
}
