package me.zhengweibao.utils.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.zhengweibao.utils.dao.RankingsRankDao;
import me.zhengweibao.utils.service.RankingsService;
import me.zhengweibao.utils.service.impl.RedisRankingsServiceImpl;
import me.zhengweibao.utils.util.ObjectMapperUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author zhengweibao
 */
@Configuration
public class RedisRankingsConfiguration {

	private RankingsRankDao rankingsRankDao;

	private ObjectMapper objectMapper;

	private JedisConnectionFactory jedisConnectionFactory;

	private RedisConfig redisConfig;

	@Autowired(required = false)
	public void setRankingsRankDao(RankingsRankDao rankingsRankDao) {
		this.rankingsRankDao = rankingsRankDao;
	}

	@Autowired(required = false)
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Autowired(required = false)
	public void setJedisConnectionFactory(JedisConnectionFactory jedisConnectionFactory) {
		this.jedisConnectionFactory = jedisConnectionFactory;
	}

	@Autowired(required = false)
	public void setRedisConfig(RedisConfig redisConfig) {
		this.redisConfig = redisConfig;
	}

	@Bean
	public JedisConnectionFactory redisRankingsConnectionFactory(){
		if (jedisConnectionFactory != null) {
			return jedisConnectionFactory;
		} else {
			Assert.notNull(redisConfig, "The redisConfig cannot be null!");

			JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
			jedisPoolConfig.setMaxIdle(redisConfig.getMaxIdle());
			jedisPoolConfig.setMaxTotal(redisConfig.getMaxTotal());
			jedisPoolConfig.setMinIdle(redisConfig.getMinIdle());

			JedisConnectionFactory connectionFactory = new JedisConnectionFactory(jedisPoolConfig);
			connectionFactory.setHostName(redisConfig.getRedisHost());
			connectionFactory.setPort(redisConfig.getRedisPort());

			return connectionFactory;
		}
	}

	@Bean
	public RankingsService redisRankingsService(){
		RedisRankingsServiceImpl redisRankingsService = new RedisRankingsServiceImpl();
		redisRankingsService.setObjectMapper(objectMapper == null ? ObjectMapperUtil.getInstance() : objectMapper);
		redisRankingsService.setRankingsRankDao(rankingsRankDao);
		redisRankingsService.setRedisTemplate(new StringRedisTemplate(redisRankingsConnectionFactory()));

		return redisRankingsService;
	}
}
