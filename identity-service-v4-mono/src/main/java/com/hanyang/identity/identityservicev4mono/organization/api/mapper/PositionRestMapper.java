package com.hanyang.identity.identityservicev4mono.organization.api.mapper;

import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.CreatePositionRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.response.PositionResponse;
import com.hanyang.identity.identityservicev4mono.organization.application.command.CreatePositionCommand;
import com.hanyang.identity.identityservicev4mono.organization.domain.Position;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PositionRestMapper {
    default CreatePositionCommand toCommand(CreatePositionRequest request) {
        return new CreatePositionCommand(request.code(), request.name());
    }

    default PositionResponse toResponse(Position position) {
        return new PositionResponse(
                position.getId().value(),
                position.getCode(),
                position.getName(),
                position.getStatus().getCode()
        );
    }
}
