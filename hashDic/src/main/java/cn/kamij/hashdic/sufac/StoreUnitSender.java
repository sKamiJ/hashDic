package cn.kamij.hashdic.sufac;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Consumer;

import org.apache.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;

import cn.kamij.hashdic.model.StoreUnit;
import cn.kamij.hashdic.service.StoreUnitService;
import cn.kamij.hashdic.utils.SpringContextUtils;

/**
 * 存储单元发送线程
 * 
 * @author KamiJ
 */
class StoreUnitSender extends Thread {
	/**
	 * 缓冲队列大小
	 */
	public static final int BUFFER_SIZE = 1000;

	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitSender.class);

	// 手动注入Service
	private static final StoreUnitService STORE_UNIT_SERVICE = (StoreUnitService) SpringContextUtils
			.getBean("storeUnitService");

	/**
	 * 所属发送线程组
	 */
	final StoreUnitSenderGroup group;

	/**
	 * 该线程是否正在运行
	 */
	private volatile boolean runFlag = true;

	/**
	 * 是否要强制停止诊断线程
	 */
	private volatile boolean fQuit = false;

	/**
	 * 下属诊断线程队列，Vector是线程安全的
	 */
	final Vector<StoreUnitSenderClinic> clinics = new Vector<>();

	/**
	 * 停止该线程，不会强制停止诊断线程
	 */
	void quit() {
		if (runFlag) {
			LOGGER.info("工厂id：" + group.factory.id + "——发送线程组id：" + group.id + "——发送线程id：" + this.getId() + "——正在停止。");
			group.groupSenderData.addSenderQuitingThreadNum(1);
			runFlag = false;
		}
	}

	/**
	 * 停止该线程，并强制停止诊断线程
	 * 
	 * @deprecated
	 */
	void fQuit() {
		if (runFlag) {
			LOGGER.info("工厂id：" + group.factory.id + "——发送线程组id：" + group.id + "——发送线程id：" + this.getId() + "——正在停止。");
			group.groupSenderData.addSenderQuitingThreadNum(1);
			runFlag = false;
		}
		fQuit = true;
	}

	/**
	 * 获取该线程状态
	 * 
	 * @return 1为正在运行，2为正在停止，3为已停止
	 */
	int getThreadState() {
		if (runFlag) {
			return 1;
		} else if (this.isAlive()) {
			return 2;
		} else {
			return 3;
		}
	}

	StoreUnitSender(StoreUnitSenderGroup group) {
		// 设置所属发送线程组
		this.group = group;
		// 添加线程数量
		this.group.groupSenderData.addSenderThreadNum(1);
		LOGGER.info("工厂id：" + this.group.factory.id + "——发送线程组id：" + this.group.id + "——发送线程id：" + this.getId()
				+ "——已被初始化，正在运行。");
		// 开始线程
		start();
	}

	@Override
	public void run() {
		// 不停止时运行下列程序
		while (runFlag) {
			// 初始化缓冲队列
			List<StoreUnit> bufferList = new ArrayList<>(BUFFER_SIZE);
			// 记录发送存储单元数量与字节数量临时变量
			long ns = 0;
			long bns = 0;
			// 不停止时填充缓冲队列
			while (runFlag) {
				// 取出发送队列顶端存储单元
				StoreUnit storeUnit = group.sendList.poll();
				// 成功取出
				if (storeUnit != null) {
					// 向缓冲队列添加该存储单元
					bufferList.add(storeUnit);
					// 更改相应统计数据
					ns++;
					int bn = storeUnit.getBytesNum();
					bns += bn;
					group.groupSenderData.addSendingNumFromNotSended(1, bn);
					// 缓冲队列满，跳出循环准备发送
					if (bufferList.size() >= BUFFER_SIZE)
						break;
				} else if (!bufferList.isEmpty()) {
					// 发送队列为空，但缓冲队列不为空，提前跳出循环准备发送
					break;
				} else {
					// 发送队列与缓冲队列均为空，休眠1ms以防止CPU飙升
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						LOGGER.warn("工厂id：" + group.factory.id + "——发送线程组id：" + group.id + "——发送线程id：" + this.getId()
								+ "——被打断，消息：" + e.getMessage());
					}
				}
			}
			// 循环发送缓冲队列
			for (;;) {
				// 当该线程要停止时，停止发送
				if (!runFlag) {
					// 将缓冲队列添加回发送队列
					if (!bufferList.isEmpty()) {
						LOGGER.info("工厂id：" + group.factory.id + "——发送线程组id：" + group.id + "——发送线程id：" + this.getId()
								+ "——因要停止而将缓冲队列添加回发送队列，缓冲队列大小：" + bufferList.size());
						group.sendList.addAll(bufferList);
						// 更改相应统计数据
						group.groupSenderData.reduceSendingNumToNotSended(ns, bns);
					}
					break;
				}
				try {
					// 发送
					STORE_UNIT_SERVICE.adds(bufferList);
					// 成功发送时更改相应统计数据
					group.groupSenderData.addSendedNumFromSending(ns, bns);
					// 继续发送下一个缓冲队列
					break;
				} catch (DuplicateKeyException e) {
					// 重复键异常
					LOGGER.warn("工厂id：" + group.factory.id + "——发送线程组id：" + group.id + "——发送线程id：" + this.getId()
							+ "——存在重复原文，已添加至诊断线程进行诊断。\r\n首原文为：" + bufferList.get(0).getText() + "\r\n尾原文为："
							+ bufferList.get(bufferList.size() - 1).getText());
					// 新建诊断线程
					clinics.add(new StoreUnitSenderClinic(bufferList, this));
					// 更改相应统计数据
					group.groupSenderData.addClinicingNum(ns, bns);
					// 继续发送下一个缓冲队列
					break;
				} catch (Exception e) {
					// 其他异常，连接超时等
					LOGGER.warn("工厂id：" + group.factory.id + "——发送线程组id：" + group.id + "——发送线程id：" + this.getId()
							+ "——发生其他异常，已重新发送该缓冲队列。\r\n首原文为：" + bufferList.get(0).getText() + "\r\n尾原文为："
							+ bufferList.get(bufferList.size() - 1).getText() + "\r\n消息：" + e.getMessage());
				}
			}
		}
		// 是否执行过强行停止程序
		boolean done = false;
		// 等待诊断线程结束
		while (clinics.size() > 0) {
			// 强制停止诊断线程
			if (fQuit && !done) {
				// 发送强制停止指令
				clinics.forEach(new Consumer<StoreUnitSenderClinic>() {
					@SuppressWarnings("deprecation")
					@Override
					public void accept(StoreUnitSenderClinic t) {
						t.fQuit();
					}
				});
				// 防止多次执行而阻塞线程
				done = true;
			}
			try {
				// 休眠10ms以防止CPU飙升
				Thread.sleep(10);
			} catch (InterruptedException e) {
				LOGGER.warn("工厂id：" + group.factory.id + "——发送线程组id：" + group.id + "——发送线程id：" + this.getId()
						+ "——被打断，消息：" + e.getMessage());
			}
		}
		// 在发送线程队列中移除自己
		group.threadChangeLock.lock();
		group.senders.remove(this);
		// 减少线程总数
		group.groupSenderData.reduceSenderAndSenderQuitingThreadNum(1);
		group.threadChangeLock.unlock();
		LOGGER.info("工厂id：" + group.factory.id + "——发送线程组id：" + group.id + "——发送线程id：" + this.getId() + "——已停止。");
	}

}
