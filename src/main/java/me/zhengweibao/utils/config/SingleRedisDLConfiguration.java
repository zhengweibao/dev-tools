package me.zhengweibao.utils.config;

import me.zhengweibao.utils.service.impl.SingleRedisDistributedLockServiceImpl;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisPool;

/**
 * @author zhengweibao
 */
@Configuration
public class SingleRedisDLConfiguration {

	private RedisConfig redisConfig;

	private JedisPool jedisPool;

	@Autowired(required = false)
	public void setRedisConfig(RedisConfig redisConfig) {
		this.redisConfig = redisConfig;
	}

	@Autowired(required = false)
	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Bean
	public JedisPool singleRedisDLJedisPool(){
		if (jedisPool != null) {
			return jedisPool;
		}
		Assert.notNull(redisConfig, "The redisConfig cannot be null!");

		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setMaxIdle(redisConfig.getMaxIdle());
		genericObjectPoolConfig.setMaxTotal(redisConfig.getMaxTotal());
		genericObjectPoolConfig.setMinIdle(redisConfig.getMinIdle());

		return new JedisPool(genericObjectPoolConfig, redisConfig.getRedisHost(), redisConfig.getRedisPort());
	}

	@Bean
	public SingleRedisDistributedLockServiceImpl singleRedisDistributedLockService(){
		return new SingleRedisDistributedLockServiceImpl(singleRedisDLJedisPool());
	}
}