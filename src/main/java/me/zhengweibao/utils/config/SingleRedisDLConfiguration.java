package me.zhengweibao.utils.config;

import me.zhengweibao.utils.lock.SingleRedisDistributedLockServiceImpl;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

/**
 * @author zhengweibao
 */
@Configuration
public class SingleRedisDLConfiguration {

	private RedisDLClientConfig redisDLClientConfig;

	@Autowired
	public void setRedisDLClientConfig(RedisDLClientConfig redisDLClientConfig) {
		this.redisDLClientConfig = redisDLClientConfig;
	}

	@Bean
	public JedisPool singleRedisDLJedisPool(){
		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setMaxIdle(redisDLClientConfig.getMaxIdle());
		genericObjectPoolConfig.setMaxTotal(redisDLClientConfig.getMaxTotal());
		genericObjectPoolConfig.setMinIdle(redisDLClientConfig.getMinIdle());

		return new JedisPool(genericObjectPoolConfig, redisDLClientConfig.getRedisHost(), redisDLClientConfig.getRedisPort());
	}

	@Bean
	public SingleRedisDistributedLockServiceImpl singleRedisDistributedLockService(){
		return new SingleRedisDistributedLockServiceImpl(singleRedisDLJedisPool());
	}
}