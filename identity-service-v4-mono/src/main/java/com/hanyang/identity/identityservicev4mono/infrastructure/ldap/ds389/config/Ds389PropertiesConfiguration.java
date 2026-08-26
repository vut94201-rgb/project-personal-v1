package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config;


import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Ds389Properties.class)
public class Ds389PropertiesConfiguration {
}