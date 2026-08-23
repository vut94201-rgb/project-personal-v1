package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.access.domain.Permission;
import com.hanyang.identity.identityservicev4mono.access.domain.PermissionStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface PermissionJpaRepository
        extends BaseJpaRepository<PermissionJpaEntity, UUID> {

    List<PermissionJpaEntity> findAllByApplicationIdOrderByCodeAsc(
            UUID applicationId
    );

    boolean existsByApplicationIdAndCode(
            UUID applicationId,
            String code
    );

  @Query(
"""
        select p from  PermissionJpaEntity  p where (:status is null  or p.status=:status)
        """)
  Set<PermissionJpaEntity> findAllByPermissionStatus(
      @Param("status") PermissionStatus permissionStatus);
}