package com.payinvariants;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
class ProcessorEventController {

    private final ProcessorEventRepository processorEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${processor.webhook-secret}") String secret;
    @Value("${processor.webhook-tolerance-seconds}") long tolerance;

    ProcessorEventController(ProcessorEventRepository processorEventRepository, ObjectMapper objectMapper){
        this.processorEventRepository = processorEventRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/webhooks/processor")
    public ResponseEntity<String> postWebhooks (
        @RequestHeader("Processor-Signature") String signature,
        @RequestBody String body) throws Exception {

        String[] parts = signature.split(",");

        long timestamp; 

        if (parts.length != 2){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Signature is not correct");
        }

        String[] timePart = parts[0].split("=", 2);
        String[] stampPart = parts[1].split("=", 2);
        
        String v1 = stampPart[1];

        if (timePart.length != 2 || stampPart.length != 2 || !timePart[0].equals("t") || !stampPart[0].equals("v1")){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Signature is not correct: " + signature);
        }

        try {
            timestamp = Long.parseLong(timePart[1]);
        }
        catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid payload format received", ex); 
        }

        String signed = timestamp + "." + body;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] ours = mac.doFinal((signed).getBytes(StandardCharsets.UTF_8));
        byte[] theirs = HexFormat.of().parseHex(v1);

        if (MessageDigest.isEqual(ours, theirs) == false){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stamps do not match");
        }

        if (Instant.now().getEpochSecond() - timestamp > tolerance){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is too old: " + (Instant.now().getEpochSecond() - timestamp));
        }

        JsonNode tree = objectMapper.readTree(body);
        String id = tree.get("id").asText();
        String card_token = tree.get("card_token").asText();
        String last_four = tree.get("last_four").asText();

        var found = processorEventRepository.findById(id);

        if (found.isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }

        ProcessorEvent processorEvent = new ProcessorEvent();

        processorEvent.setMessageID(id);
        processorEvent.setCardToken(card_token);
        processorEvent.setLastFour(last_four);

        ProcessorEvent saved = processorEventRepository.save(processorEvent);

        String json = objectMapper.writeValueAsString(java.util.Map.of(
                "card_token", saved.getCardToken(),
                "last_four", saved.getLastFour()
            ));

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(json);
        }
}
