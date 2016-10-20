package cn.kamij.hashdic.sufac;

import java.util.Arrays;

/**
 * 原文生成器T1型，生成从空字符串到5位大小写字母、数字、特殊符号组合的字符串
 * 
 * @author KamiJ
 *
 */
public class TextCreatorT1 extends TextCreator {
	/**
	 * 原文生成器类型
	 */
	public static final String TYPE = "T1";

	/**
	 * 字符库
	 */
	public static final char[] CHAR_LIB = "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
			.toCharArray();

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
		for (int i = 0; i < CHAR_LIB.length; i++)
			INT_LIB[CHAR_LIB[i]] = i;
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
	public static final String TEXT_FI = "";

	/**
	 * 该原文生成器终原文
	 */
	public static final String TEXT_LA = "!!!!!!";

	/**
	 * 原文
	 */
	private String text;

	/**
	 * 新建原文生成器，从指定的原文开始
	 */
	public TextCreatorT1(String text) {
		int len = text.length();
		// 检查原文长度是否合法
		if (len >= 6 && !text.equals(TEXT_LA))
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
	public TextCreatorT1() {
		this(TEXT_FI);
	}

	@Override
	public String getTextAndIncrement() throws TextEndException {
		// 加写锁
		lockWrite();
		int len = text.length();
		// 该原文生成器结束
		if (len >= 6) {
			unlockWrite();
			throw new TextEndException("T1");
		}
		// 记录变化前的原文
		String tmp = text;
		// 原文为空时
		if (len == 0) {
			text = String.valueOf(CHAR_LIB_FI);
		} else {
			char[] chs = text.toCharArray();
			// 从原文最后一位开始，产生进位则向前推进
			for (int i = chs.length - 1;; i--) {
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
		}
		// 解写锁
		unlockWrite();
		// 返回变化前的原文
		return tmp;
	}

	/**
	 * 原文到达n位时，所包含的n-1位原文总数，n为数组位置
	 */
	private static final long[] TEXT_NUM = new long[7];
	static {
		TEXT_NUM[0] = 0;
		for (int i = 0; i < 6; i++)
			TEXT_NUM[i + 1] = (long) (TEXT_NUM[i] + Math.pow(CHAR_LIB.length, i));
	}

	/**
	 * 获取该原文在该位数上的位置
	 */
	private static long getTextIndex(String text) {
		int len = text.length();
		long index = 0;
		for (int i = 0; i < len; i++) {
			index *= CHAR_LIB.length;
			index += INT_LIB[text.charAt(i)];
		}
		return index;
	}

	/**
	 * 原文总数
	 */
	public static final long TEXT_SUM = TEXT_NUM[6];

	@Override
	public TextData getData() {
		lockRead();
		long completeTextNum = TEXT_NUM[text.length()] + getTextIndex(text);
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
