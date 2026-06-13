package com.mymicroservice.gateway.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GatewayApplicationTests {

	@Test
	void contextLoads_ShouldStartApplicationContext_WhenTestProfileIsActive() {
	}

}
