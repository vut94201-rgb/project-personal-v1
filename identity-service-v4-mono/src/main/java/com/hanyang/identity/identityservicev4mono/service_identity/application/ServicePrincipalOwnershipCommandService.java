package com.hanyang.identity.identityservicev4mono.service_identity.application;


import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeNotFoundException;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.AssignServicePrincipalOwnerCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.RevokeServicePrincipalOwnerCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.*;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@IdentityAdminAccess
public class ServicePrincipalOwnershipCommandService {

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final ServicePrincipalOwnerRepository ownerRepository;
    private final EmployeeRepository employeeRepository;
    private final Clock clock;

    @Transactional
    public ServicePrincipalOwner assign(
            AssignServicePrincipalOwnerCommand command
    ) {
        servicePrincipalRepository.findById(command.servicePrincipalId())
                .orElseThrow(() -> new ServicePrincipalNotFoundException(
                        command.servicePrincipalId()
                ));

        Employee employee = employeeRepository.findById(command.employeeId())
                .orElseThrow(() -> new EmployeeNotFoundException(command.employeeId()));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ServicePrincipalOwnerEmployeeNotActiveException(
                    employee.getId(),
                    employee.getStatus()
            );
        }

        if (ownerRepository.existsActiveByServicePrincipalIdAndEmployeeId(
                command.servicePrincipalId(),
                command.employeeId()
        )) {
            throw new ServicePrincipalOwnerAlreadyAssignedException(
                    command.servicePrincipalId(),
                    command.employeeId()
            );
        }

        if (command.ownershipType() == ServicePrincipalOwnershipType.PRIMARY
                && ownerRepository.existsActivePrimaryByServicePrincipalId(
                command.servicePrincipalId()
        )) {
            throw new ServicePrincipalPrimaryOwnerAlreadyExistsException(
                    command.servicePrincipalId()
            );
        }

        ServicePrincipalOwner owner = ServicePrincipalOwner.create(
                ServicePrincipalOwnerId.newId(),
                command.servicePrincipalId(),
                command.employeeId(),
                command.ownershipType()
        );

        return ownerRepository.save(owner);
    }

    @Transactional
    public ServicePrincipalOwner revoke(
            RevokeServicePrincipalOwnerCommand command
    ) {
        ServicePrincipalOwner owner = ownerRepository.findById(command.ownerId())
                .orElseThrow(() -> new ServicePrincipalOwnerNotFoundException(
                        command.ownerId()
                ));

        if (owner.getStatus() == ServicePrincipalOwnerStatus.REVOKED) {
            return owner;
        }

        if (owner.getOwnershipType() == ServicePrincipalOwnershipType.PRIMARY) {
            throw new ServicePrincipalPrimaryOwnerRevocationNotAllowedException(
                    owner.getId()
            );
        }

        owner.revoke(Instant.now(clock));
        return ownerRepository.save(owner);
    }

    @Transactional
    public ServicePrincipalOwner transferPrimaryOwner(
            ServicePrincipalId servicePrincipalId,
            EmployeeId newEmployeeId
    ) {
        servicePrincipalRepository.findById(servicePrincipalId)
                .orElseThrow(() ->
                        new ServicePrincipalNotFoundException(servicePrincipalId)
                );

        Employee employee = employeeRepository.findById(newEmployeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(newEmployeeId));

        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new ServicePrincipalOwnerEmployeeNotActiveException(
                    employee.getId(),
                    employee.getStatus()
            );
        }

        List<ServicePrincipalOwner> activeOwners =
                ownerRepository.findAllActiveByServicePrincipalId(
                        servicePrincipalId
                );

        ServicePrincipalOwner currentPrimary = activeOwners.stream()
                .filter(owner ->
                        owner.getOwnershipType()
                                == ServicePrincipalOwnershipType.PRIMARY
                )
                .findFirst()
                .orElseThrow(() ->
                        new ServicePrincipalPrimaryOwnerMissingException(
                                servicePrincipalId
                        )
                );

        if (currentPrimary.getEmployeeId().equals(newEmployeeId)) {
            return currentPrimary;
        }

        Instant now = Instant.now(clock);

        activeOwners.stream()
                .filter(owner -> owner.getEmployeeId().equals(newEmployeeId))
                .findFirst()
                .ifPresent(owner -> {
                    owner.revoke(now);
                    ownerRepository.save(owner);
                });

        currentPrimary.revoke(now);
        ownerRepository.save(currentPrimary);

        ServicePrincipalOwner replacement = ServicePrincipalOwner.create(
                ServicePrincipalOwnerId.newId(),
                servicePrincipalId,
                newEmployeeId,
                ServicePrincipalOwnershipType.PRIMARY
        );

        return ownerRepository.save(replacement);
    }
}