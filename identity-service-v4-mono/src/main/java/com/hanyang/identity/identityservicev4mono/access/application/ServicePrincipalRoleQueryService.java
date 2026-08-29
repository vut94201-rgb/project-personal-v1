package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.Role;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.ServicePrincipalRoleRepository;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalNotFoundException;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@IdentityReadAccess
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicePrincipalRoleQueryService {

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final ServicePrincipalRoleRepository servicePrincipalRoleRepository;
    private final RoleRepository roleRepository;
    private final ServicePrincipalRoleProvisioningStateRepository provisioningStateRepository;

    public List<ServicePrincipalRoleAssignmentView> list(ServicePrincipalId servicePrincipalId) {
        if (servicePrincipalRepository.findById(servicePrincipalId).isEmpty()) {
            throw new ServicePrincipalNotFoundException(servicePrincipalId);
        }

        List<RoleId> roleIds =
                servicePrincipalRoleRepository.findRoleIdsByServicePrincipalId(
                        servicePrincipalId
                );

        Map<RoleId, Role> rolesById = roleRepository.findAllByIds(roleIds).stream()
                .collect(Collectors.toMap(Role::getId, Function.identity()));

        return roleIds.stream()
                .map(rolesById::get)
                .filter(java.util.Objects::nonNull)
                .sorted(Comparator.comparing(Role::getCode))
                .map(role -> new ServicePrincipalRoleAssignmentView(
                        role,
                        provisioningStateRepository
                                .findByKeyAndProvider(
                                        servicePrincipalId,
                                        role.getId(),
                                        PROVIDER
                                )
                                .orElse(null)
                ))
                .toList();
    }
}