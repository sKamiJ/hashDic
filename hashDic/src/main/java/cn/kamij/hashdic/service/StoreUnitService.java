package cn.kamij.hashdic.service;

import java.util.List;

import cn.kamij.hashdic.model.StoreUnit;

public interface StoreUnitService {
	/**
	 * 根据文本选择存储单元
	 * 
	 * @return 存在时返回该对象，不存在时返回null
	 */
	StoreUnit getByText(String text);

	/**
	 * 添加存储单元
	 * 
	 * @return 添加成功时返回1，不成功时报错
	 */
	int add(String text, byte[] md5);

	/**
	 * 添加存储单元，不成功时报错
	 * 
	 * @return 成功时返回1
	 */
	int add(StoreUnit storeUnit);

	/**
	 * 添加多个存储单元，不成功时回滚并报错
	 * 
	 * @return 成功添加的数量
	 */
	int adds(List<StoreUnit> storeUnits);
}
