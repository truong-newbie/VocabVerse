package com.vocabverse.user.mapper;

import com.vocabverse.user.dto.UserResponse;
import com.vocabverse.user.entity.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(UserEntity user);
}
