package com.hanyang.identity.identityservicev4mono.access.api.mapper;

import com.hanyang.identity.identityservicev4mono.access.api.rest.request.CreateRoleRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.RoleResponse;
import com.hanyang.identity.identityservicev4mono.access.application.command.CreateRoleCommand;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleRestMapper {

    default CreateRoleCommand toCommand(CreateRoleRequest request) {
        return new CreateRoleCommand(
                new ApplicationId(request.applicationId()),
                request.code(),
                request.name()
        );
    }

    default RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId().value(),
                role.getApplicationId().value(),
                role.getCode(),
                role.getName(),
                role.getStatus().getCode()
        );
    }
}