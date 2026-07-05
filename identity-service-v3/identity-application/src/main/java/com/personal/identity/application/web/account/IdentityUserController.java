package com.personal.identity.application.web.account;

import com.personal.identity.account.application.dto.IdentityUserSearchCriteria;
import com.personal.identity.account.application.dto.request.CreateIdentityUserRequest;
import com.personal.identity.account.application.dto.request.UpdateIdentityUserRequest;
import com.personal.identity.account.application.dto.response.IdentityUserResponse;
import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.account.domain.enums.UserStatus;
import com.personal.identity.account.application.service.IdentityUserService;
import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityUserController {

  IdentityUserService identityUserService;

  public IdentityUserController(IdentityUserService identityUserService) {
    this.identityUserService = identityUserService;
  }

  @PostMapping
  public ResponseEntity<IdentityUserResponse> create(
      @Valid @RequestBody CreateIdentityUserRequest request) {

    IdentityUserResponse response = identityUserService.createNew(request);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public IdentityUserResponse getById(@PathVariable Long id) {
    return identityUserService.getUserById(id);
  }

  @GetMapping
  public Page<IdentityUserResponse> search(
      @RequestParam(required = false) String username,
      @RequestParam(required = false) String email,
      @RequestParam(required = false) String phoneNumber,
      @RequestParam(required = false) UserStatus status,
      @RequestParam(required = false) Gender gender,
      Pageable pageable) {

    IdentityUserSearchCriteria criteria =
        IdentityUserSearchCriteria.builder()
            .username(username)
            .email(email)
            .phoneNumber(phoneNumber)
            .status(status)
            .gender(gender)
            .build();

    return identityUserService.search(criteria, pageable);
  }

  @PatchMapping("/{id}")
  public IdentityUserResponse update(
      @PathVariable Long id, @Valid @RequestBody UpdateIdentityUserRequest request) {

    return identityUserService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, Authentication authentication) {
    identityUserService.softDelete(id, authentication.getName());
  }
}
