package com.vocabverse.roleplay.mapper;

import com.vocabverse.roleplay.dto.response.RoleplayMessageResponse;
import com.vocabverse.roleplay.dto.response.RoleplayReportResponse;
import com.vocabverse.roleplay.dto.response.RoleplaySessionResponse;
import com.vocabverse.roleplay.entity.RoleplayMessageEntity;
import com.vocabverse.roleplay.entity.RoleplayReportEntity;
import com.vocabverse.roleplay.entity.RoleplaySessionEntity;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleplayMapper {

    RoleplayMessageResponse toMessageResponse(RoleplayMessageEntity message);

    @Mapping(target = "sessionId", source = "session.id")
    RoleplayReportResponse toReportResponse(RoleplayReportEntity report);

    @Mapping(target = "id", source = "session.id")
    @Mapping(target = "topic", source = "session.topic")
    @Mapping(target = "difficulty", source = "session.difficulty")
    @Mapping(target = "persona", source = "session.persona")
    @Mapping(target = "scenario", source = "session.scenario")
    @Mapping(target = "status", source = "session.status")
    @Mapping(target = "startedAt", source = "session.startedAt")
    @Mapping(target = "endedAt", source = "session.endedAt")
    @Mapping(target = "createdAt", source = "session.createdAt")
    @Mapping(target = "updatedAt", source = "session.updatedAt")
    @Mapping(target = "messages", source = "messages")
    @Mapping(target = "report", source = "report")
    RoleplaySessionResponse toSessionResponse(
            RoleplaySessionEntity session,
            List<RoleplayMessageResponse> messages,
            RoleplayReportResponse report
    );
}
