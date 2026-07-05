package com.personal.identity.account.application.service;

import com.personal.identity.account.application.dto.IdentityUserSearchCriteria;
import com.personal.identity.account.application.dto.request.CreateIdentityUserRequest;
import com.personal.identity.account.application.dto.request.UpdateIdentityUserRequest;
import com.personal.identity.account.application.dto.response.IdentityUserResponse;
import com.personal.identity.account.application.mapper.IdentityUserMapper;
import com.personal.identity.account.exception.DuplicateEmailException;
import com.personal.identity.account.exception.DuplicatePhoneNumberException;
import com.personal.identity.account.exception.DuplicateUsernameException;
import com.personal.identity.account.exception.IdentityUserNotFoundException;
import com.personal.identity.account.infrastructure.persistence.entity.IdentityUserEntity;
import com.personal.identity.account.infrastructure.persistence.repository.IdentityUserJpaRepository;
import com.personal.identity.account.infrastructure.persistence.specification.IdentityUserSpecifications;
import com.personal.identity.jpa.support.specification.SoftDeleteSpecifications;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IdentityUserService {
  Clock clock;
  IdentityUserJpaRepository identityUserJpaRepository;
  IdentityUserMapper identityUserMapper;

  public IdentityUserService(
      Clock clock,
      IdentityUserJpaRepository identityUserJpaRepository,
      IdentityUserMapper identityUserMapper) {
    this.clock = clock;
    this.identityUserJpaRepository = identityUserJpaRepository;
    this.identityUserMapper = identityUserMapper;
  }

  @Transactional(readOnly = true)
  public IdentityUserResponse getUserById(Long id) {

    return identityUserMapper.toIdentityUserResponse(findActiveById(id));
  }

  @Transactional(readOnly = true)
  public Page<IdentityUserResponse> search(
      IdentityUserSearchCriteria userSearchCriteria, Pageable pageable) {
    Specification<IdentityUserEntity> specification =
        Specification.where(SoftDeleteSpecifications.<IdentityUserEntity>notDeleted())
            .and(IdentityUserSpecifications.usernameContains(userSearchCriteria.getUsername()))
            .and(IdentityUserSpecifications.emailContains(userSearchCriteria.getEmail()))
            .and(
                IdentityUserSpecifications.phoneNumberContains(userSearchCriteria.getPhoneNumber()))
            .and(IdentityUserSpecifications.hasStatus(userSearchCriteria.getStatus()))
            .and(IdentityUserSpecifications.hasGender(userSearchCriteria.getGender()));
    return identityUserJpaRepository
        .findAll(specification, pageable)
        .map(identityUserMapper::toIdentityUserResponse);
  }

  @Transactional
  public IdentityUserResponse update(Long id, UpdateIdentityUserRequest updateIdentityUserRequest) {
    IdentityUserEntity entity = findActiveById(id);
    validateDuplicateForUpdate(
        updateIdentityUserRequest.email(), null, updateIdentityUserRequest.phoneNumber(), id);
    //    updateEntityFromUpdateIdentityUserRequest(entity, updateIdentityUserRequest);
    entity.updateProfile(
        updateIdentityUserRequest.email(),
        updateIdentityUserRequest.phoneNumber(),
        updateIdentityUserRequest.gender(),
        updateIdentityUserRequest.dateOfBirth(),
        updateIdentityUserRequest.status());
    return identityUserMapper.toIdentityUserResponse(identityUserJpaRepository.save(entity));
  }

  @Transactional
  public IdentityUserResponse createNew(CreateIdentityUserRequest request) {
    validateUniqueUsername(request.username(), null);
    validateUniqueEmail(request.email(), null);

    IdentityUserEntity entity =
        IdentityUserEntity.create(
            request.username(),
            request.email(),
            request.phoneNumber(),
            request.dateOfBirth(),
            request.gender(),
            null);
    IdentityUserEntity saved = identityUserJpaRepository.save(entity);
    return identityUserMapper.toIdentityUserResponse(entity);
  }

  @Transactional
  public void softDelete(Long id, String actor) {
    IdentityUserEntity identityUser = findActiveById(id);
    identityUser.softDelete(actor, Instant.now(clock));
  }

  private IdentityUserEntity findActiveById(Long id) {
    return identityUserJpaRepository
        .findByIdAndDeletedIsFalse(id)
        .orElseThrow(() -> new IdentityUserNotFoundException(id));
  }

  private void validateUniqueUsername(String username, Long currentId) {
    if (identityUserJpaRepository.checkDuplicateUsernameForUpdateAndCreate(username, currentId))
      throw new DuplicateUsernameException(username);
  }

  private void validateUniqueEmail(String email, Long currentId) {

    if (identityUserJpaRepository.checkDuplicateEmailForUpdateAndCreate(email, currentId)) {
      throw new DuplicateEmailException(email);
    }
  }

  private void validateUniquePhoneNumberForUpdate(String phoneNumber, Long currentId) {
    if (identityUserJpaRepository.checkDuplicatePhoneNumberForUpdateAndCreate(
        phoneNumber, currentId)) throw new DuplicatePhoneNumberException(phoneNumber);
  }

  private void validateDuplicateForUpdate(
      String email, String username, String phoneNumber, Long currentId) {
    if (StringUtils.hasText(email)) validateUniqueEmail(email, currentId);
    if (StringUtils.hasText(username)) validateUniqueUsername(username, currentId);
    if (StringUtils.hasText(phoneNumber))
      validateUniquePhoneNumberForUpdate(phoneNumber, currentId);
  }

  private void updateEntityFromUpdateIdentityUserRequest(
      IdentityUserEntity identityUserEntity, UpdateIdentityUserRequest updateIdentityUserRequest) {
    if (StringUtils.hasText(updateIdentityUserRequest.email()))
      identityUserEntity.changeEmail(updateIdentityUserRequest.email());
    if (StringUtils.hasText(updateIdentityUserRequest.phoneNumber()))
      identityUserEntity.changePhoneNUmber(updateIdentityUserRequest.phoneNumber());
    if (Objects.nonNull(updateIdentityUserRequest.dateOfBirth()))
      identityUserEntity.changeDateOfBirth(updateIdentityUserRequest.dateOfBirth());
    if (Objects.nonNull(updateIdentityUserRequest.gender()))
      identityUserEntity.changeGender(updateIdentityUserRequest.gender());
    if (Objects.nonNull(updateIdentityUserRequest.status()))
      identityUserEntity.changeUserStatus(updateIdentityUserRequest.status());
  }
}
