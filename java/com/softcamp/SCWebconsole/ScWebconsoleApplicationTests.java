package com.softcamp.SCWebconsole;

import com.softcamp.SCWebconsole.controller.token.TokenController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ScWebconsoleApplicationTests {

	@Autowired
	private TokenController tokenController;

	@Test
	void contextLoads() {
		assertThat(tokenController).isNotNull();
	}

}
