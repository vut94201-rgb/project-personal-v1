package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.PermissionNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@IdentityReadAccess
@Transactional(readOnly = true)
public class PermissionQueryService {

  private final PermissionRepository permissionRepository;
  private final ApplicationRepository applicationRepository;

  public Permission getById(PermissionId id) {
    return permissionRepository.findById(id).orElseThrow(() -> new PermissionNotFoundException(id));
  }

  public List<Permission> getByApplicationId(ApplicationId applicationId) {
    if (applicationRepository.findById(applicationId).isEmpty()) {
      throw new ApplicationNotFoundException(applicationId);
    }

    return permissionRepository.findAllByApplicationId(applicationId);
  }
}
