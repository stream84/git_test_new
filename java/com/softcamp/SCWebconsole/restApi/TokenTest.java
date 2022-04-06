package com.softcamp.SCWebconsole.restApi;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class TokenTest {
    @Autowired
    MockMvc mockMvc;
    String expectByResult = "$.[?(@.result == '%s')]";
    String expectByIntegerResult = "$.[?(@.result == '%d')]";
    String token;

    @Before //token 발급
    public void setUp() throws Exception {
        String jsonParam = "{\"managerId\": \"document\", \"managerPw\" : \"c2VjdXJpdHk\" }";
        MvcResult mvcResult = mockMvc.perform(post("/token").contentType(MediaType.APPLICATION_JSON).content(jsonParam))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.authorityName").exists())
                .andDo(print())
                .andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        token = jsonParser.parseMap(resultString).get("token").toString();
    }

    @Test
    public void getLoginCheck() throws Exception {
        String jsonParam = "{\"managerId\": \"document\", \"managerPw\" : \"c2VjdXJpdHk\" }";
        mockMvc.perform(get("/token/login-check").contentType(MediaType.APPLICATION_JSON).content(jsonParam))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json")) // Response가 Json타입인가
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath(expectByIntegerResult, 0).exists())       // 성공인가?
                .andDo(print());

        String jsonWrongParam = "{\"managerId\": \"document\", \"managerPw\" : \"c2VjdXJpdHk11\" }";
        mockMvc.perform(get("/token/login-check").contentType(MediaType.APPLICATION_JSON).content(jsonWrongParam))
                .andExpect(status().isOk())
                //.andExpect(content().string("jsonData") )
                .andExpect(jsonPath(expectByIntegerResult, 21).exists())    // 실패인가?
                .andDo(print());
    }
}
