package com.hanyang.identity.identityservicev4mono.access.api.mapper;

import com.hanyang.identity.identityservicev4mono.access.api.rest.request.CreateApplicationRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.ApplicationResponse;
import com.hanyang.identity.identityservicev4mono.access.application.command.CreateApplicationCommand;
import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationRestMapper {

    default CreateApplicationCommand toCommand(
            CreateApplicationRequest request
    ) {
        return new CreateApplicationCommand(
                request.code(),
                request.name()
        );
    }

    default ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId().value(),
                application.getCode(),
                application.getName(),
                application.getStatus().getCode()
        );
    }
}