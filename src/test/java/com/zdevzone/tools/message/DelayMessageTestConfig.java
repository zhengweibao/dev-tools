package com.zdevzone.tools.message;

import com.zdevzone.tools.config.DelayMessageClientConfig;
import com.zdevzone.tools.config.EnableDelayMessage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhengweibao
 */
@Configuration
@EnableDelayMessage
public class DelayMessageTestConfig {

	public static final String TEST_HANDLER_ID = "test_delay_message_handler";

	@Bean
	public DelayMessageClientConfig delayMessageClientConfig(){
		return DelayMessageClientConfig.builder()
				.clientId("utils_test").nodeId("local_machine")
				.rabbitHost("localhost").rabbitPort(5672)
				.rabbitUsername("admin").rabbitPassword("admin")
				.rabbitVirtualHost("utils").build();
	}
}
