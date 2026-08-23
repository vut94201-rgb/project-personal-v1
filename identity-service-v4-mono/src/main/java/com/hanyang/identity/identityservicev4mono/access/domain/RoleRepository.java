package com.hanyang.identity.identityservicev4mono.access.domain;

import jakarta.annotation.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RoleRepository {
  Set<Role> findAllByRoleStatus(@Nullable RoleStatus roleStatus);

  Role save(Role role);

  Optional<Role> findById(RoleId id);

  List<Role> findAllByApplicationId(ApplicationId applicationId);

  List<Role> findAllByIds(List<RoleId> ids);

  boolean existsByApplicationIdAndCode(ApplicationId applicationId, String code);
}
