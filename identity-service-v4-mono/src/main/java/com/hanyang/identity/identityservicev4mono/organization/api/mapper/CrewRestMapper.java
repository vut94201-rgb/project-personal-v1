package com.hanyang.identity.identityservicev4mono.organization.api.mapper;


import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.CreateCrewRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.response.CrewResponse;
import com.hanyang.identity.identityservicev4mono.organization.application.command.CreateCrewCommand;
import com.hanyang.identity.identityservicev4mono.organization.domain.Crew;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CrewRestMapper {
    default CreateCrewCommand toCommand(CreateCrewRequest request) {
        return new CreateCrewCommand(
                new DepartmentId(request.departmentId()),
                request.code(),
                request.name()
        );
    }

    default CrewResponse toResponse(Crew crew) {
        return new CrewResponse(
                crew.getId().value(),
                crew.getDepartmentId().value(),
                crew.getCode(),
                crew.getName(),
                crew.getStatus().getCode()
        );
    }
}