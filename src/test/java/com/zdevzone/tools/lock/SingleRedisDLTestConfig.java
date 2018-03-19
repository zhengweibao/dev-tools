package com.zdevzone.tools.lock;

import com.zdevzone.tools.config.RedisConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhengweibao
 */
@Configuration
public class SingleRedisDLTestConfig {

	@Bean
	public RedisConfig redisDLClientConfig(){
		return RedisConfig.builder()
				.maxIdle(1).minIdle(0)
				.maxTotal(3).redisHost("localhost")
				.redisPort(6379).build();
	}

}
