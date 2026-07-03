package com.personal.identity.jpa.support.specification;

import com.personal.identity.jpa.support.entity.base.SoftDeletableEntity;
import org.springframework.data.jpa.domain.Specification;

public final class SoftDeleteSpecifications {

  private SoftDeleteSpecifications() {}

  public static <T extends SoftDeletableEntity> Specification<T> notDeleted() {

    return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"));
  }

  public static <T extends SoftDeletableEntity> Specification<T> deleted() {
    return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("deleted"));
  }
}
