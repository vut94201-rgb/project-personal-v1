package com.hanyang.identity.identityservicev4mono.infrastructure.generation;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgresEmployeeCodeGeneratorTest {

    @Test
    void generatesHumanReadableEmployeeCodeFromDatabaseSequence() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(
                "SELECT nextval('employee_code_seq')",
                Long.class
        )).thenReturn(42L);

        PostgresEmployeeCodeGenerator generator = new PostgresEmployeeCodeGenerator(jdbcTemplate);

        assertEquals("HY000042", generator.nextCode());
    }
}