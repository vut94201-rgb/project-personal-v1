package com.hanyang.identity.identityservicev4mono.employee.application;

import com.hanyang.identity.identityservicev4mono.account.application.command.StartEmployeeOnboardingCommand;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.AccountJpaRepository;
import com.hanyang.identity.identityservicev4mono.employee.application.onbroading.EmployeeOnboardingService;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.NationalIdentityType;
import com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence.EmployeeJpaRepository;
import com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence.EmployeeNationalIdentityJpaRepository;
import com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence.EmployeeProfileJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "spring.batch.jdbc.initialize-schema=never",
        "outbox.worker.enabled=false",
        "integration.ds389.enabled=false",
        "management.health.ldap.enabled=false",
        "integration.keycloak.ldap-federation.enabled=false",
        "integration.keycloak.admin-client-secret=test-secret"
})
@ActiveProfiles("test")
@Import(EmployeeOnboardingTransactionIntegrationTest.FailureInjectionConfiguration.class)
class EmployeeOnboardingTransactionIntegrationTest {

    @Autowired
    private EmployeeOnboardingService onboardingService;

    @Autowired
    private EmployeeOnboardingPreparationService preparationService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmployeeJpaRepository employeeJpaRepository;

    @Autowired
    private EmployeeNationalIdentityJpaRepository nationalIdentityJpaRepository;

    @Autowired
    private EmployeeProfileJpaRepository employeeProfileJpaRepository;

    @Autowired
    private AccountJpaRepository accountJpaRepository;

    @BeforeEach
    void setUp() {
        accountJpaRepository.deleteAll();
        employeeProfileJpaRepository.deleteAll();
        nationalIdentityJpaRepository.deleteAll();
        employeeJpaRepository.deleteAll();

        reset(preparationService, accountRepository);

        when(accountRepository.existsByUsername("nguyenvana")).thenReturn(false);
        when(preparationService.prepare(any())).thenReturn(
                new EmployeeOnboardingPreparation(
                        EmployeeId.newId(),
                        "HY999999",
                        "VN",
                        NationalIdentityType.NATIONAL_ID_CARD,
                        "v1:test:ciphertext",
                        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        "2345"
                )
        );
        when(accountRepository.save(any()))
                .thenThrow(new IllegalStateException("forced account persistence failure"));
    }

    @Test
    void rollsBackEmployeeIdentityAndProfileWhenAccountPersistenceFails() {
        assertThrows(
                IllegalStateException.class,
                () -> onboardingService.start(command())
        );

        assertEquals(0, employeeJpaRepository.count());
        assertEquals(0, nationalIdentityJpaRepository.count());
        assertEquals(0, employeeProfileJpaRepository.count());
        assertEquals(0, accountJpaRepository.count());
    }

    private static StartEmployeeOnboardingCommand command() {
        return new StartEmployeeOnboardingCommand(
                "Nguyen Van A",
                "nguyenvana",
                "001204012345",
                "a@example.com",
                "0900000000",
                "Hanoi",
                LocalDate.of(2026, 8, 31)
        );
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FailureInjectionConfiguration {

        @Bean
        @Primary
        EmployeeOnboardingPreparationService testEmployeeOnboardingPreparationService() {
            return mock(EmployeeOnboardingPreparationService.class);
        }

        @Bean
        @Primary
        AccountRepository failingAccountRepository() {
            return mock(AccountRepository.class);
        }
    }
}