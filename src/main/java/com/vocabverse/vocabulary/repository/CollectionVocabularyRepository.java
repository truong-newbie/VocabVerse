package com.vocabverse.vocabulary.repository;

import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionVocabularyRepository extends JpaRepository<CollectionVocabularyEntity, UUID> {

    boolean existsByCollectionIdAndVocabularyId(UUID collectionId, UUID vocabularyId);

    Optional<CollectionVocabularyEntity> findByCollectionIdAndVocabularyId(UUID collectionId, UUID vocabularyId);

    Page<CollectionVocabularyEntity> findAllByCollectionIdAndVocabularyDeletedAtIsNull(UUID collectionId, Pageable pageable);

    List<CollectionVocabularyEntity> findAllByCollectionIdAndVocabularyDeletedAtIsNull(UUID collectionId);

    List<CollectionVocabularyEntity> findAllByVocabularyId(UUID vocabularyId);
}
