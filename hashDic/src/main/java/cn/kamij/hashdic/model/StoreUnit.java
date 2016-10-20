package cn.kamij.hashdic.model;

public class StoreUnit {
	private String text;

	private byte[] md5;

	public StoreUnit() {
		super();
	}

	public StoreUnit(String text, byte[] md5) {
		super();
		this.text = text;
		this.md5 = md5;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text == null ? null : text.trim();
	}

	public byte[] getMd5() {
		return md5;
	}

	public void setMd5(byte[] md5) {
		this.md5 = md5;
	}

	/**
	 * 获取该存储单元所占字节数，原文为UTF-8编码
	 */
	public int getBytesNum() {
		try {
			return text.getBytes("UTF-8").length + md5.length;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
}