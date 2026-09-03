package com.hanyang.identity.identityservicev4mono.employee.application.onbroading;

import com.hanyang.identity.identityservicev4mono.account.application.AccountCommandService;
import com.hanyang.identity.identityservicev4mono.account.application.command.CreateAccountCommand;
import com.hanyang.identity.identityservicev4mono.account.application.exception.UsernameAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeOnboardingResult;
import com.hanyang.identity.identityservicev4mono.account.application.command.StartEmployeeOnboardingCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeOnboardingPreparation;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeOnboardingPreparationService;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeEmailAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.employee.domain.*;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@IdentityAdminAccess
public class EmployeeOnboardingService {
  private final EmployeeOnboardingPreparationService preparationService;
  private final EmployeeRepository employeeRepository;
  private final EmployeeNationalIdentityRepository nationalIdentityRepository;
  private final EmployeeProfileRepository employeeProfileRepository;
  private final AccountRepository accountRepository;
  private final AccountCommandService accountCommandService;

  public EmployeeOnboardingService(
          EmployeeOnboardingPreparationService preparationService,
          EmployeeRepository employeeRepository,
          EmployeeNationalIdentityRepository nationalIdentityRepository,
          EmployeeProfileRepository employeeProfileRepository,
          AccountRepository accountRepository,
          AccountCommandService accountCommandService) {
    this.preparationService = preparationService;
    this.employeeRepository = employeeRepository;
    this.nationalIdentityRepository = nationalIdentityRepository;
    this.employeeProfileRepository = employeeProfileRepository;
    this.accountRepository = accountRepository;
    this.accountCommandService = accountCommandService;
  }

  @Transactional
  public EmployeeOnboardingResult start(StartEmployeeOnboardingCommand command) {
    String username = normalizeUsername(command.username());
    if (accountRepository.existsByUsername(username)) {
      throw new UsernameAlreadyExistsException(username);
    }

    EmployeeOnboardingPreparation preparation = preparationService.prepare(command);

    if (StringUtils.hasText(command.email())
            && employeeProfileRepository.existsByEmailIgnoreCaseAndEmployeeIdNot(
            command.email().trim(), preparation.employeeId())) {
      throw new EmployeeEmailAlreadyExistsException(command.email().trim());
    }

    Employee employee =
            Employee.create(preparation.employeeId(), preparation.employeeCode(), command.fullName());

    EmployeeNationalIdentity nationalIdentity =
            EmployeeNationalIdentity.create(
                    EmployeeNationalIdentityId.newId(),
                    preparation.employeeId(),
                    preparation.countryCode(),
                    preparation.identityType(),
                    preparation.encryptedNumber(),
                    preparation.numberFingerprint(),
                    preparation.lastFour());

    EmployeeProfile employeeProfile =
            EmployeeProfile.create(
                    preparation.employeeId(),
                    command.email(),
                    command.phone(),
                    command.address(),
                    command.joinDate());

    employeeRepository.save(employee);
    nationalIdentityRepository.save(nationalIdentity);
    employeeProfileRepository.save(employeeProfile);

    // Delegate Account creation to the account module so registration reuses the
    // canonical PENDING lifecycle and DS389 desired-state/outbox scheduling.
    // Keycloak is intentionally NOT requested here; AccountActivationCoordinator
    // schedules it only after the directory identity is current.
    Account account =
            accountCommandService.create(
                    new CreateAccountCommand(preparation.employeeId(), username));

    return new EmployeeOnboardingResult(
            preparation.employeeId(),
            preparation.employeeCode(),
            nationalIdentity.getId(),
            nationalIdentity.getMaskedNumber(),
            account.getId(),
            account.getUsername(),
            account.getStatus());
  }

  private static String normalizeUsername(String username) {
    if (username == null) {
      throw new IllegalArgumentException("username must not be null");
    }

    String normalized = username.trim();
    if (normalized.isEmpty()) {
      throw new IllegalArgumentException("username must not be blank");
    }
    return normalized;
  }
}
