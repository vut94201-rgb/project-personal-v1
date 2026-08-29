package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;
import com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter.ServicePrincipalOwnerStatusConverter;
import com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter.ServicePrincipalOwnershipTypeConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "service_principal_owners")
@Getter
@Setter
@NoArgsConstructor
public class ServicePrincipalOwnerJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "service_principal_id", nullable = false)
    private UUID servicePrincipalId;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Convert(converter = ServicePrincipalOwnershipTypeConverter.class)
    @Column(name = "ownership_type", nullable = false, length = 3)
    private ServicePrincipalOwnershipType ownershipType;

    @Convert(converter = ServicePrincipalOwnerStatusConverter.class)
    @Column(name = "status", nullable = false, length = 3)
    private ServicePrincipalOwnerStatus status;

    @Column(name = "revoked_at")
    private Instant revokedAt;
}