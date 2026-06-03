package com.vocabverse.vocabulary.repository;

import com.vocabverse.vocabulary.entity.VocabularyEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocabularyRepository extends JpaRepository<VocabularyEntity, UUID> {

    Page<VocabularyEntity> findAllByOwnerIdAndDeletedAtIsNull(UUID ownerId, Pageable pageable);

    Optional<VocabularyEntity> findByIdAndOwnerIdAndDeletedAtIsNull(UUID id, UUID ownerId);
}
