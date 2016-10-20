package cn.kamij.hashdic.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import cn.kamij.hashdic.mapper.StoreUnitMapper;
import cn.kamij.hashdic.model.StoreUnit;
import cn.kamij.hashdic.service.StoreUnitService;

@Service("storeUnitService")
public class StoreUnitServiceImpl implements StoreUnitService {
	@Resource
	private StoreUnitMapper storeUnitMapper;

	@Override
	public StoreUnit getByText(String text) {
		return storeUnitMapper.selectByPrimaryKey(text);
	}

	@Override
	public int add(String text, byte[] md5) {
		return storeUnitMapper.insert(new StoreUnit(text, md5));
	}

	@Override
	public int add(StoreUnit storeUnit) {
		return storeUnitMapper.insert(storeUnit);
	}

	@Override
	public int adds(List<StoreUnit> storeUnits) {
		return storeUnitMapper.inserts(storeUnits);
	}

}
