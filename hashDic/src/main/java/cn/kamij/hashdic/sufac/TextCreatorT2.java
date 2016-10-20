package cn.kamij.hashdic.sufac;

import java.util.Arrays;

/**
 * 原文生成器T2型，生成6位小写字母、数字组合的字符串
 * 
 * @author KamiJ
 *
 */
public class TextCreatorT2 extends TextCreator {
	/**
	 * 原文生成器类型
	 */
	public static final String TYPE = "T2";

	/**
	 * 字符库
	 */
	public static final char[] CHAR_LIB = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray();

	/**
	 * 字符库第一个字符
	 */
	private static final char CHAR_LIB_FI = CHAR_LIB[0];

	/**
	 * 该字符在字符库中下一字符的映射库
	 */
	private static final char[] CHAR_LIB_NEXT = new char[128];
	static {
		Arrays.fill(CHAR_LIB_NEXT, '�');
		// 将该字符与其在字符库中下一字符建立映射
		for (int i = 0; i < CHAR_LIB.length - 1; i++)
			CHAR_LIB_NEXT[CHAR_LIB[i]] = CHAR_LIB[i + 1];
		// 字符库最后一个字符的下一字符为第一个字符
		CHAR_LIB_NEXT[CHAR_LIB[CHAR_LIB.length - 1]] = CHAR_LIB_FI;
	}

	/**
	 * 该字符在字符库中位置的映射库
	 */
	private static final int[] INT_LIB = new int[128];
	static {
		Arrays.fill(INT_LIB, -1);
		// 将该字符与其在字符库中位置建立映射
		for (int i = 0; i < CHAR_LIB.length; i++) {
			INT_LIB[CHAR_LIB[i]] = i;
		}
	}

	/**
	 * 获取该字符在字符库中的位置
	 * 
	 * @return 字符库中存在该字符时返回其位置，否则返回-1
	 */
	private static int getCharIndex(char ch) {
		return ch >= 128 ? -1 : INT_LIB[ch];
	}

	/**
	 * 该原文生成器起始原文
	 */
	public static final String TEXT_FI = "000000";

	/**
	 * 该原文生成器终原文
	 */
	public static final String TEXT_LA = "0000000";

	/**
	 * 原文
	 */
	private String text;

	/**
	 * 新建原文生成器，从指定的原文开始
	 */
	public TextCreatorT2(String text) {
		int len = text.length();
		// 检查原文长度是否合法
		if (len != 6 && !text.equals(TEXT_LA))
			throw new IllegalArgumentException();
		// 检查是否有非法字符
		for (int i = 0; i < len; i++) {
			if (getCharIndex(text.charAt(i)) == -1)
				throw new IllegalArgumentException();
		}
		this.text = text;
	}

	/**
	 * 新建原文生成器，从起始原文开始
	 */
	public TextCreatorT2() {
		this(TEXT_FI);
	}

	@Override
	public synchronized String getTextAndIncrement() throws TextEndException {
		// 加写锁
		lockWrite();
		// 该原文生成器结束
		if (text.length() != 6) {
			unlockWrite();
			throw new TextEndException("T2");
		}
		// 记录变化前的原文
		String tmp = text;
		char[] chs = text.toCharArray();
		// 从原文最后一位开始，产生进位则向前推进
		for (int i = 5;; i--) {
			// 向后变化当前字符
			chs[i] = CHAR_LIB_NEXT[chs[i]];
			if (chs[i] != CHAR_LIB_FI) {
				// 未产生进位，生成新原文并结束
				text = new String(chs);
				break;
			} else if (i == 0) {
				// 产生进位且需要扩展位数，生成扩展后的原文并结束
				text = CHAR_LIB_FI + new String(chs);
				break;
			}
		}
		// 解写锁
		unlockWrite();
		// 返回变化前的原文
		return tmp;
	}

	/**
	 * 原文为n位时的总组合数，n为数组位置
	 */
	private static final long[] TEXT_NUM = new long[7];
	static {
		for (int i = 0; i < 7; i++)
			TEXT_NUM[i] = (long) Math.pow(CHAR_LIB.length, i);
	}

	/**
	 * 原文总数
	 */
	public static final long TEXT_SUM = TEXT_NUM[6];

	@Override
	public TextData getData() {
		lockRead();
		long completeTextNum;
		if (text.length() == 6) {
			long result = 0;
			for (int i = 0; i < 6; i++)
				result += INT_LIB[text.charAt(i)] * TEXT_NUM[5 - i];
			completeTextNum = result;
		} else {
			completeTextNum = TEXT_SUM;
		}
		TextData data = new TextData(text, completeTextNum, completeTextNum * 1.0 / TEXT_SUM);
		unlockRead();
		return data;
	}

	@Override
	public boolean isEnd() {
		lockRead();
		boolean res = text.equals(TEXT_LA);
		unlockRead();
		return res;
	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public long getTextSum() {
		return TEXT_SUM;
	}

}
