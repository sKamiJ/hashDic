package cn.kamij.hashdic.sufac;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import cn.kamij.hashdic.model.StoreUnit;

/**
 * 存储单元发送线程组，每个组有一个发送队列以及若干个发送线程
 * 
 * @author KamiJ
 */
public class StoreUnitSenderGroup {
	/**
	 * 发送线程组最大线程数
	 */
	public static final int MAX_THREAD_NUM = 20;

	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitSenderGroup.class);

	/**
	 * 所属工厂
	 */
	final StoreUnitFactory factory;

	/**
	 * 该组统计数据
	 */
	final SenderData groupSenderData;

	/**
	 * 存储单元发送队列
	 */
	// 注:这里使用了ConcurrentLinkedQueue而没有使用LinkedBlockingQueue；
	//
	// 前者使用了CAS(compare and set/swap，这是一个原子操作，CPU支持这条指令，
	// 在CAS之前通常会进行一次读操作，在用CAS进行写操作时，会比较要更改的值是否与预期值(一般为之前读取的值)一致，
	// 若一致，则说明该值未被其他线程所更改，可以安全地更改，否则说明该值被其他线程更改，要重新读值。它保证了读写的一致性。
	// ConcurrentLinkedQueue是这样使用CAS的：
	//
	// 出队列：首先获取头节点的元素，然后判断头节点元素是否为空，如果为空，表示另外一个线程已经进行了一次出队操作将该节点的元素取走，
	// 如果不为空，则使用CAS的方式将头节点的引用设置成null，如果CAS成功，则直接返回头节点的元素，如果不成功，
	// 表示另外一个线程已经进行了一次出队操作更新了head节点，导致元素发生了变化，需要重新获取头节点。
	//
	// 入队列：定位出尾节点，然后使用CAS算法能将入队节点设置成尾节点的next节点，如不成功则重试。)，保证了线程安全，
	//
	// 而后者使用了加锁来保证(获得锁的过程其实也是用的CAS)。
	//
	// 当队列为空时，前者会即时返回null，而后者会阻塞线程，直至队列不为空；当队列超过某个数时，前者依然入队，而后者会阻塞线程，直至有元素出队。
	// 这里用前者是因为有可能因发送失败而要添加元素回发送队列，而当该线程因队列满而被阻塞，且该线程被interrupt时，会异常。使用前者可以更安全地退出线程。
	//
	// 另：Java的并发操作要保证原子性(该操作要么全部执行，要么全部不执行)、可见性(一个线程更改值后其他线程立即可知，可由volatile保证)、有序性(程序按照顺序执行)。
	// volatile是直接在内存读写，而不会暂放在寄存器操作，这样就保证了可见性。其他的关键字：native：调用其他语言完成函数；transient：不序列化该变量。
	final ConcurrentLinkedQueue<StoreUnit> sendList = new ConcurrentLinkedQueue<>();

	/**
	 * 下属发送线程队列，LinkedList本身不是线程安全的，但这里做了同步处理，所以线程安全
	 */
	final List<StoreUnitSender> senders = new LinkedList<>();

	/**
	 * 该发送线程组标识符
	 */
	final int id;

	/**
	 * 线程操作锁
	 */
	final ReentrantLock threadOperateLock = new ReentrantLock(true);

	/**
	 * 线程变化锁
	 */
	final ReentrantLock threadChangeLock = new ReentrantLock(true);

	/**
	 * @param factory
	 *            所属工厂
	 * @param senderNum
	 *            发送线程数，senderNum < 0 || senderNum > MAX_THREAD_NUM
	 */
	StoreUnitSenderGroup(StoreUnitFactory factory, int senderNum) {
		// 设置所属工厂
		this.factory = factory;
		// 设置id
		id = this.factory.senderGroupSum.getAndIncrement();
		// 初始化统计数据
		groupSenderData = new SenderData(id);
		// 添加自己至可分配的发送线程组
		this.factory.distributableSenderGroups.add(this);
		// 添加发送线程
		for (int i = 0; i < senderNum; i++) {
			senders.add(new StoreUnitSender(this));
		}
		LOGGER.info("工厂id：" + this.factory.id + "——发送线程组id：" + id + "——初始化完成，发送线程数：" + senderNum + "。");
	}

	/**
	 * 该发送线程组是否可分配
	 */
	public boolean isDistributable() {
		boolean result = false;
		factory.senderGroupLock.readLock().lock();
		if (factory.distributableSenderGroups.indexOf(this) >= 0)
			result = true;
		factory.senderGroupLock.readLock().unlock();
		return result;
	}

	/**
	 * 设置该发送线程组可分配状态
	 * 
	 * @return 是否成功更改状态
	 */
	public boolean setDistributable(boolean state) {
		boolean result = false;
		factory.senderGroupLock.writeLock().lock();
		if (state) {
			if (!isDistributable()) {
				factory.distributableSenderGroups.add(this);
				LOGGER.info("工厂id：" + factory.id + "——发送线程组id：" + id + "——设置为可分配状态。");
				result = true;
			}
		} else {
			// 至少一个可分配线程组
			if (factory.distributableSenderGroups.size() > 1 && isDistributable()) {
				factory.distributableSenderGroups.remove(this);
				LOGGER.info("工厂id：" + factory.id + "——发送线程组id：" + id + "——设置为不可分配状态。");
				result = true;
			}
		}
		factory.senderGroupLock.writeLock().unlock();
		return result;
	}

	/**
	 * 添加该组发送线程
	 * 
	 * @throws IllegalArgumentException
	 *             if (num <= 0 || num > MAX_THREAD_NUM - senderThreadNum +
	 *             senderQuitingThreadNum)
	 */
	public void addThread(int num) {
		threadOperateLock.lock();
		threadChangeLock.lock();
		// 检查参数合法性
		if (num <= 0 || num > MAX_THREAD_NUM - groupSenderData.getSenderThreadNum()
				+ groupSenderData.getSenderQuitingThreadNum()) {
			threadOperateLock.unlock();
			threadChangeLock.unlock();
			throw new IllegalArgumentException("非法添加数量！");
		}
		for (int i = 0; i < num; i++) {
			senders.add(new StoreUnitSender(this));
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + factory.id + "——发送线程组id：" + id + "——添加发送线程数：" + num + "。");
	}

	/**
	 * 减少该组发送线程
	 * 
	 * @throws IllegalArgumentException
	 *             if (num <= 0 || num > senderThreadNum -
	 *             senderQuitingThreadNum)
	 */
	public void reduceThread(int num) {
		threadOperateLock.lock();
		threadChangeLock.lock();
		// 检查参数合法性
		if (num <= 0 || num > groupSenderData.getSenderThreadNum() - groupSenderData.getSenderQuitingThreadNum()) {
			threadOperateLock.unlock();
			threadChangeLock.unlock();
			throw new IllegalArgumentException("非法减少数量！");
		}
		int len = senders.size();
		// 遍历发送线程队列
		for (int i = 0, j = 0; i < len && j < num; i++) {
			StoreUnitSender sender = senders.get(i);
			// 当发现正在运行的线程时，使其停止
			if (sender.getThreadState() == 1) {
				sender.quit();
				j++;
			}
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + factory.id + "——发送线程组id：" + id + "——减少发送线程数：" + num + "。");
	}

	/**
	 * 设定该组发送线程数为num
	 * 
	 * @throws IllegalArgumentException
	 *             if (num < 0 || num > MAX_THREAD_NUM)
	 */
	public void setThreadNumTo(int num) {
		threadOperateLock.lock();
		threadChangeLock.lock();
		// 检查参数合法性
		if (num < 0 || num > MAX_THREAD_NUM) {
			threadOperateLock.unlock();
			threadChangeLock.unlock();
			throw new IllegalArgumentException("非法发送线程数！");
		}
		// 计算差值
		int diff = num - groupSenderData.getSenderThreadNum() + groupSenderData.getSenderQuitingThreadNum();
		// 差值大于0时添加线程
		if (diff > 0)
			addThread(diff);
		// 差值小于0时减少线程
		else if (diff < 0) {
			reduceThread(-diff);
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + factory.id + "——发送线程组id：" + id + "——线程数被设定为：" + num + "。");
	}

	/**
	 * 清空该组发送线程
	 */
	public void clearThread() {
		threadOperateLock.lock();
		threadChangeLock.lock();
		int len = senders.size();
		for (int i = 0; i < len; i++)
			senders.get(i).quit();
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + factory.id + "——发送线程组id：" + id + "——清空了发送线程。");
	}

	/**
	 * 强制停止所有正在停止的发送线程
	 * 
	 * @deprecated 建议让其自然停止
	 */
	public void fReduceQuitingThread() {
		threadOperateLock.lock();
		threadChangeLock.lock();
		int len = senders.size();
		for (int i = 0; i < len; i++) {
			StoreUnitSender sender = senders.get(i);
			if (sender.getThreadState() == 2)
				sender.fQuit();
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.warn("工厂id：" + factory.id + "——发送线程组id：" + id + "——强制停止了所有正在停止的发送线程。");
	}

	/**
	 * 强制清空该组发送线程
	 * 
	 * @deprecated 建议让其自然停止
	 */
	public void fClearThread() {
		threadOperateLock.lock();
		threadChangeLock.lock();
		int len = senders.size();
		for (int i = 0; i < len; i++)
			senders.get(i).fQuit();
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.warn("工厂id：" + factory.id + "——发送线程组id：" + id + "——强制清空了发送线程。");
	}

}
