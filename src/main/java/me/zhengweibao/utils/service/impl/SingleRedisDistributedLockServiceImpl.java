package me.zhengweibao.utils.service.impl;

import me.zhengweibao.utils.lock.DistributedLock;
import me.zhengweibao.utils.lock.RedisDistributedLock;
import me.zhengweibao.utils.util.Sha1DigestUtil;
import me.zhengweibao.utils.service.DistributedLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisNoScriptException;

import java.io.IOException;

/**
 * @author zhengweibao
 */
public class SingleRedisDistributedLockServiceImpl implements DistributedLockService {

	private static final Logger logger = LoggerFactory.getLogger(SingleRedisDistributedLockServiceImpl.class);

	/**
	 * SET参数， NX表示setIfNotExists
	 */
	private static final String NX = "NX";

	/**
	 * SET参数， PX表示setAndExpireAfterMilliSeconds
	 */
	private static final String PX = "PX";

	private static final String SUCCESS_STATUS_RESULT = "OK";

	private static final String FREE_LOCK_SCRIPT;

	private static final String FREE_LOCK_SCRIPT_SHA1;

	static {
		try {
			FREE_LOCK_SCRIPT = new ResourceScriptSource(new ClassPathResource("lua/free_single_redis_lock.lua")).getScriptAsString();
			FREE_LOCK_SCRIPT_SHA1 = Sha1DigestUtil.getSha1HexDigest(FREE_LOCK_SCRIPT);
		} catch (IOException e) {
			logger.error("Cannot fetch lua script from path : classpath:lua/free_single_redis_lock.lua!");
			throw new RuntimeException(e);
		}
	}

	private final JedisPool jedisPool;

	public SingleRedisDistributedLockServiceImpl(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Override
	public RedisDistributedLock getLock(String lockName, String lockContent, Long maxValidTimeMillis) {
		Assert.hasText(lockName, "The lockName cannot be empty!");
		Assert.isTrue(maxValidTimeMillis != null && maxValidTimeMillis > 0, "The value of maxValidTimeMillis must be setup and should be a positive number.");

		lockContent = StringUtils.isEmpty(lockContent) ? "" + System.currentTimeMillis() : lockContent;

		try (Jedis jedis = jedisPool.getResource()) {
			String result = jedis.set(lockName, lockContent, NX, PX, maxValidTimeMillis);
			if (!SUCCESS_STATUS_RESULT.equals(result)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Cannot get a distributed lock with params[lockName = {}, lockContent = {}, maxValidTimeMillis = {}].", lockName, lockContent, maxValidTimeMillis);
				}

				return null;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Success to get a distributed lock with params[lockName = {}, lockContent = {}, maxValidTimeMillis = {}].", lockName, lockContent, maxValidTimeMillis);
			}

			return new RedisDistributedLock(lockName, lockContent, maxValidTimeMillis);
		}
	}

	@Override
	public void freeLock(DistributedLock distributedLock) {
		if (distributedLock == null || distributedLock.isExpired() || !(distributedLock instanceof RedisDistributedLock)) {
			return;
		}

		RedisDistributedLock lock = (RedisDistributedLock) distributedLock;
		boolean success;

		try (Jedis jedis = jedisPool.getResource()) {
			Object evalResult;

			try {
				evalResult = jedis.evalsha(FREE_LOCK_SCRIPT_SHA1, 1, lock.getLockName(), lock.getLockContent());
			} catch (JedisNoScriptException e) {
				evalResult = jedis.eval(FREE_LOCK_SCRIPT, 1, lock.getLockName(), lock.getLockContent());
			}

			success = evalResult != null && ((Long) evalResult) == 1;
		}

		if (success) {
			if (logger.isDebugEnabled()) {
				logger.debug("Success to free a distributed lock[lockName = {}, lockContent = {}].", lock.getLockName(), lock.getLockContent());
			}
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("Fail to free a distributed lock[lockName = {}, lockContent = {}, isLockExpired = {}].", lock.getLockName(), lock.getLockContent(), lock.isExpired());
			}
		}
	}
}
