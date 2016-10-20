package cn.kamij.hashdic.sufac;

import org.apache.log4j.Logger;

import cn.kamij.hashdic.model.StoreUnit;
import cn.kamij.hashdic.utils.CryptoUtils;
import cn.kamij.hashdic.utils.EncodeUtils;

/**
 * 存储单元制作线程
 * 
 * @author KamiJ
 */
class StoreUnitCreator extends Thread {
	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitCreator.class);

	/**
	 * 所属制作线程组
	 */
	private final StoreUnitCreatorGroup group;

	/**
	 * 该线程是否正在运行
	 */
	private volatile boolean runFlag = true;

	/**
	 * 停止该线程
	 */
	void quit() {
		if (runFlag) {
			LOGGER.info("工厂id：" + group.factory.id + "——制作线程组——制作线程id：" + this.getId() + "——正在停止。");
			group.groupCreatorData.addCreatorQuitingThreadNum(1);
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

	StoreUnitCreator(StoreUnitCreatorGroup group) {
		// 设置所属制作线程组
		this.group = group;
		// 添加线程总数
		this.group.groupCreatorData.addCreatorThreadNum(1);
		LOGGER.info("工厂id：" + this.group.factory.id + "——制作线程组——制作线程id：" + this.getId() + "——已被初始化，正在运行。");
		// 开始线程
		start();
	}

	@Override
	public void run() {
		// 不停止时运行下列程序
		while (runFlag) {
			try {
				// 从原文生成器获取原文
				String text = group.factory.textCreator.getTextAndIncrement();
				// 加密
				byte[] md5 = CryptoUtils.md5(EncodeUtils.utf8ToBytes(text));
				// 生成存储单元
				StoreUnit storeUnit = new StoreUnit(text, md5);
				// 向制作队列中添加存储单元
				group.createList.offer(storeUnit);
				// 更改相应统计数据
				group.groupCreatorData.addCreatedAndNotDistributedNum(1, storeUnit.getBytesNum());
			} catch (TextEndException e) {
				// 原文生成器结束异常，将自动停止制作线程
				LOGGER.error("工厂id：" + group.factory.id + "——制作线程组——制作线程id：" + this.getId()
						+ "——发生原文生成器结束异常，即将停止。\r\n消息：" + e.getMessage());
				group.threadChangeLock.lock();
				quit();
				group.threadChangeLock.unlock();
			}
		}
		// 在制作线程队列中移除自己
		group.threadChangeLock.lock();
		group.creators.remove(this);
		// 减少线程总数
		group.groupCreatorData.reduceCreatorAndCreatorQuitingThreadNum(1);
		group.threadChangeLock.unlock();
		LOGGER.info("工厂id：" + group.factory.id + "——制作线程组——制作线程id：" + this.getId() + "——已停止。");
	}

}
