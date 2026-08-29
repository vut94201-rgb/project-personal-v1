package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;
import com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter.ServicePrincipalOwnerStatusConverter;
import com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter.ServicePrincipalOwnershipTypeConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServicePrincipalOwnerConvertersTest {

    @Test
    void convertsOwnershipTypeCodes() {
        ServicePrincipalOwnershipTypeConverter converter =
                new ServicePrincipalOwnershipTypeConverter();

        assertEquals("PRI", converter.convertToDatabaseColumn(
                ServicePrincipalOwnershipType.PRIMARY
        ));
        assertEquals(ServicePrincipalOwnershipType.TECHNICAL,
                converter.convertToEntityAttribute("TEC"));
    }

    @Test
    void convertsOwnerStatusCodes() {
        ServicePrincipalOwnerStatusConverter converter =
                new ServicePrincipalOwnerStatusConverter();

        assertEquals("ACT", converter.convertToDatabaseColumn(
                ServicePrincipalOwnerStatus.ACTIVE
        ));
        assertEquals(ServicePrincipalOwnerStatus.REVOKED,
                converter.convertToEntityAttribute("REV"));
    }
}