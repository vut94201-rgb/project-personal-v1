package com.hanyang.identity.identityservicev4mono.shared.persistence;

import org.springframework.data.jpa.domain.Specification;

public final class SoftDeleteSpecifications {

    private SoftDeleteSpecifications() {
    }

    public static <T extends SoftDeletableEntity>
    Specification<T> notDeleted() {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isFalse(root.get("deleted"));
    }

    public static <T extends SoftDeletableEntity>
    Specification<T> deleted() {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.isTrue(root.get("deleted"));
    }
}