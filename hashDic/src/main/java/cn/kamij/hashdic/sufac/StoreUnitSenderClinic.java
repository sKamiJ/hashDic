package cn.kamij.hashdic.sufac;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;

import cn.kamij.hashdic.model.StoreUnit;
import cn.kamij.hashdic.service.StoreUnitService;
import cn.kamij.hashdic.utils.SpringContextUtils;

/**
 * 存储单元发送线程诊断线程
 * 
 * @author KamiJ
 *
 */
class StoreUnitSenderClinic extends Thread {
	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitSenderClinic.class);

	// 手动注入Service
	private static final StoreUnitService STORE_UNIT_SERVICE = (StoreUnitService) SpringContextUtils
			.getBean("storeUnitService");

	/**
	 * 所属发送线程
	 */
	private final StoreUnitSender sender;

	/**
	 * 诊断队列，因为只有自己操作，所以无需保证线程安全
	 */
	private final List<StoreUnit> clinicList;

	/**
	 * 是否要强制停止
	 */
	private volatile boolean fQuit = false;

	/**
	 * 使该诊断线程强制停止
	 * 
	 * @deprecated
	 */
	void fQuit() {
		fQuit = true;
	}

	StoreUnitSenderClinic(List<StoreUnit> clinicList, StoreUnitSender sender) {
		// 设置所属发送线程
		this.sender = sender;
		// 设置诊断队列
		this.clinicList = clinicList;
		// 添加线程总数
		this.sender.group.groupSenderData.addClinicThreadNum(1);
		LOGGER.info("工厂id：" + this.sender.group.factory.id + "——发送线程组id：" + this.sender.group.id + "——发送线程id："
				+ this.sender.getId() + "——诊断线程id：" + this.getId() + "——已被初始化，正在运行。所要诊断数量：" + this.clinicList.size());
		// 开始线程
		start();
	}

	@Override
	public void run() {
		int len = clinicList.size();
		loop: for (int i = 0; i < len; i++) {
			// 循环发送该存储单元
			StoreUnit storeUnit = clinicList.get(i);
			for (;;) {
				// 强制停止
				if (fQuit) {
					long notCliNum = len - i;
					long notCliBytesNum = 0;
					for (int j = i; j < len; j++) {
						notCliBytesNum += clinicList.get(j).getBytesNum();
					}
					// 输出日志
					LOGGER.warn("工厂id：" + sender.group.factory.id + "——发送线程组id：" + sender.group.id + "——发送线程id："
							+ sender.getId() + "——诊断线程id：" + this.getId() + "——正被强制停止，尚有" + notCliNum + "个存储单元，共计"
							+ notCliBytesNum + "字节未诊断。\r\n首原文：" + storeUnit.getText() + "\r\n尾原文："
							+ clinicList.get(len - 1).getText());
					// 更改相应统计数据
					sender.group.groupSenderData.reduceClinicingAndSendingNumToFQuit(notCliNum, notCliBytesNum);
					// 跳出循环
					break loop;
				}
				try {
					// 发送
					STORE_UNIT_SERVICE.add(storeUnit);
					// 成功发送时更改相应统计数据
					sender.group.groupSenderData.reduceClinicingAndSendingNumToSended(1, storeUnit.getBytesNum());
					// 继续诊断下一个
					break;
				} catch (DuplicateKeyException e) {
					// 重复键异常
					LOGGER.error("工厂id：" + sender.group.factory.id + "——发送线程组id：" + sender.group.id + "——发送线程id："
							+ sender.getId() + "——诊断线程id：" + this.getId() + "——发现重复原文：" + storeUnit.getText());
					// 更改相应统计数据
					sender.group.groupSenderData.reduceClinicingAndSendingNumToDuplicate(1, storeUnit.getBytesNum());
					// 继续诊断下一个
					break;
				} catch (Exception e) {
					// 其他异常，连接超时等
					LOGGER.warn("工厂id：" + sender.group.factory.id + "——发送线程组id：" + sender.group.id + "——发送线程id："
							+ sender.getId() + "——诊断线程id：" + this.getId() + "——发生其他异常，已重新发送该存储单元，其原文为："
							+ storeUnit.getText() + "\r\n消息：" + e.getMessage());
				}
			}
		}
		// 在诊断线程队列中移除自己
		sender.clinics.remove(this);
		// 减少线程总数
		sender.group.groupSenderData.reduceClinicThreadNum(1);
		LOGGER.info("工厂id：" + sender.group.factory.id + "——发送线程组id：" + sender.group.id + "——发送线程id：" + sender.getId()
				+ "——诊断线程id：" + this.getId() + "——已停止。");
	}
}
