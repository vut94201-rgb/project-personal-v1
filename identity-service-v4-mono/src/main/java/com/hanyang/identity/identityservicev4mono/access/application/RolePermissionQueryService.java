package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.exception.RoleNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor@IdentityReadAccess
@Transactional(readOnly = true)
public class RolePermissionQueryService {

            private final RoleRepository roleRepository;
private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;

           public List<Permission> getPermissions(RoleId roleId) {
                if (roleRepository.findById(roleId).isEmpty()) {
                        throw new RoleNotFoundException(roleId);
                    }

                        return permissionRepository
                                .findAllByIds(
                                        rolePermissionRepository
                                                        .findPermissionIdsByRoleId(roleId))
                        .stream()
                               .sorted(java.util.Comparator.comparing(Permission::getCode))
                                .toList();
            }
}