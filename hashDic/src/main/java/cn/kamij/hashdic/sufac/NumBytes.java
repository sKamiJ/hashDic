package cn.kamij.hashdic.sufac;

/**
 * 数量与字节总数，因为上层在调用它时均加锁了，所以这里没有做同步操作
 * 
 * @author KamiJ
 *
 */
public class NumBytes {
	/**
	 * 数量
	 */
	private volatile long num = 0;

	/**
	 * 字节总数
	 */
	private volatile long bytesNum = 0;

	public NumBytes() {
		super();
	}

	public NumBytes(long num, long bytesNum) {
		this.num = num;
		this.bytesNum = bytesNum;
	}

	public long getNum() {
		return num;
	}

	public long getBytesNum() {
		return bytesNum;
	}

	/**
	 * 添加数量与字节总数
	 */
	void add(long num, long bytesNum) {
		this.num += num;
		this.bytesNum += bytesNum;
	}

	/**
	 * 添加数量与字节总数
	 */
	void add(NumBytes numBytes) {
		num += numBytes.num;
		bytesNum += numBytes.bytesNum;
	}

	/**
	 * 减少数量与字节总数
	 */
	void reduce(long num, long bytesNum) {
		this.num -= num;
		this.bytesNum -= bytesNum;
	}

	/**
	 * 复制当前对象
	 */
	NumBytes copy() {
		return new NumBytes(num, bytesNum);
	}

}
