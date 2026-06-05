package com.vocabverse.vocabulary.repository;

import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionVocabularyRepository extends JpaRepository<CollectionVocabularyEntity, UUID> {

    boolean existsByCollectionIdAndVocabularyIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
            UUID collectionId,
            UUID vocabularyId
    );

    boolean existsByCollectionIdAndVocabularyNormalizedWordAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
            UUID collectionId,
            String normalizedWord
    );

    Optional<CollectionVocabularyEntity> findByCollectionIdAndVocabularyIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
            UUID collectionId,
            UUID vocabularyId
    );

    Page<CollectionVocabularyEntity> findAllByCollectionIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
            UUID collectionId,
            Pageable pageable
    );

    List<CollectionVocabularyEntity> findAllByCollectionIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
            UUID collectionId
    );

    Page<CollectionVocabularyEntity> findAllByCollectionIdAndCollectionVisibilityAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
            UUID collectionId,
            CollectionVisibility visibility,
            Pageable pageable
    );

    List<CollectionVocabularyEntity> findAllByVocabularyId(UUID vocabularyId);

    long countByCollectionIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(UUID collectionId);
}
