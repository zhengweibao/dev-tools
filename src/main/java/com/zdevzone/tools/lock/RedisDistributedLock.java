package com.zdevzone.tools.lock;

/**
 * @author zhengweibao
 */
public class RedisDistributedLock implements DistributedLock {

	private final Long expiredAt;

	private final String lockName;

	private final String lockContent;

	public RedisDistributedLock(String lockName, String lockContent, Long maxValidTime) {
		this.expiredAt = System.currentTimeMillis() + maxValidTime;
		this.lockName = lockName;
		this.lockContent = lockContent;
	}

	@Override
	public boolean isExpired() {
		return expiredAt < System.currentTimeMillis();
	}

	public String getLockName() {
		return lockName;
	}

	public String getLockContent() {
		return lockContent;
	}
}
