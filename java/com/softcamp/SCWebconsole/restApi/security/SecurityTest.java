package com.softcamp.SCWebconsole.restApi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.softcamp.SCWebconsole.model.security.CategoryClassAuthInfo;
import com.softcamp.SCWebconsole.model.security.CategoryInfo;
import com.softcamp.SCWebconsole.model.security.ClassificationInfo;
import com.softcamp.SCWebconsole.model.security.GradeInfo;
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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class SecurityTest {
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
    public void getClassificationInfoList() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/security/classification");

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void updateClassificationInfoList() throws Exception {

        String url = String.format("/security/classification");

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<ClassificationInfo> classificationInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, ClassificationInfo.class));

        //object to json
        String jsonInString = objectMapper.writeValueAsString(classificationInfoList);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void getCategoryInfoList() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/security/category/info");

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateCategoryInfoList() throws Exception {

        String url = String.format("/security/category/info");

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<CategoryInfo> categoryInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, CategoryInfo.class));

        //object to json
        String jsonInString = objectMapper.writeValueAsString(categoryInfoList);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void getCategoryClassAuthInfoList() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/security/category/class-auth");

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateCategoryClassAuthInfoList() throws Exception {

        String url = String.format("/security/category/class-auth");

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<CategoryClassAuthInfo> categoryClassAuthInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, CategoryClassAuthInfo.class));

        //object to json
        String jsonInString = objectMapper.writeValueAsString(categoryClassAuthInfoList);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }

    @Test
    public void getGradeEncryptKeyInfoList() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/security/grade/encrypt-key");

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void getGradeInfoList() throws Exception {
        String expectBylogTypeName = "$.[?(@.logTypeName == '%s')]";
        String url = String.format("/security/grade/info");

        mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @Test
    public void updateGradeInfoList() throws Exception {

        String url = String.format("/security/grade/info");

        MvcResult mvcResult = mockMvc.perform(get(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print()).andReturn();

        //가져온데이터 그대로 업데이트
        //string to object
        String resultString = mvcResult.getResponse().getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        List<GradeInfo> categoryClassAuthInfoList = new ObjectMapper().readValue(resultString, typeFactory.constructCollectionType(List.class, GradeInfo.class));

        //object to json
        String jsonInString = objectMapper.writeValueAsString(categoryClassAuthInfoList);
        //받은 데이터
        mockMvc.perform(post(url).header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(jsonInString))
                .andExpect(status().isOk())
                //.andExpect(jsonPath(expectByResult, "1").exists())
                .andDo(print());
    }
}
