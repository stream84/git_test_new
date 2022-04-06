package com.softcamp.SCWebconsole.restApi;

import org.json.simple.JSONObject;
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

import org.apache.commons.codec.binary.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ManagerTest {
    @Autowired
    MockMvc mockMvc;
    //String expectByResourceName = "$.[?(@.resourceName == '%s')]";
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
    public void managerTest1_addManager() throws Exception {
        //testAdmin 추가
        String jsonManager = "{ \"managerId\": \"testAdmin\"," +
                "\"managerPw\" : \"c2VjdXJpdHk\"," +
                "\"managerMail\" : \"testAdmin@softcamp.co.kr\"," +
                "\"managerName\" : \"테스트 관리자\"," +
                "\"otherManagerTools\" : \"\"}";

        String managerId = "testAdmin";
        String managerIdEncode = Base64.encodeBase64String(managerId.getBytes(StandardCharsets.UTF_8));
        String jsonManagerEncoded = "{ \"managerId\": \""+ managerIdEncode + "\"," +
                "\"managerPw\" : \"c2VjdXJpdHk\"," +
                "\"managerMail\" : \"testAdmin@softcamp.co.kr\"," +
                "\"managerName\" : \"테스트 관리자\"," +
                "\"otherManagerTools\" : \"\"}";

//        String jsonManager = "{ \"managerId\": \"yjlee\"," +
//                "\"managerPw\" : \"c2VjdXJpdHk\"," +
//                "\"managerMail\" : \"yjlee@softcamp.co.kr\"," +
//                "\"managerName\" : \"yjlee\"," +
//                "\"otherManagerTools\" : \"\"}";
        mockMvc.perform(
                post("/managers").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonManager)
        ).andExpect(status().isOk());
    }

    @Test
    public void managerTest2_getCheckManager() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        String managerId = "testAdmin";
        String managerIdEncode = Base64.encodeBase64String(managerId.getBytes(StandardCharsets.UTF_8));
        String urlCheckManager = String.format("/managers/check/%s", managerIdEncode);

        mockMvc.perform(get(urlCheckManager).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    //사용자 수정
    @Test
    public void managerTest3_updateManager() throws Exception {
        //testAdmin 추가
        String jsonManager = "{ \"managerId\": \"testAdmin\"," +
                "\"managerPw\" : \"c2VjdXJpdHk\"," +
                "\"managerMail\" : \"testAdmin@softcamp.co.kr_edit\"," +
                "\"managerName\" : \"테스트 관리자 수정\"," +
                "\"otherManagerTools\" : \"\"}";
        String expectByMail = "$.[?(@.managerMail == '%s')]";
        String expectByName = "$.[?(@.managerName == '%s')]";
        String managerId = "testAdmin";
        String managerIdEncode = Base64.encodeBase64String(managerId.getBytes(StandardCharsets.UTF_8));
        String urlUpdateManager = String.format("/managers/%s", managerIdEncode);


        mockMvc.perform(
                        post(urlUpdateManager).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON).content(jsonManager)
                ).andExpect(status().isOk())
                .andExpect(jsonPath(expectByMail, "testAdmin@softcamp.co.kr_edit").exists())
                .andExpect(jsonPath(expectByName, "테스트 관리자 수정").exists());
    }

    @Test
    public void managerTest4_deleteManager() throws Exception {
        //testAdmin 삭제
        String managerId = "testAdmin";
        String managerIdEncode = Base64.encodeBase64String(managerId.getBytes(StandardCharsets.UTF_8));
        String urlDeleteManager = String.format("/managers/%s", managerIdEncode);

        mockMvc.perform(
                delete(urlDeleteManager).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        ).andExpect(status().isOk());
    }

    private String encodeValue(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).toString();
    }

    // Manager ID yjlee 에 대한 요청 테스트
    @Test
    public void getManagerInfo() throws Exception {
        String expectByUsername = "$.[?(@.managerId == '%s')]";
        String managerName = "yjlee";

        String managerIdEncode = Base64.encodeBase64String(managerName.getBytes(StandardCharsets.UTF_8));
        String urlGetManager = String.format("/managers/%s", managerIdEncode);


        mockMvc.perform(get(urlGetManager).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(content().string("jsonData") )
                .andExpect(jsonPath(expectByUsername, "yjlee").exists())
                .andDo(print());
    }

    // DB에 존재하지 않는 Manager ID 를 요청한 경우
    @Test
    public void getManagerInfo2() throws Exception {
        mockMvc.perform(get("/managers/noname").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                //.andExpect(content().string("jsonData") )
                //.andExpect(jsonPath(expectByResourceName, "ui").exists())
                .andDo(print());
    }

    @Test
    public void getCheckManager_yjlee() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        String managerName = "yjlee";
        String managerIdEncode = Base64.encodeBase64String(managerName.getBytes(StandardCharsets.UTF_8));
        String urlCheckManager = String.format("/managers/check/%s", managerIdEncode);

        mockMvc.perform(get(urlCheckManager).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(content().string("jsonData") )
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void getAuthManager_yjlee() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        String managerName = "yjlee";
        String managerIdEncode = Base64.encodeBase64String(managerName.getBytes(StandardCharsets.UTF_8));
        String urlAuthManager = String.format("/managers/%s/auth", managerIdEncode);

        mockMvc.perform(get(urlAuthManager).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(content().string("jsonData") )
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void resourceTest0_updateManagerResource() throws Exception {
        //Manager용 리소스 요청
        String managerId = "testAdminResource2";
        String managerIdEncode = Base64.encodeBase64String(managerId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/managers/%s/resource/auth", managerIdEncode);

        String jsonSampleData = "{ \"testresource1\": \"111\"," +
                "\"testresource2\" : \"222\" }";
        JSONObject json_in = new JSONObject();
        JSONObject json = new JSONObject();

        json_in.put("test1", "tese1-value");
        json_in.put("test2", "tese2-value");
        json_in.put("test3", 3);
        json.put("managerId", managerId);
        json.put("jsonData", json_in.toJSONString());

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                //.andExpect(content().string("jsonData") )
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void resourceTest0_selectManagerResource() throws Exception {
        String managerId = "testAdminResource2";
        String managerIdEncode = Base64.encodeBase64String(managerId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/managers/%s/resource/auth", managerIdEncode);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                //.andExpect(content().string("jsonData") )
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());


    }

}
