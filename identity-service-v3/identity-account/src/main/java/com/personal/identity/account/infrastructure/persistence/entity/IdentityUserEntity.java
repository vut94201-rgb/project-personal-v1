package com.personal.identity.account.infrastructure.persistence.entity;

import com.personal.identity.jpa.support.entity.base.SoftDeletableEntity;
import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.account.domain.enums.UserStatus;
import com.personal.shared.utility.PhoneNumberNormalizer;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
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

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  protected IdentityUserEntity() {}

  private IdentityUserEntity(
      String username, String email, String phoneNumber, LocalDate dateOfBirth, Gender gender) {

    this.username = normalizeUsername(username);
    this.email = normalizeEmail(email);
    this.phoneNumber = PhoneNumberNormalizer.normalize(phoneNumber);
    this.dateOfBirth = dateOfBirth;
    this.gender = Objects.requireNonNull(gender, "gender must not be null");
    this.status = UserStatus.PENDING;
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

  public void changeDateOfBirth(LocalDate dateOfBirth) {
    this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
  }

  public void changePhoneNUmber(String phoneNumber) {
    this.phoneNumber = PhoneNumberNormalizer.normalize(phoneNumber);
  }

  public void changeUserStatus(UserStatus userStatus) {
    this.status = Objects.requireNonNull(userStatus);
  }

  public static IdentityUserEntity create(
      String username,
      String email,
      String phoneNumber,
      LocalDate dateOfBirth,
      Gender gender,
      UUID keycloakUserId) {
    IdentityUserEntity entity =
        new IdentityUserEntity(username, email, phoneNumber, dateOfBirth, gender);
    entity.keycloakUserId = keycloakUserId;
    return entity;
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

  //  public void updateProfile(
  //      String email, String phoneNumber, Gender gender, LocalDate dateOfBirth,UserStatus
  // userStatus) {
  //
  //    if (Objects.nonNull(email)) this.email = normalizeEmail(email);
  //
  //    if (Objects.nonNull(phoneNumber))
  //      this.phoneNumber = PhoneNumberNormalizer.normalize(phoneNumber);
  //
  //    if (Objects.nonNull(gender)) this.gender = gender;
  //
  //    if (Objects.nonNull(dateOfBirth)) this.dateOfBirth = dateOfBirth;
  //  }
  public void updateProfile(
      String email,
      String phoneNumber,
      Gender gender,
      LocalDate dateOfBirth,
      UserStatus userStatus) {

    if (StringUtils.hasText(email)) changeEmail(email);
    if (StringUtils.hasText(phoneNumber)) changePhoneNUmber(phoneNumber);
    if (Objects.nonNull(dateOfBirth)) changeDateOfBirth(dateOfBirth);
    if (Objects.nonNull(gender)) changeGender(gender);
    if (Objects.nonNull(status)) changeUserStatus(status);
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
