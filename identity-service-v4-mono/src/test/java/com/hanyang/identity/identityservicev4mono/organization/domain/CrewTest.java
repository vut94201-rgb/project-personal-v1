package com.hanyang.identity.identityservicev4mono.organization.domain;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CrewTest {

    @Test
    void createNormalizesCodeAndStartsActive() {
        DepartmentId departmentId = DepartmentId.newId();

        Crew crew = Crew.create(CrewId.newId(), departmentId, "a", "Crew A");

        assertEquals(departmentId, crew.getDepartmentId());
        assertEquals("A", crew.getCode());
        assertEquals("Crew A", crew.getName());
        assertEquals(OrganizationReferenceStatus.ACTIVE, crew.getStatus());
    }

    @Test
    void renameAndDisableKeepIdentityAndDepartmentStable() {
        Crew crew = Crew.create(CrewId.newId(), DepartmentId.newId(), "B", "Crew B");
        CrewId id = crew.getId();
        DepartmentId departmentId = crew.getDepartmentId();

        crew.rename("OQC SSD Crew B");
        crew.disable();

        assertEquals(id, crew.getId());
        assertEquals(departmentId, crew.getDepartmentId());
        assertEquals("B", crew.getCode());
        assertEquals("OQC SSD Crew B", crew.getName());
        assertEquals(OrganizationReferenceStatus.DISABLED, crew.getStatus());
    }
}