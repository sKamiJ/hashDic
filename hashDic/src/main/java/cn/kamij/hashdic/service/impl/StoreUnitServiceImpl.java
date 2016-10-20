package cn.kamij.hashdic.service.impl;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Resource;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import cn.kamij.hashdic.mapper.StoreUnitMapper;
import cn.kamij.hashdic.model.StoreUnit;
import cn.kamij.hashdic.service.StoreUnitService;
import cn.kamij.hashdic.utils.PropUtils;

@Service("storeUnitService")
public class StoreUnitServiceImpl implements StoreUnitService {
	@Resource
	private StoreUnitMapper storeUnitMapper;

	/**
	 * 存储当前信息的properties文件名
	 */
	public static final String PROP_NAME = "current_info";

	/**
	 * 表的前缀名
	 */
	public static final String TABLE_PREFIX = "store_unit_";

	/**
	 * 表序号在properties文件中的名称
	 */
	public static final String TABLE_INDEX = "tableIndex";

	/**
	 * 表的元组数量在properties文件中的名称
	 */
	public static final String TABLE_TUPLE_NUM = "tableTupleNum";

	/**
	 * 每张表最大元组数，为1亿
	 */
	public static final int TABLE_MAX_TUPLE_NUM = 100000000;

	/**
	 * 当前表名
	 */
	private volatile String tableName = TABLE_PREFIX + PropUtils.getPropInRoot(PROP_NAME, TABLE_INDEX);

	/**
	 * 当前表的元组数量
	 */
	private volatile int tableTupleNum = Integer.parseInt(PropUtils.getPropInRoot(PROP_NAME, TABLE_TUPLE_NUM));

	/**
	 * 创建、更新表名，并重置当前元组数
	 */
	private void updateTableName() {
		tableLock.writeLock().lock();
		int tableIndex = Integer.parseInt(PropUtils.getPropInRoot(PROP_NAME, TABLE_INDEX)) + 1;
		PropUtils.setPropInRoot(PROP_NAME, TABLE_INDEX, String.valueOf(tableIndex), true);
		tableName = TABLE_PREFIX + tableIndex;
		storeUnitMapper.createNewTable(tableName);
		tableTupleNum = 0;
		tableLock.writeLock().unlock();
	}

	/**
	 * 表锁
	 */
	private final ReentrantReadWriteLock tableLock = new ReentrantReadWriteLock(true);

	/**
	 * 表的元组锁
	 */
	private final ReentrantLock tableTupleLock = new ReentrantLock(true);

	@Override
	public int add(String text, byte[] md5) {
		int result = -1;
		tableLock.readLock().lock();
		try {
			result = storeUnitMapper.insert(new StoreUnit(text, md5), tableName);
		} catch (DuplicateKeyException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			tableLock.readLock().unlock();
		}
		// 这里并没有保证强一致性，分表时元组的具体数量是会比tableTupleNum多的，
		// 因为在updateTableName中要锁tableLock，别的线程会在插入完成后阻塞在此处，而将它的数量加在重置后的tableTupleNum中
		tableTupleLock.lock();
		if ((tableTupleNum += result) >= TABLE_MAX_TUPLE_NUM) {
			updateTableName();
		}
		PropUtils.setPropInRoot(PROP_NAME, TABLE_TUPLE_NUM, String.valueOf(tableTupleNum), true);
		tableTupleLock.unlock();
		return result;
	}

	@Override
	public int add(StoreUnit storeUnit) {
		int result = -1;
		tableLock.readLock().lock();
		try {
			result = storeUnitMapper.insert(storeUnit, tableName);
		} catch (DuplicateKeyException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			tableLock.readLock().unlock();
		}
		tableTupleLock.lock();
		if ((tableTupleNum += result) >= TABLE_MAX_TUPLE_NUM) {
			updateTableName();
		}
		PropUtils.setPropInRoot(PROP_NAME, TABLE_TUPLE_NUM, String.valueOf(tableTupleNum), true);
		tableTupleLock.unlock();
		return result;
	}

	@Override
	public int adds(List<StoreUnit> storeUnits) {
		int result = -1;
		tableLock.readLock().lock();
		try {
			result = storeUnitMapper.inserts(storeUnits, tableName);
		} catch (DuplicateKeyException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			tableLock.readLock().unlock();
		}
		tableTupleLock.lock();
		if ((tableTupleNum += result) >= TABLE_MAX_TUPLE_NUM) {
			updateTableName();
		}
		PropUtils.setPropInRoot(PROP_NAME, TABLE_TUPLE_NUM, String.valueOf(tableTupleNum), true);
		tableTupleLock.unlock();
		return result;
	}

}
