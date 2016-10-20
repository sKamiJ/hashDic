package cn.kamij.hashdic.sufac;

/**
 * 原文生成器统计数据
 * 
 * @author KamiJ
 *
 */
public class TextData {
	/**
	 * 当前原文
	 */
	private final String text;

	/**
	 * 已完成的原文数，完成的原文数为[起始原文,当前原文)
	 */
	private final long completeTextNum;

	/**
	 * 已完成的原文数占总原文数之比
	 */
	private final double progress;

	public TextData(String text, long completeTextNum, double progress) {
		this.text = text;
		this.completeTextNum = completeTextNum;
		this.progress = progress;
	}

	// 用于读数据
	public String getText() {
		return text;
	}

	public long getCompleteTextNum() {
		return completeTextNum;
	}

	public double getProgress() {
		return progress;
	}
}
