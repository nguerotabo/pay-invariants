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
class ChargesTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ChargesRepository chargesRepository;

    @Test
    void same_key_same_body_charges_once() throws Exception {
        String jsonBody = "{\"amount\":50}";

        MvcResult first = mockMvc.perform(post("/charges")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Idempotency-Key", "abc")
        .content(jsonBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.amount").value(50))
        .andReturn();

        MvcResult second = mockMvc.perform(post("/charges")
        .contentType(MediaType.APPLICATION_JSON)
        .header("Idempotency-Key", "abc")
        .content(jsonBody))
        .andReturn();

        assertThat(chargesRepository.count()).isEqualTo(1);
        assertThat(second.getResponse().getStatus())
            .isEqualTo(first.getResponse().getStatus());
        assertThat(second.getResponse().getContentAsString())
            .isEqualTo(first.getResponse().getContentAsString());
    }
}