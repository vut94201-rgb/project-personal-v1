package com.hanyang.identity.identityservicev4mono.shared.operations.provisioning;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ProvisioningHealthProperties.class)
public class ProvisioningOperationsConfiguration {
}