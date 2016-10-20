package cn.kamij.hashdic.sufac;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 制作模块统计数据
 * 
 * @author KamiJ
 *
 */
public class CreatorData {
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
	 * 正在运行的制作线程数，包括正在停止的线程
	 */
	private volatile int creatorThreadNum;

	/**
	 * 正在停止的制作线程数
	 */
	private volatile int creatorQuitingThreadNum;

	/**
	 * 已制作的存储单元数量
	 */
	private final NumBytes createdNum;

	/**
	 * 尚未分配的存储单元数量
	 */
	private final NumBytes notDistributedNum;

	// 用于读数据
	public int getCreatorThreadNum() {
		return creatorThreadNum;
	}

	public int getCreatorQuitingThreadNum() {
		return creatorQuitingThreadNum;
	}

	public NumBytes getCreatedNum() {
		return createdNum;
	}

	public NumBytes getNotDistributedNum() {
		return notDistributedNum;
	}

	// 构造函数
	public CreatorData() {
		creatorThreadNum = 0;
		creatorQuitingThreadNum = 0;
		createdNum = new NumBytes();
		notDistributedNum = new NumBytes();
	}

	public CreatorData(int creatorThreadNum, int creatorQuitingThreadNum, NumBytes createdNum,
			NumBytes notDistributedNum) {
		this.creatorThreadNum = creatorThreadNum;
		this.creatorQuitingThreadNum = creatorQuitingThreadNum;
		this.createdNum = createdNum;
		this.notDistributedNum = notDistributedNum;
	}

	// 内部调用的变化函数
	/**
	 * 添加正在运行的制作线程数
	 */
	void addCreatorThreadNum(int num) {
		lockWrite();
		creatorThreadNum += num;
		unlockWrite();
	}

	/**
	 * 添加正在停止的制作线程数
	 */
	void addCreatorQuitingThreadNum(int num) {
		lockWrite();
		creatorQuitingThreadNum += num;
		unlockWrite();
	}

	/**
	 * 减少正在运行与正在停止的制作线程数
	 */
	void reduceCreatorAndCreatorQuitingThreadNum(int num) {
		lockWrite();
		creatorQuitingThreadNum -= num;
		creatorThreadNum -= num;
		unlockWrite();
	}

	/**
	 * 添加已制作的和尚未分配的存储单元数量
	 */
	void addCreatedAndNotDistributedNum(long num, long bytesNum) {
		lockWrite();
		createdNum.add(num, bytesNum);
		notDistributedNum.add(num, bytesNum);
		unlockWrite();
	}
	
	/**
	 * 减少尚未分配的存储单元数量，该方法未同步，需加该对象的写锁
	 */
	void reduceNotDistributedNum(long num, long bytesNum) {
		notDistributedNum.reduce(num, bytesNum);
	}

	/**
	 * 添加统计数据，该方法未同步，需自行保证两个CreatorData不变化
	 */
	void add(CreatorData creatorData) {
		creatorThreadNum += creatorData.creatorThreadNum;
		creatorQuitingThreadNum += creatorData.creatorQuitingThreadNum;
		createdNum.add(creatorData.createdNum);
		notDistributedNum.add(creatorData.notDistributedNum);
	}

	/**
	 * 复制当前对象
	 */
	CreatorData copy() {
		lockRead();
		CreatorData creatorData = new CreatorData(creatorThreadNum, creatorQuitingThreadNum, createdNum.copy(),
				notDistributedNum.copy());
		unlockRead();
		return creatorData;
	}
}
