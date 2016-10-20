package cn.kamij.hashdic.mapper;

import java.util.List;

import org.springframework.stereotype.Repository;

import cn.kamij.hashdic.model.StoreUnit;

@Repository("storeUnitMapper")
public interface StoreUnitMapper {

	/**
	 * 根据主键选择存储单元
	 */
	StoreUnit selectByPrimaryKey(String text);

	/**
	 * 根据主键删除存储单元
	 */
	int deleteByPrimaryKey(String text);

	/**
	 * 添加存储单元
	 */
	int insert(StoreUnit record);

	/**
	 * 添加多个存储单元
	 */
	int inserts(List<StoreUnit> records);

	/**
	 * 根据主键更新存储单元
	 */
	int updateByPrimaryKey(StoreUnit record);
}