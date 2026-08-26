package com.hanyang.identity.identityservicev4mono.security;



import com.hanyang.identity.identityservicev4mono.security.authorization.IdentitySecurityRoles;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(properties = {
        "spring.cloud.consul.enabled=false",
        "spring.cloud.consul.discovery.enabled=false",
        "spring.cloud.consul.discovery.register=false",
        "outbox.worker.enabled=false",
        "operations.provisioning-health.provider-probe-enabled=false",
        "integration.keycloak.admin-client-secret=test-secret"
})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "keycloak"})
@Import(KeycloakResourceServerHttpSecurityIntegrationTest.JwtDecoderTestConfiguration.class)
class KeycloakResourceServerHttpSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthAndInfoRemainPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    void missingTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/applications/get-active"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void invalidBearerTokenIsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/applications/get-active")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void viewerCanReadIdentityData() throws Exception {
        mockMvc.perform(get("/api/v1/applications/get-active")
                        .with(jwt().authorities(() -> IdentitySecurityRoles.VIEWER_AUTHORITY)))
                .andExpect(status().isOk());
    }

    @Test
    void unrelatedApplicationRoleCannotReadIdentityData() throws Exception {
        mockMvc.perform(get("/api/v1/applications/get-active")
                        .with(jwt().authorities(() -> "ROLE_OQC_OPERATOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotMutateEmployeeData() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .with(jwt().authorities(() -> IdentitySecurityRoles.VIEWER_AUTHORITY))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeCode": "SEC-VIEWER",
                                  "fullName": "Security Viewer"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanMutateEmployeeData() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                        .with(jwt().authorities(() -> IdentitySecurityRoles.ADMIN_AUTHORITY))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employeeCode": "SEC-ADMIN",
                                  "fullName": "Security Admin"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class JwtDecoderTestConfiguration {

        @Bean
        @Primary
        JwtDecoder jwtDecoder() {
            return token -> {
                throw new JwtException("Rejected test bearer token");
            };
        }
    }
}