package com.hanyang.identity.identityservicev4mono.access.application;


import com.hanyang.identity.identityservicev4mono.access.application.exception.*;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@IdentityAdminAccess
@RequiredArgsConstructor
public class RolePermissionCommandService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

    @Transactional
    public void assign(
            RoleId roleId,
            PermissionId permissionId
    ) {
        Role role = getRole(roleId);
        Permission permission = getPermission(permissionId);

        validateAssignable(role, permission);

        if (rolePermissionRepository.exists(roleId, permissionId)) {
            throw new RolePermissionAlreadyAssignedException(
                    roleId,
                    permissionId
            );
        }

        rolePermissionRepository.save(
                RolePermission.create(roleId, permissionId)
        );
    }

    @Transactional
    public void revoke(
            RoleId roleId,
            PermissionId permissionId
    ) {
        getRole(roleId);
        getPermission(permissionId);

        if (!rolePermissionRepository.exists(roleId, permissionId)) {
            throw new RolePermissionNotAssignedException(
                    roleId,
                    permissionId
            );
        }

        rolePermissionRepository.delete(roleId, permissionId);
    }

    private Role getRole(RoleId roleId) {
        return roleRepository
                .findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException(roleId));
    }

    private Permission getPermission(PermissionId permissionId) {
        return permissionRepository
                .findById(permissionId)
                .orElseThrow(() ->
                        new PermissionNotFoundException(permissionId)
                );
    }

    private void validateAssignable(
            Role role,
            Permission permission
    ) {
        if (role.getStatus() != RoleStatus.ACTIVE) {
            throw new RoleDisabledException(role.getId());
        }

        if (permission.getStatus() != PermissionStatus.ACTIVE) {
            throw new PermissionDisabledException(permission.getId());
        }

        if (!role.getApplicationId().equals(permission.getApplicationId())) {
            throw new RolePermissionApplicationMismatchException(
                    role.getId(),
                    permission.getId()
            );
        }
    }
}