package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleJpaRepository
        extends BaseJpaRepository<RoleJpaEntity, UUID> {

    List<RoleJpaEntity> findAllByApplicationIdOrderByCodeAsc(
            UUID applicationId
    );

    boolean existsByApplicationIdAndCode(
            UUID applicationId,
            String code
    );

  @Query(
"""
        SELECT r from RoleJpaEntity r where  (:status is null  or r.status=:status)
        """)
  Set<RoleJpaEntity> findAllByRoleStatus(@Param("status") RoleStatus roleStatus);
}