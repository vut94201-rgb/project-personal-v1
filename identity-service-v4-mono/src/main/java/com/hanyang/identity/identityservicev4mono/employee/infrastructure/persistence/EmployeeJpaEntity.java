package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;
@Entity
@Table(name = "employees")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EmployeeJpaEntity extends AuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(
            name = "employee_code",
            nullable = false,
            unique = true,
            length = 50
    )
    private String employeeCode;

    @Column(
            name = "full_name",
            nullable = false,
            length = 150
    )
    private String fullName;

    @Convert(converter = EmployeeStatusConverter.class)
    @Column(
            name = "status",
            nullable = false,
            length = 3
    )
    private EmployeeStatus status;
}