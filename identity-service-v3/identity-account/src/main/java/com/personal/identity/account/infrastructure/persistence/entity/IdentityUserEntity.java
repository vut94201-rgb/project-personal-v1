package com.personal.identity.account.infrastructure.persistence.entity;

import com.personal.identity.jpa.support.entity.base.SoftDeletableEntity;
import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.account.domain.enums.UserStatus;
import jakarta.persistence.*;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "identity_users")
public class IdentityUserEntity extends SoftDeletableEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "identity_user_seq_generator")
  @SequenceGenerator(
      name = "identity_user_seq_generator",
      sequenceName = "identity_user_seq",
      allocationSize = 1)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "keycloak_user_id", nullable = false, updatable = false, unique = true)
  private UUID keycloakUserId;

  @Column(name = "username", nullable = false, length = 100, unique = true)
  private String username;

  @Column(name = "status", nullable = false, length = 1)
  private UserStatus status;

  @Column(name = "email", nullable = false, length = 320, unique = true)
  private String email;

  @Column(name = "phone_number", length = 80)
  private String phoneNumber;

  @Column(name = "gender", nullable = false, length = 1)
  private Gender gender;

  protected IdentityUserEntity() {}

  public IdentityUserEntity(UUID keycloakUserId, String username, String email, Gender gender) {
    this.keycloakUserId = Objects.requireNonNull(keycloakUserId, "keycloakUserId must not be null");

    this.username = normalizeUsername(username);
    this.email = normalizeEmail(email);
    this.gender = requireGender(gender);
    this.status = UserStatus.PENDING;
  }

  public Long getId() {
    return id;
  }

  public UUID getKeycloakUserId() {
    return keycloakUserId;
  }

  public String getUsername() {
    return username;
  }

  public UserStatus getStatus() {
    return status;
  }

  public String getEmail() {
    return email;
  }

  public Gender getGender() {
    return gender;
  }

  public void changeUsername(String username) {
    this.username = normalizeUsername(username);
  }

  public void activate() {
    this.status = UserStatus.ACTIVE;
  }

  public void deactivate() {
    this.status = UserStatus.INACTIVE;
  }

  public void lock() {
    this.status = UserStatus.LOCKED;
  }

  public void changeGender(Gender gender) {
    this.gender = requireGender(gender);
  }

  public void changeEmail(String email) {
    this.email = normalizeEmail(email);
  }

  //  private static String requireValidUsername(String username) {
  //    if (!StringUtils.hasText(username)) {
  //      throw new IllegalArgumentException("username must not be blank");
  //    }
  //
  //    String normalizedUsername = username.trim();
  //
  //    if (normalizedUsername.length() > 100) {
  //      throw new IllegalArgumentException("username must not exceed 100 characters");
  //    }
  //
  //    return normalizedUsername;
  //  }
  private static String normalizeUsername(String username) {
    if (!StringUtils.hasText(username)) {
      throw new IllegalArgumentException("username must not be blank");
    }

    String normalized = username.trim();

    if (normalized.length() > 100) {
      throw new IllegalArgumentException("username must not exceed 100 characters");
    }

    return normalized;
  }

  private static String normalizeEmail(String email) {
    if (!StringUtils.hasText(email)) {
      throw new IllegalArgumentException("email must not be blank");
    }

    String normalized = email.trim().toLowerCase(Locale.ROOT);

    if (normalized.length() > 320) {
      throw new IllegalArgumentException("email must not exceed 320 characters");
    }

    return normalized;
  }

  private static Gender requireGender(Gender gender) {
    return Objects.requireNonNull(gender, "gender must not be null");
  }
}
