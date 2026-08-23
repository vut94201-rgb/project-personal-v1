package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.command.CreatePermissionCommand;
import com.hanyang.identity.identityservicev4mono.access.application.command.UpdatePermissionCommand;
import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationDisabledException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.PermissionCodeAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.PermissionNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionCommandService {

    private final PermissionRepository permissionRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public Permission create(CreatePermissionCommand command) {
        Application application = applicationRepository
                .findById(command.applicationId())
                .orElseThrow(() ->
                        new ApplicationNotFoundException(
                                command.applicationId()
                        )
                );

        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new ApplicationDisabledException(
                    command.applicationId()
            );
        }

        Permission permission = Permission.create(
                PermissionId.newId(),
                command.applicationId(),
                command.code(),
                command.name()
        );

        if (permissionRepository.existsByApplicationIdAndCode(
                permission.getApplicationId(),
                permission.getCode()
        )) {
            throw new PermissionCodeAlreadyExistsException(
                    permission.getApplicationId(),
                    permission.getCode()
            );
        }

        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission update(UpdatePermissionCommand command) {
        Permission permission = permissionRepository
                .findById(command.permissionId())
                .orElseThrow(() ->
                        new PermissionNotFoundException(
                                command.permissionId()
                        )
                );

        permission.rename(command.name());

        return permissionRepository.save(permission);
    }

    @Transactional
    public void disable(PermissionId permissionId) {
        Permission permission = permissionRepository
                .findById(permissionId)
                .orElseThrow(() ->
                        new PermissionNotFoundException(permissionId)
                );

        permission.disable();
        permissionRepository.save(permission);
    }
}