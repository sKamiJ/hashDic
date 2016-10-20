package cn.kamij.hashdic.sufac;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

import cn.kamij.hashdic.utils.PropUtils;

/**
 * 存储单元工厂，每个工厂有一个制作线程组，一个分配线程组以及至少一个可分配的发送线程组，需提供一个原文生成器来启动该工厂
 * 
 * @author KamiJ
 *
 */
public class StoreUnitFactory {
	/**
	 * 最大发送线程组数量
	 */
	public static final int MAX_SENDER_GROUP_NUM = 5;

	/**
	 * 更新统计数据的任务周期
	 */
	public static final long UPDATE_DATA_TASK_PERIOD = 1000;

	/**
	 * 存储当前原文的properties文件名
	 */
	public static final String PROP_NAME = "curTexts";

	/**
	 * 更新当前原文的任务周期
	 */
	public static final long UPDATE_TEXT_TASK_PERIOD = 10000;

	/**
	 * 自动控制的任务周期
	 */
	public static final long AUTO_CONTROL_TASK_PERIOD = UPDATE_DATA_TASK_PERIOD;

	/**
	 * 自动控制时制作线程数
	 */
	public static final int AUTO_CONTROL_CREATOR_NUM = 1;

	/**
	 * 自动控制时分配线程数
	 */
	public static final int AUTO_CONTROL_DISTRIBUTOR_NUM = 1;

	/**
	 * 自动控制时发送线程总数
	 */
	public static final int AUTO_CONTROL_SENDER_SUM = 20;

	/**
	 * 自动控制时最多未发送数量
	 */
	public static final long AUTO_CONTROL_MAX_NOT_SENDED_SUM = 3000000;

	/**
	 * 自动控制时最少未发送数量
	 */
	public static final long AUTO_CONTROL_MIN_NOT_SENDED_SUM = 1000000;

	/**
	 * 自动控制时最大诊断线程数量
	 */
	public static final int AUTO_CONTROL_MAX_CLINIC_THREAD_NUM = 100;

	/**
	 * 工厂停止时分配线程数
	 */
	public static final int STOPING_DISTRIBUTOR_NUM = StoreUnitDistributorGroup.MAX_THREAD_NUM;

	/**
	 * 工厂停止时发送线程数
	 */
	public static final int STOPING_SENDER_NUM = StoreUnitSenderGroup.MAX_THREAD_NUM;

	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitFactory.class);

	/**
	 * 生成的工厂总数
	 */
	private static final AtomicInteger FACTORY_SUM = new AtomicInteger(0);

	/**
	 * 该工厂标识符
	 */
	final int id;

	/**
	 * 该工厂的原文生成器
	 */
	final TextCreator textCreator;

	// 成员变量的初始化在构造函数之前，所以这里不能初始化，要放在构造函数中初始化
	/**
	 * 该工厂的制作线程组
	 */
	final StoreUnitCreatorGroup creatorGroup;

	/**
	 * 该工厂的分配线程组
	 */
	private final StoreUnitDistributorGroup distributorGroup;

	/**
	 * 该工厂生成的发送线程组总数，交由发送线程组改写
	 */
	// AtomicXxx类是封装了的原子性的基本数据类型，例如i++的操作并不是原子操作，因为它要先读i的值，加1后再写回去。
	// 该类先读i的值，写入时使用CAS，成功则写入，失败则重新读i的值，保证了线程安全。
	final AtomicInteger senderGroupSum = new AtomicInteger(0);

	/**
	 * 该工厂的发送线程组
	 */
	private final List<StoreUnitSenderGroup> senderGroups = new ArrayList<>(MAX_SENDER_GROUP_NUM);

	/**
	 * 该工厂可分配的发送线程组，交由发送线程组改写
	 */
	final List<StoreUnitSenderGroup> distributableSenderGroups = new ArrayList<>(MAX_SENDER_GROUP_NUM);

	/**
	 * 发送线程组锁
	 */
	final ReentrantReadWriteLock senderGroupLock = new ReentrantReadWriteLock(true);

	/**
	 * 该工厂统计数据
	 */
	private FactoryData data;

	/**
	 * 定时器
	 */
	private Timer timer;

	/**
	 * 定时器锁
	 */
	private final ReentrantLock timerLock = new ReentrantLock(true);

	/**
	 * 是否拥有定时器
	 */
	private volatile boolean hasTimer = false;

	/**
	 * 自动控制的任务
	 */
	private TimerTask autoControlTask;

	/**
	 * 自动控制锁
	 */
	private final ReentrantLock autoControlLock = new ReentrantLock(true);

	/**
	 * 自动控制是否开启
	 */
	private volatile boolean autoControlling = false;

	/**
	 * 该工厂是否正在停止
	 */
	private volatile boolean stopping = false;

	/**
	 * 是否取消停止工厂
	 */
	private volatile boolean cancelStop = false;

	/**
	 * cancelStop变化锁
	 */
	private final ReentrantLock cancelStopLock = new ReentrantLock(true);

	/**
	 * @param textCreator
	 *            该工厂的原文生成器
	 * @param creatorNum
	 *            初始制作线程数
	 * @param distributorNum
	 *            初始分配线程数
	 * @param senderNum
	 *            初始发送线程数
	 * @throws IllegalArgumentException
	 *             if (creatorNum < 0 || creatorNum >
	 *             StoreUnitCreatorGroup.MAX_THREAD_NUM)
	 * @throws IllegalArgumentException
	 *             if (distributorNum < 0 || distributorNum >
	 *             StoreUnitDistributorGroup.MAX_THREAD_NUM)
	 * @throws IllegalArgumentException
	 *             if (senderNum < 0 || senderNum >
	 *             StoreUnitSenderGroup.MAX_THREAD_NUM)
	 */
	public StoreUnitFactory(TextCreator textCreator, int creatorNum, int distributorNum, int senderNum) {
		// 检查参数合法性
		if (creatorNum < 0 || creatorNum > StoreUnitCreatorGroup.MAX_THREAD_NUM)
			throw new IllegalArgumentException("非法制作线程数！");
		if (distributorNum < 0 || distributorNum > StoreUnitDistributorGroup.MAX_THREAD_NUM)
			throw new IllegalArgumentException("非法分配线程数！");
		if (senderNum < 0 || senderNum > StoreUnitSenderGroup.MAX_THREAD_NUM)
			throw new IllegalArgumentException("非法发送线程数！");
		// 设置原文生成器
		this.textCreator = textCreator;
		// 设置id
		id = FACTORY_SUM.getAndIncrement();
		// 获取原文生成器统计数据
		TextData textData = this.textCreator.getData();
		SenderData[] senderDatas = { new SenderData(senderGroupSum.get()) };
		// 初始化统计数据
		data = new FactoryData(id, textData, new CreatorData(), new DistributorData(), senderDatas,
				isAutoControlling());
		data.addDistributableSenderGroupId(senderGroupSum.get());
		// 初始化制作线程组
		creatorGroup = new StoreUnitCreatorGroup(this, creatorNum);
		// 初始化发送线程组
		senderGroups.add(new StoreUnitSenderGroup(this, senderNum));
		// 初始化分配线程组
		distributorGroup = new StoreUnitDistributorGroup(this, distributorNum);
		// 开启自动更新
		startTimerAndUpdateTask();
		LOGGER.info("工厂id：" + id + "——初始化完成，原文生成器起始原文：" + textData.getText() + "，原文生成器类型：" + this.textCreator.getType()
				+ "。");
	}

	/**
	 * 添加发送线程组
	 * 
	 * @param senderNum
	 *            初始发送线程数
	 * @param bufferSize
	 *            发送线程缓冲队列大小
	 * @throws IllegalArgumentException
	 *             if (senderGroups.size() >= MAX_SENDER_GROUP_NUM)
	 * @throws IllegalArgumentException
	 *             if (senderNum < 0 || senderNum >
	 *             StoreUnitSenderGroup.MAX_THREAD_NUM)
	 */
	public void addSenderGroup(int senderNum) {
		senderGroupLock.writeLock().lock();
		if (senderGroups.size() >= MAX_SENDER_GROUP_NUM) {
			senderGroupLock.writeLock().unlock();
			throw new IllegalArgumentException("发送线程组数已达上限！");
		}
		if (senderNum < 0 || senderNum > StoreUnitSenderGroup.MAX_THREAD_NUM) {
			senderGroupLock.writeLock().unlock();
			throw new IllegalArgumentException("非法发送线程数！");
		}
		senderGroups.add(new StoreUnitSenderGroup(this, senderNum));
		senderGroupLock.writeLock().unlock();
		LOGGER.info("工厂id：" + id + "——添加了发送线程组。");
	}

	/**
	 * 获取id
	 */
	public int getId() {
		return id;
	}

	/**
	 * 获取原文生成器
	 */
	public TextCreator getTextCreator() {
		return textCreator;
	}

	/**
	 * 获取制作线程组
	 */
	public StoreUnitCreatorGroup getCreatorGroup() {
		return creatorGroup;
	}

	/**
	 * 获取分配线程组
	 */
	public StoreUnitDistributorGroup getDistributorGroup() {
		return distributorGroup;
	}

	/**
	 * 根据id获取发送线程组，无该id时返回null
	 */
	public StoreUnitSenderGroup getSenderGroupById(int id) {
		StoreUnitSenderGroup result = null;
		senderGroupLock.readLock().lock();
		int len = senderGroups.size();
		for (int i = 0; i < len; i++) {
			StoreUnitSenderGroup senderGroup = senderGroups.get(i);
			if (senderGroup.id == id) {
				result = senderGroup;
				break;
			}
		}
		senderGroupLock.readLock().unlock();
		return result;
	}

	/**
	 * 获取该工厂统计数据
	 */
	public FactoryData getData() {
		return data;
	}

	/**
	 * 该工厂是否开启自动更新
	 */
	public boolean isUpdating() {
		timerLock.lock();
		boolean result = hasTimer;
		timerLock.unlock();
		return result;
	}

	/**
	 * 该工厂是否开启自动控制
	 */
	public boolean isAutoControlling() {
		timerLock.lock();
		boolean result = autoControlling;
		timerLock.unlock();
		return result;
	}

	/**
	 * 更新该工厂统计数据
	 */
	private void updateData() {
		// 加读锁
		senderGroupLock.readLock().lock();
		textCreator.lockRead();
		creatorGroup.groupCreatorData.lockRead();
		distributorGroup.groupDistributorData.lockRead();
		int len = senderGroups.size();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).groupSenderData.lockRead();
		}
		// 复制各数据
		SenderData[] senderDatas = new SenderData[len];
		for (int i = 0; i < len; i++) {
			senderDatas[i] = senderGroups.get(i).groupSenderData.copy();
		}
		FactoryData newData = new FactoryData(id, textCreator.getData(), creatorGroup.groupCreatorData.copy(),
				distributorGroup.groupDistributorData.copy(), senderDatas, isAutoControlling());
		int l = distributableSenderGroups.size();
		for (int i = 0; i < l; i++) {
			newData.addDistributableSenderGroupId(distributableSenderGroups.get(i).id);
		}
		data = newData;
		// 解读锁
		textCreator.unlockRead();
		creatorGroup.groupCreatorData.unlockRead();
		distributorGroup.groupDistributorData.unlockRead();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).groupSenderData.unlockRead();
		}
		senderGroupLock.readLock().unlock();
	}

	/**
	 * 更新文件中的当前原文
	 */
	private void updateText() {
		String text = data.getTextData().getText();
		String type = textCreator.getType();
		if (PropUtils.setPropInRoot(PROP_NAME, type, text, true))
			LOGGER.info("工厂id：" + id + "——更新原文生成器" + type + "的当前原文为：" + text);
	}

	/**
	 * 开启计时器与自动更新
	 * 
	 * @return 是否成功开启
	 */
	public boolean startTimerAndUpdateTask() {
		boolean result = false;
		timerLock.lock();
		if (!hasTimer) {
			hasTimer = true;
			// 新建计时器
			timer = new Timer();
			LOGGER.info("工厂id：" + id + "——开启计时器与自动更新。");
			// 添加更新统计数据的任务
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					updateData();
				}
			}, 0, UPDATE_DATA_TASK_PERIOD);
			// 添加更新当前原文的任务
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					updateText();
				}
			}, 0, UPDATE_TEXT_TASK_PERIOD);
			result = true;
		}
		timerLock.unlock();
		return result;
	}

	/**
	 * 关闭计时器与自动更新，关闭后data将不会更新，也无法进行自动控制
	 * 
	 * @return 关闭计时器后工厂的统计数据，关闭失败时返回null
	 */
	public FactoryData cancelTimerAndUpdateTask() {
		FactoryData result = null;
		timerLock.lock();
		if (hasTimer) {
			hasTimer = false;
			// 未开启自动控制
			autoControlling = false;
			// 关闭计时器
			timer.cancel();
			// 清除出任务队列
			timer.purge();
			// 更新一次数据
			updateData();
			// 更新一次原文
			updateText();
			result = data;
			LOGGER.info("工厂id：" + id + "——关闭计时器与自动更新。");
		}
		timerLock.unlock();
		return result;
	}

	/**
	 * 根据更新的data进行自动控制
	 */
	private void autoControl() {
		// 能够获取自动控制锁时进行自动控制
		if (autoControlLock.tryLock()) {
			// 获取各数据
			CreatorData creatorData = data.getCreatorData();
			DistributorData distributorData = data.getDistributorData();
			SenderData[] senderDatas = data.getSenderDatas();
			// 若原文生成器结束时，停止自动控制并停止工厂
			if (textCreator.isEnd()) {
				cancelAutoControl();
				new Thread(new Runnable() {
					@Override
					public void run() {
						stop();
					}
				}).start();
				autoControlLock.unlock();
				return;
			}
			// 计算诊断线程总数
			int clinicThreadSum = 0;
			for (int i = 0; i < senderDatas.length; i++) {
				clinicThreadSum += senderDatas[i].getClinicThreadNum();
			}
			// 若诊断线程总数超过上限时，停止自动控制并清空工厂所有线程
			if (clinicThreadSum > AUTO_CONTROL_MAX_CLINIC_THREAD_NUM) {
				cancelAutoControl();
				new Thread(new Runnable() {
					@Override
					public void run() {
						clearAllThread();
					}
				}).start();
				autoControlLock.unlock();
				return;
			}
			// 计算未分配总数
			long notDistributedSum = creatorData.getNotDistributedNum().getNum()
					+ distributorData.getDistributingNum().getNum();
			// 计算未发送总数
			long notSendedSum = notDistributedSum;
			for (int i = 0; i < senderDatas.length; i++) {
				notSendedSum += senderDatas[i].getNotSendedNum().getNum() + senderDatas[i].getSendingNum().getNum();
			}
			// 计算未退出的制作线程数
			int runningCreatorNum = creatorData.getCreatorThreadNum() - creatorData.getCreatorQuitingThreadNum();
			// 未发送总数超过上限时，清空制作线程
			if (notSendedSum > AUTO_CONTROL_MAX_NOT_SENDED_SUM) {
				if (runningCreatorNum > 0)
					creatorGroup.clearThread();
			} else if (notSendedSum < AUTO_CONTROL_MIN_NOT_SENDED_SUM) {
				// 未发送总数低于下限时，设置制作线程数
				if (runningCreatorNum != AUTO_CONTROL_CREATOR_NUM)
					creatorGroup.setThreadNumTo(AUTO_CONTROL_CREATOR_NUM);
			}
			// 计算未退出的分配线程数
			int runningDistributorNum = distributorData.getDistributorThreadNum()
					- distributorData.getDistributorQuitingThreadNum();
			// 无制作线程及无可分配存储单元时，清空分配线程
			if (runningCreatorNum <= 0 && notDistributedSum <= 0) {
				if (runningDistributorNum > 0)
					distributorGroup.clearThread();
			} else if (runningDistributorNum != AUTO_CONTROL_DISTRIBUTOR_NUM)
				// 否则设置分配线程数
				distributorGroup.setThreadNumTo(AUTO_CONTROL_DISTRIBUTOR_NUM);
			// 计算平均发送线程数与剩余发送线程数
			int avgSenderNum = AUTO_CONTROL_SENDER_SUM / senderDatas.length;
			int leftSenderNum = AUTO_CONTROL_SENDER_SUM - avgSenderNum * senderDatas.length;
			// 设置发送线程数
			for (int i = 0; i < senderDatas.length; i++) {
				// 计算未退出的发送线程数
				int runningSenderNum = senderDatas[i].getSenderThreadNum() - senderDatas[i].getSenderQuitingThreadNum();
				// 计算该组应设置的发送线程数
				int setSenderNum = leftSenderNum-- > 0 ? avgSenderNum + 1 : avgSenderNum;
				if (runningSenderNum != setSenderNum)
					getSenderGroupById(senderDatas[i].getId()).setThreadNumTo(setSenderNum);
			}
			// 获取发送线程组中未发送数最多与最少的组
			int min = 0;
			long minSendingSum = senderDatas[0].getNotSendedNum().getNum() + senderDatas[0].getSendingNum().getNum();
			int max = 0;
			long maxSengingSum = minSendingSum;
			for (int i = 1; i < senderDatas.length; i++) {
				long sendingSum = senderDatas[i].getNotSendedNum().getNum() + senderDatas[i].getSendingNum().getNum();
				if (sendingSum > maxSengingSum) {
					maxSengingSum = sendingSum;
					max = i;
				} else if (sendingSum < minSendingSum) {
					minSendingSum = sendingSum;
					min = i;
				}
			}
			// 设置最多组不可分配，最少组可分配，以使各组未发送数平均
			if (min != max) {
				getSenderGroupById(senderDatas[max].getId()).setDistributable(false);
				getSenderGroupById(senderDatas[min].getId()).setDistributable(true);
			}
			autoControlLock.unlock();
		}
	}

	/**
	 * 开启自动控制
	 * 
	 * @return 是否成功开启
	 */
	public boolean startAutoControl() {
		boolean result = false;
		timerLock.lock();
		// 未开启自动控制且有计时器时开启
		if (!autoControlling && hasTimer) {
			autoControlling = true;
			// 新建自动控制任务
			autoControlTask = new TimerTask() {
				@Override
				public void run() {
					autoControl();
				}
			};
			// 添加至任务队列
			timer.scheduleAtFixedRate(autoControlTask, 0, AUTO_CONTROL_TASK_PERIOD);
			LOGGER.info("工厂id：" + id + "——开启自动控制。");
			result = true;
		}
		timerLock.unlock();
		return result;
	}

	/**
	 * 关闭自动控制
	 * 
	 * @return 是否成功关闭
	 */
	public boolean cancelAutoControl() {
		boolean result = false;
		timerLock.lock();
		// 开启自动控制时关闭
		if (autoControlling) {
			autoControlling = false;
			// 取消自动控制任务，任务会在本次执行完后退出
			autoControlTask.cancel();
			// 清除出任务队列
			timer.purge();
			LOGGER.info("工厂id：" + id + "——关闭自动控制。");
			result = true;
		}
		timerLock.unlock();
		return result;
	}

	/**
	 * 线程休眠millis毫秒
	 */
	private void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			LOGGER.warn("工厂id：" + id + "——被打断，消息：" + e.getMessage());
		}
	}

	/**
	 * 停止该工厂
	 */
	public void stop() {
		// 对自动控制锁加锁
		autoControlLock.lock();
		// 对发送线程组锁加读锁
		senderGroupLock.readLock().lock();
		// 对各线程操作锁加锁
		creatorGroup.threadOperateLock.lock();
		distributorGroup.threadOperateLock.lock();
		int len = senderGroups.size();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).threadOperateLock.lock();
		}
		LOGGER.info("工厂id：" + id + "——正在停止。");
		// 更改停止状态
		stopping = true;
		stop: {
			// 取消自动控制
			cancelAutoControl();
			// 清空制作线程组线程
			creatorGroup.clearThread();
			while (creatorGroup.groupCreatorData.getCreatorThreadNum() > 0) {
				if (cancelStop)
					break stop;
				sleep(1);
			}
			// 是否完成标志
			boolean done = false;
			// 等待分配线程组分配完成
			while (creatorGroup.groupCreatorData.getNotDistributedNum().getNum() > 0
					|| distributorGroup.groupDistributorData.getDistributingNum().getNum() > 0) {
				if (cancelStop)
					break stop;
				// 设置分配线程数
				if (!done) {
					done = true;
					distributorGroup.setThreadNumTo(STOPING_DISTRIBUTOR_NUM);
				}
				// 休眠10ms防止CPU飙升
				sleep(10);
			}
			// 清空分配线程组线程
			distributorGroup.clearThread();
			while (distributorGroup.groupDistributorData.getDistributorThreadNum() > 0) {
				if (cancelStop)
					break stop;
				sleep(1);
			}
			for (int i = 0; i < len; i++) {
				// 获取发送线程组
				StoreUnitSenderGroup senderGroup = senderGroups.get(i);
				// 获取统计数据
				SenderData senderData = senderGroup.groupSenderData;
				// 设置未完成
				done = false;
				// 等待发送线程组发送完成
				while (senderData.getNotSendedNum().getNum() > 0 || senderData.getSendingNum().getNum() > 0) {
					if (cancelStop)
						break stop;
					// 设置发送线程数
					if (!done) {
						done = true;
						senderGroup.setThreadNumTo(STOPING_SENDER_NUM);
					}
					// 休眠10ms防止CPU飙升
					sleep(10);
				}
				// 清空发送线程组线程
				senderGroup.clearThread();
				while (senderGroup.groupSenderData.getSenderThreadNum() > 0) {
					if (cancelStop)
						break stop;
					sleep(1);
				}
			}
		}
		// 更改停止状态
		cancelStopLock.lock();
		if (!cancelStop)
			LOGGER.info("工厂id：" + id + "——已停止。");
		stopping = false;
		cancelStop = false;
		cancelStopLock.unlock();
		// 对各线程操作锁解锁
		creatorGroup.threadOperateLock.unlock();
		distributorGroup.threadOperateLock.unlock();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).threadOperateLock.unlock();
		}
		// 对发送线程组锁解读锁
		senderGroupLock.readLock().unlock();
		// 对自动控制锁解锁
		autoControlLock.unlock();
	}

	/**
	 * 取消停止工厂
	 * 
	 * @return 是否成功取消
	 */
	public boolean cancelStop() {
		boolean result = false;
		cancelStopLock.lock();
		if (stopping && !cancelStop) {
			cancelStop = true;
			LOGGER.info("工厂id：" + id + "——取消停止。");
			result = true;
		}
		cancelStopLock.unlock();
		return result;
	}

	/**
	 * 清空工厂所有线程
	 */
	public void clearAllThread() {
		// 对自动控制锁加锁
		autoControlLock.lock();
		// 对发送线程组锁加读锁
		senderGroupLock.readLock().lock();
		// 对各线程操作锁加锁
		creatorGroup.threadOperateLock.lock();
		distributorGroup.threadOperateLock.lock();
		int len = senderGroups.size();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).threadOperateLock.lock();
		}
		// 取消自动控制
		cancelAutoControl();
		// 清空各线程
		creatorGroup.clearThread();
		distributorGroup.clearThread();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).clearThread();
		}
		// 对各线程操作锁解锁
		creatorGroup.threadOperateLock.unlock();
		distributorGroup.threadOperateLock.unlock();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).threadOperateLock.unlock();
		}
		// 对发送线程组锁解读锁
		senderGroupLock.readLock().unlock();
		// 对自动控制锁解锁
		autoControlLock.unlock();
		LOGGER.info("工厂id：" + id + "——清空了所有线程。");
	}

	/**
	 * 强制清空工厂所有线程
	 * 
	 * @deprecated
	 */
	public void fClearAllThread() {
		// 对自动控制锁加锁
		autoControlLock.lock();
		// 对发送线程组锁加读锁
		senderGroupLock.readLock().lock();
		// 对各线程操作锁加锁
		creatorGroup.threadOperateLock.lock();
		distributorGroup.threadOperateLock.lock();
		int len = senderGroups.size();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).threadOperateLock.lock();
		}
		// 取消自动控制
		cancelAutoControl();
		// 强制清空各线程
		creatorGroup.clearThread();
		distributorGroup.clearThread();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).fClearThread();
		}
		// 对各线程操作锁解锁
		creatorGroup.threadOperateLock.unlock();
		distributorGroup.threadOperateLock.unlock();
		for (int i = 0; i < len; i++) {
			senderGroups.get(i).threadOperateLock.unlock();
		}
		// 对发送线程组锁解读锁
		senderGroupLock.readLock().unlock();
		// 对自动控制锁解锁
		autoControlLock.unlock();
		LOGGER.warn("工厂id：" + id + "——强制清空了所有线程。");
	}

}
