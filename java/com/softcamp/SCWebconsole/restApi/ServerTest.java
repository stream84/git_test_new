package com.softcamp.SCWebconsole.restApi;

import com.softcamp.SCWebconsole.common.auth.Serial;
import org.apache.commons.codec.binary.Base64;
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

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ServerTest {
    @Autowired
    MockMvc mockMvc;
    String expectByResult = "$.[?(@.result == '%s')]";
    String token;

    String securityDomain = "SECURITYDOMAIN";
    String managerId = "document";
    String securityDomainEncoded = Base64.encodeBase64String(securityDomain.getBytes(StandardCharsets.UTF_8));
    String managerIdEncoded = Base64.encodeBase64String(managerId.getBytes(StandardCharsets.UTF_8));

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
    public void getServerProfiles() throws Exception {
        mockMvc.perform(get("/server/profile").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getServerProfile() throws Exception {

        String urlServerProfile = String.format("/server/profile/%s/%s", securityDomainEncoded, managerIdEncoded);
        mockMvc.perform(get(urlServerProfile).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //서버프로파일 일부수정
    @Test
    public void updateServerProfile() throws Exception {
        String jsonServerProfile = "{ \"securityDomain\": \"SECURITYDOMAIN\"," +
                "\"highestSecurityManagerId\" : \"document\"," +
                "\"highestSecurityManagerMail\" : \"testAdmin@softcamp.co.kr_edit\"," +
                "\"highestSecurityManagerHp\" : \"010-1111-1111\"," +
                "\"networkOption\" : \"TCP\"}";

        String urlServerProfile = String.format("/server/profile/%s/%s", securityDomainEncoded, managerIdEncoded);

        mockMvc.perform(post(urlServerProfile).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonServerProfile))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getEmergencyKey() throws Exception {
        String expectByKey = "$.[?(@.emergency-key == '%s')]";
        String urlEmergencyKey = String.format("/server/emergency-key/%s/%s", securityDomainEncoded, managerIdEncoded);
        String securityDomainNotFound = "SECURITYDOMAIN11";
        String securityDomainNotFoundEncoded = Base64.encodeBase64String(securityDomainNotFound.getBytes(StandardCharsets.UTF_8));
        String urlEmergencyKeyNotFound = String.format("/server/emergency-key/%s/%s", securityDomainNotFoundEncoded, managerIdEncoded);
        String emergencyKey = "1111";
        String emergencyKeyEncoded = Base64.encodeBase64String(emergencyKey.getBytes(StandardCharsets.UTF_8));
        String urlEmergencyKeyEdit = String.format("/server/emergency-key/%s/%s/edit/%s", securityDomainEncoded, managerIdEncoded,emergencyKeyEncoded);

        mockMvc.perform(get(urlEmergencyKey).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByKey, "0000").exists())
                .andDo(print());

        mockMvc.perform(get(urlEmergencyKeyNotFound).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andDo(print());

        mockMvc.perform(post(urlEmergencyKeyEdit).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(content().string("jsonData") )
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void getEmergencyKeyLog() throws Exception {

        mockMvc.perform(get("/server/emergency-key/history").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void crcTest() throws Exception{
        String pcid = "TEST-TEST-TEST-TEST-TEST";
        //String pcid = "TEST-TEST-TEST-TEST-";
        Serial serial = new Serial(pcid.getBytes(StandardCharsets.US_ASCII), "20211109");
        //Serial serial = new Serial("TEST-TEST-TEST-TEST-", "20211109");
        serial.makeKey();
    }

}
