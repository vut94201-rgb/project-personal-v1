package com.hanyang.identity.identityservicev4mono.access.domain;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

import java.util.List;

public interface ServicePrincipalRoleRepository {

    ServicePrincipalRole save(ServicePrincipalRole servicePrincipalRole);

    void delete(ServicePrincipalId servicePrincipalId, RoleId roleId);

    boolean exists(ServicePrincipalId servicePrincipalId, RoleId roleId);

    List<RoleId> findRoleIdsByServicePrincipalId(ServicePrincipalId servicePrincipalId);
}