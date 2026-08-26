ALTER TABLE organizational_assignments
    ADD COLUMN crew_id UUID;

ALTER TABLE organizational_assignments
    ADD CONSTRAINT fk_org_assignments_crew
        FOREIGN KEY (crew_id)
        REFERENCES crews (id);

CREATE INDEX idx_org_assignments_crew_id
    ON organizational_assignments (crew_id);
