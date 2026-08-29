package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicePrincipalTest {

    @Test
    void newServicePrincipalStartsPendingAndNormalizesCode() {
        ServicePrincipal servicePrincipal = ServicePrincipal.create(
                ServicePrincipalId.newId(),
                "mes_integration",
                "MES Integration",
                "Synchronize production data from MES",
                "  "
        );

        assertEquals("MES_INTEGRATION", servicePrincipal.getCode());
        assertEquals(ServicePrincipalStatus.PENDING, servicePrincipal.getStatus());
        assertNull(servicePrincipal.getDescription());
    }

    @Test
    void pendingServicePrincipalCanBeActivatedByCoordinator() {
        ServicePrincipal servicePrincipal = ServicePrincipal.create(
                ServicePrincipalId.newId(),
                "MES_INTEGRATION",
                "MES Integration",
                "Synchronize production data from MES",
                null
        );

        servicePrincipal.activate();

        assertEquals(ServicePrincipalStatus.ACTIVE, servicePrincipal.getStatus());
    }

    @Test
    void disabledServicePrincipalCannotBeReactivated() {
        ServicePrincipal servicePrincipal = ServicePrincipal.rehydrate(
                ServicePrincipalId.newId(),
                "MES_INTEGRATION",
                "MES Integration",
                "Synchronize production data from MES",
                null,
                ServicePrincipalStatus.DISABLED
        );

        assertThrows(IllegalStateException.class, servicePrincipal::activate);
        assertEquals(ServicePrincipalStatus.DISABLED, servicePrincipal.getStatus());
    }

    @Test
    void invalidCodeIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ServicePrincipal.create(
                        ServicePrincipalId.newId(),
                        "mes-integration",
                        "MES Integration",
                        "Synchronize production data from MES",
                        null
                )
        );
    }

    @Test
    void purposeIsMandatory() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ServicePrincipal.create(
                        ServicePrincipalId.newId(),
                        "MES_INTEGRATION",
                        "MES Integration",
                        "   ",
                        null
                )
        );
    }
}