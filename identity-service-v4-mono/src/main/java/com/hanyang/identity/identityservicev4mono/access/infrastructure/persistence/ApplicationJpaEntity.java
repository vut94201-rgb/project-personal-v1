package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationStatus;
import com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.converter.ApplicationStatusConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
public class ApplicationJpaEntity extends AuditableEntity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(
            name = "code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String code;

    @Column(
            name = "name",
            nullable = false,
            length = 150
    )
    private String name;

    @Convert(converter = ApplicationStatusConverter.class)
    @Column(
            name = "status",
            nullable = false,
            length = 3
    )
    private ApplicationStatus status;
}