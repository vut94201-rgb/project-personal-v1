package com.hanyang.identity.identityservicev4mono.access.application;


import com.hanyang.identity.identityservicev4mono.access.application.command.CreateRoleCommand;
import com.hanyang.identity.identityservicev4mono.access.application.command.UpdateRoleCommand;
import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationDisabledException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.RoleCodeAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.RoleNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleCommandService {

    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;

    @Transactional
    public Role create(CreateRoleCommand command) {
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

        Role role = Role.create(
                RoleId.newId(),
                command.applicationId(),
                command.code(),
                command.name()
        );

        if (roleRepository.existsByApplicationIdAndCode(
                role.getApplicationId(),
                role.getCode()
        )) {
            throw new RoleCodeAlreadyExistsException(
                    role.getApplicationId(),
                    role.getCode()
            );
        }

        return roleRepository.save(role);
    }

    @Transactional
    public Role update(UpdateRoleCommand command) {
        Role role = roleRepository
                .findById(command.roleId())
                .orElseThrow(() ->
                        new RoleNotFoundException(command.roleId())
                );

        role.rename(command.name());

        return roleRepository.save(role);
    }

    @Transactional
    public void disable(RoleId roleId) {
        Role role = roleRepository
                .findById(roleId)
                .orElseThrow(() ->
                        new RoleNotFoundException(roleId)
                );

        role.disable();
        roleRepository.save(role);
    }
}