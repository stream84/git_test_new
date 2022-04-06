package com.softcamp.SCWebconsole.restApi.orgManagement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.softcamp.SCWebconsole.model.addin.AddInPolicyAuthInfoListVo;
import com.softcamp.SCWebconsole.model.enc.*;
import com.softcamp.SCWebconsole.model.pcSecurity.PcSecurityInfo;
import com.softcamp.SCWebconsole.model.pcSecurity.UpdatePcSecurityInfo;
import com.softcamp.SCWebconsole.model.profile.AppControlInfo;
import com.softcamp.SCWebconsole.model.profile.LoginInfo;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class GroupTest {
    @Autowired
    MockMvc mockMvc;
    String expectByResult = "$.[?(@.result == '%s')]";
    String token;

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
    public void getGroupInfoList() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/org-management/groups");

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBylogTypeName, logTypeName).exists())
                .andDo(print());
    }

    @Test
    public void insertGroupInfo() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        String userId = "AutoMation_0001";
        String url = String.format("/org-management/groups");

        JSONObject json = new JSONObject();
        json.put("spParentGroupId", "SECURITYDOMAIN");
        json.put("spGroupName", "group_insert_test1");

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isCreated())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void deleteGroupInfo() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String groupId = "SCDS_000000012";
        String url = String.format("/org-management/groups/delete/%s",groupId);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBylogTypeName, logTypeName).exists())
                .andDo(print());
    }

    @Test
    public void updateGroupInfo() throws Exception {

        String groupId = "SCDS_000000011";
        String url = String.format("/org-management/groups/%s", groupId);
        JSONObject json = new JSONObject();

        json.put("groupId", groupId);
        json.put("groupName", "group_edit_test");

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //커스텀 정책
    @Test
    public void getGroupCustomPolicyDetail() throws Exception {
        String expectBypolicyTitle = "$.[?(@.policyTitle == '%s')]";
        String policyId = "IE_CTL_EXCEPT_URL_12";
        String groupId = "SCDS_000000063";

        String policyIdEncoded = Base64.encodeBase64String(policyId.getBytes(StandardCharsets.UTF_8));
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/custom-policy/%s",groupIdEncoded,policyIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectBypolicyTitle, "오프라인시 IE 종료예외").exists())
                .andDo(print());
    }

    @Test
    public void updateGroupCustomPolicyDetail() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        String policyId = "IE_CTL_EXCEPT_URL_12";
        String groupId = "SCDS_000000210";
        String policyValue = "1|0|res://ieframe.dll_test_3";
        int userUseFlag = 0;

        String policyIdEncoded = Base64.encodeBase64String(policyId.getBytes(StandardCharsets.UTF_8));
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/custom-policy/%s",groupIdEncoded,policyIdEncoded);
        JSONObject json = new JSONObject();
        json.put("policyValue", policyValue);
        json.put("userUseFlag", userUseFlag);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void updateSubGroupCustomPolicyDetail() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        String policyId = "IE_CTL_EXCEPT_URL_12";
        String groupId = "SCDS_000000210";
        String policyValue = "1|0|res://ieframe.dll_test_3";
        int userUseFlag = 0;

        String policyIdEncoded = Base64.encodeBase64String(policyId.getBytes(StandardCharsets.UTF_8));
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/custom-policy/%s/%s",groupIdEncoded,policyIdEncoded,"sub");
        JSONObject json = new JSONObject();
        json.put("policyValue", policyValue);
        json.put("userUseFlag", userUseFlag);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void getGroupCustomPolicyList() throws Exception {
        String expectBypolicyTitle = "$.[?(@.policyTitle == '%s')]";
        String groupId = "SCDS_000000063";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/custom-policy",groupIdEncoded);
        JSONObject json = new JSONObject();


        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBypolicyTitle, "오프라인시 IE 종료예외").exists())
                .andDo(print());
    }

    //애드인
    @Test
    public void getGroupAddInList() throws Exception {
        String groupId = "SCDS_000000008";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/groups/%s/add-in",groupIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBypolicyTitle, "오프라인시 IE 종료예외").exists())
                .andDo(print());
    }

    @Test
    public void getGroupUnusedAddInList() throws Exception {
        String groupId = "SCDS_000000008";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/groups/%s/add-in/unused",groupIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBypolicyTitle, "오프라인시 IE 종료예외").exists())
                .andDo(print());
    }

    //애드인 삭제
    @Test
    public void deleteGroupAddIn() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        int addInFileId = 126;
        String groupId = "SCDS_000000008";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/groups/%s/add-in/%d/delete",groupIdEncoded,addInFileId);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //애드인 추가
    @Test
    public void insertGroupAddIn() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        int addInFileId = 126;
        String groupId = "SCDS_000000008";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/groups/%s/add-in/%s",groupIdEncoded,addInFileId);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //애드인 저장- 기존 애드인 리스트 삭제, 신규 애드인 리스트 적용
    @Test
    public void updateGroupAddIn() throws Exception {
        String expectByResult = "$.[?(@.result == '%s')]";
        String groupId = "SCDP00000";//"SECURITYDOMAIN";//"SCDS_000000209";
        //String groupId = "SCDS_000000210";
        //String groupId = "SECURITYDOMAIN";
        String groupIdNotFound = "SCDS_00000020912312";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/groups/%s/add-in/update/%s",groupIdEncoded,"sub");
        String urlGet = String.format("/org-management/groups/%s/add-in",groupIdEncoded);

        JSONObject json = new JSONObject();

        //1. 기존 리스트 받아서 - 사용중인 정책 리스트 요청

        MvcResult mvcResult = mockMvc.perform(get(urlGet).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print())
                .andReturn();

        //json 결과 값 parsing
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<AddInPolicyAuthInfoListVo> addInPolicyAuthInfoListVoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, AddInPolicyAuthInfoListVo.class));

        //2. 변경 리스트 적용하기
        //List<Integer> fileIdList = List.of(106,121,126,128,134,136,140,142);
        List<Integer> fileIdList = new ArrayList<>();
        addInPolicyAuthInfoListVoList.forEach(item -> {
            fileIdList.add(item.getFileId());
        });

        fileIdList.add(121);

        json.put("fileIdList", fileIdList);

        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(json.toJSONString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath(expectByResult, 1).exists())
                .andDo(print());
    }

    //암호화 정책
    //  기본
    @Test
    public void getGroupBasicEncInfo() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/enc",groupIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateBasicEncInfo() throws Exception {
        String groupId = "SCDP00000";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String urlTestInfo = String.format("/org-management/groups/%s/enc",groupIdEncoded);
        String url = String.format("/org-management/groups/%s/enc/%s",groupIdEncoded,"sub");

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

    //  생성자 권한
    @Test
    public void getGroupConstructorPerInfo() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/enc/constructor-per",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateGroupConstructorPerInfo() throws Exception {
        //String groupId = "SCDS_000000210";
        String groupId = "SCDP00000";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String urlTestInfo = String.format("/org-management/groups/%s/enc/constructor-per",groupIdEncoded);
        String url = String.format("/org-management/groups/%s/enc/constructor-per/%s",groupIdEncoded,"sub");

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

    //  암호화 가능 범주
    @Test
    public void getGroupEncCategoryInfo() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/enc/category",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateGroupEncCategoryInfo() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String urlTestInfo = String.format("/org-management/groups/%s/enc/category",groupIdEncoded);
        String url = String.format("/org-management/groups/%s/enc/category/%s",groupIdEncoded,"sub");

        MvcResult mvcResult = mockMvc.perform(get(urlTestInfo).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        EncryptCategoryInfo encryptCategoryInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(EncryptCategoryInfo.class));

        List<String> categoryIdList = encryptCategoryInfo.getCategoryIdList();

        if (!categoryIdList.get(0).equalsIgnoreCase("0000001"))
            categoryIdList.add(0,"0000001" );

        encryptCategoryInfo.setCategoryIdList(categoryIdList);

        //object to json
        String jsonInString = objectMapper.writeValueAsString(encryptCategoryInfo);

        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBypolicyTitle, "오프라인시 IE 종료예외").exists())
                .andDo(print());
    }

    //  접근대상 지정 가능 그룹
    @Test
    public void getGroupAccessGroupInfo() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/enc/access-group",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateGroupAccessGroupInfo() throws Exception {
        //String groupId = "SCDS_000000210";
        String groupId = "SCDP00000";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String urlTestInfo = String.format("/org-management/groups/%s/enc/access-group",groupIdEncoded);
        String url = String.format("/org-management/groups/%s/enc/access-group/%s",groupIdEncoded,"sub");
        //String url = String.format("/org-management/groups/%s/enc/category",groupIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(urlTestInfo).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                //.andExpect(status().isOk())
                .andDo(print()).andReturn();

        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        AccessGroupInfo accessGroupInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(AccessGroupInfo.class));

        List<String> targetGroupIdList = accessGroupInfo.getTargetGroupIdList();

        if(null == targetGroupIdList)
            targetGroupIdList = new ArrayList<>();

        if (targetGroupIdList.isEmpty() || !targetGroupIdList.get(0).equalsIgnoreCase("SCDP00000"))
            targetGroupIdList.add(0,"SCDP00000" );

        accessGroupInfo.setTargetGroupIdList(targetGroupIdList);

        //object to json
        String jsonInString = objectMapper.writeValueAsString(accessGroupInfo);

        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBypolicyTitle, "오프라인시 IE 종료예외").exists())
                .andDo(print());
    }

    //강제암호화 정책
    @Test
    public void getForcedEncryptInfo() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/enc/forced-enc",groupIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void updateForcedEncryptInfo() throws Exception {
        String groupId = "SCDP00000";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/groups/%s/enc/forced-enc",groupIdEncoded);
        String urlSubTest = String.format("/org-management/groups/%s/enc/forced-enc/%s",groupIdEncoded,"sub");

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectBypolicyTitle, "오프라인시 IE 종료예외").exists())
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
        mockMvc.perform(post(urlSubTest).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andDo(print());

    }

    //프로파일
    // - 로그인
    @Test
    public void getProfileLoginPolicy() throws Exception {
        String groupId = "SCDP00001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/profile/login",groupIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateProfileLoginPolicy() throws Exception {
        String groupId = "SECURITYDOMAIN";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/profile/login",groupIdEncoded);
        String urlSubTest = String.format("/org-management/groups/%s/profile/login/%s",groupIdEncoded,"sub");

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        LoginInfo loginInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(LoginInfo.class));

        loginInfo.setLimitLoginFailNum(11);

        //object to json
        String jsonInString = objectMapper.writeValueAsString(loginInfo);
        //받은 데이터
        mockMvc.perform(post(urlSubTest).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andDo(print());

    }
    // - 앱제어
    @Test
    public void getProfileAppControlPolicy() throws Exception {
        String groupId = "SCDP00001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/profile/app-control",groupIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateProfileAppControlPolicy() throws Exception {
        String groupId = "SCDP00001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/profile/app-control",groupIdEncoded);

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
        String groupId = "SCDP00001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/profile/password",groupIdEncoded);
        JSONObject json = new JSONObject();

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    // - 외부전송
    @Test
    public void getProfileCheckoutPolicy() throws Exception {
        String groupId = "SCDP00001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/profile/checkout",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    // - 기타
    @Test
    public void getProfileEtcPolicy() throws Exception {
        String groupId = "SCDP00001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/profile/etc",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    // - 프린트 마킹
    @Test
    public void getProfilePrintMarkPolicy() throws Exception {
        String groupId = "SCDP00001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/profile/print-mark",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //하위 관리자
    @Test
    public void getGroupManagerPolicy() throws Exception {
        String groupId = "SCDP00001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/group-manager",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //PC 보안
    @Test
    public void getPCSecurityPolicy() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/pc-security",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void updatePCSecurityPolicy() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/pc-security",groupIdEncoded);
        String urlSubTest = String.format("/org-management/groups/%s/pc-security/%s",groupIdEncoded,"sub");

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<PcSecurityInfo> pcSecurityInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, PcSecurityInfo.class));


        UpdatePcSecurityInfo updatePcSecurityInfo = new UpdatePcSecurityInfo();
        updatePcSecurityInfo.setPcSecurityInfoList(pcSecurityInfoList);

        //object to json
        String jsonInString = objectMapper.writeValueAsString(updatePcSecurityInfo);
        //받은 데이터
        mockMvc.perform(post(urlSubTest).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //강제 권한 - 요청
    @Test
    public void getForcePermissionGradePolicy() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //강제 권한 - 추가
    @Test
    public void test1_insertForcePermissionGradePolicy() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade",groupIdEncoded);

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
                gradeInfo1.setTargetId(groupId);
                gradeInfo1.setTargetType("G");
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
    public void test2_deleteForcePermissionGradePolicy() throws Exception {
        String groupId = "SCDS_000000210";
        String gradeId = "0000005";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade/%s",groupIdEncoded, gradeId);

        mockMvc.perform(delete(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    //강제 권한 - 추가 - 하위 적용
    @Test
    public void test3_insertForcePermissionGradePolicySub() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade/%s",groupIdEncoded, "sub");

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
                gradeInfo1.setTargetId(groupId);
                gradeInfo1.setTargetType("G");
                jsonInString = objectMapper.writeValueAsString(gradeInfo1);
            }
        }

        if (!jsonInString.isEmpty())
        {
            mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                    .andExpect(status().isOk())
                    //.andExpect(jsonPath(expectByResult, "1").exists())
                    .andDo(print());
        }

    }

    //강제 권한 - 삭제 - 하위적용
    @Test
    public void test4_deleteForcePermissionGradePolicySub() throws Exception {
        String groupId = "SCDS_000000210";
        String gradeId = "0000005";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade/%s/sub",groupIdEncoded, gradeId);

        mockMvc.perform(delete(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    //강제 권한 - 문서권한 요청
    @Test
    public void test3_getForcePermissionGradeAuthPolicy() throws Exception {
        String groupId = "SCDS_000000210";
        String targetGradeId = "0000001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade/auth/%s",groupIdEncoded, targetGradeId);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    //강제 권한 - 문서권한 추가
    @Test
    public void test2_insertForcePermissionGradeAuthPolicy() throws Exception {
        String groupId = "SCDS_000000202";
        String gradeId = "0000001";
        String targetGroupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));
        String targetGroupIdEncoded = Base64.encodeBase64String(targetGroupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade/auth/sub",targetGroupIdEncoded);

        String getUrl = String.format("/org-management/groups/%s/force-permission/grade/auth/%s",groupIdEncoded, gradeId);
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
            gradeAuthInfoList.get(i).setTargetId(targetGroupId);
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
        String groupId = "SCDS_000000210";
        String gradeId = "0000001";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade/auth/%s/sub",groupIdEncoded, gradeId);

        mockMvc.perform(delete(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    //강제 권한 - DAC권한 요청
    @Test
    public void getForcePermissionGradeDACPolicy() throws Exception {
        String groupId = "SCDS_000000210";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/grade/dac",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //강제 권한 - DAC권한 수정
    @Test
    public void updateForcePermissionGradeDACPolicy() throws Exception {
        //String targetGroupId = "SCDS_000000210";
        String targetGroupId = "SCDP00000";
        String targetGroupIdEncoded = Base64.encodeBase64String(targetGroupId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/groups/%s/force-permission/grade/dac/sub",targetGroupIdEncoded);
        String getUrl = String.format("/org-management/groups/%s/force-permission/grade/dac",targetGroupIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(getUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();

        ForcedAuthInfo forcedAuthInfo = new ObjectMapper().readValue(resultString, typeFactory.constructType(ForcedAuthInfo.class));
        String jsonInString = new String();

        forcedAuthInfo.setReadAuth("1");    //허용 , null : 미사용, 0 : 차단

        jsonInString = objectMapper.writeValueAsString(forcedAuthInfo);

        if (!jsonInString.isEmpty())
        {
            mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                    .andExpect(status().isOk())
//                    .andExpect(jsonPath(expectByResult, "1").exists())
                    .andDo(print());
        }
    }

    //강제 권한 - MAC권한 요청
    @Test
    public void getForcePermissionCategoryPolicy() throws Exception {
        String groupId = "SCDS_000000202";
        String groupIdEncoded = Base64.encodeBase64String(groupId.getBytes(StandardCharsets.UTF_8));

        String url = String.format("/org-management/groups/%s/force-permission/category/auth",groupIdEncoded);

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    //강제 권한 - MAC권한 수정
    @Test
    public void updateForcePermissionCategoryPolicy() throws Exception {
        String targetGroupId = "SCDP00000";
        //String targetGroupId = "SCDS_000000202";
        String targetGroupIdEncoded = Base64.encodeBase64String(targetGroupId.getBytes(StandardCharsets.UTF_8));
        String url = String.format("/org-management/groups/%s/force-permission/category/auth/sub",targetGroupIdEncoded);
        String getUrl = String.format("/org-management/groups/%s/force-permission/category/auth",targetGroupIdEncoded);

        MvcResult mvcResult = mockMvc.perform(get(getUrl).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();

        List<CategoryAuthInfo> categoryAuthInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, CategoryAuthInfo.class));
        String jsonInString = new String();

//        forcedAuthInfo.setReadAuth("1");    //허용 , null : 미사용, 0 : 차단

        jsonInString = objectMapper.writeValueAsString(categoryAuthInfoList);

        if (!jsonInString.isEmpty())
        {
            mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                    .andExpect(status().isOk())
//                    .andExpect(jsonPath(expectByResult, "1").exists())
                    .andDo(print());
        }
    }

}
