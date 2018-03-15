package me.zhengweibao.utils.lock;

/**
 * @author zhengweibao
 */
public interface DistributedLockService {

	/**
	 * 获取一个分布式锁对象
	 * 对于一个不需要手动释放的锁，锁的内容可以用占位符表示，但当需要进行手动释放时，需要考虑如下情形：
	 * 现有三个节点A，B，C尝试获取同一个锁LOCK_TEST，锁的内容为“content”
	 * A节点成功获取到分布式锁，开始执行某段代码逻辑，这段代码逻辑因为IO时延执行较为缓慢，导致锁已经自动过期
	 * 这时B节点获取到该锁，A节点执行结束，手动释放锁，将会导致B节点获取到的锁被提前不正确的释放掉
	 * 因此，当代码需要手动释放锁时，需要为每个锁设置一个唯一标识，推荐使用当前时间戳
	 *
	 * @param lockName 锁的名称
	 * @param lockContent 锁的内容
	 * @param maxValidTime 锁的最大存活时间
	 * @return 成功获取到锁时，返回一个分布式锁对象，否则返回Null
	 */
	DistributedLock getLock(String lockName, String lockContent, Long maxValidTime);

	/**
	 * 手动释放锁
	 * 会尝试判断该锁是否已过期，如果是，则直接返回（该锁已自动过期）
	 *      if(distributedLock.isExpired()){
	 *          return;
	 *      }
	 * 否则将手动释放该锁
	 * @param distributedLock 分布式锁
	 */
	void freeLock(DistributedLock distributedLock);
}
