package com.personal.identity.account.application.dto;

import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.account.domain.enums.UserStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IdentityUserSearchCriteria {
  Long id;
  UUID keycloakUserId;
  String username;
  String email;
  String phoneNumber;
  Gender gender;
  UserStatus status;
  Instant createdAt;
  Instant updatedAt;
}
