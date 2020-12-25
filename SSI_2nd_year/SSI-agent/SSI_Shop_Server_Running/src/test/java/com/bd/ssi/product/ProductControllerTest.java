package com.bd.ssi.product;

import com.bd.ssi.auth.AuthRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class ProductControllerTest {

    @Autowired
    MockMvc mvc;

    String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMiIsImlzcyI6IlNTSV9TSE9QIiwiZXhwIjoxNTk4ODUwMzY4LCJpYXQiOjE1OTgyNDU1NjgsInVzZXJuYW1lIjoidXNlcjIifQ.sy7cANIwe1r1wCS0lfj6YdW6fIoSJ_UsPbQqTM0JqQU";

    @Test
    public void testList() throws Exception {
        mvc.perform(get("/product/list").header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testDetail() throws Exception {
        mvc.perform(get("/product/detail/2").header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testPurchase() throws Exception {
        PurchaseRequest request = new PurchaseRequest();
        request.setPaymentMethod("신용카드");
        request.setProductId(2);
        request.setCount(1);
        request.setAddress("서울시 구로구 디지털로 288 1708");
        request.setPhone("010-4703-7925");
//        request.setRequest("문 앞에");
        request.setVp(null);
        ObjectMapper om = new ObjectMapper();

        mvc.perform(
                post("/product/purchase")
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());

    }

    @Test
    public void testAdd() throws Exception {

        ProductRequest request = new ProductRequest();
        request.setAddress("서울특별시 구로구 디지털로 288");
        request.setDescription("테스트용");
        request.setDid("test did1111");
        request.setPrice(10000L);
        request.setProductName("테스트 상품");
        request.setType("새제품");
        
        ObjectMapper om = new ObjectMapper();

        MockMultipartFile multipartJson = new MockMultipartFile("body", "", MediaType.APPLICATION_JSON_VALUE, om.writeValueAsBytes(request));
        MockMultipartFile multipartFile = new MockMultipartFile("files", "test.txt",
                "text/plain", "Spring Framework".getBytes());
        MockMultipartFile multipartFile2 = new MockMultipartFile("files", "test2.txt",
                "text/plain", "Spring Framework2".getBytes());


        mvc.perform(
                multipart("/product/add")
                        .file(multipartJson)
                        .file(multipartFile)
                        .file(multipartFile2)
                        .header("Authorization", token))
                .andDo(print())
                .andExpect(status().isOk());
    }
    
    
}