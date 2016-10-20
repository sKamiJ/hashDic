package cn.kamij.hashdic.sufac;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

/**
 * 存储单元分配线程组，每个组有若干个分配线程
 * 
 * @author KamiJ
 *
 */
public class StoreUnitDistributorGroup {
	/**
	 * 分配线程组最大线程数
	 */
	public static final int MAX_THREAD_NUM = 3;

	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitDistributorGroup.class);

	/**
	 * 所属工厂
	 */
	final StoreUnitFactory factory;

	/**
	 * 该组统计数据
	 */
	final DistributorData groupDistributorData = new DistributorData();

	/**
	 * 下属分配线程队列，LinkedList本身不是线程安全的，但这里做了同步处理，所以线程安全
	 */
	final List<StoreUnitDistributor> distributors = new LinkedList<>();

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
	 * @param distributorNum
	 *            分配线程数，distributorNum < 0 || distributorNum > MAX_THREAD_NUM
	 */
	StoreUnitDistributorGroup(StoreUnitFactory factory, int distributorNum) {
		// 设置所属工厂
		this.factory = factory;
		// 添加分配线程
		for (int i = 0; i < distributorNum; i++) {
			distributors.add(new StoreUnitDistributor(this));
		}
		LOGGER.info("工厂id：" + this.factory.id + "——分配线程组——初始化完成，分配线程数：" + distributorNum + "。");
	}

	/**
	 * 添加该组分配线程
	 * 
	 * @throws IllegalArgumentException
	 *             if (num <= 0 || num > MAX_THREAD_NUM - distributorThreadNum +
	 *             distributorQuitingThreadNum)
	 */
	public void addThread(int num) {
		threadOperateLock.lock();
		threadChangeLock.lock();
		// 检查参数合法性
		if (num <= 0 || num > MAX_THREAD_NUM - groupDistributorData.getDistributorThreadNum()
				+ groupDistributorData.getDistributorQuitingThreadNum()) {
			threadOperateLock.unlock();
			threadChangeLock.unlock();
			throw new IllegalArgumentException("非法添加数量！");
		}
		for (int i = 0; i < num; i++) {
			distributors.add(new StoreUnitDistributor(this));
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + factory.id + "——分配线程组——添加分配线程数：" + num + "。");
	}

	/**
	 * 减少该组分配线程
	 * 
	 * @throws IllegalArgumentException
	 *             if (num <= 0 || num > distributorThreadNum -
	 *             distributorQuitingThreadNum)
	 */
	public void reduceThread(int num) {
		threadOperateLock.lock();
		threadChangeLock.lock();
		// 检查参数合法性
		if (num <= 0 || num > groupDistributorData.getDistributorThreadNum()
				- groupDistributorData.getDistributorQuitingThreadNum()) {
			threadOperateLock.unlock();
			threadChangeLock.unlock();
			throw new IllegalArgumentException("非法减少数量！");
		}
		int len = distributors.size();
		// 遍历分配线程队列
		for (int i = 0, j = 0; i < len && j < num; i++) {
			StoreUnitDistributor distributor = distributors.get(i);
			// 当发现正在运行的线程时，使其停止
			if (distributor.getThreadState() == 1) {
				distributor.quit();
				j++;
			}
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + factory.id + "——分配线程组——减少分配线程数：" + num + "。");
	}

	/**
	 * 设定该组分配线程数为num
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
			throw new IllegalArgumentException("非法分配线程数！");
		}
		// 计算差值
		int diff = num - groupDistributorData.getDistributorThreadNum()
				+ groupDistributorData.getDistributorQuitingThreadNum();
		// 差值大于0时添加线程
		if (diff > 0)
			addThread(diff);
		// 差值小于0时减少线程
		else if (diff < 0) {
			reduceThread(-diff);
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + factory.id + "——分配线程组——线程数被设定为：" + num + "。");
	}

	/**
	 * 清空该组分配线程
	 */
	public void clearThread() {
		threadOperateLock.lock();
		threadChangeLock.lock();
		int len = distributors.size();
		for (int i = 0; i < len; i++)
			distributors.get(i).quit();
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + factory.id + "——分配线程组——清空了分配线程。");
	}
}
