CREATE TABLE webhooks (
    message_id VARCHAR(255) PRIMARY KEY NOT NULL,
    card_token VARCHAR(255) NOT NULL,
    last_four VARCHAR(4) NOT NULL 
);

