package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.access.domain.PermissionStatus;
import com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.converter.PermissionStatusConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
public class PermissionJpaEntity extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(
            name = "application_id",
            nullable = false
    )
    private UUID applicationId;

    @Column(
            name = "code",
            nullable = false,
            length = 100
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Convert(converter = PermissionStatusConverter.class)
    @Column(
            name = "status",
            nullable = false,
            length = 3
    )
    private PermissionStatus status;
}