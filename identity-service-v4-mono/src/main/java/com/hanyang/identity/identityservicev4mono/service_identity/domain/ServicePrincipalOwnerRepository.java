package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

import java.util.List;
import java.util.Optional;


public interface ServicePrincipalOwnerRepository {

    ServicePrincipalOwner save(ServicePrincipalOwner owner);

    Optional<ServicePrincipalOwner> findById(ServicePrincipalOwnerId id);

    List<ServicePrincipalOwner> findAllActiveByServicePrincipalId(
            ServicePrincipalId servicePrincipalId
    );

    boolean existsActiveByServicePrincipalIdAndEmployeeId(
            ServicePrincipalId servicePrincipalId,
            EmployeeId employeeId
    );

    boolean existsActivePrimaryByServicePrincipalId(
            ServicePrincipalId servicePrincipalId
    );
}