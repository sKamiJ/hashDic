package cn.kamij.hashdic.sufac;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.kamij.hashdic.model.StoreUnit;
import cn.kamij.hashdic.utils.RandomUtils;

/**
 * 存储单元分配线程
 * 
 * @author KamiJ
 */
class StoreUnitDistributor extends Thread {
	/**
	 * 缓冲队列大小
	 */
	public static final int BUFFER_SIZE = 1000;

	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitDistributor.class);

	/**
	 * 所属分配线程组
	 */
	private final StoreUnitDistributorGroup group;

	/**
	 * 该线程是否正在运行
	 */
	private volatile boolean runFlag = true;

	/**
	 * 停止该线程
	 */
	void quit() {
		if (runFlag) {
			LOGGER.info("工厂id：" + group.factory.id + "——分配线程组——分配线程id：" + this.getId() + "——正在停止。");
			group.groupDistributorData.addDistributorQuitingThreadNum(1);
			runFlag = false;
		}
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

	StoreUnitDistributor(StoreUnitDistributorGroup group) {
		// 设置所属分配线程组
		this.group = group;
		// 添加线程总数
		this.group.groupDistributorData.addDistributorThreadNum(1);
		LOGGER.info("工厂id：" + this.group.factory.id + "——分配线程组——分配线程id：" + this.getId() + "——已被初始化，正在运行。");
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
				// 取出制作队列顶端存储单元
				StoreUnit storeUnit = group.factory.creatorGroup.createList.poll();
				// 成功取出
				if (storeUnit != null) {
					// 向缓冲队列添加该存储单元
					bufferList.add(storeUnit);
					// 更改相应统计数据
					ns++;
					int bn = storeUnit.getBytesNum();
					bns += bn;
					// 加锁
					group.factory.creatorGroup.groupCreatorData.lockWrite();
					group.groupDistributorData.lockWrite();
					// 更改值
					group.factory.creatorGroup.groupCreatorData.reduceNotDistributedNum(1, bn);
					group.groupDistributorData.addDistributingNum(1, bn);
					// 解锁
					group.factory.creatorGroup.groupCreatorData.unlockWrite();
					group.groupDistributorData.unlockWrite();
					// 缓冲队列满，跳出循环准备分配
					if (bufferList.size() >= BUFFER_SIZE)
						break;
				} else if (!bufferList.isEmpty()) {
					// 制作队列为空，但缓冲队列不为空，提前跳出循环准备分配
					break;
				} else {
					// 制作队列与缓冲队列均为空，休眠1ms以防止CPU飙升
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						LOGGER.warn("工厂id：" + group.factory.id + "——分配线程组——分配线程id：" + this.getId() + "——被打断，消息："
								+ e.getMessage());
					}
				}
			}
			// 缓冲队列不为空时，添加至发送队列
			if (!bufferList.isEmpty()) {
				// 确保发送线程组不会变化
				group.factory.senderGroupLock.readLock().lock();
				// 随机选取一个发送线程组
				StoreUnitSenderGroup senderGroup = group.factory.distributableSenderGroups
						.get(RandomUtils.nextInt(group.factory.distributableSenderGroups.size()));
				// 向该发送线程组发送队列添加缓冲队列
				senderGroup.sendList.addAll(bufferList);
				// 更改相应统计数据
				// 加锁
				group.groupDistributorData.lockWrite();
				senderGroup.groupSenderData.lockWrite();
				// 更改值
				senderGroup.groupSenderData.addNotSendedNum(ns, bns);
				group.groupDistributorData.addDistributedNumFromDistributing(ns, bns);
				// 解锁
				senderGroup.groupSenderData.unlockWrite();
				group.groupDistributorData.unlockWrite();
				group.factory.senderGroupLock.readLock().unlock();
			}
		}
		// 在制作线程队列中移除自己
		group.threadChangeLock.lock();
		group.distributors.remove(this);
		// 减少线程总数
		group.groupDistributorData.reduceDistributorAndDistributorQuitingThreadNum(1);
		group.threadChangeLock.unlock();
		LOGGER.info("工厂id：" + group.factory.id + "——分配线程组——分配线程id：" + this.getId() + "——已停止。");
	}

}
