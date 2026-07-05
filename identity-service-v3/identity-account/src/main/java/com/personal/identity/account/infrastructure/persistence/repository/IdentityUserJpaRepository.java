package com.personal.identity.account.infrastructure.persistence.repository;

import com.personal.identity.account.infrastructure.persistence.entity.IdentityUserEntity;
import com.personal.identity.jpa.support.repository.SoftDeleteJpaRepository;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdentityUserJpaRepository
    extends SoftDeleteJpaRepository<IdentityUserEntity, Long> {
  Optional<IdentityUserEntity> findByKeycloakUserIdAndDeletedFalse(UUID keycloakUserId);

  Optional<IdentityUserEntity> findByUsernameAndDeletedFalse(String username);

  boolean existsByUsernameIgnoreCaseAndDeletedFalse(String username);

  boolean existsByEmailIgnoreCaseAndDeletedFalse(String email);

  boolean existsByPhoneNumberIgnoreCaseAndDeletedFalse(String phoneNumber);


  boolean existsByUsernameIgnoreCaseAndDeletedFalseAndIdNot(String username, Long id);

  boolean existsByPhoneNumberIgnoreCaseAndDeletedFalseAndIdNot(String phoneNumber, Long id);

  boolean existsByEmailIgnoreCaseAndDeletedFalseAndIdNot(String email, Long id);

  @Query(
      """
         SELECT case
                 when  count(iu)>0 then  true
                 else false end
                    from IdentityUserEntity  iu
                            where iu.phoneNumber=trim(:phoneNumber) 
                            and (:id is null or iu.id != :id)
        """)
  boolean checkDuplicatePhoneNumberForUpdateAndCreate(
      @NotBlank @Param("phoneNumber") String phoneNumber, @Nullable @Param("id") Long id);

  @Query(
      """
        select case
                when count(iu)>0 then  true
                else false
                end
               from  IdentityUserEntity  iu where iu.email=lower(trim(:email)) and  (:id is null  or iu.id!=:id)
        """)
  boolean checkDuplicateEmailForUpdateAndCreate(
      @NotBlank @Param("email") String email, @Nullable @Param("id") Long id);

  @Query(
      """
        select case
                when count(iu)>0 then  true
                else  false
                end
                from  IdentityUserEntity  iu where  iu.username=trim(:username) and  (:id is  null or :id !=iu.id)
        """)
  boolean checkDuplicateUsernameForUpdateAndCreate(
      @NotBlank @Param("username") String username, @Nullable @Param("id") Long id);

  Optional<IdentityUserEntity> findByIdAndDeletedIsFalse(Long id);
}
