package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence;


import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.access.domain.RolePermission;
import com.hanyang.identity.identityservicev4mono.access.domain.RolePermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RolePermissionRepositoryAdapter
        implements RolePermissionRepository {

    private final RolePermissionJpaRepository jpaRepository;

    @Override
    public RolePermission save(RolePermission rolePermission) {
        RolePermissionJpaId id = toJpaId(
                rolePermission.getRoleId(),
                rolePermission.getPermissionId()
        );

        RolePermissionJpaEntity entity = new RolePermissionJpaEntity();
        entity.setId(id);

        RolePermissionJpaEntity saved = jpaRepository.save(entity);

        return RolePermission.rehydrate(
                new RoleId(saved.getId().getRoleId()),
                new PermissionId(saved.getId().getPermissionId())
        );
    }

    @Override
    public void delete(
            RoleId roleId,
            PermissionId permissionId
    ) {
        jpaRepository.deleteById(toJpaId(roleId, permissionId));
    }

    @Override
    public boolean exists(
            RoleId roleId,
            PermissionId permissionId
    ) {
        return jpaRepository.existsById(
                toJpaId(roleId, permissionId)
        );
    }

    @Override
    public List<PermissionId> findPermissionIdsByRoleId(RoleId roleId) {
        return jpaRepository
                .findAllById_RoleId(roleId.value())
                .stream()
                .map(entity ->
                        new PermissionId(entity.getId().getPermissionId())
                )
                .toList();
    }

    private RolePermissionJpaId toJpaId(
            RoleId roleId,
            PermissionId permissionId
    ) {
        return new RolePermissionJpaId(
                roleId.value(),
                permissionId.value()
        );
    }
}