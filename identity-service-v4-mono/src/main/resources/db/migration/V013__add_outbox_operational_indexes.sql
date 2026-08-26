CREATE INDEX idx_outbox_events_pending_created_at
    ON outbox_events (created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_events_dead_updated_at
    ON outbox_events (updated_at DESC)
    WHERE status = 'DEAD';
