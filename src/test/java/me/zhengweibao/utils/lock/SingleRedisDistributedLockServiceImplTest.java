package me.zhengweibao.utils.lock;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

/**
 * @author zhengweibao
 */
public class SingleRedisDistributedLockServiceImplTest {

	private static final Logger logger = LoggerFactory.getLogger(SingleRedisDistributedLockServiceImplTest.class);

	/**
	 * redis 主机名
	 */
	private static final String redisHost = "localhost";

	/**
	 * redis 端口
	 */
	private static final Integer redisPort = 6379;

	private SingleRedisDistributedLockServiceImpl distributedLockService;

	@Before
	public void setUp() throws Exception {
		// Redis 链接池
		JedisPool pool = new JedisPool(redisHost, redisPort);

		distributedLockService = new SingleRedisDistributedLockServiceImpl(pool);
	}

	@Test
	public void freeLock() {
		RedisDistributedLock testLock = distributedLockService.getLock("test_lock_name", System.currentTimeMillis() + "", 100000L);

		logger.info("Get a lock [lockName = {}, lockContent = {}]", testLock == null ? null : testLock.getLockName(), testLock == null ? null : testLock.getLockContent());

		distributedLockService.freeLock(testLock);
	}
}