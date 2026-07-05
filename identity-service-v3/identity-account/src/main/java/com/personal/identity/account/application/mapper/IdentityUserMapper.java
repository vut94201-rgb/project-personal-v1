package com.personal.identity.account.application.mapper;

import com.personal.identity.account.application.dto.request.UpdateIdentityUserRequest;
import com.personal.identity.account.application.dto.response.IdentityUserResponse;
import com.personal.identity.account.infrastructure.persistence.entity.IdentityUserEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = true))
public interface IdentityUserMapper {
  @Mapping(target = "id", ignore = false)

  IdentityUserResponse toIdentityUserResponse(IdentityUserEntity identityUserEntity);

//  default void updateEntityFromUpdateIdentityUserRequest(
//      IdentityUserEntity identityUserEntity, UpdateIdentityUserRequest updateIdentityUserRequest) {
//    if (StringUtils.hasText(updateIdentityUserRequest.email()))
//      identityUserEntity.changeEmail(updateIdentityUserRequest.email());
//    if (StringUtils.hasText(updateIdentityUserRequest.phoneNumber()))
//      identityUserEntity.changePhoneNUmber(updateIdentityUserRequest.phoneNumber());
//    if (Objects.nonNull(updateIdentityUserRequest.dateOfBirth()))
//      identityUserEntity.changeDateOfBirth(updateIdentityUserRequest.dateOfBirth());
//    if (Objects.nonNull(updateIdentityUserRequest.gender()))
//      identityUserEntity.changeGender(updateIdentityUserRequest.gender());
//    if (Objects.nonNull(updateIdentityUserRequest.status()))
//      identityUserEntity.changeUserStatus(updateIdentityUserRequest.status());
//  }
}
