-- V025 intentionally created provider bindings without outbox work because no
-- runtime handler existed yet. The handler is introduced together with the
-- Keycloak machine-identity adapter in this release, so existing unsynchronized
-- service principals can now safely enter normal reconciliation.

INSERT INTO outbox_events
(
    id,
    aggregate_type,
    aggregate_id,
    event_type,
    payload,
    status,
    attempt_count,
    available_at,
    version,
    created_at,
    updated_at
)
SELECT
    gen_random_uuid(),
    'SERVICE_PRINCIPAL',
    binding.service_principal_id::TEXT,
    'SERVICE_PRINCIPAL_PROVISIONING_REQUESTED',
    NULL,
    'PENDING',
    0,
    CURRENT_TIMESTAMP,
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM service_principal_identity_provider_bindings binding
WHERE binding.provider = 'KEYCLOAK'
  AND binding.sync_status <> 'SYNCED'
  AND NOT EXISTS
  (
      SELECT 1
      FROM outbox_events existing
      WHERE existing.aggregate_type = 'SERVICE_PRINCIPAL'
        AND existing.aggregate_id = binding.service_principal_id::TEXT
        AND existing.event_type = 'SERVICE_PRINCIPAL_PROVISIONING_REQUESTED'
        AND existing.status IN ('PENDING', 'PROCESSING')
  );
