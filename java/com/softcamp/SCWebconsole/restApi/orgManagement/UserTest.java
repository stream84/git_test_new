package com.softcamp.SCWebconsole.restApi.orgManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.softcamp.SCWebconsole.model.enc.ConstructorPerInfo;
import com.softcamp.SCWebconsole.model.enc.EncryptPolicyInfo;
import com.softcamp.SCWebconsole.model.enc.ForcedEncReport;
import com.softcamp.SCWebconsole.model.profile.*;
import com.softcamp.SCWebconsole.model.security.GradeInfo;
import com.softcamp.SCWebconsole.model.security.forced.CategoryAuthInfo;
import com.softcamp.SCWebconsole.model.security.forced.ForcedAuthInfo;
import com.softcamp.SCWebconsole.model.security.forced.SecurityGradeAuthInfo;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class UserTest {
    @Autowired
    MockMvc mockMvc;
    String expectByResult = "$.[?(@.result == '%s')]";
    String token;

    String securityDomain = "SECURITYDOMAIN";
    String managerId = "document";

    @Autowired
    private WebApplicationContext ctx;

    @Before //token 발급
    public void setUp() throws Exception {
        String jsonParam = "{\"managerId\": \"scadmin\", \"managerPw\" : \"c29mdGNhbXAyMDE4IUA\" }";
        MvcResult mvcResult = mockMvc.perform(post("/token").contentType(MediaType.APPLICATION_JSON).content(jsonParam))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.authorityName").exists())
                .andDo(print())
                .andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        JacksonJsonParser jsonParser = new JacksonJsonParser();
        token = jsonParser.parseMap(resultString).get("token").toString();

        //mock 한글 처리를 위해서 UTF-8 필터 추가
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))  // 필터 추가
                .alwaysDo(print())
                .build();
    }

    @Test
    public void getUserInfoList() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/org-management/users");

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getUserInfo() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String userId = "AutoMation_0001";
        String url = String.format("/org-management/users/%s",userId);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void insertUserInfo() throws Exception {
        String url = String.format("/org-management/users");

        JSONObject json = new JSONObject();
        json.put("spUserId", "dev_test_0001");
        json.put("spUserName", "dev_test_0001_name11");
        //json.put("spUserEncryptKey", "kdR8BnnVgklhj0aN");
        json.put("spUserPw", "1234");
        json.put("spGroupId", "SCDS_000000009");
        json.put("spDutyId", null);
        json.put("spClassId", "0000001");
        json.put("spUseAuthPeriod", "");

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isCreated())
                .andDo(print());
    }

    @Test
    public void deleteUserInfo() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String userId = "dev_test_0001";
        String url = String.format("/org-management/users/delete/%s",userId);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBylogTypeName, logTypeName).exists())
                .andDo(print());
    }

    @Test
    public void updateUserInfo() throws Exception {

        String userId = "dev_test_0001";
        String url = String.format("/org-management/users/%s", userId);
        JSONObject json = new JSONObject();

        json.put("userId", "dev_test_0001");
        json.put("userName", "dev_test_0001_name_edit");
        json.put("userPw", "U0hBNTEyI4as3/zoFvKyeqMruBPC089khM18B5rFvTOyMCbAGk4m1KDdCouHwMEWJxOUxk3hHgJOSYa5mJreJPuHmUQB1Q");

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void updateSpUserInfo() throws Exception {

        String userId = "dev_test_0001";
        String url = String.format("/org-management/users/sp/%s", userId);
        JSONObject json = new JSONObject();

        json.put("spUserId", userId);
        json.put("spUserName", "dev_test_0001_name_edit111");
        json.put("spUserPw", "U0hBNTEyI4as3/zoFvKyeqMruBPC089khM18B5rFvTOyMCbAGk4m1KDdCouHwMEWJxOUxk3hHgJOSYa5mJreJPuHmUQB1Q");

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getSpUserInfo() throws Exception {
        String expectByUserId = "$.[?(@.userId == '%s')]";
        String userId = "AutoMation_0001";
        String url = String.format("/org-management/users/sp/%s",userId);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByUserId, userId).exists())
                .andDo(print());
    }

    @Test
    public void getUserCustomPolicyDetail() throws Exception {
        String expectBypolicyTitle = "$.[?(@.policyTitle == '%s')]";
        String policyId = "IE_CTL_EXCEPT_URL_12";
        String userId = "myeongseok.seo";

        String policyIdEncoded = Base64.encodeBase64String(policyId.getBytes(StandardCharsets.UTF_8));
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/custom-policy/%s",userIdEncoded,policyIdEncoded);
        JSONObject json = new JSONObject();


        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectBypolicyTitle, "오프라인시 IE 종료예외").exists())
                .andDo(print());
    }

    @Test
    public void updateUserCustomPolicyDetail() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        String policyId = "IE_CTL_EXCEPT_URL_12";
        String userId = "myeongseok.seo";
        String policyValue = "1|0|res://ieframe.dll_test_2";
        int userUseFlag = 1;

        String policyIdEncoded = Base64.encodeBase64String(policyId.getBytes(StandardCharsets.UTF_8));
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/custom-policy/%s",userIdEncoded,policyIdEncoded);
        JSONObject json = new JSONObject();
        json.put("policyValue", policyValue);
        json.put("userUseFlag", userUseFlag);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void getUserCustomPolicyList() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/custom-policy",userIdEncoded);
        JSONObject json = new JSONObject();


        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getUserAddInList() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/add-in",userIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void saveUserAddInList() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/add-in",userIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getUserUnusedAddInList() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/add-in/unused",userIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }


    //애드인 삭제
    @Test
    public void deleteUserAddIn() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        int addInFileId = 126;
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/add-in/%d/delete",userIdEncoded,addInFileId);
        JSONObject json = new JSONObject();

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //애드인 추가
    @Test
    public void insertUserAddIn() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        int addInFileId = 126;
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/add-in/%s",userIdEncoded,addInFileId);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //암호화정책
    //기본설정
    @Test
    public void getUserBasicEncInfo() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/enc",userIdEncoded);
        JSONObject json = new JSONObject();


        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateUserBasicEncInfo() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/enc",userIdEncoded);
        String urlTestInfo = String.format("/org-management/users/%s/enc",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(urlTestInfo).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        EncryptPolicyInfo encryptPolicyInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(EncryptPolicyInfo.class));

        if(encryptPolicyInfo.getEncryptPoint().equals("0"))
            encryptPolicyInfo.setEncryptPoint("1");
        else
            encryptPolicyInfo.setEncryptPoint("0");

        //object to json
        String jsonInString = objectMapper.writeValueAsString(encryptPolicyInfo);

        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andDo(print());
    }
    //생성자 권한
    @Test
    public void getUserConstructorPerInfo() throws Exception {
        String expectBypolicyTitle = "$.[?(@.policyTitle == '%s')]";
        //String policyId = "IE_CTL_EXCEPT_URL_12";
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/enc/constructor-per",userIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateUserConstructorPerInfo() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/enc/constructor-per",userIdEncoded);
        String urlTestInfo = String.format("/org-management/users/%s/enc/constructor-per",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(urlTestInfo).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        ConstructorPerInfo constructorPerInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(ConstructorPerInfo.class));

        if(constructorPerInfo.getReadAuth().equals("0"))
            constructorPerInfo.setReadAuth("1");
        else
            constructorPerInfo.setReadAuth("0");

        //object to json
        String jsonInString = objectMapper.writeValueAsString(constructorPerInfo);

        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getUserEncCategoryInfo() throws Exception {
        String expectBypolicyTitle = "$.[?(@.policyTitle == '%s')]";
        //String policyId = "IE_CTL_EXCEPT_URL_12";
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/enc/category",userIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateUserEncCategoryInfo() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/enc/category",userIdEncoded);
        String urlTestInfo = String.format("/org-management/users/%s/enc/category",userIdEncoded);


        List<String> categoryList = new ArrayList<>();
        categoryList.add("0000001");
        categoryList.add("0000002");
        categoryList.add("0000003");

        JSONObject json = new JSONObject();
        json.put("targetId", userId);
        json.put("categoryIdList", categoryList);

        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getUserAccessGroupinfo() throws Exception {
        String expectBypolicyTitle = "$.[?(@.policyTitle == '%s')]";
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/enc/access-group",userIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateUserAccessGroupinfo() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/enc/access-group",userIdEncoded);
        String urlTestInfo = String.format("/org-management/users/%s/access-group",userIdEncoded);


        List<String> groupList = new ArrayList<>();
        groupList.add("SCDP00000");
        groupList.add("SCDS_000000289");

        JSONObject json = new JSONObject();
        json.put("targetId", userId);
        json.put("targetGroupIdList", groupList);

        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //강제암호화 정책
    @Test
    public void getForcedEncryptInfo() throws Exception {
        String expectBypolicyTitle = "$.[?(@.policyTitle == '%s')]";
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/enc/forced-enc",userIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void updateForcedEncryptInfo() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/enc/forced-enc",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        ForcedEncReport forcedEncReport = new ObjectMapper().readValue(resultString, typeFactory.constructType(ForcedEncReport.class));

        EncryptPolicyInfo encryptPolicyInfo = forcedEncReport.getEncryptPolicyInfo();
        if (!encryptPolicyInfo.getAutoEncryptTargetApplication().isEmpty())
        {
            encryptPolicyInfo.setAutoEncryptTargetApplication("");
            forcedEncReport.setEncryptPolicyInfo(encryptPolicyInfo);
        }

        //object to json
        String jsonInString = objectMapper.writeValueAsString(forcedEncReport);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andDo(print());

    }

    //프로파일
    // - 로그인
    @Test
    public void getProfileLoginPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/login",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateProfileLoginPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/login",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        LoginInfo loginInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(LoginInfo.class));

        loginInfo.setLimitLoginFailNum(10);

        //object to json
        String jsonInString = objectMapper.writeValueAsString(loginInfo);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());

    }
    // - 앱제어
    @Test
    public void getProfileAppControlPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/app-control",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateProfileAppControlPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/app-control",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        AppControlInfo appControlInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(AppControlInfo.class));

        if(appControlInfo.getScreenCaptureControl().equals("1"))
            appControlInfo.setScreenCaptureControl("0");
        else
            appControlInfo.setScreenCaptureControl("1");

        //object to json
        String jsonInString = objectMapper.writeValueAsString(appControlInfo);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());

    }
    // - 패스워드
    @Test
    public void getProfilePasswordPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/password",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateProfilePasswordPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/password",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        PasswordInfo passwordInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(PasswordInfo.class));

        if (passwordInfo.getFirstLoginChangePw().equals("1"))
            passwordInfo.setFirstLoginChangePw("0");
        else
            passwordInfo.setFirstLoginChangePw("1");

        //object to json
        String jsonInString = objectMapper.writeValueAsString(passwordInfo);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());

    }
    // - 외부전송
    @Test
    public void getProfileCheckoutPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/checkout",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateProfileCheckoutPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/checkout",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        CheckOutDocInfo checkOutDocInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(CheckOutDocInfo.class));

        if ( checkOutDocInfo.getPrintButtonAuth().equals("1"))
            checkOutDocInfo.setPrintButtonAuth("0");
        else
            checkOutDocInfo.setPrintButtonAuth("1");

        //object to json
        String jsonInString = objectMapper.writeValueAsString(checkOutDocInfo);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());

    }
    // - 기타
    @Test
    public void getProfileEtcPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/etc",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateProfileEtcPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/etc",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        EtcInfo etcInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(EtcInfo.class));

        if ( etcInfo.getUninstallClient().equals("1"))
            etcInfo.setUninstallClient("0");
        else
            etcInfo.setUninstallClient("1");

        //object to json
        String jsonInString = objectMapper.writeValueAsString(etcInfo);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());

    }
    // - 프린트 마킹
    @Test
    public void getProfilePrintMarkPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/print-mark",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateProfilePrintMarkPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/profile/print-mark",userIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        PrintMarkInfo printMarkInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(PrintMarkInfo.class));


        //object to json
        String jsonInString = objectMapper.writeValueAsString(printMarkInfo);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());

    }
    @Test
    public void getProfilePrintMarkImageList() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/print-mark-image");

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //하위 관리자
    @Test
    public void getGroupManagerPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String groupId = "SCDS_000000210";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/group-manager/%s",userIdEncoded, groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //pc 보안
    @Test
    public void getPcSecurityPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/pc-security",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //강제 권한 - 요청
    @Test
    public void getForcePermissionGradePolicy() throws Exception {
        String userId = "jeyoung.you";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/grade",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //강제 권한 - 추가
    @Test
    public void insertForcePermissionGradePolicy() throws Exception {
        String userId = "jeyoung.you";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/grade",userIdEncoded);

        String gradeUrl = "/security/grade/info";
        MvcResult mvcResult = mockMvc.perform(get(gradeUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();

        List<GradeInfo> gradeInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, GradeInfo.class));
        GradeInfo gradeInfo = new GradeInfo();
        //object to json
        String jsonInString = new String();

        for (int i=0; i<gradeInfoList.size(); i++)
        {
            GradeInfo gradeInfo1 = gradeInfoList.get(i);
            if(gradeInfo1.getGradeId().equals("0000005") ) {
                gradeInfo1.setTargetId(userId);
                gradeInfo1.setTargetType("U");
                jsonInString = objectMapper.writeValueAsString(gradeInfo1);
            }
        }

        if (!jsonInString.isEmpty())
        {
            mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(expectByResult, "1").exists())
                    .andDo(print());
        }

    }

    //강제 권한 - 삭제
    @Test
    public void deleteForcePermissionGradePolicy() throws Exception {
        String userId = "jeyoung.you";
        String gradeId = "0000005";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/grade/%s",userIdEncoded, gradeId);

        mockMvc.perform(delete(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    //강제 권한 - 문서권한 요청
    @Test
    public void test3_getForcePermissionGradeAuthPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String targetGradeId = "0000001";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/grade/auth/%s",userIdEncoded, targetGradeId);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    //강제 권한 - 문서권한 추가
    @Test
    public void test2_insertForcePermissionGradeAuthPolicy() throws Exception {
        String userId = "tskim";
        String gradeId = "0000001";
        String targetUserId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String targetUserIdEncoded = Base64.encodeBase64String(targetUserId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/grade/auth",targetUserIdEncoded);

        String getUrl = String.format("/org-management/users/%s/force-permission/grade/auth/%s",userIdEncoded, gradeId);
        MvcResult mvcResult = mockMvc.perform(get(getUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();

        List<SecurityGradeAuthInfo> gradeAuthInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, SecurityGradeAuthInfo.class));
        String jsonInString = new String();

        for (int i=0; i<gradeAuthInfoList.size(); i++)
        {
            gradeAuthInfoList.get(i).setTargetId(targetUserId);
        }

        jsonInString = objectMapper.writeValueAsString(gradeAuthInfoList);

        if (!jsonInString.isEmpty())
        {
            mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                    .andExpect(status().isOk())
                    //.andExpect(jsonPath(expectByResult, "1").exists())
                    .andDo(print());
        }
    }

    //강제 권한 - 문서권한 삭제
    @Test
    public void test1_deleteForcePermissionGradeAuthPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String gradeId = "0000001";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/grade/auth/%s",userIdEncoded, gradeId);

        mockMvc.perform(delete(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    //강제 권한 - DAC권한 요청
    @Test
    public void getForcePermissionGradeDACPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/grade/dac",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //강제 권한 - DAC권한 수정
    @Test
    public void updateForcePermissionGradeDACPolicy() throws Exception {
        String targetUserId = "myeongseok.seo";
        String targetUserIdEncoded = Base64.encodeBase64String(targetUserId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/users/%s/force-permission/grade/dac",targetUserIdEncoded);
        String getUrl = String.format("/org-management/users/%s/force-permission/grade/dac",targetUserIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(getUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();

        ForcedAuthInfo forcedAuthInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(ForcedAuthInfo.class));
        String jsonInString = new String();

        jsonInString = objectMapper.writeValueAsString(forcedAuthInfo);

        if (!jsonInString.isEmpty())
        {
            mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath(expectByResult, "1").exists())
                    .andDo(print());
        }
    }

    //강제 권한 - MAC권한 요청
    @Test
    public void getForcePermissionCategoryPolicy() throws Exception {
        String userId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/category/auth",userIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    //강제 권한 - MAC권한 추가
    @Test
    public void insertForcePermissionCategoryPolicy() throws Exception {
        String userId = "tskim";
        String gradeId = "0000001";
        String targetUserId = "myeongseok.seo";
        String userIdEncoded = Base64.encodeBase64String(userId.getBytes(StandardCharsets.UTF_8));
        String targetUserIdEncoded = Base64.encodeBase64String(targetUserId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/users/%s/force-permission/category/auth",userIdEncoded);

        String getUrl = String.format("/org-management/users/%s/force-permission/category/auth",targetUserIdEncoded);
        MvcResult mvcResult = mockMvc.perform(get(getUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();

        List<CategoryAuthInfo> categoryAuthInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, CategoryAuthInfo.class));
        String jsonInString = new String();

        for (int i=0; i<categoryAuthInfoList.size(); i++)
        {
            categoryAuthInfoList.get(i).setTargetId(userId);
        }

        jsonInString = objectMapper.writeValueAsString(categoryAuthInfoList);

        if (!jsonInString.isEmpty())
        {
            mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                    .andExpect(status().isOk())
                    //.andExpect(jsonPath(expectByResult, "1").exists())
                    .andDo(print());
        }
    }


}
