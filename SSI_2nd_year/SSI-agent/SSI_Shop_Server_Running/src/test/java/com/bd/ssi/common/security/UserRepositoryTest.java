package com.bd.ssi.common.security;

import com.bd.ssi.auth.AuthRequest;
import com.bd.ssi.auth.User;
import com.bd.ssi.auth.UserRepository;
import com.bd.ssi.product.PurchaseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class UserRepositoryTest {
    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    MockMvc mvc;

    @Test
    public void testSave(){

        User user = new User();
        user.setUsername("user1");
        user.setPassword(passwordEncoder.encode("user1"));
        user.setRole("USER");
        userRepository.save(user);

    }

    @Test
    public void testLogin() throws Exception {

        AuthRequest request = new AuthRequest();
        request.setUsername("user2");
        request.setPassword("user2");
        ObjectMapper om = new ObjectMapper();

        mvc.perform(
                post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
