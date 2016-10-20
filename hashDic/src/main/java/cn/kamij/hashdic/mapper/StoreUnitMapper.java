package cn.kamij.hashdic.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import cn.kamij.hashdic.model.StoreUnit;

@Repository("storeUnitMapper")
public interface StoreUnitMapper {

	/**
	 * 根据主键选择存储单元
	 */
	StoreUnit selectByPrimaryKey(@Param(value = "text") String text, @Param(value = "tableName") String tableName);

	/**
	 * 根据主键删除存储单元
	 */
	int deleteByPrimaryKey(@Param(value = "text") String text, @Param(value = "tableName") String tableName);

	/**
	 * 添加存储单元
	 */
	int insert(@Param(value = "record") StoreUnit record, @Param(value = "tableName") String tableName);

	/**
	 * 添加多个存储单元
	 */
	int inserts(@Param(value = "list") List<StoreUnit> records, @Param(value = "tableName") String tableName);

	/**
	 * 根据主键更新存储单元
	 */
	int updateByPrimaryKey(@Param(value = "record") StoreUnit record, @Param(value = "tableName") String tableName);

	/**
	 * 创建新表
	 */
	int createNewTable(@Param(value = "tableName") String tableName);
}