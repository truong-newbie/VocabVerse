package com.vocabverse.collection.mapper;

import com.vocabverse.collection.dto.request.CreateCollectionRequest;
import com.vocabverse.collection.dto.request.UpdateCollectionRequest;
import com.vocabverse.collection.dto.response.CollectionResponse;
import com.vocabverse.collection.entity.CollectionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CollectionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "totalWords", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    CollectionEntity toEntity(CreateCollectionRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(target = "vocabularyCount", source = "totalWords")
    CollectionResponse toResponse(CollectionEntity entity);

    default CollectionResponse toResponse(CollectionEntity entity, int vocabularyCount) {
        return new CollectionResponse(
                entity.getId(),
                entity.getOwner() == null ? null : entity.getOwner().getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getVisibility(),
                vocabularyCount,
                entity.getThumbnailUrl(),
                entity.getTotalWords(),
                entity.isFeatured(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "totalWords", ignore = true)
    @Mapping(target = "featured", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateCollectionRequest request, @MappingTarget CollectionEntity entity);
}
