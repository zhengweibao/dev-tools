package me.zhengweibao.utils.lock;

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
