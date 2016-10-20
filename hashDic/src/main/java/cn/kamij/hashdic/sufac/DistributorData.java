package cn.kamij.hashdic.sufac;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 分配模块统计数据
 * 
 * @author KamiJ
 *
 */
public class DistributorData {
	// 锁
	private final ReadWriteLock rwl = new ReentrantReadWriteLock(true);

	/**
	 * 加读锁
	 */
	void lockRead() {
		rwl.readLock().lock();
	}

	/**
	 * 解读锁
	 */
	void unlockRead() {
		rwl.readLock().unlock();
	}

	/**
	 * 加写锁
	 */
	void lockWrite() {
		rwl.writeLock().lock();
	}

	/**
	 * 解写锁
	 */
	void unlockWrite() {
		rwl.writeLock().unlock();
	}

	// 各统计数据
	/**
	 * 正在运行的分配线程数，包括正在停止的线程
	 */
	private volatile int distributorThreadNum;

	/**
	 * 正在停止的分配线程数
	 */
	private volatile int distributorQuitingThreadNum;

	/**
	 * 正在分配的存储单元数量
	 */
	private final NumBytes distributingNum;

	/**
	 * 已分配的存储单元数量
	 */
	private final NumBytes distributedNum;

	// 用于读数据
	public int getDistributorThreadNum() {
		return distributorThreadNum;
	}

	public int getDistributorQuitingThreadNum() {
		return distributorQuitingThreadNum;
	}

	public NumBytes getDistributingNum() {
		return distributingNum;
	}

	public NumBytes getDistributedNum() {
		return distributedNum;
	}

	// 构造函数
	public DistributorData() {
		distributorThreadNum = 0;
		distributorQuitingThreadNum = 0;
		distributingNum = new NumBytes();
		distributedNum = new NumBytes();
	}

	public DistributorData(int distributorThreadNum, int distributorQuitingThreadNum, NumBytes distributingNum,
			NumBytes distributedNum) {
		this.distributorThreadNum = distributorThreadNum;
		this.distributorQuitingThreadNum = distributorQuitingThreadNum;
		this.distributingNum = distributingNum;
		this.distributedNum = distributedNum;
	}

	// 内部调用的变化函数
	/**
	 * 添加正在运行的分配线程数
	 */
	void addDistributorThreadNum(int num) {
		lockWrite();
		distributorThreadNum += num;
		unlockWrite();
	}

	/**
	 * 添加正在停止的分配线程数
	 */
	void addDistributorQuitingThreadNum(int num) {
		lockWrite();
		distributorQuitingThreadNum += num;
		unlockWrite();
	}

	/**
	 * 减少正在运行与正在停止的分配线程数
	 */
	void reduceDistributorAndDistributorQuitingThreadNum(int num) {
		lockWrite();
		distributorQuitingThreadNum -= num;
		distributorThreadNum -= num;
		unlockWrite();
	}

	/**
	 * 添加正在分配的存储单元数量，该方法未同步，需加该对象的写锁
	 */
	void addDistributingNum(long num, long bytesNum) {
		distributingNum.add(num, bytesNum);
	}

	/**
	 * 添加已分配的存储单元数量，同时减少正在分配的存储单元数量，该方法未同步，需加该对象的写锁
	 */
	void addDistributedNumFromDistributing(long num, long bytesNum) {
		distributingNum.reduce(num, bytesNum);
		distributedNum.add(num, bytesNum);
	}

	/**
	 * 添加统计数据，该方法未同步，需自行保证两个DistributorData不变化
	 */
	void add(DistributorData distributorData) {
		distributorThreadNum += distributorData.distributorThreadNum;
		distributorQuitingThreadNum += distributorData.distributorQuitingThreadNum;
		distributingNum.add(distributorData.distributingNum);
		distributedNum.add(distributorData.distributedNum);
	}

	/**
	 * 复制当前对象
	 */
	DistributorData copy() {
		lockRead();
		DistributorData distributorData = new DistributorData(distributorThreadNum, distributorQuitingThreadNum,
				distributingNum.copy(), distributedNum.copy());
		unlockRead();
		return distributorData;
	}

}
