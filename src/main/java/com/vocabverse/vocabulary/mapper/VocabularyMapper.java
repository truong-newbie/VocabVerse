package com.vocabverse.vocabulary.mapper;

import com.vocabverse.vocabulary.dto.request.CreateVocabularyRequest;
import com.vocabverse.vocabulary.dto.request.UpdateVocabularyRequest;
import com.vocabverse.vocabulary.dto.response.VocabularyResponse;
import com.vocabverse.vocabulary.entity.VocabularyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VocabularyMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "normalizedWord", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    VocabularyEntity toEntity(CreateVocabularyRequest request);

    @Mapping(target = "ownerId", source = "owner.id")
    VocabularyResponse toResponse(VocabularyEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "normalizedWord", ignore = true)
    @Mapping(target = "source", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(UpdateVocabularyRequest request, @MappingTarget VocabularyEntity entity);
}
