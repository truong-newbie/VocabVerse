package com.vocabverse.review.mapper;

import com.vocabverse.learning.progress.entity.LearningProgressEntity;
import com.vocabverse.review.dto.response.ReviewDueItemResponse;
import com.vocabverse.review.dto.response.ReviewHistoryResponse;
import com.vocabverse.review.entity.ReviewHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "vocabularyId", source = "vocabulary.id")
    @Mapping(target = "word", source = "vocabulary.word")
    ReviewDueItemResponse toDueItemResponse(LearningProgressEntity progress);

    @Mapping(target = "vocabularyId", source = "vocabulary.id")
    @Mapping(target = "word", source = "vocabulary.word")
    ReviewHistoryResponse toHistoryResponse(ReviewHistoryEntity history);
}
