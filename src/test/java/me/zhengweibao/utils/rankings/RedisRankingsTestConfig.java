package me.zhengweibao.utils.rankings;

import me.zhengweibao.utils.config.EnableRedisRankings;
import me.zhengweibao.utils.config.RedisConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author zhengweibao
 */
@Configuration
@EnableRedisRankings
public class RedisRankingsTestConfig {

	@Bean
	public RedisConfig redisConfig(){
		return RedisConfig.builder()
				.maxIdle(1).minIdle(0)
				.maxTotal(3).redisHost("localhost")
				.redisPort(6379).build();
	}
}
