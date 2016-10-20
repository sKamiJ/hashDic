package cn.kamij.hashdic.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;

import cn.kamij.hashdic.service.StoreUnitService;
import cn.kamij.hashdic.sufac.FactoryData;
import cn.kamij.hashdic.sufac.SenderData;
import cn.kamij.hashdic.sufac.StoreUnitCreatorGroup;
import cn.kamij.hashdic.sufac.StoreUnitDistributorGroup;
import cn.kamij.hashdic.sufac.StoreUnitFactory;
import cn.kamij.hashdic.sufac.StoreUnitSenderGroup;
import cn.kamij.hashdic.sufac.TextCreator;
import cn.kamij.hashdic.sufac.TextCreatorT1;
import cn.kamij.hashdic.sufac.TextCreatorT2;
import cn.kamij.hashdic.sufac.TextCreatorT3;
import cn.kamij.hashdic.utils.PropUtils;

/**
 * 存储单元工厂控制器
 * 
 * @author KamiJ
 *
 */
@Controller("storeUnitFactoryController")
public class StoreUnitFactoryController {
	@Resource
	private StoreUnitService storeUnitService;

	/**
	 * 生成日志文件
	 */
	private static final Logger LOGGER = Logger.getLogger(StoreUnitFactoryController.class);

	/**
	 * 存储当前信息的properties文件名
	 */
	private static final String PROP_NAME = "current_info";

	/**
	 * 所有的原文生成器
	 */
	private final TextCreator[] textCreators = { new TextCreatorT1(PropUtils.getPropInRoot(PROP_NAME, "T1")),
			new TextCreatorT2(PropUtils.getPropInRoot(PROP_NAME, "T2")),
			new TextCreatorT3(PropUtils.getPropInRoot(PROP_NAME, "T3")) };

	/**
	 * 该原文生成器是否被使用
	 */
	private final boolean[] hasTextCreator = { false, false, false };

	/**
	 * 所有的工厂
	 */
	private final List<StoreUnitFactory> factories = new ArrayList<>();

	/**
	 * 所有正在摧毁的工厂
	 */
	private final Vector<StoreUnitFactory> destroyingFactories = new Vector<>();

	/**
	 * 工厂读写锁
	 */
	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);

	private void lockRead() {
		rwl.readLock().lock();
	}

	private void unlockRead() {
		rwl.readLock().unlock();
	}

	private void lockWrite() {
		rwl.writeLock().lock();
	}

	private void unlockWrite() {
		rwl.writeLock().unlock();
	}

	/**
	 * 根据工厂id获取该工厂
	 * 
	 * @return 无该id的工厂时返回null
	 */
	private StoreUnitFactory getFactoryById(int id) {
		StoreUnitFactory factory = null;
		lockRead();
		int len = factories.size();
		for (int i = 0; i < len; i++) {
			StoreUnitFactory temp = factories.get(i);
			if (temp.getId() == id) {
				factory = temp;
				break;
			}
		}
		unlockRead();
		return factory;
	}

	/**
	 * 返回控制台页面
	 */
	@RequestMapping("/console")
	public String console() {
		return "console";
	}

	/**
	 * 添加工厂
	 * 
	 * @return 是否成功添加
	 */
	@RequestMapping("/console/add_factory")
	@ResponseBody
	public String addFactory(int textCreatorType, int creatorNum, int distributorNum, int senderNum) {
		lockWrite();
		// 检查各参数是否正确，以及该原文生成器是否被使用
		if (textCreatorType < 0 || textCreatorType >= textCreators.length || creatorNum < 0
				|| creatorNum > StoreUnitCreatorGroup.MAX_THREAD_NUM || distributorNum < 0
				|| distributorNum > StoreUnitDistributorGroup.MAX_THREAD_NUM || senderNum < 0
				|| senderNum > StoreUnitSenderGroup.MAX_THREAD_NUM) {
			unlockWrite();
			return "illegalArgument";
		}
		if (hasTextCreator[textCreatorType]) {
			unlockWrite();
			return "hasTextCreator";
		}
		// 更改标志
		hasTextCreator[textCreatorType] = true;
		// 添加工厂
		StoreUnitFactory factory = new StoreUnitFactory(textCreators[textCreatorType], creatorNum, distributorNum,
				senderNum);
		factories.add(factory);
		unlockWrite();
		LOGGER.info(
				"控制台——添加了工厂，id：" + factory.getId() + "——使用原文生成器类型：" + textCreators[textCreatorType].getType() + "。");
		return "success";
	}

	/**
	 * 摧毁工厂
	 * 
	 * @return 工厂被摧毁后的统计数据，无该工厂时返回null
	 */
	@RequestMapping("/console/destroy_factory")
	@ResponseBody
	public String destroyFactory(int factoryId) {
		lockWrite();
		StoreUnitFactory factory = getFactoryById(factoryId);
		if (factory == null) {
			unlockWrite();
			return "noFactory";
		}
		factories.remove(factory);
		// 在摧毁队列中添加该工厂，这样摧毁期间也能获取统计数据
		destroyingFactories.add(factory);
		unlockWrite();
		// 停止工厂
		factory.stop();
		// 关闭统计数据
		FactoryData data = factory.cancelTimerAndUpdateTask();
		// 根据原文生成器类型更改标志
		int i = 0;
		switch (factory.getTextCreator().getType()) {
		case "T1":
			i = 0;
			break;
		case "T2":
			i = 1;
			break;
		case "T3":
			i = 2;
			break;
		}
		lockWrite();
		hasTextCreator[i] = false;
		destroyingFactories.remove(factory);
		unlockWrite();
		long sendedNum = 0;
		long sendedBytesNum = 0;
		long duplicateNum = 0;
		long duplicateBytesNum = 0;
		long fQuitNum = 0;
		long fQuitBytesNum = 0;
		SenderData[] senderDatas = data.getSenderDatas();
		for (int j = 0; j < senderDatas.length; j++) {
			sendedNum += senderDatas[j].getSendedNum().getNum();
			sendedBytesNum += senderDatas[j].getSendedNum().getBytesNum();
			duplicateNum += senderDatas[j].getDuplicateNum().getNum();
			duplicateBytesNum += senderDatas[j].getDuplicateNum().getBytesNum();
			fQuitNum += senderDatas[j].getfQuitNum().getNum();
			fQuitBytesNum += senderDatas[j].getfQuitNum().getBytesNum();
		}
		LOGGER.info("控制台——摧毁了工厂，id：" + factory.getId() + "——原文生成器类型：" + factory.getTextCreator().getType() + "——当前原文："
				+ data.getTextData().getText() + "——已制作数量：" + data.getCreatorData().getCreatedNum().getNum() + "("
				+ data.getCreatorData().getCreatedNum().getBytesNum() + "B)——已发送数量：" + sendedNum + "(" + sendedBytesNum
				+ "B)——重复数量：" + duplicateNum + "(" + duplicateBytesNum + "B)——放弃数量：" + fQuitNum + "(" + fQuitBytesNum
				+ "B)。");
		return JSON.toJSONString(data);
	}

	/**
	 * 获取所有工厂的统计数据
	 */
	@RequestMapping("/console/get_data")
	@ResponseBody
	public List<FactoryData> getData() {
		List<FactoryData> result = new ArrayList<>();
		lockRead();
		int len = factories.size();
		for (int i = 0; i < len; i++) {
			result.add(factories.get(i).getData());
		}
		destroyingFactories.forEach(new Consumer<StoreUnitFactory>() {
			@Override
			public void accept(StoreUnitFactory t) {
				result.add(t.getData());
			}
		});
		unlockRead();
		return result;
	}

	/**
	 * 添加发送线程组
	 * 
	 * @return 是否成功添加
	 */
	@RequestMapping("/console/add_sender_group")
	@ResponseBody
	public String addSenderGroup(int factoryId, int senderNum) {
		lockRead();
		StoreUnitFactory factory = getFactoryById(factoryId);
		if (factory == null) {
			unlockRead();
			return "noFactory";
		}
		if (senderNum < 0 || senderNum > StoreUnitSenderGroup.MAX_THREAD_NUM) {
			unlockRead();
			return "illegalSenderNum";
		}
		try {
			factory.addSenderGroup(senderNum);
		} catch (Exception e) {
			unlockRead();
			return "maxSenderNum";
		}
		unlockRead();
		return "success";
	}

	/**
	 * 处理工厂事件
	 * 
	 * @return 处理结果
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping("/console/handle_factory_event")
	@ResponseBody
	public String handleFactoryEvent(int factoryId, String event) {
		lockRead();
		StoreUnitFactory factory = getFactoryById(factoryId);
		if (factory == null) {
			unlockRead();
			return "noFactory";
		}
		String result = "success";
		switch (event) {
		case "停止工厂":
			factory.stop();
			break;
		case "清空线程":
			factory.clearAllThread();
			break;
		case "强制清空线程":
			factory.fClearAllThread();
			break;
		case "取消停止":
			if (!factory.cancelStop())
				result = "fail";
			break;
		case "开启自动控制":
			if (!factory.startAutoControl())
				result = "fail";
			break;
		case "关闭自动控制":
			if (!factory.cancelAutoControl())
				result = "fail";
			break;
		default:
			result = "noEvent";
			break;
		}
		unlockRead();
		return result;
	}

	/**
	 * 设置非发送线程组的线程数
	 * 
	 * @return 设置结果
	 */
	@RequestMapping("/console/set_thread_num_not_sender")
	@ResponseBody
	public String setThreadNumNotSender(int factoryId, String groupType, int threadNum) {
		lockRead();
		StoreUnitFactory factory = getFactoryById(factoryId);
		if (factory == null) {
			unlockRead();
			return "noFactory";
		}
		if (threadNum < 0 || threadNum > StoreUnitCreatorGroup.MAX_THREAD_NUM
				|| threadNum > StoreUnitDistributorGroup.MAX_THREAD_NUM) {
			unlockRead();
			return "illegalThreadNum";
		}
		switch (groupType) {
		case "制作":
			factory.getCreatorGroup().setThreadNumTo(threadNum);
			break;
		case "分配":
			factory.getDistributorGroup().setThreadNumTo(threadNum);
			break;
		default:
			unlockRead();
			return "illegalGroupType";
		}
		unlockRead();
		return "success";
	}

	/**
	 * 清空非发送线程组的线程
	 * 
	 * @return 处理结果
	 */
	@RequestMapping("/console/clear_thread_not_sender")
	@ResponseBody
	public String clearThreadNotSender(int factoryId, String groupType) {
		lockRead();
		StoreUnitFactory factory = getFactoryById(factoryId);
		if (factory == null) {
			unlockRead();
			return "noFactory";
		}
		switch (groupType) {
		case "制作":
			factory.getCreatorGroup().clearThread();
			break;
		case "分配":
			factory.getDistributorGroup().clearThread();
			break;
		default:
			unlockRead();
			return "illegalGroupType";
		}
		unlockRead();
		return "success";
	}

	/**
	 * 设置发送线程组的线程数
	 * 
	 * @return 设置结果
	 */
	@RequestMapping("/console/set_thread_num_sender")
	@ResponseBody
	public String setThreadNumSender(int factoryId, int senderGroupId, int threadNum) {
		lockRead();
		StoreUnitFactory factory = getFactoryById(factoryId);
		if (factory == null) {
			unlockRead();
			return "noFactory";
		}
		if (threadNum < 0 || threadNum > StoreUnitSenderGroup.MAX_THREAD_NUM) {
			unlockRead();
			return "illegalThreadNum";
		}
		StoreUnitSenderGroup senderGroup = factory.getSenderGroupById(senderGroupId);
		if (senderGroup == null) {
			unlockRead();
			return "noSenderGroup";
		}
		senderGroup.setThreadNumTo(threadNum);
		unlockRead();
		return "success";
	}

	/**
	 * 处理发送线程组事件
	 * 
	 * @return 处理结果
	 */
	@SuppressWarnings("deprecation")
	@RequestMapping("/console/handle_sender_group_event")
	@ResponseBody
	public String handleSenderGroupEvent(int factoryId, int senderGroupId, String event) {
		lockRead();
		StoreUnitFactory factory = getFactoryById(factoryId);
		if (factory == null) {
			unlockRead();
			return "noFactory";
		}
		StoreUnitSenderGroup senderGroup = factory.getSenderGroupById(senderGroupId);
		if (senderGroup == null) {
			unlockRead();
			return "noSenderGroup";
		}
		String result = "success";
		switch (event) {
		case "允许分配":
			if (!senderGroup.setDistributable(true))
				result = "fail";
			break;
		case "禁止分配":
			if (!senderGroup.setDistributable(false))
				result = "fail";
			break;
		case "清空线程":
			senderGroup.clearThread();
			break;
		case "强制清空线程":
			senderGroup.fClearThread();
			break;
		case "强制停止正在退出的线程":
			senderGroup.fReduceQuitingThread();
			break;
		default:
			result = "noEvent";
			break;
		}
		unlockRead();
		return result;
	}

	/**
	 * 获取新建工厂面板页面
	 */
	@RequestMapping("/console/new_factory_panel")
	public String newFactoryPanel(int factoryId, Model model) {
		model.addAttribute("factoryId", factoryId);
		StoreUnitFactory factory = getFactoryById(factoryId);
		if (factory != null) {
			model.addAttribute("textCreatorType", factory.getTextCreator().getType());
			model.addAttribute("textSum", factory.getTextCreator().getTextSum());
		}
		return "newFactoryPanel";
	}

}
