package cn.kamij.hashdic.sufac;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import cn.kamij.hashdic.model.StoreUnit;

/**
 * 存储单元制作线程组，每个组有一个制作队列以及若干个制作线程
 * 
 * @author KamiJ
 */
public class StoreUnitCreatorGroup {
	/**
	 * 制作线程组最大线程数
	 */
	public static final int MAX_THREAD_NUM = 3;

	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitCreatorGroup.class);

	/**
	 * 所属工厂
	 */
	final StoreUnitFactory factory;

	/**
	 * 该组统计数据
	 */
	final CreatorData groupCreatorData = new CreatorData();

	/**
	 * 存储单元制作队列
	 */
	final ConcurrentLinkedQueue<StoreUnit> createList = new ConcurrentLinkedQueue<>();

	/**
	 * 下属制作线程队列，LinkedList本身不是线程安全的，但这里做了同步处理，所以线程安全
	 */
	final List<StoreUnitCreator> creators = new LinkedList<>();

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
	 * @param creatorNum
	 *            制作线程数，creatorNum < 0 || creatorNum > MAX_THREAD_NUM
	 */
	StoreUnitCreatorGroup(StoreUnitFactory factory, int creatorNum) {
		// 设置所属工厂
		this.factory = factory;
		// 添加制作线程
		for (int i = 0; i < creatorNum; i++) {
			creators.add(new StoreUnitCreator(this));
		}
		LOGGER.info("工厂id：" + this.factory.id + "——制作线程组——初始化完成，制作线程数：" + creatorNum + "。");
	}

	/**
	 * 添加该组制作线程
	 * 
	 * @throws IllegalArgumentException
	 *             if (num <= 0 || num > MAX_THREAD_NUM - creatorThreadNum +
	 *             creatorQuitingThreadNum)
	 */
	public void addThread(int num) {
		threadOperateLock.lock();
		threadChangeLock.lock();
		// 检查参数合法性
		if (num <= 0 || num > MAX_THREAD_NUM - groupCreatorData.getCreatorThreadNum()
				+ groupCreatorData.getCreatorQuitingThreadNum()) {
			threadOperateLock.unlock();
			threadChangeLock.unlock();
			throw new IllegalArgumentException("非法添加数量！");
		}
		for (int i = 0; i < num; i++) {
			creators.add(new StoreUnitCreator(this));
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + this.factory.id + "——制作线程组——添加制作线程数：" + num + "。");
	}

	/**
	 * 减少该组制作线程
	 * 
	 * @throws IllegalArgumentException
	 *             if (num <= 0 || num > creatorThreadNum -
	 *             creatorQuitingThreadNum)
	 */
	public void reduceThread(int num) {
		threadOperateLock.lock();
		threadChangeLock.lock();
		// 检查参数合法性
		if (num <= 0 || num > groupCreatorData.getCreatorThreadNum() - groupCreatorData.getCreatorQuitingThreadNum()) {
			threadOperateLock.unlock();
			threadChangeLock.unlock();
			throw new IllegalArgumentException("非法减少数量！");
		}
		int len = creators.size();
		// 遍历制作线程队列
		for (int i = 0, j = 0; i < len && j < num; i++) {
			StoreUnitCreator creator = creators.get(i);
			// 当发现正在运行的线程时，使其停止
			if (creator.getThreadState() == 1) {
				creator.quit();
				j++;
			}
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + this.factory.id + "——制作线程组——减少制作线程数：" + num + "。");
	}

	/**
	 * 设定该组制作线程数为num
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
			throw new IllegalArgumentException("非法制作线程数！");
		}
		// 计算差值
		int diff = num - groupCreatorData.getCreatorThreadNum() + groupCreatorData.getCreatorQuitingThreadNum();
		// 差值大于0时添加线程
		if (diff > 0)
			addThread(diff);
		// 差值小于0时减少线程
		else if (diff < 0) {
			reduceThread(-diff);
		}
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + this.factory.id + "——制作线程组——线程数被设定为：" + num + "。");
	}

	/**
	 * 清空该组制作线程
	 */
	public void clearThread() {
		threadOperateLock.lock();
		threadChangeLock.lock();
		int len = creators.size();
		for (int i = 0; i < len; i++)
			creators.get(i).quit();
		threadOperateLock.unlock();
		threadChangeLock.unlock();
		LOGGER.info("工厂id：" + this.factory.id + "——制作线程组——清空了制作线程。");
	}

}
