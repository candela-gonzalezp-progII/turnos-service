-- Receptor idempotente: cada eventId de Kafka se procesa una sola vez.
CREATE TABLE processed_event (
    event_id     VARCHAR(100) PRIMARY KEY,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);
