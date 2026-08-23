package com.hanyang.identity.identityservicev4mono.access.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository {

    Permission save(Permission permission);

    Optional<Permission> findById(PermissionId id);

    List<Permission> findAllByIds(Collection<PermissionId> ids);

    List<Permission> findAllByApplicationId(ApplicationId applicationId);

    boolean existsByApplicationIdAndCode(
            ApplicationId applicationId,
            String code
    );
}