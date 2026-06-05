package com.vocabverse.publiccollection.service;

import com.vocabverse.collection.entity.CollectionEntity;
import com.vocabverse.collection.enums.CollectionVisibility;
import com.vocabverse.collection.repository.CollectionRepository;
import com.vocabverse.common.constant.ErrorCode;
import com.vocabverse.common.exception.BusinessException;
import com.vocabverse.publiccollection.dto.response.CloneCollectionResponse;
import com.vocabverse.publiccollection.dto.response.PublicCollectionPageResponse;
import com.vocabverse.publiccollection.dto.response.PublicCollectionResponse;
import com.vocabverse.publiccollection.dto.response.PublicVocabularyPageResponse;
import com.vocabverse.publiccollection.dto.response.PublicVocabularyResponse;
import com.vocabverse.user.entity.UserEntity;
import com.vocabverse.user.repository.UserRepository;
import com.vocabverse.vocabulary.entity.CollectionVocabularyEntity;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import com.vocabverse.vocabulary.repository.CollectionVocabularyRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicCollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionVocabularyRepository collectionVocabularyRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PublicCollectionPageResponse getPublicCollections(Pageable pageable) {
        UUID currentUserId = getCurrentUser().getId();
        Page<CollectionEntity> page = collectionRepository.findAllByVisibilityAndOwnerIdNotAndDeletedAtIsNull(
                CollectionVisibility.PUBLIC,
                currentUserId,
                pageable
        );

        return new PublicCollectionPageResponse(
                page.map(this::toCollectionResponse).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PublicCollectionResponse getPublicCollectionDetail(UUID collectionId) {
        return toCollectionResponse(getPublicCollection(collectionId));
    }

    @Transactional(readOnly = true)
    public PublicVocabularyPageResponse getPublicCollectionVocabularies(UUID collectionId, Pageable pageable) {
        getPublicCollection(collectionId);
        Page<CollectionVocabularyEntity> page = collectionVocabularyRepository
                .findAllByCollectionIdAndCollectionVisibilityAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(
                        collectionId,
                        CollectionVisibility.PUBLIC,
                        pageable
                );

        return new PublicVocabularyPageResponse(
                page.map(CollectionVocabularyEntity::getVocabulary)
                        .map(this::toVocabularyResponse)
                        .getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Transactional
    public CloneCollectionResponse clonePublicCollection(UUID collectionId) {
        UserEntity user = getCurrentUser();
        CollectionEntity source = getPublicCollection(collectionId);
        List<CollectionVocabularyEntity> sourceItems = collectionVocabularyRepository
                .findAllByCollectionIdAndCollectionDeletedAtIsNullAndVocabularyDeletedAtIsNull(source.getId());

        CollectionEntity clonedCollection = CollectionEntity.builder()
                .owner(user)
                .title(source.getTitle())
                .description(source.getDescription())
                .visibility(CollectionVisibility.PRIVATE)
                .thumbnailUrl(source.getThumbnailUrl())
                .totalWords(sourceItems.size())
                .featured(false)
                .build();

        CollectionEntity savedCollection = collectionRepository.save(clonedCollection);
        List<CollectionVocabularyEntity> clonedItems = sourceItems.stream()
                .map(sourceItem -> CollectionVocabularyEntity.builder()
                        .collection(savedCollection)
                        .vocabulary(sourceItem.getVocabulary())
                        .addedBy(user)
                        .position(sourceItem.getPosition())
                        .build())
                .toList();
        collectionVocabularyRepository.saveAll(clonedItems);

        return new CloneCollectionResponse(
                source.getId(),
                savedCollection.getId(),
                savedCollection.getTitle(),
                savedCollection.getVisibility(),
                savedCollection.getTotalWords()
        );
    }

    private CollectionEntity getPublicCollection(UUID collectionId) {
        return collectionRepository.findByIdAndVisibilityAndDeletedAtIsNull(
                        collectionId,
                        CollectionVisibility.PUBLIC
                )
                .orElseThrow(() -> new BusinessException(ErrorCode.COLLECTION_NOT_FOUND));
    }

    private PublicCollectionResponse toCollectionResponse(CollectionEntity collection) {
        UserEntity owner = collection.getOwner();
        return new PublicCollectionResponse(
                collection.getId(),
                collection.getTitle(),
                collection.getDescription(),
                collection.getVisibility(),
                collection.getThumbnailUrl(),
                collection.getTotalWords(),
                collection.isFeatured(),
                owner == null ? null : owner.getId(),
                owner == null ? null : owner.getFullName(),
                owner == null ? null : owner.getAvatarUrl(),
                collection.getCreatedAt(),
                collection.getUpdatedAt()
        );
    }

    private PublicVocabularyResponse toVocabularyResponse(VocabularyEntity vocabulary) {
        return new PublicVocabularyResponse(
                vocabulary.getId(),
                vocabulary.getWord(),
                vocabulary.getNormalizedWord(),
                vocabulary.getPhonetic(),
                vocabulary.getAudioUrl(),
                vocabulary.getPartOfSpeech(),
                vocabulary.getMeaningVi(),
                vocabulary.getMeaningEn(),
                vocabulary.getSynonyms(),
                vocabulary.getAntonyms(),
                vocabulary.getExamples()
        );
    }

    private UserEntity getCurrentUser() {
        String email = getAuthenticatedEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
        return authentication.getName();
    }
}
