package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.access.domain.RoleStatus;
import com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.converter.RoleStatusConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class RoleJpaEntity extends AuditableEntity {

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
            length = 50
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Convert(converter = RoleStatusConverter.class)
    @Column(
            name = "status",
            nullable = false,
            length = 3
    )
    private RoleStatus status;
}