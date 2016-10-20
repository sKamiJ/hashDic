package cn.kamij.hashdic.sufac;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 原文生成器接口
 * 
 * @author KamiJ
 *
 */
public abstract class TextCreator {
	// 锁
	private final ReadWriteLock rwl = new ReentrantReadWriteLock(true);

	/**
	 * 加读锁
	 */
	public void lockRead() {
		rwl.readLock().lock();
	}

	/**
	 * 解读锁
	 */
	public void unlockRead() {
		rwl.readLock().unlock();
	}

	/**
	 * 加写锁
	 */
	public void lockWrite() {
		rwl.writeLock().lock();
	}

	/**
	 * 解写锁
	 */
	public void unlockWrite() {
		rwl.writeLock().unlock();
	}

	/**
	 * 获取当前原文，并使该生成器的原文向后变化，实现时需加写锁同步
	 * 
	 * @throws TextEndException
	 *             原文生成器结束异常
	 */
	public abstract String getTextAndIncrement() throws TextEndException;

	/**
	 * 获取该原文生成器统计数据，实现时需加读锁同步
	 */
	public abstract TextData getData();

	/**
	 * 该原文生成器是否结束，实现时需加读锁同步
	 */
	public abstract boolean isEnd();

	/**
	 * 获取原文生成器类型
	 */
	public abstract String getType();

	/**
	 * 获取原文总数
	 */
	public abstract long getTextSum();
}
