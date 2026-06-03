package com.vocabverse.learning.progress.mapper;

import com.vocabverse.learning.progress.dto.LearningProgressResponse;
import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LearningProgressMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "vocabularyId", source = "vocabulary.id")
    @Mapping(target = "word", source = "vocabulary.word")
    LearningProgressResponse toResponse(LearningProgressEntity entity);
}
