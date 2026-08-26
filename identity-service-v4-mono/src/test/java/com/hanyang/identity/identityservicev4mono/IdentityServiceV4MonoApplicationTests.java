package com.hanyang.identity.identityservicev4mono;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:identity-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.open-in-view=false",
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "spring.batch.jdbc.initialize-schema=never",
        "outbox.worker.enabled=false",
        "integration.keycloak.admin-client-secret=test-secret"
})
@ActiveProfiles("test")
class IdentityServiceV4MonoApplicationTests {

    @Test
    void contextLoads() {
    }

}