package cn.kamij.hashdic.utils;

import java.util.Arrays;

/**
 * 提供字节与字符之间转换的功能；<br/>
 * <br/>
 * 字符编码：对字节进行编码，以字符的形式展示给用户；<br/>
 * 我觉得主要有两种用途：<br/>
 * 1、显示字节的信息，如二进制、八进制、十六进制、Base64编码；<br/>
 * 2、显示字节所蕴含的文本的信息，如ASCII、GBK、UTF-8等；<br/>
 * <br/>
 * 注：<br/>
 * org.apache.commons.codec.binary.Hex的decodeHex方法并不安全，因为它使用了Character.digit方法，
 * 如会将'٠'解析为0；<br/>
 * 
 * @author KamiJ @2016/9/3
 * @version 1.0
 */
public class EncodeUtils {
	// 所支持的字符编码
	/**
	 * 2进制编码。<br/>
	 * 1个字符表示8位比特，每8个字符表示1个字节。<br/>
	 */
	public static final String BIN = "Bin";
	/**
	 * 16进制编码，A-F用来表示10-15。<br/>
	 * 1个字符表示4位比特，每2个字符表示1个字节。<br/>
	 */
	public static final String HEX = "Hex";
	/**
	 * A-Z、a-z、0-9、+、/分别对应0-25、26-51、52-61、62、63。<br/>
	 * 1个字符表示6位比特，每4个字符表示3个字节。<br/>
	 * 编码后的字符长度为4的倍数，无字节信息的字符用'='表示。<br/>
	 */
	public static final String BASE64 = "Base64";
	/**
	 * ASCII（American Standard Code for Information Interchange，美国信息交换标准代码）
	 * 是基于拉丁字母的一套电脑编码系统，主要用于显示现代英语和其他西欧语言。<br/>
	 * 1个字符占1个字节，但只使用了7位比特。<br/>
	 */
	public static final String ASCII = "ASCII";
	/**
	 * 兼容ASCII，又收录了西欧语言、希腊语、泰语、阿拉伯语、希伯来语对应的文字符号。<br/>
	 * 1个字符占1个字节。<br/>
	 */
	public static final String ISO_8859_1 = "ISO-8859-1";
	/**
	 * 兼容ASCII，主要收录了简体汉字。<br/>
	 * 具体为：共收录6763个汉字，其中一级汉字3755个，二级汉字3008个；
	 * 同时收录了包括拉丁字母、希腊字母、日文平假名及片假名字母、俄语西里尔字母在内的682个全角字符。<br/>
	 * 1个中文字符2个字节。<br/>
	 */
	public static final String GB2312 = "GB2312";
	/**
	 * 兼容ASCII，主要收录了繁体汉字。<br/>
	 * 1个中文字符2个字节。<br/>
	 */
	public static final String BIG5 = "Big5";
	/**
	 * 兼容GB2312，添加了对繁体中文的支持。<br/>
	 * 具体为：共收录了21003个汉字，完全兼容GB2312-80标准，
	 * 支持国际标准ISO/IEC10646-1和国家标准GB13000-1中的全部中日韩汉字， 并包含了BIG5编码中的所有汉字。<br/>
	 * 1个中文字符2个字节。<br/>
	 */
	public static final String GBK = "GBK";
	/**
	 * 基本兼容GBK，扩展了汉字集。<br/>
	 * 收录了27484个汉字，同时收录了藏文、蒙文、维吾尔文等主要的少数民族文字。<br/>
	 * 1个中文字符2/4个字节。<br/>
	 */
	public static final String GB18030 = "GB18030";
	/**
	 * Unicode的其中一个使用方式，全球统一编码，大部分字符为2个字节（导致不兼容ASCII）也有一部分字符为4个字节。<br/>
	 * 1个中文2个字节。<br/>
	 */
	public static final String UTF16 = "UTF-16LE";
	/**
	 * Unicode的其中一个使用方式，1到4个字节变长编码，兼容ASCII。<br/>
	 * 1个中文3个字节。<br/>
	 */
	public static final String UTF8 = "UTF-8";

	/**
	 * 2进制字符串字符库
	 */
	public static final char[] BIN_CHAR_LIBRARY = { '0', '1' };
	/**
	 * 2进制字符串字符至数值映射数组
	 */
	public static final int[] BIN_INT_LIBRARY = new int[50];
	static {
		Arrays.fill(BIN_INT_LIBRARY, -1);
		// 建立映射关系
		for (int i = 0; i < BIN_CHAR_LIBRARY.length; i++)
			BIN_INT_LIBRARY[BIN_CHAR_LIBRARY[i]] = i;
	}

	/**
	 * 将2进制字符转化为相应整数值
	 * 
	 * @param ch
	 *            2进制字符
	 * @return 相应整数值，无该字符时返回-1
	 */
	public static int binCharToInt(char ch) {
		return ch > '1' ? -1 : BIN_INT_LIBRARY[ch];
	}

	/**
	 * 将整数值转化为相应2进制字符
	 * 
	 * @param i
	 *            整数值，在0-1之间
	 * @return 相应字符，输入错误时返回�
	 */
	public static char intToBinChar(int i) {
		return (i >> 1) == 0 ? BIN_CHAR_LIBRARY[i] : '�';
	}

	/**
	 * 将2进制字符串转化为字节数组
	 * 
	 * @param binText
	 *            2进制字符串，长度为8的倍数，且字符仅能为0和1
	 * @return 输入正确时返回转化后的字节数组，错误时返回null
	 */
	public static byte[] binToBytes(final String binText) {
		// 为null
		if (binText == null)
			return null;
		// 字符串长度
		int len = binText.length();
		// 查看字符串长度是否为8的倍数
		if ((len & 0x7) != 0)
			return null;
		// 每8个字符转化为1个字节
		byte[] result = new byte[len >> 3];
		// i为字节数组位置，j为字符位置
		for (int i = 0, j = 0; i < result.length; i++) {
			// 将字符转化为2进制数字
			int t = binCharToInt(binText.charAt(j++));
			// 出现非法字符
			if (t == -1)
				return null;
			// 临时值
			int v = t;
			for (int k = 0; k < 7; k++) {
				// 将字符转化为2进制数字
				t = binCharToInt(binText.charAt(j++));
				// 出现非法字符
				if (t == -1)
					return null;
				// 位移
				v <<= 1;
				// 按位或
				v |= t;
			}
			// 添加至字节数组
			result[i] = (byte) v;
		}
		return result;
	}

	/**
	 * 将符合规范的2进制字符串转化为字节数组，未保障安全性
	 * 
	 * @param binText
	 *            2进制字符串，长度为8的倍数，且字符仅能为0和1
	 * @return 返回转化后的字节数组
	 */
	public static byte[] binToBytesFast(final String binText) {
		// 每8个字符转化为1个字节
		byte[] result = new byte[binText.length() >> 3];
		// i为字节数组位置，j为字符位置
		for (int i = 0, j = 0; i < result.length; i++) {
			// 将字符转化为2进制数字
			int v = BIN_INT_LIBRARY[binText.charAt(j++)];
			for (int k = 0; k < 7; k++) {
				// 位移
				v <<= 1;
				// 将字符转化为2进制数字并按位或
				v |= BIN_INT_LIBRARY[binText.charAt(j++)];
			}
			// 添加至字节数组
			result[i] = (byte) v;
		}
		return result;
	}

	/**
	 * 将相邻字节之间添加空格的2进制字符串转化为字节数组
	 * 
	 * @param binText
	 *            2进制字符串，长度为8的倍数，且字符仅能为0和1
	 * @return 输入正确时返回转化后的字节数组，错误时返回null
	 */
	public static byte[] binWithSpaceToBytes(final String binText) {
		// 为null
		if (binText == null)
			return null;
		// 字符串长度 + 1
		int len = binText.length() + 1;
		// 处理空字符串
		if (len == 1)
			return new byte[0];
		// 查看字符串长度是否符合要求
		if (len % 9 != 0)
			return null;
		// 每9个字符转化为1个字节
		byte[] result = new byte[len / 9];
		// 字节数 - 1
		int len1 = result.length - 1;
		// 字节数组位置
		int i = 0;
		// 字符位置
		int j = 0;
		// 处理非最后一个字节
		while (i < len1) {
			// 将字符转化为2进制数字
			int t = binCharToInt(binText.charAt(j++));
			// 出现非法字符
			if (t == -1)
				return null;
			// 临时值
			int v = t;
			for (int k = 0; k < 7; k++) {
				// 将字符转化为2进制数字
				t = binCharToInt(binText.charAt(j++));
				// 出现非法字符
				if (t == -1)
					return null;
				// 位移
				v <<= 1;
				// 按位或
				v |= t;
			}
			// 未正确添加空格
			if (binText.charAt(j++) != ' ')
				return null;
			// 添加至字节数组
			result[i++] = (byte) v;
		}
		// 处理最后一个字节
		// 将字符转化为2进制数字
		int t = binCharToInt(binText.charAt(j++));
		// 出现非法字符
		if (t == -1)
			return null;
		// 临时值
		int v = t;
		for (int k = 0; k < 7; k++) {
			// 将字符转化为2进制数字
			t = binCharToInt(binText.charAt(j++));
			// 出现非法字符
			if (t == -1)
				return null;
			// 位移
			v <<= 1;
			// 按位或
			v |= t;
		}
		// 添加至字节数组
		result[i] = (byte) v;
		return result;
	}

	/**
	 * 将符合规范的相邻字节之间添加空格的2进制字符串转化为字节数组，未保障安全性
	 * 
	 * @param binText
	 *            2进制字符串，长度为8的倍数，且字符仅能为0和1
	 * @return 返回转化后的字节数组
	 */
	public static byte[] binWithSpaceToBytesFast(final String binText) {
		// 每9个字符转化为1个字节
		byte[] result = new byte[(binText.length() + 1) / 9];
		// i为字节数组位置，j为字符位置
		for (int i = 0, j = 0; i < result.length; i++, j++) {
			// 将字符转化为2进制数字
			int v = BIN_INT_LIBRARY[binText.charAt(j++)];
			for (int k = 0; k < 7; k++) {
				// 位移
				v <<= 1;
				// 将字符转化为2进制数字并按位或
				v |= BIN_INT_LIBRARY[binText.charAt(j++)];
			}
			// 添加至字节数组
			result[i] = (byte) v;
		}
		return result;
	}

	/**
	 * 将字节数组转化为2进制字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null
	 */
	public static String bytesToBin(final byte[] bytes) {
		// 为null
		if (bytes == null)
			return null;
		// 计算字符数组长度
		char[] chs = new char[bytes.length << 3];
		// i为字节数组位置，j为字符位置
		for (int i = 0, j = 0; i < bytes.length; i++) {
			// 每1个字节转化为8个字符
			for (int k = 7; k >= 0; k--) {
				// 从头获取每位信息并添加相应字符
				chs[j++] = BIN_CHAR_LIBRARY[bytes[i] >> k & 0x1];
			}
		}
		// 转化为字符串
		return new String(chs);
	}

	/**
	 * 将字节数组转化为相邻字节之间添加空格的2进制字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null
	 */
	public static String bytesToBinWithSpace(final byte[] bytes) {
		// 为null
		if (bytes == null)
			return null;
		// 处理空字符串
		if (bytes.length == 0)
			return "";
		// 字节数 - 1
		int len = bytes.length - 1;
		// 计算字符数组长度
		char[] chs = new char[(bytes.length << 3) + len];
		// 字节数组位置
		int i = 0;
		// 字符位置
		int j = 0;
		// 处理非最后一个字节
		for (; i < len; i++) {
			// 每1个字节转化为8个字符
			for (int k = 7; k >= 0; k--) {
				// 从头获取每位信息并添加相应字符
				chs[j++] = BIN_CHAR_LIBRARY[bytes[i] >> k & 0x1];
			}
			// 添加字节之间空格
			chs[j++] = ' ';
		}
		// 处理最后一个字节
		for (int k = 7; k >= 0; k--) {
			// 从头获取每位信息并添加相应字符
			chs[j++] = BIN_CHAR_LIBRARY[bytes[i] >> k & 0x1];
		}
		// 转化为字符串
		return new String(chs);
	}

	/**
	 * 16进制字符串字符库（小写字母）
	 */
	public static final char[] HEX_CHAR_LIBRARY_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b',
			'c', 'd', 'e', 'f' };
	/**
	 * 16进制字符串字符库（大写字母）
	 */
	public static final char[] HEX_CHAR_LIBRARY_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
			'C', 'D', 'E', 'F' };
	/**
	 * 16进制字符串字符至数值映射数组
	 */
	public static final int[] HEX_INT_LIBRARY = new int[103];
	static {
		Arrays.fill(HEX_INT_LIBRARY, -1);
		// 建立映射关系
		for (int i = 0; i < HEX_CHAR_LIBRARY_LOWER.length; i++)
			HEX_INT_LIBRARY[HEX_CHAR_LIBRARY_LOWER[i]] = i;
		for (int i = 10; i < HEX_CHAR_LIBRARY_UPPER.length; i++)
			HEX_INT_LIBRARY[HEX_CHAR_LIBRARY_UPPER[i]] = i;
	}

	/**
	 * 将16进制字符转化为相应整数值
	 * 
	 * @param ch
	 *            16进制字符
	 * @return 相应整数值，无该字符时返回-1
	 */
	public static int hexCharToInt(char ch) {
		return ch > 'f' ? -1 : HEX_INT_LIBRARY[ch];
	}

	/**
	 * 将整数值转化为相应16进制字符
	 * 
	 * @param i
	 *            整数值，在0-15之间
	 * @param toLowerCase
	 *            是否转化为小写字母
	 * @return 相应字符，输入错误时返回�
	 */
	public static char intToHexChar(int i, boolean toLowerCase) {
		return (i >> 4) == 0 ? (toLowerCase ? HEX_CHAR_LIBRARY_LOWER[i] : HEX_CHAR_LIBRARY_UPPER[i]) : '�';
	}

	/**
	 * 将16进制字符串转化为字节数组
	 * 
	 * @param hexText
	 *            16进制字符串，长度为偶数，且字符仅能为0-9、A-F、a-f
	 * @return 输入正确时返回转化后的字节数组，错误时返回null
	 */
	public static byte[] hexToBytes(final String hexText) {
		// 为null
		if (hexText == null)
			return null;
		// 字符串长度
		int len = hexText.length();
		// 查看字符串长度是否为2的倍数
		if ((len & 0x1) != 0)
			return null;
		// 每2个字符转化为1个字节
		byte[] result = new byte[len >> 1];
		// i为字节数组位置，j为字符位置
		for (int i = 0, j = 0; i < result.length; i++) {
			// 将字符转化为16进制数字
			int t = hexCharToInt(hexText.charAt(j++));
			// 出现非法字符
			if (t == -1)
				return null;
			// 临时值
			int v = t;
			// 将字符转化为16进制数字
			t = hexCharToInt(hexText.charAt(j++));
			// 出现非法字符
			if (t == -1)
				return null;
			// 添加至字节数组
			result[i] = (byte) (v << 4 | t);
		}
		return result;
	}

	/**
	 * 将符合规范的16进制字符串转化为字节数组，未保障安全性
	 * 
	 * @param hexText
	 *            16进制字符串，长度为偶数，且字符仅能为0-9、A-F、a-f
	 * @return 返回转化后的字节数组
	 */
	public static byte[] hexToBytesFast(final String hexText) {
		// 每2个字符转化为1个字节
		byte[] result = new byte[hexText.length() >> 1];
		// i为字节数组位置，j为字符位置
		for (int i = 0, j = 0; i < result.length; i++) {
			// 将字符转化为16进制数字并添加至字节数组
			result[i] = (byte) (HEX_INT_LIBRARY[hexText.charAt(j++)] << 4 | HEX_INT_LIBRARY[hexText.charAt(j++)]);
		}
		return result;
	}

	/**
	 * 将相邻字节之间添加空格的16进制字符串转化为字节数组
	 * 
	 * @param hexText
	 *            16进制字符串，长度为偶数，且字符仅能为0-9、A-F、a-f
	 * @return 输入正确时返回转化后的字节数组，错误时返回null
	 */
	public static byte[] hexWithSpaceToBytes(final String hexText) {
		// 为null
		if (hexText == null)
			return null;
		// 字符串长度 + 1
		int len = hexText.length() + 1;
		// 处理空字符串
		if (len == 1)
			return new byte[0];
		// 查看字符串长度是否符合要求
		if (len % 3 != 0)
			return null;
		// 每3个字符转化为1个字节
		byte[] result = new byte[len / 3];
		// 字节数 - 1
		int len1 = result.length - 1;
		// 字节数组位置
		int i = 0;
		// 字符位置
		int j = 0;
		// 处理非最后一个字节
		while (i < len1) {
			// 将字符转化为16进制数字
			int t = hexCharToInt(hexText.charAt(j++));
			// 出现非法字符
			if (t == -1)
				return null;
			// 临时值
			int v = t;
			// 将字符转化为16进制数字
			t = hexCharToInt(hexText.charAt(j++));
			// 出现非法字符
			if (t == -1)
				return null;
			// 未正确添加空格
			if (hexText.charAt(j++) != ' ')
				return null;
			// 添加至字节数组
			result[i++] = (byte) (v << 4 | t);
		}
		// 处理最后一个字节
		// 将字符转化为16进制数字
		int t = hexCharToInt(hexText.charAt(j++));
		// 出现非法字符
		if (t == -1)
			return null;
		// 临时值
		int v = t;
		// 将字符转化为16进制数字
		t = hexCharToInt(hexText.charAt(j));
		// 出现非法字符
		if (t == -1)
			return null;
		// 添加至字节数组
		result[i] = (byte) (v << 4 | t);
		return result;
	}

	/**
	 * 将符合规范的相邻字节之间添加空格的16进制字符串转化为字节数组，未保障安全性
	 * 
	 * @param hexText
	 *            16进制字符串，长度为偶数，且字符仅能为0-9、A-F、a-f
	 * @return 返回转化后的字节数组
	 */
	public static byte[] hexWithSpaceToBytesFast(final String hexText) {
		// 每3个字符转化为1个字节
		byte[] result = new byte[(hexText.length() + 1) / 3];
		// i为字节数组位置，j为字符位置
		for (int i = 0, j = 0; i < result.length; i++, j++) {
			// 将字符转化为16进制数字并添加至字节数组
			result[i] = (byte) (HEX_INT_LIBRARY[hexText.charAt(j++)] << 4 | HEX_INT_LIBRARY[hexText.charAt(j++)]);
		}
		return result;
	}

	/**
	 * 将字节数组转化为16进制字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @param toLowerCase
	 *            是否转化为小写字母
	 * @return 输入正确时返回转化后的字符串，错误时返回null
	 */
	public static String bytesToHex(final byte[] bytes, boolean toLowerCase) {
		// 为null
		if (bytes == null)
			return null;
		// 根据是否转化为小写字母选择字符库
		final char[] hexCharLibrary = toLowerCase ? HEX_CHAR_LIBRARY_LOWER : HEX_CHAR_LIBRARY_UPPER;
		// 计算字符数组长度
		char[] chs = new char[bytes.length << 1];
		// i为字节数组位置，j为字符位置
		for (int i = 0, j = 0; i < bytes.length; i++) {
			// 每1个字节转化为2个字符
			chs[j++] = hexCharLibrary[bytes[i] >> 4 & 0xF];
			chs[j++] = hexCharLibrary[bytes[i] & 0xF];
		}
		// 转化为字符串
		return new String(chs);
	}

	/**
	 * 将字节数组转化为16进制字符串，为小写字母
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null
	 */
	public static String bytesToHex(final byte[] bytes) {
		return bytesToHex(bytes, true);
	}

	/**
	 * 将字节数组转化为相邻字节之间添加空格的16进制字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @param toLowerCase
	 *            是否转化为小写字母
	 * @return 输入正确时返回转化后的字符串，错误时返回null
	 */
	public static String bytesToHexWithSpace(final byte[] bytes, boolean toLowerCase) {
		// 为null
		if (bytes == null)
			return null;
		// 处理空字符串
		if (bytes.length == 0)
			return "";
		// 字节数 - 1
		int len = bytes.length - 1;
		// 根据是否转化为小写字母选择字符库
		final char[] hexCharLibrary = toLowerCase ? HEX_CHAR_LIBRARY_LOWER : HEX_CHAR_LIBRARY_UPPER;
		// 计算字符数组长度
		char[] chs = new char[(bytes.length << 1) + len];
		// 字节数组位置
		int i = 0;
		// 字符位置
		int j = 0;
		// 处理非最后一个字节
		for (; i < len; i++) {
			// 每1个字节转化为2个字符
			chs[j++] = hexCharLibrary[bytes[i] >> 4 & 0xF];
			chs[j++] = hexCharLibrary[bytes[i] & 0xF];
			// 添加字节之间空格
			chs[j++] = ' ';
		}
		// 处理最后一个字节
		chs[j++] = hexCharLibrary[bytes[i] >> 4 & 0xF];
		chs[j++] = hexCharLibrary[bytes[i] & 0xF];
		// 转化为字符串
		return new String(chs);
	}

	/**
	 * 将字节数组转化为相邻字节之间添加空格的16进制字符串，为小写字母
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null
	 */
	public static String bytesToHexWithSpace(final byte[] bytes) {
		return bytesToHexWithSpace(bytes, true);
	}

	/**
	 * Base64字符串字符库
	 */
	public static final char[] BASE64_CHAR_LIBRARY = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
			'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
			'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2',
			'3', '4', '5', '6', '7', '8', '9', '+', '/' };
	/**
	 * Base64字符串字符至数值映射数组
	 */
	public static final int[] BASE64_INT_LIBRARY = new int[123];
	static {
		Arrays.fill(BASE64_INT_LIBRARY, -1);
		// 建立映射关系
		for (int i = 0; i < BASE64_CHAR_LIBRARY.length; i++)
			BASE64_INT_LIBRARY[BASE64_CHAR_LIBRARY[i]] = i;
	}

	/**
	 * 将Base64字符转化为相应整数值
	 * 
	 * @param ch
	 *            Base64字符
	 * @return 相应整数值，无该字符时返回-1
	 */
	public static int base64CharToInt(char ch) {
		return ch > 'z' ? -1 : BASE64_INT_LIBRARY[ch];
	}

	/**
	 * 将整数值转化为相应Base64字符
	 * 
	 * @param i
	 *            整数值，在0-63之间
	 * @return 相应字符，输入错误时返回�
	 */
	public static char intToBase64Char(int i) {
		return (i >> 6) == 0 ? BASE64_CHAR_LIBRARY[i] : '�';
	}

	/**
	 * 将Base64字符串转化为字节数组
	 * 
	 * @param base64Text
	 *            Base64字符串，长度为4的倍数，且要符合Base64编码格式
	 * @return 输入正确时返回转化后的字节数组，错误时返回null
	 */
	public static byte[] base64ToBytes(final String base64Text) {
		// 为null
		if (base64Text == null)
			return null;
		// 字符串长度
		int len = base64Text.length();
		// 查看字符串长度是否为4的倍数
		if ((len & 0x3) != 0)
			return null;
		// 处理空字符串
		if (len == 0)
			return new byte[0];
		// 非完整分组的字节数
		int surplusByteNum = base64Text.charAt(len - 1) == '=' ? (base64Text.charAt(len - 2) == '=' ? 1 : 2) : 0;
		// 完整分组的字节数
		int byteNum = ((len - surplusByteNum) >> 2) * 3;
		// 每4个字符转化为3个字节
		byte[] result = new byte[byteNum + surplusByteNum];
		// 临时值
		int[] vs = new int[4];
		// 字节数组位置
		int i = 0;
		// 字符位置
		int j = 0;
		// 处理完整分组
		while (i < byteNum) {
			// 将字符映射为相应的值
			vs[0] = base64CharToInt(base64Text.charAt(j++));
			vs[1] = base64CharToInt(base64Text.charAt(j++));
			vs[2] = base64CharToInt(base64Text.charAt(j++));
			vs[3] = base64CharToInt(base64Text.charAt(j++));
			// 出现非法字符
			if (vs[0] == -1 || vs[1] == -1 || vs[2] == -1 || vs[3] == -1)
				return null;
			vs[3] |= vs[0] << 18 | vs[1] << 12 | vs[2] << 6;
			// 第一个字节
			result[i++] = (byte) (vs[3] >> 16);
			// 第二个字节
			result[i++] = (byte) (vs[3] >> 8);
			// 第三个字节
			result[i++] = (byte) vs[3];
		}
		// 处理多余的字节
		switch (surplusByteNum) {
		case 1:
			// 多余1个字节
			vs[0] = base64CharToInt(base64Text.charAt(j++));
			vs[1] = base64CharToInt(base64Text.charAt(j));
			// 查看是否有非法字符与倒数第3个字符是否符合规范
			if (vs[0] == -1 || (vs[1] & 0xF) != 0)
				return null;
			result[i] = (byte) (vs[0] << 2 | vs[1] >> 4);
			break;
		case 2:
			// 多余2个字节
			vs[0] = base64CharToInt(base64Text.charAt(j++));
			vs[1] = base64CharToInt(base64Text.charAt(j++));
			vs[2] = base64CharToInt(base64Text.charAt(j));
			// 查看是否有非法字符与倒数第2个字符是否符合规范
			if (vs[0] == -1 || vs[1] == -1 || (vs[2] & 0x3) != 0)
				return null;
			vs[0] = vs[0] << 10 | vs[1] << 4 | vs[2] >> 2;
			result[i++] = (byte) (vs[0] >> 8);
			result[i] = (byte) vs[0];
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * 将符合规范的Base64字符串转化为字节数组，未保障安全性
	 * 
	 * @param base64Text
	 *            Base64字符串，长度为4的倍数，且要符合Base64编码格式
	 * @return 返回转化后的字节数组
	 */
	public static byte[] base64ToBytesFast(final String base64Text) {
		// 字符串长度
		int len = base64Text.length();
		// 处理空字符串
		if (len == 0)
			return new byte[0];
		// 非完整分组的字节数
		int surplusByteNum = base64Text.charAt(len - 1) == '=' ? (base64Text.charAt(len - 2) == '=' ? 1 : 2) : 0;
		// 完整分组的字节数
		int byteNum = ((len - surplusByteNum) >> 2) * 3;
		// 每4个字符转化为3个字节
		byte[] result = new byte[byteNum + surplusByteNum];
		// 临时值
		int v;
		// 字节数组位置
		int i = 0;
		// 字符位置
		int j = 0;
		// 处理完整分组
		while (i < byteNum) {
			// 将字符映射为相应的值
			v = BASE64_INT_LIBRARY[base64Text.charAt(j++)] << 18 | BASE64_INT_LIBRARY[base64Text.charAt(j++)] << 12
					| BASE64_INT_LIBRARY[base64Text.charAt(j++)] << 6 | BASE64_INT_LIBRARY[base64Text.charAt(j++)];
			// 第一个字节
			result[i++] = (byte) (v >> 16);
			// 第二个字节
			result[i++] = (byte) (v >> 8);
			// 第三个字节
			result[i++] = (byte) v;
		}
		// 处理多余的字节
		switch (surplusByteNum) {
		case 1:
			// 多余1个字节
			result[i] = (byte) (BASE64_INT_LIBRARY[base64Text.charAt(j++)] << 2
					| BASE64_INT_LIBRARY[base64Text.charAt(j)] >> 4);
			break;
		case 2:
			// 多余2个字节
			v = BASE64_INT_LIBRARY[base64Text.charAt(j++)] << 10 | BASE64_INT_LIBRARY[base64Text.charAt(j++)] << 4
					| BASE64_INT_LIBRARY[base64Text.charAt(j)] >> 2;
			// 取v的前8位
			result[i++] = (byte) (v >> 8);
			result[i] = (byte) v;
			break;
		default:
			break;
		}
		return result;
	}

	/**
	 * 将字节数组转化为Base64字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null
	 */
	public static String bytesToBase64(final byte[] bytes) {
		// 为null
		if (bytes == null)
			return null;
		// 完整组数
		int fullGroupNum = bytes.length / 3;
		// 完整组所含字节数
		int fullGroupByteNum = fullGroupNum * 3;
		// 多余的字节数
		int surplusByteNum = bytes.length - fullGroupByteNum;
		// 计算字符数组长度
		char[] chs = new char[(fullGroupNum << 2) + (surplusByteNum == 0 ? 0 : 4)];
		// 字节数组位置
		int i = 0;
		// 字符位置
		int j = 0;
		// 临时值
		int v;
		// 处理完整组的字符
		while (i < fullGroupByteNum) {
			// 注：这里要将字节按位与0xFF，否则转int时会产生负数，且将多个byte融合在1个int中，可以减少后面转化为字符时处理变量的数量，提高效率
			v = (bytes[i++] & 0xFF) << 16 | (bytes[i++] & 0xFF) << 8 | (bytes[i++] & 0xFF);
			// 转化为字符
			chs[j++] = BASE64_CHAR_LIBRARY[v >> 18];
			chs[j++] = BASE64_CHAR_LIBRARY[v >> 12 & 0x3F];
			chs[j++] = BASE64_CHAR_LIBRARY[v >> 6 & 0x3F];
			chs[j++] = BASE64_CHAR_LIBRARY[v & 0x3F];
		}
		// 处理非完整组的字符
		// 注：用switch-case效率比用if-else高，因为前者只需将surplusByteNum读一次，而后者需要读多次
		switch (surplusByteNum) {
		case 1:
			// 一个多余字节
			v = (bytes[i] & 0xFF) << 4;
			chs[j++] = BASE64_CHAR_LIBRARY[v >> 6];
			chs[j++] = BASE64_CHAR_LIBRARY[v & 0x3F];
			chs[j++] = '=';
			chs[j] = '=';
			break;
		case 2:
			// 两个多余字节
			v = (bytes[i++] & 0xFF) << 10 | (bytes[i] & 0xFF) << 2;
			chs[j++] = BASE64_CHAR_LIBRARY[v >> 12];
			chs[j++] = BASE64_CHAR_LIBRARY[v >> 6 & 0x3F];
			chs[j++] = BASE64_CHAR_LIBRARY[v & 0x3F];
			chs[j] = '=';
			break;
		default:
			break;
		}
		// 转化为字符串
		return new String(chs);
	}

	/**
	 * 将字符集字符串转化为字节数组
	 * 
	 * @param text
	 *            字符集字符串，不能为null
	 * @param charset
	 *            字符集种类
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] charsetToBytes(final String text, final String charset) {
		try {
			return text.getBytes(charset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将字节数组转化为字符集字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @param charset
	 *            字符集种类
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToCharset(final byte[] bytes, final String charset) {
		try {
			return new String(bytes, charset);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 将ASCII码字符串转化为字节数组
	 * 
	 * @param asciiText
	 *            ASCII码字符串，不能为null
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] asciiToBytes(final String asciiText) {
		return charsetToBytes(asciiText, ASCII);
	}

	/**
	 * 将字节数组转化为ASCII码字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToAscii(final byte[] bytes) {
		return bytesToCharset(bytes, ASCII);
	}

	/**
	 * 将ISO-8859-1码字符串转化为字节数组
	 * 
	 * @param iso88591Text
	 *            ISO-8859-1码字符串，不能为null
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] iso88591ToBytes(final String iso88591Text) {
		return charsetToBytes(iso88591Text, ISO_8859_1);
	}

	/**
	 * 将字节数组转化为ISO-8859-1码字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToIso88591(final byte[] bytes) {
		return bytesToCharset(bytes, ISO_8859_1);
	}

	/**
	 * 将GB2312码字符串转化为字节数组
	 * 
	 * @param gb2312Text
	 *            GB2312码字符串，不能为null
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] gb2312ToBytes(final String gb2312Text) {
		return charsetToBytes(gb2312Text, GB2312);
	}

	/**
	 * 将字节数组转化为GB2312码字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToGb2312(final byte[] bytes) {
		return bytesToCharset(bytes, GB2312);
	}

	/**
	 * 将Big5码字符串转化为字节数组
	 * 
	 * @param big5Text
	 *            Big5码字符串，不能为null
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] big5ToBytes(final String big5Text) {
		return charsetToBytes(big5Text, BIG5);
	}

	/**
	 * 将字节数组转化为Big5码字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToBig5(final byte[] bytes) {
		return bytesToCharset(bytes, BIG5);
	}

	/**
	 * 将GBK码字符串转化为字节数组
	 * 
	 * @param gbkText
	 *            GBK码字符串，不能为null
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] gbkToBytes(final String gbkText) {
		return charsetToBytes(gbkText, GBK);
	}

	/**
	 * 将字节数组转化为GBK码字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToGbk(final byte[] bytes) {
		return bytesToCharset(bytes, GBK);
	}

	/**
	 * 将GB18030码字符串转化为字节数组
	 * 
	 * @param gb18030Text
	 *            GB18030码字符串，不能为null
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] gb18030ToBytes(final String gb18030Text) {
		return charsetToBytes(gb18030Text, GB18030);
	}

	/**
	 * 将字节数组转化为GB18030码字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToGb18030(final byte[] bytes) {
		return bytesToCharset(bytes, GB18030);
	}

	/**
	 * 将UTF16码字符串转化为字节数组
	 * 
	 * @param utf16Text
	 *            UTF16码字符串，不能为null
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] utf16ToBytes(final String utf16Text) {
		return charsetToBytes(utf16Text, UTF16);
	}

	/**
	 * 将字节数组转化为UTF16码字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToUtf16(final byte[] bytes) {
		return bytesToCharset(bytes, UTF16);
	}

	/**
	 * 将UTF8码字符串转化为字节数组
	 * 
	 * @param utf8Text
	 *            UTF8码字符串，不能为null
	 * @return 输入正确时返回转化后的字节数组，错误时返回null，字符集中无该字符时会返回63(?)
	 */
	public static byte[] utf8ToBytes(final String utf8Text) {
		return charsetToBytes(utf8Text, UTF8);
	}

	/**
	 * 将字节数组转化为UTF8码字符串
	 * 
	 * @param bytes
	 *            字节数组，不能为null
	 * @return 输入正确时返回转化后的字符串，错误时返回null，无该字节编码的字符时返回�
	 */
	public static String bytesToUtf8(final byte[] bytes) {
		return bytesToCharset(bytes, UTF8);
	}

}
