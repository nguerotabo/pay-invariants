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
    void correct_payment() {

        String body = "{\"id\":\"evt_1\",\"card_token\":\"tok_abc\",\"last_four\":\"4242\",\"card_number\":\"4242424242424242\"}";
        long timestamp = Instant.now().getEpochSecond();

        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        String header = "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(raw);

        MockMvc result = mockMvc.perform(post("/webhooks/processor")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Processor-Signature", header)
        .content(body))
        .andExpect(jsonPath("$.card_token").value("tok_abc"))
        .andExpect(jsonPath("$.last_four").value(4242))
        .andExpect(status().isOk());
    }
}