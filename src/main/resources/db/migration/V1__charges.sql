CREATE TABLE charges (
    charge_id UUID PRIMARY KEY NOT NULL,
    amount INT NOT NULL
);

CREATE TABLE idempotency_keys (
    idempotency_key VARCHAR(255) UNIQUE NOT NULL,
    body_hash VARCHAR(64) NOT NULL,
    http_status INT NOT NULL,
    response_body TEXT NOT NULL
);
