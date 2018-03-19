package com.zdevzone.tools.lock;

import com.zdevzone.tools.service.DistributedLockService;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zhengweibao
 */
public class SingleRedisDLServiceTest extends SingleRedisDLAbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(SingleRedisDLServiceTest.class);

	@Autowired
	private DistributedLockService distributedLockService;

	@Test
	public void freeLock() {
		DistributedLock distributedLock = distributedLockService.getLock("test_lock_name", System.currentTimeMillis() + "", 100000L);

		RedisDistributedLock testLock = (RedisDistributedLock) distributedLock;

		logger.info("Get a lock [lockName = {}, lockContent = {}]", testLock == null ? null : testLock.getLockName(), testLock == null ? null : testLock.getLockContent());

		distributedLockService.freeLock(testLock);
	}
}