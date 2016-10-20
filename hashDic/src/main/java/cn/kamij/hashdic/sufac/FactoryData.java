package cn.kamij.hashdic.sufac;

import java.util.ArrayList;
import java.util.List;

/**
 * 工厂统计数据
 * 
 * @author KamiJ
 *
 */
public class FactoryData {

	/**
	 * 所属工厂id
	 */
	private final int id;

	private final TextData textData;

	private final CreatorData creatorData;

	private final DistributorData distributorData;

	private final SenderData[] senderDatas;

	private final boolean autoControlling;

	private final List<Integer> distributableSenderGroupIds = new ArrayList<>();

	// 用于读数据
	public int getId() {
		return id;
	}

	public TextData getTextData() {
		return textData;
	}

	public CreatorData getCreatorData() {
		return creatorData;
	}

	public DistributorData getDistributorData() {
		return distributorData;
	}

	public SenderData[] getSenderDatas() {
		return senderDatas;
	}

	public boolean isAutoControlling() {
		return autoControlling;
	}

	public List<Integer> getDistributableSenderGroupIds() {
		return distributableSenderGroupIds;
	}

	public FactoryData(int id, TextData textData, CreatorData creatorData, DistributorData distributorData,
			SenderData[] senderDatas, boolean autoControlling) {
		this.id = id;
		this.textData = textData;
		this.creatorData = creatorData;
		this.distributorData = distributorData;
		this.senderDatas = senderDatas;
		this.autoControlling = autoControlling;
	}

	void addDistributableSenderGroupId(int id) {
		distributableSenderGroupIds.add(new Integer(id));
	}

}
