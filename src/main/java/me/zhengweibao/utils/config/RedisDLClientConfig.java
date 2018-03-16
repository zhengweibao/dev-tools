package me.zhengweibao.utils.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.util.Assert;

/**
 * @author zhengweibao
 */
public class RedisDLClientConfig {
	
	public static class Builder {
		private Integer maxTotal = GenericObjectPoolConfig.DEFAULT_MAX_TOTAL;

		private Integer maxIdle = GenericObjectPoolConfig.DEFAULT_MAX_IDLE;

		private Integer minIdle = GenericObjectPoolConfig.DEFAULT_MIN_IDLE;

		private String redisHost;

		private Integer redisPort;
		
		private Builder(){
		}

		public Builder maxTotal(Integer maxTotal) {
			this.maxTotal = maxTotal;
			return this;
		}

		public Builder maxIdle(Integer maxIdle) {
			this.maxIdle = maxIdle;
			return this;
		}

		public Builder minIdle(Integer minIdle) {
			this.minIdle = minIdle;
			return this;
		}

		public Builder redisHost(String redisHost) {
			this.redisHost = redisHost;
			return this;
		}

		public Builder redisPort(Integer redisPort) {
			this.redisPort = redisPort;
			return this;
		}
		
		public RedisDLClientConfig build(){
			Assert.notNull(maxTotal, "The maxTotal cannot be null!");
			Assert.notNull(maxIdle, "The maxIdle cannot be null!");
			Assert.notNull(minIdle, "The minIdle cannot be null!");
			Assert.hasText(redisHost, "The redisHost cannot be empty!");
			Assert.notNull(redisPort, "The redisPort cannot be empty!");
			
			RedisDLClientConfig redisDLClientConfig = new RedisDLClientConfig();

			redisDLClientConfig.maxTotal = this.maxTotal;
			redisDLClientConfig.maxIdle = this.maxIdle;
			redisDLClientConfig.minIdle = this.minIdle;
			redisDLClientConfig.redisHost = this.redisHost;
			redisDLClientConfig.redisPort = this.redisPort;
			
			return redisDLClientConfig;
		}
	}

	private Integer maxTotal;

	private Integer maxIdle;

	private Integer minIdle;
	
	private String redisHost;
	
	private Integer redisPort;

	public Integer getMaxTotal() {
		return maxTotal;
	}

	public Integer getMaxIdle() {
		return maxIdle;
	}

	public Integer getMinIdle() {
		return minIdle;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public Integer getRedisPort() {
		return redisPort;
	}

	public static Builder builder(){
		return new Builder();
	}
}
