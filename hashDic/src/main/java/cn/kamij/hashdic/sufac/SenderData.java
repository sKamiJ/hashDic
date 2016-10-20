package cn.kamij.hashdic.sufac;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 发送模块统计数据
 * 
 * @author KamiJ
 *
 */
public class SenderData {
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

	/**
	 * 所属发送线程组id
	 */
	private final int id;

	// 各统计数据
	/**
	 * 正在运行的发送线程数，包括正在停止的线程
	 */
	private volatile int senderThreadNum;

	/**
	 * 正在停止的发送线程数
	 */
	private volatile int senderQuitingThreadNum;

	/**
	 * 正在运行的诊断线程数
	 */
	private volatile int clinicThreadNum;

	/**
	 * 尚未发送的存储单元数量
	 */
	private final NumBytes notSendedNum;

	/**
	 * 发送中的存储单元数量，包括正在诊断的
	 */
	private final NumBytes sendingNum;

	/**
	 * 已发送的存储单元数量
	 */
	private final NumBytes sendedNum;

	/**
	 * 诊断中的存储单元数量
	 */
	private final NumBytes clinicingNum;

	/**
	 * 重复的存储单元数量
	 */
	private final NumBytes duplicateNum;

	/**
	 * 因强制退出而未处理的存储单元数量
	 */
	private final NumBytes fQuitNum;

	// 用于读数据
	public int getId() {
		return id;
	}

	public int getSenderThreadNum() {
		return senderThreadNum;
	}

	public int getSenderQuitingThreadNum() {
		return senderQuitingThreadNum;
	}

	public int getClinicThreadNum() {
		return clinicThreadNum;
	}

	public NumBytes getNotSendedNum() {
		return notSendedNum;
	}

	public NumBytes getSendingNum() {
		return sendingNum;
	}

	public NumBytes getSendedNum() {
		return sendedNum;
	}

	public NumBytes getClinicingNum() {
		return clinicingNum;
	}

	public NumBytes getDuplicateNum() {
		return duplicateNum;
	}

	public NumBytes getfQuitNum() {
		return fQuitNum;
	}

	// 构造函数
	public SenderData(int id) {
		this.id = id;
		this.senderThreadNum = 0;
		this.senderQuitingThreadNum = 0;
		this.clinicThreadNum = 0;
		this.notSendedNum = new NumBytes();
		this.sendingNum = new NumBytes();
		this.sendedNum = new NumBytes();
		this.clinicingNum = new NumBytes();
		this.duplicateNum = new NumBytes();
		this.fQuitNum = new NumBytes();
	}

	public SenderData(int id, int senderThreadNum, int senderQuitingThreadNum, int clinicThreadNum,
			NumBytes notSendedNum, NumBytes sendingNum, NumBytes sendedNum, NumBytes clinicingNum,
			NumBytes duplicateNum, NumBytes fQuitNum) {
		this.id = id;
		this.senderThreadNum = senderThreadNum;
		this.senderQuitingThreadNum = senderQuitingThreadNum;
		this.clinicThreadNum = clinicThreadNum;
		this.notSendedNum = notSendedNum;
		this.sendingNum = sendingNum;
		this.sendedNum = sendedNum;
		this.clinicingNum = clinicingNum;
		this.duplicateNum = duplicateNum;
		this.fQuitNum = fQuitNum;
	}

	// 内部调用的变化函数
	/**
	 * 添加正在运行的发送线程数
	 */
	void addSenderThreadNum(int num) {
		lockWrite();
		senderThreadNum += num;
		unlockWrite();
	}

	/**
	 * 添加正在停止的发送线程数
	 */
	void addSenderQuitingThreadNum(int num) {
		lockWrite();
		senderQuitingThreadNum += num;
		unlockWrite();
	}

	/**
	 * 减少正在运行与正在停止的发送线程数
	 */
	void reduceSenderAndSenderQuitingThreadNum(int num) {
		lockWrite();
		senderQuitingThreadNum -= num;
		senderThreadNum -= num;
		unlockWrite();
	}

	/**
	 * 添加正在运行的诊断线程数
	 */
	void addClinicThreadNum(int num) {
		lockWrite();
		clinicThreadNum += num;
		unlockWrite();
	}

	/**
	 * 减少正在运行的诊断线程数
	 */
	void reduceClinicThreadNum(int num) {
		lockWrite();
		clinicThreadNum -= num;
		unlockWrite();
	}

	/**
	 * 添加发送中的存储单元数量，同时减少尚未发送的存储单元数量
	 */
	void addSendingNumFromNotSended(long num, long bytesNum) {
		lockWrite();
		sendingNum.add(num, bytesNum);
		notSendedNum.reduce(num, bytesNum);
		unlockWrite();
	}

	/**
	 * 减少发送中的存储单元数量，同时添加尚未发送的存储单元数量
	 */
	void reduceSendingNumToNotSended(long num, long bytesNum) {
		lockWrite();
		notSendedNum.add(num, bytesNum);
		sendingNum.reduce(num, bytesNum);
		unlockWrite();
	}

	/**
	 * 添加已发送的存储单元数量，同时减少发送中的存储单元数量
	 */
	void addSendedNumFromSending(long num, long bytesNum) {
		lockWrite();
		sendedNum.add(num, bytesNum);
		sendingNum.reduce(num, bytesNum);
		unlockWrite();
	}

	/**
	 * 添加诊断中的存储单元数量
	 */
	void addClinicingNum(long num, long bytesNum) {
		lockWrite();
		clinicingNum.add(num, bytesNum);
		unlockWrite();
	}

	/**
	 * 减少诊断中的与发送中的存储单元数量，同时添加已发送的存储单元数量
	 */
	void reduceClinicingAndSendingNumToSended(long num, long bytesNum) {
		lockWrite();
		sendedNum.add(num, bytesNum);
		clinicingNum.reduce(num, bytesNum);
		sendingNum.reduce(num, bytesNum);
		unlockWrite();
	}

	/**
	 * 减少诊断中的与发送中的存储单元数量，同时添加重复的存储单元数量
	 */
	void reduceClinicingAndSendingNumToDuplicate(long num, long bytesNum) {
		lockWrite();
		duplicateNum.add(num, bytesNum);
		clinicingNum.reduce(num, bytesNum);
		sendingNum.reduce(num, bytesNum);
		unlockWrite();
	}

	/**
	 * 减少诊断中的与发送中的存储单元数量，同时添加因强制退出而未处理的存储单元数量
	 */
	void reduceClinicingAndSendingNumToFQuit(long num, long bytesNum) {
		lockWrite();
		fQuitNum.add(num, bytesNum);
		clinicingNum.reduce(num, bytesNum);
		sendingNum.reduce(num, bytesNum);
		unlockWrite();
	}

	/**
	 * 添加尚未发送的存储单元数量，该方法未同步，需加该对象的写锁
	 */
	void addNotSendedNum(long num, long bytesNum) {
		notSendedNum.add(num, bytesNum);
	}

	/**
	 * 添加统计数据，该方法未同步，需自行保证两个SenderData不变化
	 */
	void add(SenderData senderData) {
		senderThreadNum += senderData.senderThreadNum;
		senderQuitingThreadNum += senderData.senderQuitingThreadNum;
		clinicThreadNum += senderData.clinicThreadNum;
		notSendedNum.add(senderData.notSendedNum);
		sendingNum.add(senderData.sendingNum);
		sendedNum.add(senderData.sendedNum);
		clinicingNum.add(senderData.clinicingNum);
		duplicateNum.add(senderData.duplicateNum);
		fQuitNum.add(senderData.fQuitNum);
	}

	/**
	 * 复制当前对象
	 */
	SenderData copy() {
		lockRead();
		SenderData senderData = new SenderData(id, senderThreadNum, senderQuitingThreadNum, clinicThreadNum,
				notSendedNum.copy(), sendingNum.copy(), sendedNum.copy(), clinicingNum.copy(), duplicateNum.copy(),
				fQuitNum.copy());
		unlockRead();
		return senderData;
	}

}
