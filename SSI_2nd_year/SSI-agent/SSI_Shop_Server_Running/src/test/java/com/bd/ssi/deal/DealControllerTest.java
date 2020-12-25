package com.bd.ssi.deal;

import com.bd.ssi.common.api.ApiResponse;
import com.bd.ssi.common.security.SecurityHolder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.TableGenerator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class DealControllerTest {

    @Autowired
    MockMvc mvc;

    String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMSIsImlzcyI6IlNTSV9TSE9QIiwiZXhwIjoxNTk3NjMxNDQ3LCJpYXQiOjE1OTcwMjY2NDcsInVzZXJuYW1lIjoidXNlcjEifQ.ALThJUnzwj6T5nLfZZZr69B0sJxdHyq6L1dMPyqfz_E";


    @Test
    public void testDetail() throws Exception {
        mvc.perform(get("/deal/detail/7").header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testBuyList() throws Exception {
        mvc.perform(get("/deal/buyList").header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testSellList() throws Exception {
        mvc.perform(get("/deal/sellList").header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testReceive() throws Exception {
        Deal deal = new Deal();
        deal.setDealId(2);
        ObjectMapper om = new ObjectMapper();
        mvc.perform(
                post("/deal/receive")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(deal)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testConfirm() throws Exception {
        Deal deal = new Deal();
        deal.setDealId(2);
        ObjectMapper om = new ObjectMapper();
        mvc.perform(
                post("/deal/confirm")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(deal)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}