package com.payinvariants;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
public class WebhooksTest{

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ProcessorEventRepository processorEventRepository;

    @Test
    void correct_payment() throws Exception {

        String body = "{\"id\":\"evt_1\",\"card_token\":\"tok_abc\",\"last_four\":\"4242\",\"card_number\":\"4242424242424242\"}";
        long timestamp = Instant.now().getEpochSecond();

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        String header = "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(raw);

        long currCount = processorEventRepository.count();

        mockMvc.perform(post("/webhooks/processor")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Processor-Signature", header)
        .content(body))
        .andExpect(jsonPath("$.card_token").value("tok_abc"))
        .andExpect(jsonPath("$.last_four").value("4242"))
        .andExpect(status().isOk());

        assertThat(processorEventRepository.count()).isEqualTo(currCount + 1);   
    }

    @Test
    void wrong_secret() throws Exception {

        String body = "{\"id\":\"evt_1\",\"card_token\":\"tok_abc\",\"last_four\":\"4242\",\"card_number\":\"4242424242424242\"}";
        long timestamp = Instant.now().getEpochSecond();

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("false".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        String header = "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(raw);

        long currCount = processorEventRepository.count();

        mockMvc.perform(post("/webhooks/processor")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Processor-Signature", header)
        .content(body))
        .andExpect(status().isBadRequest());

        assertThat(processorEventRepository.count()).isEqualTo(currCount);
    }

    @Test 
    void double_request() throws Exception {

        String body = "{\"id\":\"evt_2\",\"card_token\":\"tok_abc\",\"last_four\":\"4242\",\"card_number\":\"4242424242424242\"}";
        long timestamp = Instant.now().getEpochSecond();

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        String header = "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(raw);

        long currCount = processorEventRepository.count();

        mockMvc.perform(post("/webhooks/processor")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Processor-Signature", header)
        .content(body))
        .andExpect(status().isOk());

        mockMvc.perform(post("/webhooks/processor")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Processor-Signature", header)
        .content(body))
        .andExpect(status().isConflict());

        assertThat(processorEventRepository.count()).isEqualTo(currCount + 1);
    }

    @Test 
    void old_message() throws Exception {

        String body = "{\"id\":\"evt_3\",\"card_token\":\"tok_abc\",\"last_four\":\"4242\",\"card_number\":\"4242424242424242\"}";
        long timestamp = Instant.now().getEpochSecond() - 301;

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        String header = "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(raw);

        long currCount = processorEventRepository.count();

        mockMvc.perform(post("/webhooks/processor")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Processor-Signature", header)
        .content(body))
        .andExpect(status().isBadRequest());

        assertThat(processorEventRepository.count()).isEqualTo(currCount);
    }
}