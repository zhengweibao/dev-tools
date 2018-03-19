package com.zdevzone.tools.lock;

/**
 * @author zhengweibao
 */
public interface DistributedLock {

	/**
	 * 是否该锁已经过期
	 *
	 * @return T/F
	 */
	boolean isExpired();

}
