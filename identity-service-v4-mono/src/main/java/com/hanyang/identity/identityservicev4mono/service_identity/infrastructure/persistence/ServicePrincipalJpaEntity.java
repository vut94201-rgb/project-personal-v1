package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter.ServicePrincipalStatusConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "service_principals")
@Getter
@Setter
@NoArgsConstructor
public class ServicePrincipalJpaEntity extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 100
    )
    private String code;

    @Column(
            name = "display_name",
            nullable = false,
            length = 150
    )
    private String displayName;

    @Column(
            name = "purpose",
            nullable = false,
            length = 500
    )
    private String purpose;

    @Column(
            name = "description",
            length = 1000
    )
    private String description;

    @Convert(converter = ServicePrincipalStatusConverter.class)
    @Column(
            name = "status",
            nullable = false,
            length = 3
    )
    private ServicePrincipalStatus status;
}