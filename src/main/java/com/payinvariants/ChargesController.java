package com.payinvariants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
class ChargesController {

    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ProcessorEventRepository processorEventRepository;
    private final ChargesRepository chargesRepository;
    private final ObjectMapper objectMapper;

    ChargesController(IdempotencyKeyRepository idempotencyKeyRepository, 
                  ProcessorEventRepository processorEventRepository,
                  ChargesRepository chargesRepository,
                  ObjectMapper objectMapper) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.processorEventRepository = processorEventRepository;
        this.chargesRepository = chargesRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/charges")
    public ResponseEntity<String> postCharges (
        @RequestHeader("Idempotency-Key") String key,
        @RequestHeader(value = "Crash-After-Save", required = false) String crashAfterSave,
        @RequestBody String body)throws Exception {
            
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(body.getBytes(StandardCharsets.UTF_8));
        String hex = HexFormat.of().formatHex(hash);
            
        // Check whether the hex already exists in our idempotencyKeyRepository
        var foundIdemId = idempotencyKeyRepository.findById(key);

        // If it exists, check if it matches the hex -> if it does, return the status, JSON, body
        if(foundIdemId.isPresent()){
            IdempotencyKey row = foundIdemId.get();
            if (row.getBodyHash().equals(hex)){
                return ResponseEntity.status(row.getHttpStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(row.getResponseBody());
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Charge not found with key: " + hex);
            }
            
        } else {

            // Add the new key to the idempotencyKey repo and add the new amount to the charges repo.
            JsonNode tree = objectMapper.readTree(body);
            int amount = tree.get("amount").asInt();
            String card_token = tree.get("card_token").asString();

            Charges charges = new Charges();
            charges.setAmount(amount);
            

            // Check if the card_token exists in our processorEventRepository;
            var foundCardToken = processorEventRepository.findByCardToken(card_token);

            if(foundCardToken.isEmpty()){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card_token was not found: " + foundCardToken);
            } else {
                charges.setCardToken(card_token);
            }

            Charges saved = chargesRepository.save(charges);

            String json = objectMapper.writeValueAsString(java.util.Map.of(
                "id", saved.getId(),
                "amount", saved.getAmount(),
                "card_token", saved.getCardToken()
            ));

            IdempotencyKey idem = new IdempotencyKey();
            idem.setIdempotencyKey(key);
            idem.setBodyHash(hex);
            idem.setHttpStatus(200);
            idem.setResponseBody(json);
            idempotencyKeyRepository.save(idem);

            // Specifically for the crash-after-save test
            if ("true".equals(crashAfterSave)) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "crash after save");
            }

            // Return the body
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
        }
    }
} 