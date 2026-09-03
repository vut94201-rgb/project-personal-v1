package com.hanyang.identity.identityservicev4mono.employee.infrastructure.protection;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NationalIdentityProtectionProperties.class)
public class NationalIdentityProtectionConfiguration {
}