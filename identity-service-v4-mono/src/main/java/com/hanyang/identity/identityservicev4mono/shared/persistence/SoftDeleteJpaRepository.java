package com.hanyang.identity.identityservicev4mono.shared.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface SoftDeleteJpaRepository<
        T extends SoftDeletableEntity,
        ID>
        extends BaseJpaRepository<T, ID> {

    Optional<T> findByIdAndDeletedFalse(ID id);

    boolean existsByIdAndDeletedFalse(ID id);

    Page<T> findAllByDeletedFalse(Pageable pageable);
}