package com.hanyang.identity.identityservicev4mono.acces.application;

import com.hanyang.identity.identityservicev4mono.access.application.AccountRoleCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.exception.AccountNotProvisionedException;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.AccountRoleProvisioningService;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AccountRoleCommandServiceTest {

    @Test
    void pendingAccountCannotBeAssignedRoleAndDoesNotTriggerProvisioning() {
        Account pending = Account.create(
                AccountId.newId(),
                EmployeeId.newId(),
                "emp001"
        );
        Application application = Application.create(
                ApplicationId.newId(),
                "OQC",
                "OQC"
        );
        Role role = Role.create(
                RoleId.newId(),
                application.getId(),
                "OQC_OPERATOR",
                "OQC Operator"
        );

        AccountRepository accountRepository = mock(AccountRepository.class);
        RoleRepository roleRepository = mock(RoleRepository.class);
        ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
        AccountRoleRepository accountRoleRepository = mock(AccountRoleRepository.class);
        AccountRoleProvisioningService provisioningService =
                mock(AccountRoleProvisioningService.class);

        when(accountRepository.findById(pending.getId()))
                .thenReturn(Optional.of(pending));
        when(roleRepository.findById(role.getId()))
                .thenReturn(Optional.of(role));

        AccountRoleCommandService service = new AccountRoleCommandService(
                accountRepository,
                roleRepository,
                applicationRepository,
                accountRoleRepository,
                provisioningService
        );

        assertThrows(
                AccountNotProvisionedException.class,
                () -> service.assign(pending.getId(), role.getId())
        );

        verify(accountRoleRepository, never()).save(any(AccountRole.class));
        verifyNoInteractions(provisioningService);
    }
}