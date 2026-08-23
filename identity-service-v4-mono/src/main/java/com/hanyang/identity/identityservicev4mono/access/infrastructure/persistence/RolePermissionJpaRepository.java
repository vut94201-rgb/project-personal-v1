package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.shared.persistence.BaseJpaRepository;

import java.util.List;
import java.util.UUID;

public interface RolePermissionJpaRepository
        extends BaseJpaRepository<RolePermissionJpaEntity, RolePermissionJpaId> {

                List<RolePermissionJpaEntity> findAllById_RoleId(UUID roleId);
    }