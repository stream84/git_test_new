package com.softcamp.SCWebconsole.restApi;

import com.softcamp.SCWebconsole.controller.token.TokenController;
//import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Controller Test Sample
 */
@SpringBootTest
public class RestApiTestSample {

    @Autowired
    private TokenController tokenController;

//    @Test
//    public void controller_load(){
//        //assertThat(tokenController).isNotNull();
//    }
}
