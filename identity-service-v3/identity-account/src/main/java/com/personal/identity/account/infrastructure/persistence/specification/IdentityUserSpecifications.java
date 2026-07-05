package com.personal.identity.account.infrastructure.persistence.specification;

import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.account.domain.enums.UserStatus;
import com.personal.identity.account.infrastructure.persistence.entity.IdentityUserEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.Objects;

public final class IdentityUserSpecifications {

  private IdentityUserSpecifications() {}

  public static Specification<IdentityUserEntity> usernameContains(String username) {

    if (!StringUtils.hasText(username)) {
      return Specification.unrestricted();
    }
    String pattern = "%" + username.strip().toLowerCase() + "%";

    return ((root, query, criteriaBuilder) ->
        criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern));
  }

  public static Specification<IdentityUserEntity> hasStatus(UserStatus userStatus) {

    if (Objects.isNull(userStatus)) return Specification.unrestricted();
    return ((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("status"), userStatus));
  }

  public static Specification<IdentityUserEntity> hasGender(Gender gender) {
    if (Objects.isNull(gender)) return Specification.unrestricted();
    return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("gender"), gender);
  }

  public static Specification<IdentityUserEntity> phoneNumberContains(String phoneNumber) {

    if (!StringUtils.hasText(phoneNumber)) return Specification.unrestricted();
    String pattern = "%" + phoneNumber.strip() + "%";
    return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("phoneNumber"), pattern);
  }

  public static Specification<IdentityUserEntity> emailContains(String email) {
    if (!StringUtils.hasText(email)) return Specification.unrestricted();

    String pattern = "%" + email.strip() + "%";
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), pattern);
  }
}
