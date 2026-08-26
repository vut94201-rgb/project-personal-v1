package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CrewJpaRepository extends BaseJpaRepository<CrewJpaEntity, UUID> {
    Optional<CrewJpaEntity> findByDepartmentIdAndCode(UUID departmentId, String code);
    boolean existsByDepartmentIdAndCode(UUID departmentId, String code);

    @Query("""
            select c from CrewJpaEntity c
            where c.departmentId = :departmentId
              and (:status is null or c.status = :status)
            order by c.code
            """)
    List<CrewJpaEntity> findAllByDepartmentAndStatus(
            @Param("departmentId") UUID departmentId,
            @Param("status") OrganizationReferenceStatus status
    );

    boolean existsByDepartmentIdAndStatus(UUID departmentId, OrganizationReferenceStatus status);
}