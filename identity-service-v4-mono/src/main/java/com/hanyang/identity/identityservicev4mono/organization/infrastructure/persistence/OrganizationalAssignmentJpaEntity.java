package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationalAssignmentStatus;
import com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence.converter.OrganizationalAssignmentStatusConverter;
import com.hanyang.identity.identityservicev4mono.shared.persistence.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "organizational_assignments")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrganizationalAssignmentJpaEntity extends AuditableEntity {
    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "employee_id", nullable = false)
    private UUID employeeId;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(name = "position_id", nullable = false)
    private UUID positionId;

    @Column(name = "crew_id")
    private UUID crewId;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Convert(converter = OrganizationalAssignmentStatusConverter.class)
    @Column(name = "status", nullable = false, length = 3)
    private OrganizationalAssignmentStatus status;
}