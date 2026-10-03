# Pay Invariants

A payment company sends `POST /webhooks/processor` with a card token, the last four digits, and a `Processor-Signature` header. The app rebuilds that stamp with HMAC-SHA256 and a shared secret, and compares the two. Only a matching, recent, first-time message is saved. A customer then sends `POST /charges` with an amount, that `card_token`, and an `Idempotency-Key`. The app charges the card only if that token was already saved. The same key and the same body return the saved response and do not charge again.

## Rules

1. The same charge request twice charges the customer once. The same key with a different body is a 409.
2. A webhook is checked before it is trusted. Only then is the card token saved.
3. A charge can only use a card token a webhook already saved.

## Walkthrough

1. The payment company sends the webhook.
2. The app checks the signature, the age, and that this message id is new.
3. A message that passes is saved as a card token and the last four digits.
4. The customer requests a charge and sends the idempotency key with that request.
5. A new key is checked against the saved tokens. A missing token is rejected and no charge is written.
6. A known token is charged once. The key and the response are saved, so a repeat returns that response.

## Request path

```mermaid
flowchart LR
  processor["Payment company"] -->|"POST /webhooks/processor + Processor-Signature"| check["Check the stamp"]
  check --> save["Save card_token and last four"]
  client["Customer"] -->|"POST /charges + card_token + Idempotency-Key"| lookup["Look up the token"]
  save --> lookup
  lookup --> charge["Write one charge"]
  charge --> again["Same key again returns that charge"]
