package com.hanyang.identity.identityservicev4mono.access.api.mapper;

import com.hanyang.identity.identityservicev4mono.access.api.rest.request.CreatePermissionRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.PermissionResponse;
import com.hanyang.identity.identityservicev4mono.access.application.command.CreatePermissionCommand;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionRestMapper {

            default CreatePermissionCommand toCommand(
           CreatePermissionRequest request
   ) {
               return new CreatePermissionCommand(
                              new ApplicationId(request.applicationId()),
                               request.code(),
                              request.name()
                                );}
          default PermissionResponse toResponse(Permission permission) {
               return new PermissionResponse(
                               permission.getId().value(),
                               permission.getApplicationId().value(),
                              permission.getCode(),
                               permission.getName(),
                               permission.getStatus().getCode()
                             );
           }}