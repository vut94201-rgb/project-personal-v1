package com.personal.identity.account.infrastructure.persistence.repository;

import com.personal.identity.account.infrastructure.persistence.entity.IdentityUserEntity;
import com.personal.identity.jpa.support.repository.SoftDeleteJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends SoftDeleteJpaRepository<IdentityUserEntity,Long> {



}
