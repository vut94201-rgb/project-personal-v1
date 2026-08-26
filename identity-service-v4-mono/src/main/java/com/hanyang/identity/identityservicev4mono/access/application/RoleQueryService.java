package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.exception.ApplicationNotFoundException;
import com.hanyang.identity.identityservicev4mono.access.application.exception.RoleNotFoundException;
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
public class RoleQueryService {

  private final RoleRepository roleRepository;
  private final ApplicationRepository applicationRepository;

  public Role getById(RoleId id) {
    return roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException(id));
  }

  public List<Role> getByApplicationId(ApplicationId applicationId) {
    if (applicationRepository.findById(applicationId).isEmpty()) {
      throw new ApplicationNotFoundException(applicationId);
    }

    return roleRepository.findAllByApplicationId(applicationId);
  }
}
