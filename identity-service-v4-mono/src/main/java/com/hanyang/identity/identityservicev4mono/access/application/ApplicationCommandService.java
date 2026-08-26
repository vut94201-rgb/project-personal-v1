package com.hanyang.identity.identityservicev4mono.access.application;


import com.hanyang.identity.identityservicev4mono.access.application.command.CreateApplicationCommand;
import com.hanyang.identity.identityservicev4mono.access.application.command.UpdateApplicationCommand;
import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationCodeAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ApplicationProvisioningService;
import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationRepository;

import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@IdentityAdminAccess
@Service
@RequiredArgsConstructor
public class ApplicationCommandService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationProvisioningService provisioningService;

    @Transactional
    public Application create(CreateApplicationCommand command) {
        Application application = Application.create(
                ApplicationId.newId(),
                command.code(),
                command.name()
        );

        if (applicationRepository.existsByCode(application.getCode())) {
            throw new ApplicationCodeAlreadyExistsException(
                    application.getCode()
            );
        }

        Application saved = applicationRepository.save(application);
        provisioningService.requestSynchronization(saved.getId());

        return saved;
    }

    @Transactional
    public Application update(UpdateApplicationCommand command) {
        Application application = applicationRepository
                .findById(command.applicationId())
                .orElseThrow(() ->
                        new ApplicationNotFoundException(
                                command.applicationId()
                        )
                );

        application.rename(command.name());

        Application saved = applicationRepository.save(application);
        provisioningService.requestSynchronization(saved.getId());

        return saved;
    }

    @Transactional
    public void disable(ApplicationId applicationId) {
        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() ->
                        new ApplicationNotFoundException(applicationId)
                );

        application.disable();
        Application saved = applicationRepository.save(application);
        provisioningService.requestSynchronization(saved.getId());
    }
}