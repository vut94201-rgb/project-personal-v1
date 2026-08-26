
UPDATE account_identity_provider_bindings binding
SET external_id = account.keycloak_subject,
    external_code = COALESCE(binding.external_code, account.username),
    updated_at = CURRENT_TIMESTAMP
FROM accounts account
WHERE binding.account_id = account.id
  AND binding.provider = 'KEYCLOAK'
  AND binding.external_id IS NULL
  AND account.keycloak_subject IS NOT NULL;


CREATE UNIQUE INDEX IF NOT EXISTS uk_account_idp_bindings_provider_external_id
    ON account_identity_provider_bindings (provider, external_id)
    WHERE external_id IS NOT NULL;

ALTER TABLE accounts
    DROP CONSTRAINT IF EXISTS uk_accounts_keycloak_subject;

ALTER TABLE accounts
    DROP COLUMN IF EXISTS keycloak_subject;
