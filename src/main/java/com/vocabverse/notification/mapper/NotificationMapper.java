package com.vocabverse.notification.mapper;

import com.vocabverse.notification.dto.response.NotificationResponse;
import com.vocabverse.notification.entity.NotificationEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(NotificationEntity notification);
}
