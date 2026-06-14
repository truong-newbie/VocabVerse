package com.vocabverse.vocabulary.repository;

import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
            select distinct cv.collection.id
            from CollectionVocabularyEntity cv
            where cv.vocabulary.id = :vocabularyId
              and cv.collection.owner.id = :userId
              and cv.collection.deletedAt is null
              and cv.vocabulary.deletedAt is null
            """)
    List<UUID> findOwnedCollectionIdsByVocabularyId(
            @Param("userId") UUID userId,
            @Param("vocabularyId") UUID vocabularyId
    );

    @Query("""
            select cv.vocabulary.id
            from CollectionVocabularyEntity cv
            where cv.collection.id = :collectionId
              and cv.collection.owner.id = :userId
              and cv.collection.deletedAt is null
              and cv.vocabulary.deletedAt is null
            """)
    List<UUID> findVocabularyIdsByOwnedCollectionId(
            @Param("userId") UUID userId,
            @Param("collectionId") UUID collectionId
    );

    @Query("""
            select cv.collection.title
            from CollectionVocabularyEntity cv
            where cv.vocabulary.id = :vocabularyId
              and cv.collection.owner.id = :userId
              and cv.collection.deletedAt is null
              and cv.vocabulary.deletedAt is null
            order by cv.createdAt asc
            """)
    List<String> findOwnedCollectionTitlesByVocabularyId(
            @Param("userId") UUID userId,
            @Param("vocabularyId") UUID vocabularyId
    );
}
