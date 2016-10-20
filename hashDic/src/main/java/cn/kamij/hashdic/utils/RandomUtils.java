package cn.kamij.hashdic.utils;

import java.util.Random;

/**
 * 提供生成随机数相关的功能；<br/>
 * 
 * @author KamiJ @2016/9/2
 * @version 1.0
 */
public class RandomUtils {
	/**
	 * 随机数生成器
	 */
	private static final Random random = new Random();

	/**
	 * 生成true或false
	 */
	public static boolean nextBoolean() {
		return random.nextBoolean();
	}

	/**
	 * 将字节数组充满随机数
	 * 
	 * @param bytes
	 *            需要填充随机数的字节数组
	 */
	public static void nextBytes(byte[] bytes) {
		random.nextBytes(bytes);
	}

	/**
	 * 生成一个长度为length且充满随机数的字节数组
	 * 
	 * @param length
	 *            字节数组长度
	 */
	public static byte[] nextBytes(int length) {
		byte[] bytes = new byte[length];
		random.nextBytes(bytes);
		return bytes;
	}

	/**
	 * 生成[0,bound)之间的随机整数
	 * 
	 * @param bound
	 *            随机数上限，需大于0
	 */
	public static int nextInt(int bound) {
		return random.nextInt(bound);
	}

	/**
	 * 生成[min,max)之间的随机整数
	 * 
	 * @param min
	 *            随机数下限
	 * @param max
	 *            随机数上限，需大于下限
	 */
	public static int nextInt(int min, int max) {
		// 上限需大于下限
		if (max <= min)
			throw new IllegalArgumentException("max must be bigger than min");
		return min + random.nextInt(max - min);
	}

	/**
	 * 生成一个随机int数
	 */
	public static int nextInt() {
		return random.nextInt();
	}

	/**
	 * 生成一个随机float数，在0-1之间
	 */
	public static float nextFloat() {
		return random.nextFloat();
	}

	/**
	 * 生成一个随机double数，在0-1之间
	 */
	public static double nextDouble() {
		return random.nextDouble();
	}

	/**
	 * 生成一个随机long数
	 */
	public static long nextLong() {
		return random.nextLong();
	}

	/**
	 * 使用字符库中字符随机生成长度为length的字符串
	 * 
	 * @param length
	 *            字符串长度
	 * @param charLibrary
	 *            字符库
	 */
	public static String nextString(int length, final char[] charLibrary) {
		char[] chs = new char[length];
		for (int i = 0; i < chs.length; i++) {
			chs[i] = charLibrary[random.nextInt(charLibrary.length)];
		}
		return new String(chs);
	}

	/**
	 * 使用提供的字符串中字符随机生成长度为length的字符串
	 * 
	 * @param length
	 *            字符串长度
	 * @param charLibrary
	 *            字符库
	 */
	public static String nextString(int length, final String charLibrary) {
		char[] chs = new char[length];
		int len = charLibrary.length();
		for (int i = 0; i < chs.length; i++) {
			chs[i] = charLibrary.charAt(random.nextInt(len));
		}
		return new String(chs);
	}

}
