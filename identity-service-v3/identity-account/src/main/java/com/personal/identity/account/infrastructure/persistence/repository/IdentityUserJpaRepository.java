package com.personal.identity.account.infrastructure.persistence.repository;

import com.personal.identity.account.infrastructure.persistence.entity.IdentityUserEntity;
import com.personal.identity.jpa.support.repository.SoftDeleteJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdentityUserJpaRepository extends SoftDeleteJpaRepository<IdentityUserEntity,Long> {



}
