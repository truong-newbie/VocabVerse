package com.vocabverse.learning.typing.mapper;

import com.vocabverse.learning.typing.dto.response.TypingQuestionResponse;
import com.vocabverse.learning.typing.dto.response.TypingSessionResponse;
import com.vocabverse.learning.typing.entity.TypingQuestionEntity;
import com.vocabverse.learning.typing.entity.TypingSessionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TypingMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "collectionId", source = "collection.id")
    TypingSessionResponse toSessionResponse(TypingSessionEntity session);

    @Mapping(target = "vocabularyId", source = "vocabulary.id")
    TypingQuestionResponse toQuestionResponse(TypingQuestionEntity question);
}
