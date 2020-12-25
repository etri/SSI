package com.bd.ssi.common.auth;

import com.bd.ssi.auth.AuthRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class TokenTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    public void testLogin() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("user1");
        request.setPassword("user1");
        ObjectMapper om = new ObjectMapper();
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(request))
        ).andDo(print()).andExpect(status().isOk());
    }
}
