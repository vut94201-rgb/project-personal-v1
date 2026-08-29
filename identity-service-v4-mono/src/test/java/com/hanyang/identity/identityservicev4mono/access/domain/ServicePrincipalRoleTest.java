package com.hanyang.identity.identityservicev4mono.access.domain;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicePrincipalRoleTest {

    @Test
    void createKeepsServicePrincipalAndRoleIdentity() {
        ServicePrincipalId servicePrincipalId = ServicePrincipalId.newId();
        RoleId roleId = RoleId.newId();

        ServicePrincipalRole assignment = ServicePrincipalRole.create(
                servicePrincipalId,
                roleId
        );

        assertEquals(servicePrincipalId, assignment.getServicePrincipalId());
        assertEquals(roleId, assignment.getRoleId());
    }

    @Test
    void createRejectsNullReferences() {
        assertThrows(
                NullPointerException.class,
                () -> ServicePrincipalRole.create(null, RoleId.newId())
        );
        assertThrows(
                NullPointerException.class,
                () -> ServicePrincipalRole.create(ServicePrincipalId.newId(), null)
        );
    }
}