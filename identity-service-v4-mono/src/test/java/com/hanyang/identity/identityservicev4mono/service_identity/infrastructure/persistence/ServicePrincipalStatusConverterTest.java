package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter.ServicePrincipalStatusConverter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServicePrincipalStatusConverterTest {

    private final ServicePrincipalStatusConverter converter =
            new ServicePrincipalStatusConverter();

    @Test
    void convertsStatusToStableDatabaseCode() {
        assertEquals(
                "PND",
                converter.convertToDatabaseColumn(ServicePrincipalStatus.PENDING)
        );
        assertEquals(
                "ACT",
                converter.convertToDatabaseColumn(ServicePrincipalStatus.ACTIVE)
        );
        assertEquals(
                "DIS",
                converter.convertToDatabaseColumn(ServicePrincipalStatus.DISABLED)
        );
    }

    @Test
    void convertsStableDatabaseCodeToStatus() {
        assertEquals(
                ServicePrincipalStatus.PENDING,
                converter.convertToEntityAttribute("PND")
        );
    }

    @Test
    void rejectsUnknownDatabaseCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute("BAD")
        );
    }
}