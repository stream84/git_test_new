package com.softcamp.SCWebconsole.restApi.management;

import org.apache.commons.codec.binary.Base64;
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

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class ManagementTest {
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
    public void getCustomPolicyList() throws Exception {

        String expectByPolicyId = "$.[?(@.policyId == '%s')]";

        mockMvc.perform(get("/management/custom-policy-list").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByPolicyId, "DS_HANOFFICE_2014").exists())
                .andDo(print());
    }

    @Test
    public void getCustomPolicy() throws Exception {
        String policyId = "DS_HANOFFICE_2014";
        String managerIdEncode = Base64.encodeBase64String(policyId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/management/custom-policy-list/%s", managerIdEncode);
        String expectByPolicyId = "$.[?(@.policyId == '%s')]";

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByPolicyId, "DS_HANOFFICE_2014").exists())
                .andDo(print());
    }

    //수정
    //일부수정 지원
    @Test
    public void updateCustomPolicy() throws Exception {
        String policyId = "DS_HANOFFICE_2014";
        String managerIdEncode = Base64.encodeBase64String(policyId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/management/custom-policy-list/%s", managerIdEncode);
        String expectByPolicyId = "$.[?(@.policyId == '%s')]";

        String jsonCustomPolicy = "{ \"policyId\": \""+ policyId + "\"," +
                "\"policyTitle\" : \"한글2014 지원22\"," +
                "\"policyDesc\" : \"체크시 적용 (수정테스트)2\"}";

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonCustomPolicy))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    @Test
    public void deleteCustomPolicy() throws Exception {
        String policyId = "DS_HANOFFICE_2014_NEW2";
        String managerIdEncode = Base64.encodeBase64String(policyId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/management/custom-policy-list/delete/%s", managerIdEncode);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //수정
    //일부수정 지원
    @Test
    public void insertCustomPolicy() throws Exception {
        String policyId = "DS_HANOFFICE_2014_NEW2";
        String url = String.format("/management/custom-policy-list/registration");
        String expectByPolicyId = "$.[?(@.policyId == '%s')]";

        String jsonCustomPolicy = "{ \"policyId\": \""+ policyId + " \"," +
                "\"valueType\" : 0," +
                "\"listString\" : \"\"," +
                "\"policyTitle\" : \"한글2014_NEW 지원\"," +
                "\"useFlag\" : 1," +
                "\"policyDesc\" : \"체크시 적용 (수정테스트) NEW\"," +
                "\"regManagerId\" : \"document\"," +
//                "\"regDate\" : \"\"," +
//                "\"updateDate\" : \"\"," +
                "\"accessType\" : \"0\"," +
                "\"anotherPolicyFlag\" : \"0\"," +
                "\"policyTitleName\" : \"\"," +
//                "\"parentPolicyId\" : \"\"," +
//                "\"parentPolicyType\" : \"\"," +
//                "\"productType\" : \"1\"," +
                "\"mainTitleName\" : \"\"," +
                "\"policyDesc\" : \"체크시 적용 (수정테스트)2\"}";

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonCustomPolicy))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //test url - /management/add-in-list
    @Test
    public void getAddInPolicyList() throws Exception {

        String expectByPolicyId = "$.[?(@.fileName == '%s')]";

        mockMvc.perform(get("/management/add-in-list").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByPolicyId, "SCRA_MSO_LargeFileSupportUtil64.dll").exists())
                .andDo(print());
    }

    //test url - /management/add-in-list
    @Test
    public void getAddInPolicy() throws Exception {
        String fileName = "SCRA_MSO_LargeFileSupportUtil64.dll";
        //String fileNameEncode = Base64.encodeBase64String(fileName.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/management/add-in-list/1");
        String expectByPolicyId = "$.[?(@.fileName == '%s')]";

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByPolicyId, fileName).exists())
                .andDo(print());

    }

    //test url - /management/add-in-list
    @Test
    public void updateAddInPolicy() throws Exception {
        String fileName = "SCRA_MSO_LargeFileSupportUtil64.dll";
        String url = String.format("/management/add-in-list/1");
        String fileDescription = "파일 대용량 애드인 edit";
        String fileDeveloper = "softcamp";

        String jsonCustomPolicy = "{ \"fileName\": \""+ fileName + "\"," +
                "\"fileDescription\" : \"" + fileDescription + "\"," +
                "\"fileId\" : 1," +
                "\"fileDeveloper\" : \"" + fileDeveloper + "\"}";

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonCustomPolicy))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //test url - /management/add-in-list/registration
    @Test
    public void insertAddInPolicy() throws Exception {
        String fileName = "SCRA_MSO_SAMPLE.dll";
        String url = String.format("/management/add-in-list/registration");
        String expectByPolicyId = "$.[?(@.policyId == '%s')]";

        String jsonCustomPolicy = "{ \"fileName\": \""+ fileName + "\"," +
                "\"fileHashData\" : \"asdfasdfsa2\"," +
                "\"filePath\" : \"$SOFTCAMP\\\\SDK\"," +
                "\"fileDescription\" : \"TEST dll\"," +
                "\"fileVersion\" : \"1.0\"," +
                "\"fileDeveloper\" : \"softcamp\"," +
                //"\"fileId\" : 3," +
                "\"productType\" : \"00\"," +
                "\"runType\" : -1}";

        MvcResult mvcResult = mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonCustomPolicy))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print())
                .andReturn();

//        String resultString = mvcResult.getResponse().getContentAsString();
//        JacksonJsonParser jsonParser = new JacksonJsonParser();
//        String fileId = jsonParser.parseMap(resultString).get("fileId").toString();
//
//        //delete
//        String deleteUrl = String.format("/management/add-in-list/delete/%s", fileId);
//        mockMvc.perform(post(deleteUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath(expectByResult, 1).exists())
//                .andDo(print());
    }

    @Test
    public void deleteAddInPolicy() throws Exception {
        int fielId = 6;
        String url = String.format("/management/add-in-list/delete/%d", fielId);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //test url - /management/duty
    @Test
    public void getDutyInfo() throws Exception {

        String expectByDutyName = "$.[?(@.dutyName == '%s')]";

        mockMvc.perform(get("/management/duty").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByDutyName, "사장").exists())
                .andDo(print());
    }

    @Test
    public void insertDutyInfo() throws Exception {
        String dutyName = "테스트터";
        String dutyId = "SC_TEST_01";
        String url = String.format("/management/duty/SC_TEST_01");
        String jsonCustomPolicy = "{ \"dutyName\": \""+ dutyName + "\"," +
//                "\"dutyId\" : \"" + dutyId + "\"," +
                "\"dutyId\" : \"" + dutyId + "\"}";

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonCustomPolicy))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    @Test
    public void updateDutyInfo() throws Exception {
        String dutyName = "테스트터 EDIT";
        String dutyId = "SC_TEST_01";
        String url = String.format("/management/duty/%s", "SC_TEST_01");
        String jsonCustomPolicy = "{ \"dutyName\": \""+ dutyName + "\"," +
//                "\"dutyId\" : \"" + dutyId + "\"," +
                "\"dutyId\" : \"" + dutyId + "\"}";

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonCustomPolicy))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    @Test
    public void deleteDutyInfo() throws Exception {
        String dutyName = "테스트터 EDIT";
        String dutyId = "SC_TEST_01";
        String url = String.format("/management/duty/delete/%s", dutyId);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //로그관리
    //사용자 로그 요청
    @Test
    public void getUserLogInfo() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/management/log/users");
        String userId = "SW_0001";
        String userGroup = "영역보안";
        String logTypeName = "로그인";
        String jsonParam = "{ \"userId\": \""+ userId + "\"," +
                "\"logTypeName\" : \"" + logTypeName + "\"," +
                "\"userGroup\" : \"" + userGroup + "\"}";

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonParam))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectBylogTypeName, logTypeName).exists())
                .andDo(print());
    }

    //모든 관리자 로그 요청
    @Test
    public void getManagerLogInfo() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/management/log/managers");
        String managerId = "document";
        String managerGroup = null;
        String logTypeName = "로그인";
        String jsonParam = "{ \"managerId\": \""+ managerId + "\"," +
                //"\"logTypeName\" : \"" + logTypeName + "\"," +
                "\"logTypeName\" : \"" + logTypeName + "\"}";

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonParam))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectBylogTypeName, logTypeName).exists())
                .andDo(print());
    }

    //모든 문서보안 로그 요청
    @Test
    public void getDsLogInfo() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/management/log/ds");
        String userId = "AutoMation_0001";
        String logTypeName = "암호화 취소";
        String searchFileName = "C:\\Users\\softcamp\\Desktop\\1.3GB_대용량 문서.xlsx";

        JSONObject json = new JSONObject();
        json.put("userId", userId);
        json.put("searchFileName", "C:\\Users\\softcamp\\Desktop\\1.3GB_대용량 문서.xlsx");
        json.put("logTypeName", logTypeName);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectBylogTypeName, logTypeName).exists())
                .andDo(print());
    }

}
