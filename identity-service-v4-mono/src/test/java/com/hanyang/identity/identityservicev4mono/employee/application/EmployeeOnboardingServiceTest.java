package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.account.application.AccountCommandService;
import com.hanyang.identity.identityservicev4mono.account.application.command.CreateAccountCommand;
import com.hanyang.identity.identityservicev4mono.account.application.command.StartEmployeeOnboardingCommand;
import com.hanyang.identity.identityservicev4mono.account.application.exception.UsernameAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.application.exception.EmployeeEmailAlreadyExistsException;
import com.hanyang.identity.identityservicev4mono.employee.application.onbroading.EmployeeOnboardingService;
import com.hanyang.identity.identityservicev4mono.employee.domain.*;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmployeeOnboardingServiceTest {

    @Test
    void persistsEmployeeIdentityAndProfileThenDelegatesPendingAccountCreationWithSameEmployeeId() {
        EmployeeOnboardingPreparationService preparationService =
                mock(EmployeeOnboardingPreparationService.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeNationalIdentityRepository nationalIdentityRepository =
                mock(EmployeeNationalIdentityRepository.class);
        EmployeeProfileRepository employeeProfileRepository =
                mock(EmployeeProfileRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountCommandService accountCommandService = mock(AccountCommandService.class);

        EmployeeId employeeId = EmployeeId.newId();
        Account pendingAccount = Account.create(
                AccountId.newId(),
                employeeId,
                "nguyenvana"
        );

        when(accountRepository.existsByUsername("nguyenvana")).thenReturn(false);
        when(preparationService.prepare(any())).thenReturn(
                new EmployeeOnboardingPreparation(
                        employeeId,
                        "HY000042",
                        "VN",
                        NationalIdentityType.NATIONAL_ID_CARD,
                        "v1:iv:ciphertext",
                        "fingerprint",
                        "2345"
                )
        );
        when(accountCommandService.create(any(CreateAccountCommand.class)))
                .thenReturn(pendingAccount);

        EmployeeOnboardingService service = new EmployeeOnboardingService(
                preparationService,
                employeeRepository,
                nationalIdentityRepository,
                employeeProfileRepository,
                accountRepository,
                accountCommandService
        );

        EmployeeOnboardingResult result = service.start(command());

        assertEquals(employeeId, result.employeeId());
        assertEquals("HY000042", result.employeeCode());
        assertEquals("********2345", result.maskedNationalIdentity());
        assertNotNull(result.nationalIdentityId());
        assertEquals(pendingAccount.getId(), result.accountId());
        assertEquals("nguyenvana", result.username());
        assertEquals(AccountStatus.PENDING, result.accountStatus());

        ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
        ArgumentCaptor<EmployeeNationalIdentity> identityCaptor =
                ArgumentCaptor.forClass(EmployeeNationalIdentity.class);
        ArgumentCaptor<EmployeeProfile> profileCaptor = ArgumentCaptor.forClass(EmployeeProfile.class);
        ArgumentCaptor<CreateAccountCommand> accountCommandCaptor =
                ArgumentCaptor.forClass(CreateAccountCommand.class);

        var order = inOrder(
                accountRepository,
                preparationService,
                employeeRepository,
                nationalIdentityRepository,
                employeeProfileRepository,
                accountCommandService
        );
        order.verify(accountRepository).existsByUsername("nguyenvana");
        order.verify(preparationService).prepare(any());
        order.verify(employeeRepository).save(employeeCaptor.capture());
        order.verify(nationalIdentityRepository).save(identityCaptor.capture());
        order.verify(employeeProfileRepository).save(profileCaptor.capture());
        order.verify(accountCommandService).create(accountCommandCaptor.capture());

        assertEquals(employeeId, employeeCaptor.getValue().getId());
        assertEquals(employeeId, identityCaptor.getValue().getEmployeeId());
        assertEquals(employeeId, profileCaptor.getValue().getEmployeeId());
        assertEquals(employeeId, accountCommandCaptor.getValue().employeeId());
        assertEquals("nguyenvana", accountCommandCaptor.getValue().username());
    }

    @Test
    void rejectsDuplicateProfileEmailBeforePersistingRegistration() {
        EmployeeOnboardingPreparationService preparationService =
                mock(EmployeeOnboardingPreparationService.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeNationalIdentityRepository nationalIdentityRepository =
                mock(EmployeeNationalIdentityRepository.class);
        EmployeeProfileRepository employeeProfileRepository =
                mock(EmployeeProfileRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountCommandService accountCommandService = mock(AccountCommandService.class);

        EmployeeId employeeId = EmployeeId.newId();
        when(accountRepository.existsByUsername("nguyenvana")).thenReturn(false);
        when(preparationService.prepare(any())).thenReturn(
                new EmployeeOnboardingPreparation(
                        employeeId,
                        "HY000042",
                        "VN",
                        NationalIdentityType.NATIONAL_ID_CARD,
                        "v1:iv:ciphertext",
                        "fingerprint",
                        "2345"
                )
        );
        when(employeeProfileRepository.existsByEmailIgnoreCaseAndEmployeeIdNot(
                "a@example.com", employeeId)).thenReturn(true);

        EmployeeOnboardingService service = new EmployeeOnboardingService(
                preparationService,
                employeeRepository,
                nationalIdentityRepository,
                employeeProfileRepository,
                accountRepository,
                accountCommandService
        );

        assertThrows(EmployeeEmailAlreadyExistsException.class, () -> service.start(command()));

        verify(employeeProfileRepository).existsByEmailIgnoreCaseAndEmployeeIdNot(
                "a@example.com", employeeId);
        verify(employeeRepository, never()).save(any());
        verify(nationalIdentityRepository, never()).save(any());
        verify(employeeProfileRepository, never()).save(any());
        verifyNoInteractions(accountCommandService);
    }

    @Test
    void rejectsDuplicateNormalizedUsernameBeforePreparingOrPersistingRegistration() {
        EmployeeOnboardingPreparationService preparationService =
                mock(EmployeeOnboardingPreparationService.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        EmployeeNationalIdentityRepository nationalIdentityRepository =
                mock(EmployeeNationalIdentityRepository.class);
        EmployeeProfileRepository employeeProfileRepository =
                mock(EmployeeProfileRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        AccountCommandService accountCommandService = mock(AccountCommandService.class);

        when(accountRepository.existsByUsername("nguyenvana")).thenReturn(true);

        EmployeeOnboardingService service = new EmployeeOnboardingService(
                preparationService,
                employeeRepository,
                nationalIdentityRepository,
                employeeProfileRepository,
                accountRepository,
                accountCommandService
        );

        assertThrows(
                UsernameAlreadyExistsException.class,
                () -> service.start(commandWithUsername("  nguyenvana  "))
        );

        verify(accountRepository).existsByUsername("nguyenvana");
        verifyNoInteractions(
                preparationService,
                employeeRepository,
                nationalIdentityRepository,
                employeeProfileRepository,
                accountCommandService
        );
    }

    @Test
    void startMethodDeclaresTransactionalBoundary() throws Exception {
        var method = EmployeeOnboardingService.class.getMethod(
                "start",
                StartEmployeeOnboardingCommand.class
        );

        assertTrue(method.isAnnotationPresent(Transactional.class));
    }

    private static StartEmployeeOnboardingCommand command() {
        return commandWithUsername("nguyenvana");
    }

    private static StartEmployeeOnboardingCommand commandWithUsername(String username) {
        return new StartEmployeeOnboardingCommand(
                "Nguyen Van A",
                username,
                "001204012345",
                "a@example.com",
                "0900000000",
                "Hanoi",
                LocalDate.of(2026, 8, 31)
        );
    }
}