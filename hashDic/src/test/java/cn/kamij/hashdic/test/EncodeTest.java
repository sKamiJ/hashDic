package cn.kamij.hashdic.test;

import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import cn.kamij.hashdic.utils.EncodeUtils;
import cn.kamij.hashdic.utils.RandomUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 测试各编码解码器效率；<br/>
 * 
 * @author KamiJ
 *
 */
public class EncodeTest {
	private static void print(String str) {
		System.out.println(str);
	}

	/**
	 * 测试各Base64编码器编码效率；<br/>
	 * <br/>
	 * 参与测试的类：<br/>
	 * 1、cn.kamij.hashdic.utils.EncodeUtils.bytesToBase64(byte[])<br/>
	 * 2、org.apache.commons.codec.binary.Base64.encodeBase64String(byte[])<br/>
	 * 3、com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(byte[])
	 * <br/>
	 * 4、com.sun.org.apache.xml.internal.security.utils.Base64.encode(byte[])
	 * <br/>
	 * 5、java.util.Base64.getEncoder().encodeToString(byte[])<br/>
	 * 6、new sun.misc.BASE64Encoder().encode(byte[])<br/>
	 * <br/>
	 * 可以处理null的类：1、2、3、4；<br/>
	 * 可以处理空字节数组的类：1、2、3、4、5、6；<br/>
	 * 生成的Base64字符串进行换行的类：4、6；<br/>
	 * <br/>
	 * 测试结果（取5轮均值，单位ms）：<br/>
	 * 对1万个长度为10字节的数组进行编码，每个字节数组编码1000次（共1亿字节）：<br/>
	 * 1:00935 <br/>
	 * 2:13023 <br/>
	 * 3:01012 <br/>
	 * 4:01156 <br/>
	 * 5:01338 <br/>
	 * 6:41006 <br/>
	 * <br/>
	 * 对1万个长度为100字节的数组进行编码，每个字节数组编码100次（共1亿字节）：<br/>
	 * 1:0389 <br/>
	 * 2:2996 <br/>
	 * 3:0541 <br/>
	 * 4:0768 <br/>
	 * 5:0518 <br/>
	 * 6:6519 <br/>
	 * <br/>
	 * 对1万个长度为1000字节的数组进行编码，每个字节数组编码100次（共10亿字节）：<br/>
	 * 1:03819 <br/>
	 * 2:17727 <br/>
	 * 3:05399 <br/>
	 * 4:07349 <br/>
	 * 5:04529 <br/>
	 * 6:25671 <br/>
	 * <br/>
	 * 对1000个长度为1万字节的数组进行编码，每个字节数组编码100次（共10亿字节）：<br/>
	 * 1:03904 <br/>
	 * 2:15963 <br/>
	 * 3:07900 <br/>
	 * 4:12576 <br/>
	 * 5:04190 <br/>
	 * 6:21409 <br/>
	 * <br/>
	 * 对100个长度为100万字节的数组进行编码，每个字节数组编码10次（共10亿字节）：<br/>
	 * 1:04098 <br/>
	 * 2:16928 <br/>
	 * 3:12066 <br/>
	 * 4:14876 <br/>
	 * 5:04813 <br/>
	 * 6:21667 <br/>
	 * <br/>
	 * 总结果：<br/>
	 * 1:102MB/s(10字节)--245MB/s(100字节)--250MB/s(1000字节)--244MB/s(1万字节)--233MB/s(
	 * 100万字节)<br/>
	 * 2:007MB/s(10字节)--032MB/s(100字节)--054MB/s(1000字节)--060MB/s(1万字节)--056MB/s(
	 * 100万字节)<br/>
	 * 3:094MB/s(10字节)--176MB/s(100字节)--177MB/s(1000字节)--121MB/s(1万字节)--079MB/s(
	 * 100万字节)<br/>
	 * 4:082MB/s(10字节)--124MB/s(100字节)--130MB/s(1000字节)--076MB/s(1万字节)--064MB/s(
	 * 100万字节)<br/>
	 * 5:071MB/s(10字节)--184MB/s(100字节)--211MB/s(1000字节)--228MB/s(1万字节)--198MB/s(
	 * 100万字节) <br/>
	 * 6:002MB/s(10字节)--015MB/s(100字节)--037MB/s(1000字节)--045MB/s(1万字节)--044MB/s(
	 * 100万字节) <br/>
	 * <br/>
	 * 结论：<br/>
	 * 随着数组长度的变化，各编码器效率会产生变化；<br/>
	 * 1较稳定，且速率最高；<br/>
	 * 2、6效率较低；<br/>
	 * 3、4在对长字节数组时效率明显下降；<br/>
	 * 5在对长字节数组时效率上升；<br/>
	 * 综上，1、5、3效率较高，4效率次之，但换行，2效率再次之，6效率最低；现在被使用最多的似乎是2和6；<br/>
	 */
	@Test
	public void testBase64Encoder() {
		// 计时起始结束标志
		long start, end;

		// 测试配置
		print("对长度为100万字节的数组编码:");
		print("共进行5轮；");
		print("每轮对100个字节数组进行编码；");
		print("每个字节数组编码10次；");
		print("开始:");
		// 字节数组字节数
		int num0 = 1000000;

		// 编码字节数组个数
		int num1 = 100;

		// 每同一数组编码次数
		int num2 = 10;

		// 对应下方耗时，用于计算平均耗时
		long time0Sum = 0;
		long time1Sum = 0;
		long time2Sum = 0;
		long time3Sum = 0;
		long time4Sum = 0;
		long time5Sum = 0;

		// 测试5轮
		for (int i = 0; i < 5; i++) {

			// cn.kamij.hashdic.utils.EncodeUtils.bytesToBase64(byte[])耗时
			long time0 = 0;

			// org.apache.commons.codec.binary.Base64.encodeBase64String(byte[])耗时
			long time1 = 0;

			// com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(byte[])耗时
			long time2 = 0;

			// com.sun.org.apache.xml.internal.security.utils.Base64.encode(byte[])耗时
			long time3 = 0;

			// java.util.Base64.getEncoder().encodeToString(byte[])耗时
			long time4 = 0;

			// new sun.misc.BASE64Encoder().encode(byte[])耗时
			long time5 = 0;

			for (int j = 0; j < num1; j++) {
				// 生成随机字节数组
				byte[] bs = RandomUtils.nextBytes(num0);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.bytesToBase64(bs);
				}
				end = System.currentTimeMillis();
				time0 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					Base64.encodeBase64String(bs);
				}
				end = System.currentTimeMillis();
				time1 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(bs);
				}
				end = System.currentTimeMillis();
				time2 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					com.sun.org.apache.xml.internal.security.utils.Base64.encode(bs);
				}
				end = System.currentTimeMillis();
				time3 += (end - start);

				Encoder encoder = java.util.Base64.getEncoder();

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					encoder.encodeToString(bs);
				}
				end = System.currentTimeMillis();
				time4 += (end - start);

				BASE64Encoder base64Encoder = new BASE64Encoder();

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					base64Encoder.encode(bs);
				}
				end = System.currentTimeMillis();
				time5 += (end - start);

			}
			print("第" + (i + 1) + "轮:");
			print("cn.kamij.hashdic.utils.EncodeUtils.bytesToBase64(byte[]):" + time0 + "ms");
			print("org.apache.commons.codec.binary.Base64.encodeBase64String(byte[]):" + time1 + "ms");
			print("com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(byte[]):" + time2 + "ms");
			print("com.sun.org.apache.xml.internal.security.utils.Base64.encode(byte[]):" + time3 + "ms");
			print("java.util.Base64.getEncoder().encodeToString(byte[]):" + time4 + "ms");
			print("new sun.misc.BASE64Encoder().encode(byte[]):" + time5 + "ms");

			time0Sum += time0;
			time1Sum += time1;
			time2Sum += time2;
			time3Sum += time3;
			time4Sum += time4;
			time5Sum += time5;
		}
		print("平均:");
		print("cn.kamij.hashdic.utils.EncodeUtils.bytesToBase64(byte[]):" + time0Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time0Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("org.apache.commons.codec.binary.Base64.encodeBase64String(byte[]):" + time1Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time1Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("com.sun.org.apache.xerces.internal.impl.dv.util.Base64.encode(byte[]):" + time2Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time2Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("com.sun.org.apache.xml.internal.security.utils.Base64.encode(byte[]):" + time3Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time3Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("java.util.Base64.getEncoder().encodeToString(byte[]):" + time4Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time4Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("new sun.misc.BASE64Encoder().encode(byte[]):" + time5Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time5Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
	}

	/**
	 * 测试各Base64解码器解码效率；<br/>
	 * <br/>
	 * 参与测试的类:<br/>
	 * 1、cn.kamij.hashdic.utils.EncodeUtils.base64ToBytes(String)<br/>
	 * 2、org.apache.commons.codec.binary.Base64.decodeBase64(String)<br/>
	 * 3、com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(String)
	 * <br/>
	 * 4、com.sun.org.apache.xml.internal.security.utils.Base64.decode(String)
	 * <br/>
	 * 5、java.util.Base64.getDecoder().decode(String)<br/>
	 * 6、new sun.misc.BASE64Decoder().decodeBuffer(String)<br/>
	 * 7、cn.kamij.hashdic.utils.EncodeUtils.base64ToBytesFast(String)<br/>
	 * 8、com.alibaba.fastjson.util.Base64.decodeFast(String)<br/>
	 * <br/>
	 * 可以处理null的类:1、2、3、4；<br/>
	 * 可以处理空字符串的类:1、2、3、4、5、6、7、8；<br/>
	 * 可以处理带换行符的Base64字符串的类:2、3、4、6；(8似乎会多解析出'/')<br/>
	 * 对非法Base64字符串:1、3返回null，2跳过错误字符并解析至'='，4、5报错，6、7、8解析错误；<br/>
	 * <br/>
	 * 测试结果（取5轮均值，单位ms）:<br/>
	 * 对1万个长度为10字节的字符串进行解码，每个字符串解码1000次（共1亿字节）:<br/>
	 * 1:01077 <br/>
	 * 2:14414 <br/>
	 * 3:02078 <br/>
	 * 4:01697 <br/>
	 * 5:02216 <br/>
	 * 6:43990 <br/>
	 * 7:00836 <br/>
	 * 8:01219 <br/>
	 * <br/>
	 * 对1万个长度为100字节的字符串进行解码，每个字符串解码100次（共1亿字节）:<br/>
	 * 1:0697 <br/>
	 * 2:3641 <br/>
	 * 3:1029 <br/>
	 * 4:0898 <br/>
	 * 5:0917 <br/>
	 * 6:7506 <br/>
	 * 7:0406 <br/>
	 * 8:0654 <br/>
	 * <br/>
	 * 对1000个长度为1000字节的字符串进行解码，每个字符串解码100次（共1亿字节）:<br/>
	 * 1:0687 <br/>
	 * 2:1908 <br/>
	 * 3:1004 <br/>
	 * 4:0842 <br/>
	 * 5:0725 <br/>
	 * 6:3384 <br/>
	 * 7:0352 <br/>
	 * 8:0549 <br/>
	 * <br/>
	 * 对1000个长度为1万字节的字符串进行解码，每个字符串解码100次（共10亿字节）:<br/>
	 * 1:05017 <br/>
	 * 2:16667 <br/>
	 * 3:08960 <br/>
	 * 4:07591 <br/>
	 * 5:06625 <br/>
	 * 6:26765 <br/>
	 * 7:04690 <br/>
	 * 8:04812 <br/>
	 * <br/>
	 * 对100个长度为100万字节的字符串进行解码，每个字符串解码10次（共10亿字节）:<br/>
	 * 1:05817 <br/>
	 * 2:17524 <br/>
	 * 3:09832 <br/>
	 * 4:07819 <br/>
	 * 5:08661 <br/>
	 * 6:25830 <br/>
	 * 7:04994 <br/>
	 * 8:04578 <br/>
	 * <br/>
	 * 总结果：<br/>
	 * 1:088MB/s(10字节)--136MB/s(100字节)--138MB/s(1000字节)--190MB/s(1万字节)--163MB/s(
	 * 100万字节)<br/>
	 * 2:006MB/s(10字节)--026MB/s(100字节)--049MB/s(1000字节)--057MB/s(1万字节)--054MB/s(
	 * 100万字节)<br/>
	 * 3:045MB/s(10字节)--092MB/s(100字节)--094MB/s(1000字节)--106MB/s(1万字节)--096MB/s(
	 * 100万字节)<br/>
	 * 4:056MB/s(10字节)--106MB/s(100字节)--113MB/s(1000字节)--125MB/s(1万字节)--121MB/s(
	 * 100万字节)<br/>
	 * 5:043MB/s(10字节)--103MB/s(100字节)--131MB/s(1000字节)--143MB/s(1万字节)--110MB/s(
	 * 100万字节) <br/>
	 * 6:002MB/s(10字节)--012MB/s(100字节)--028MB/s(1000字节)--035MB/s(1万字节)--036MB/s(
	 * 100万字节) <br/>
	 * 7:114MB/s(10字节)--234MB/s(100字节)--270MB/s(1000字节)--203MB/s(1万字节)--190MB/s(
	 * 100万字节) <br/>
	 * 8:078MB/s(10字节)--145MB/s(100字节)--173MB/s(1000字节)--198MB/s(1万字节)--208MB/s(
	 * 100万字节) <br/>
	 * <br/>
	 * 结论：<br/>
	 * 随着字符串长度的变化，各解码器效率会产生变化；<br/>
	 * 1、3、4、5较稳定；<br/>
	 * 2、6效率较低；<br/>
	 * 7在对长字节数组时效率下降；<br/>
	 * 8在对长字节数组时效率上升；<br/>
	 * 综上，7、8效率最高，但不安全，1安全且效率次之，5再次之，4、3与5效率相近，但可以处理换行，2效率再次之，6效率最低；
	 * 现在被使用最多的似乎是2和6；<br/>
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testBase64Decoder() throws Exception {
		// 计时起始结束标志
		long start, end;

		// 测试配置
		print("对长度为100万字节的字符串解码:");
		print("共进行5轮；");
		print("每轮对100个字符串进行解码；");
		print("每个字符串解码10次；");
		print("开始:");
		// 字符串字节数
		int num0 = 1000000;

		// 解码字符串个数
		int num1 = 100;

		// 每同一字符串解码次数
		int num2 = 10;

		// 对应下方耗时，用于计算平均耗时
		long time0Sum = 0;
		long time1Sum = 0;
		long time2Sum = 0;
		long time3Sum = 0;
		long time4Sum = 0;
		long time5Sum = 0;
		long time6Sum = 0;
		long time7Sum = 0;

		// 测试5轮
		for (int i = 0; i < 5; i++) {

			// cn.kamij.hashdic.utils.EncodeUtils.base64ToBytes(String)耗时
			long time0 = 0;

			// org.apache.commons.codec.binary.Base64.decodeBase64(String)耗时
			long time1 = 0;

			// com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(String)耗时
			long time2 = 0;

			// com.sun.org.apache.xml.internal.security.utils.Base64.decode(String)耗时
			long time3 = 0;

			// java.util.Base64.getDecoder().decode(String)耗时
			long time4 = 0;

			// new sun.misc.BASE64Decoder().decodeBuffer(String)耗时
			long time5 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.base64ToBytesFast(String)耗时
			long time6 = 0;

			// com.alibaba.fastjson.util.Base64.decodeFast(String)耗时
			long time7 = 0;

			for (int j = 0; j < num1; j++) {
				// 生成随机字节数组的字符串
				String str = EncodeUtils.bytesToBase64(RandomUtils.nextBytes(num0));

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.base64ToBytes(str);
				}
				end = System.currentTimeMillis();
				time0 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					Base64.decodeBase64(str);
				}
				end = System.currentTimeMillis();
				time1 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(str);
				}
				end = System.currentTimeMillis();
				time2 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					com.sun.org.apache.xml.internal.security.utils.Base64.decode(str);
				}
				end = System.currentTimeMillis();
				time3 += (end - start);

				Decoder decoder = java.util.Base64.getDecoder();

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					decoder.decode(str);
				}
				end = System.currentTimeMillis();
				time4 += (end - start);

				BASE64Decoder base64Decoder = new BASE64Decoder();

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					base64Decoder.decodeBuffer(str);
				}
				end = System.currentTimeMillis();
				time5 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.base64ToBytesFast(str);
				}
				end = System.currentTimeMillis();
				time6 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					com.alibaba.fastjson.util.Base64.decodeFast(str);
				}
				end = System.currentTimeMillis();
				time7 += (end - start);

			}
			print("第" + (i + 1) + "轮:");
			print("cn.kamij.hashdic.utils.EncodeUtils.base64ToBytes(String):" + time0 + "ms");
			print("org.apache.commons.codec.binary.Base64.decodeBase64(String):" + time1 + "ms");
			print("com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(String):" + time2 + "ms");
			print("com.sun.org.apache.xml.internal.security.utils.Base64.decode(String):" + time3 + "ms");
			print("java.util.Base64.getDecoder().decode(String):" + time4 + "ms");
			print("new sun.misc.BASE64Decoder().decodeBuffer(String):" + time5 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.base64ToBytesFast(String):" + time6 + "ms");
			print("com.alibaba.fastjson.util.Base64.decodeFast(String):" + time7 + "ms");

			time0Sum += time0;
			time1Sum += time1;
			time2Sum += time2;
			time3Sum += time3;
			time4Sum += time4;
			time5Sum += time5;
			time6Sum += time6;
			time7Sum += time7;
		}
		print("平均:");
		print("cn.kamij.hashdic.utils.EncodeUtils.base64ToBytes(String):" + time0Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time0Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("org.apache.commons.codec.binary.Base64.decodeBase64(String):" + time1Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time1Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("com.sun.org.apache.xerces.internal.impl.dv.util.Base64.decode(String):" + time2Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time2Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("com.sun.org.apache.xml.internal.security.utils.Base64.decode(String):" + time3Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time3Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("java.util.Base64.getDecoder().decode(String):" + time4Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time4Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("new sun.misc.BASE64Decoder().decodeBuffer(String):" + time5Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time5Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.base64ToBytesFast(String):" + time6Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time6Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("com.alibaba.fastjson.util.Base64.decodeFast(String):" + time7Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time7Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
	}

	/**
	 * 测试各Hex编码器编码效率；<br/>
	 * <br/>
	 * 参与测试的类：<br/>
	 * 1、cn.kamij.hashdic.utils.EncodeUtils.bytesToHex(byte[])<br/>
	 * 2、cn.kamij.hashdic.utils.EncodeUtils.bytesToHexWithSpace(byte[])<br/>
	 * 3、org.apache.commons.codec.binary.Hex.encodeHexString(byte[])<br/>
	 * 4、com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.encode(byte[])
	 * <br/>
	 * <br/>
	 * 可以处理null的类：1、2、4；<br/>
	 * 可以处理空字节数组的类：1、2、3、4；<br/>
	 * 生成的Hex字符串带字节间空格的类：2；<br/>
	 * 1、2、3均可选择是否大小写，默认小写；4不可选择，默认大写；<br/>
	 * <br/>
	 * 测试结果（取5轮均值，单位ms）：<br/>
	 * 对1万个长度为10字节的数组进行编码，每个字节数组编码1000次（共1亿字节）：<br/>
	 * 1:1353 <br/>
	 * 2:1583 <br/>
	 * 3:1362 <br/>
	 * 4:1458 <br/>
	 * <br/>
	 * 对1万个长度为100字节的数组进行编码，每个字节数组编码100次（共1亿字节）：<br/>
	 * 1:1056 <br/>
	 * 2:1490 <br/>
	 * 3:1080 <br/>
	 * 4:1119 <br/>
	 * <br/>
	 * 对1万个长度为1000字节的数组进行编码，每个字节数组编码100次（共10亿字节）：<br/>
	 * 1:7193 <br/>
	 * 2:9870 <br/>
	 * 3:7655 <br/>
	 * 4:7494 <br/>
	 * <br/>
	 * 对1000个长度为1万字节的数组进行编码，每个字节数组编码100次（共10亿字节）：<br/>
	 * 1:07152 <br/>
	 * 2:10343 <br/>
	 * 3:07349 <br/>
	 * 4:07311 <br/>
	 * <br/>
	 * 对100个长度为100万字节的数组进行编码，每个字节数组编码10次（共10亿字节）：<br/>
	 * 1:05989 <br/>
	 * 2:11046 <br/>
	 * 3:07625 <br/>
	 * 4:07214 <br/>
	 * <br/>
	 * 总结果：<br/>
	 * 1:70MB/s(10字节)--90MB/s(100字节)--132MB/s(1000字节)--133MB/s(1万字节)--159MB/s(
	 * 100万字节)<br/>
	 * 2:60MB/s(10字节)--64MB/s(100字节)--096MB/s(1000字节)--092MB/s(1万字节)--086MB/s(
	 * 100万字节)<br/>
	 * 3:70MB/s(10字节)--88MB/s(100字节)--124MB/s(1000字节)--129MB/s(1万字节)--125MB/s(
	 * 100万字节)<br/>
	 * 4:65MB/s(10字节)--85MB/s(100字节)--127MB/s(1000字节)--130MB/s(1万字节)--132MB/s(
	 * 100万字节)<br/>
	 * <br/>
	 * 结论：<br/>
	 * 各编码器效率相近，1因为函数调用层数较少，效率略高，2因为在字节之间添加空格，效率较低；<br/>
	 */
	@Test
	public void testHexEncoder() {
		// 计时起始结束标志
		long start, end;

		// 测试配置
		print("对长度为100万字节的数组编码:");
		print("共进行5轮；");
		print("每轮对100个字节数组进行编码；");
		print("每个字节数组编码10次；");
		print("开始:");
		// 字节数组字节数
		int num0 = 1000000;

		// 编码字节数组个数
		int num1 = 100;

		// 每同一数组编码次数
		int num2 = 10;

		// 对应下方耗时，用于计算平均耗时
		long time0Sum = 0;
		long time1Sum = 0;
		long time2Sum = 0;
		long time3Sum = 0;

		// 测试5轮
		for (int i = 0; i < 5; i++) {

			// cn.kamij.hashdic.utils.EncodeUtils.bytesToHex(byte[])耗时
			long time0 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.bytesToHexWithSpace(byte[])耗时
			long time1 = 0;

			// org.apache.commons.codec.binary.Hex.encodeHexString(byte[])耗时
			long time2 = 0;

			// com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.encode(byte[])耗时
			long time3 = 0;

			for (int j = 0; j < num1; j++) {
				// 生成随机字节数组
				byte[] bs = RandomUtils.nextBytes(num0);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.bytesToHex(bs);
				}
				end = System.currentTimeMillis();
				time0 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.bytesToHexWithSpace(bs);
				}
				end = System.currentTimeMillis();
				time1 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					Hex.encodeHexString(bs);
				}
				end = System.currentTimeMillis();
				time2 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					HexBin.encode(bs);
				}
				end = System.currentTimeMillis();
				time3 += (end - start);

			}
			print("第" + (i + 1) + "轮:");
			print("cn.kamij.hashdic.utils.EncodeUtils.bytesToHex(byte[]):" + time0 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.bytesToHexWithSpace(byte[]):" + time1 + "ms");
			print("org.apache.commons.codec.binary.Hex.encodeHexString(byte[]):" + time2 + "ms");
			print("com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.encode(byte[]):" + time2 + "ms");

			time0Sum += time0;
			time1Sum += time1;
			time2Sum += time2;
			time3Sum += time3;
		}
		print("平均:");
		print("cn.kamij.hashdic.utils.EncodeUtils.bytesToHex(byte[]):" + time0Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time0Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.bytesToHexWithSpace(byte[]):" + time1Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time1Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("org.apache.commons.codec.binary.Hex.encodeHexString(byte[]):" + time2Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time2Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.encode(byte[]):" + time3Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time3Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
	}

	/**
	 * 测试各Hex解码器解码效率；<br/>
	 * <br/>
	 * 参与测试的类:<br/>
	 * 1、cn.kamij.hashdic.utils.EncodeUtils.hexToBytes(String)<br/>
	 * 2、cn.kamij.hashdic.utils.EncodeUtils.hexToBytesFast(String)<br/>
	 * 3、cn.kamij.hashdic.utils.EncodeUtils.hexWithSpaceToBytes(String) <br/>
	 * 4、cn.kamij.hashdic.utils.EncodeUtils.hexWithSpaceToBytesFast(String)
	 * <br/>
	 * 5、org.apache.commons.codec.binary.Hex.decodeHex(char[])<br/>
	 * 6、com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.decode(String)
	 * <br/>
	 * <br/>
	 * 可以处理null的类:1、3、6；<br/>
	 * 可以处理空字符串的类:1、2、3、4、5、6；<br/>
	 * 只能处理带字节间空格的Hex字符串的类:3、4；<br/>
	 * 对非法Hex字符串:1、3、6返回null，2、4解析错误，5报错；<br/>
	 * 5并不安全，因为它使用了Character.digit方法，如会将'٠'解析为0；<br/>
	 * <br/>
	 * 测试结果（取5轮均值，单位ms）:<br/>
	 * 对1万个长度为10字节的字符串进行解码，每个字符串解码1000次（共1亿字节）:<br/>
	 * 1:1088 <br/>
	 * 2:0990 <br/>
	 * 3:1573 <br/>
	 * 4:1163 <br/>
	 * 5:2424 <br/>
	 * 6:1408 <br/>
	 * <br/>
	 * 对1万个长度为100字节的字符串进行解码，每个字符串解码100次（共1亿字节）:<br/>
	 * 1:0754 <br/>
	 * 2:0640 <br/>
	 * 3:1355 <br/>
	 * 4:0968 <br/>
	 * 5:2282 <br/>
	 * 6:0929 <br/>
	 * <br/>
	 * 对1000个长度为1000字节的字符串进行解码，每个字符串解码100次（共1亿字节）:<br/>
	 * 1:0733 <br/>
	 * 2:0598 <br/>
	 * 3:1237 <br/>
	 * 4:0932 <br/>
	 * 5:2354 <br/>
	 * 6:0832 <br/>
	 * <br/>
	 * 对1000个长度为1万字节的字符串进行解码，每个字符串解码100次（共10亿字节）:<br/>
	 * 1:05974 <br/>
	 * 2:05233 <br/>
	 * 3:11521 <br/>
	 * 4:08369 <br/>
	 * 5:35606 <br/>
	 * 6:07769 <br/>
	 * <br/>
	 * 对100个长度为100万字节的字符串进行解码，每个字符串解码10次（共10亿字节）:<br/>
	 * 1:06523 <br/>
	 * 2:04772 <br/>
	 * 3:12903 <br/>
	 * 4:08547 <br/>
	 * 5:35542 <br/>
	 * 6:09243 <br/>
	 * <br/>
	 * 总结果：<br/>
	 * 1:87MB/s(10字节)--126MB/s(100字节)--130MB/s(1000字节)--159MB/s(1万字节)--146MB/s(
	 * 100万字节)<br/>
	 * 2:96MB/s(10字节)--149MB/s(100字节)--159MB/s(1000字节)--182MB/s(1万字节)--199MB/s(
	 * 100万字节)<br/>
	 * 3:60MB/s(10字节)--070MB/s(100字节)--077MB/s(1000字节)--082MB/s(1万字节)--073MB/s(
	 * 100万字节)<br/>
	 * 4:82MB/s(10字节)--098MB/s(100字节)--102MB/s(1000字节)--113MB/s(1万字节)--111MB/s(
	 * 100万字节)<br/>
	 * 5:39MB/s(10字节)--041MB/s(100字节)--040MB/s(1000字节)--026MB/s(1万字节)--026MB/s(
	 * 100万字节) <br/>
	 * 6:67MB/s(10字节)--102MB/s(100字节)--114MB/s(1000字节)--122MB/s(1万字节)--103MB/s(
	 * 100万字节) <br/>
	 * <br/>
	 * 结论：<br/>
	 * 2效率最高，但并不安全；<br/>
	 * 1、6安全且效率次之；<br/>
	 * 3、4只能处理带字节间空格的字符串；<br/>
	 * 5效率最低，且会错误解析字符串；<br/>
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testHexDecoder() throws Exception {
		// 计时起始结束标志
		long start, end;

		// 测试配置
		print("对长度为100万字节的字符串解码:");
		print("共进行5轮；");
		print("每轮对100个字符串进行解码；");
		print("每个字符串解码10次；");
		print("开始:");
		// 字符串字节数
		int num0 = 1000000;

		// 解码字符串个数
		int num1 = 100;

		// 每同一字符串解码次数
		int num2 = 10;

		// 对应下方耗时，用于计算平均耗时
		long time0Sum = 0;
		long time1Sum = 0;
		long time2Sum = 0;
		long time3Sum = 0;
		long time4Sum = 0;
		long time5Sum = 0;

		// 测试5轮
		for (int i = 0; i < 5; i++) {

			// cn.kamij.hashdic.utils.EncodeUtils.hexToBytes(String)耗时
			long time0 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.hexToBytesFast(String)耗时
			long time1 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.hexWithSpaceToBytes(String)耗时
			long time2 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.hexWithSpaceToBytesFast(String)耗时
			long time3 = 0;

			// org.apache.commons.codec.binary.Hex.decodeHex(char[])耗时
			long time4 = 0;

			// com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.decode(String)耗时
			long time5 = 0;

			for (int j = 0; j < num1; j++) {
				// 生成随机字节数组的字符串
				byte[] bs = RandomUtils.nextBytes(num0);
				String str = EncodeUtils.bytesToHex(bs);
				String strWithSpace = EncodeUtils.bytesToHexWithSpace(bs);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.hexToBytes(str);
				}
				end = System.currentTimeMillis();
				time0 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.hexToBytesFast(str);
				}
				end = System.currentTimeMillis();
				time1 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.hexWithSpaceToBytes(strWithSpace);
				}
				end = System.currentTimeMillis();
				time2 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.hexWithSpaceToBytesFast(strWithSpace);
				}
				end = System.currentTimeMillis();
				time3 += (end - start);

				char[] chs = str.toCharArray();

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					Hex.decodeHex(chs);
				}
				end = System.currentTimeMillis();
				time4 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					HexBin.decode(str);
				}
				end = System.currentTimeMillis();
				time5 += (end - start);

			}
			print("第" + (i + 1) + "轮:");
			print("cn.kamij.hashdic.utils.EncodeUtils.hexToBytes(String):" + time0 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.hexToBytesFast(String):" + time1 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.hexWithSpaceToBytes(String):" + time2 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.hexWithSpaceToBytesFast(String):" + time3 + "ms");
			print("org.apache.commons.codec.binary.Hex.decodeHex(char[]):" + time4 + "ms");
			print("com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.decode(String):" + time5 + "ms");

			time0Sum += time0;
			time1Sum += time1;
			time2Sum += time2;
			time3Sum += time3;
			time4Sum += time4;
			time5Sum += time5;
		}
		print("平均:");
		print("cn.kamij.hashdic.utils.EncodeUtils.hexToBytes(String):" + time0Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time0Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.hexToBytesFast(String):" + time1Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time1Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.hexWithSpaceToBytes(String):" + time2Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time2Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.hexWithSpaceToBytesFast(String):" + time3Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time3Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("org.apache.commons.codec.binary.Hex.decodeHex(char[]):" + time4Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time4Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.decode(String):" + time5Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time5Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
	}

	/**
	 * 测试各Bin编码器编码效率；<br/>
	 * <br/>
	 * 参与测试的类：<br/>
	 * 1、cn.kamij.hashdic.utils.EncodeUtils.bytesToBin(byte[])<br/>
	 * 2、cn.kamij.hashdic.utils.EncodeUtils.bytesToBinWithSpace(byte[])<br/>
	 * <br/>
	 * 可以处理null的类：1、2；<br/>
	 * 可以处理空字节数组的类：1、2；<br/>
	 * 生成的Bin字符串带字节间空格的类：2；<br/>
	 * <br/>
	 * 测试结果（取5轮均值，单位ms）：<br/>
	 * 对1万个长度为10字节的数组进行编码，每个字节数组编码1000次（共1亿字节）：<br/>
	 * 1:3399 <br/>
	 * 2:3884 <br/>
	 * <br/>
	 * 对1万个长度为100字节的数组进行编码，每个字节数组编码100次（共1亿字节）：<br/>
	 * 1:2989 <br/>
	 * 2:3578 <br/>
	 * <br/>
	 * 对1万个长度为1000字节的数组进行编码，每个字节数组编码100次（共10亿字节）：<br/>
	 * 1:23962 <br/>
	 * 2:28872 <br/>
	 * <br/>
	 * 对1000个长度为1万字节的数组进行编码，每个字节数组编码100次（共10亿字节）：<br/>
	 * 1:21024 <br/>
	 * 2:27590 <br/>
	 * <br/>
	 * 对100个长度为100万字节的数组进行编码，每个字节数组编码10次（共10亿字节）：<br/>
	 * 1:18578 <br/>
	 * 2:28581 <br/>
	 * <br/>
	 * 总结果：<br/>
	 * 1:28MB/s(10字节)--31MB/s(100字节)--39MB/s(1000字节)--45MB/s(1万字节)--51MB/s(
	 * 100万字节)<br/>
	 * 2:24MB/s(10字节)--26MB/s(100字节)--33MB/s(1000字节)--34MB/s(1万字节)--33MB/s(
	 * 100万字节)<br/>
	 * <br/>
	 * 结论：<br/>
	 * 2因为在字节之间添加空格，效率较低；<br/>
	 */
	@Test
	public void testBinEncoder() {
		// 计时起始结束标志
		long start, end;

		// 测试配置
		print("对长度为100万字节的数组编码:");
		print("共进行5轮；");
		print("每轮对100个字节数组进行编码；");
		print("每个字节数组编码10次；");
		print("开始:");
		// 字节数组字节数
		int num0 = 1000000;

		// 编码字节数组个数
		int num1 = 100;

		// 每同一数组编码次数
		int num2 = 10;

		// 对应下方耗时，用于计算平均耗时
		long time0Sum = 0;
		long time1Sum = 0;

		// 测试5轮
		for (int i = 0; i < 5; i++) {

			// cn.kamij.hashdic.utils.EncodeUtils.bytesToBin(byte[])耗时
			long time0 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.bytesToBinWithSpace(byte[])耗时
			long time1 = 0;

			for (int j = 0; j < num1; j++) {
				// 生成随机字节数组
				byte[] bs = RandomUtils.nextBytes(num0);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.bytesToBin(bs);
				}
				end = System.currentTimeMillis();
				time0 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.bytesToBinWithSpace(bs);
				}
				end = System.currentTimeMillis();
				time1 += (end - start);

			}
			print("第" + (i + 1) + "轮:");
			print("cn.kamij.hashdic.utils.EncodeUtils.bytesToBin(byte[]):" + time0 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.bytesToBinWithSpace(byte[]):" + time1 + "ms");

			time0Sum += time0;
			time1Sum += time1;
		}
		print("平均:");
		print("cn.kamij.hashdic.utils.EncodeUtils.bytesToBin(byte[]):" + time0Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time0Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.bytesToBinWithSpace(byte[]):" + time1Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time1Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
	}

	/**
	 * 测试各Bin解码器解码效率；<br/>
	 * <br/>
	 * 参与测试的类:<br/>
	 * 1、cn.kamij.hashdic.utils.EncodeUtils.binToBytes(String)<br/>
	 * 2、cn.kamij.hashdic.utils.EncodeUtils.binToBytesFast(String)<br/>
	 * 3、cn.kamij.hashdic.utils.EncodeUtils.binWithSpaceToBytes(String) <br/>
	 * 4、cn.kamij.hashdic.utils.EncodeUtils.binWithSpaceToBytesFast(String)
	 * <br/>
	 * <br/>
	 * 可以处理null的类:1、3；<br/>
	 * 可以处理空字符串的类:1、2、3、4；<br/>
	 * 只能处理带字节间空格的Bin字符串的类:3、4；<br/>
	 * 对非法Bin字符串:1、3返回null，2、4解析错误；<br/>
	 * <br/>
	 * 测试结果（取5轮均值，单位ms）:<br/>
	 * 对1万个长度为10字节的字符串进行解码，每个字符串解码1000次（共1亿字节）:<br/>
	 * 1:5563 <br/>
	 * 2:2268 <br/>
	 * 3:5600 <br/>
	 * 4:2494 <br/>
	 * <br/>
	 * 对1万个长度为100字节的字符串进行解码，每个字符串解码100次（共1亿字节）:<br/>
	 * 1:4996 <br/>
	 * 2:1407 <br/>
	 * 3:4972 <br/>
	 * 4:2031 <br/>
	 * <br/>
	 * 对1000个长度为1000字节的字符串进行解码，每个字符串解码100次（共1亿字节）:<br/>
	 * 1:4824 <br/>
	 * 2:1279 <br/>
	 * 3:4801 <br/>
	 * 4:1943 <br/>
	 * <br/>
	 * 对1000个长度为1万字节的字符串进行解码，每个字符串解码100次（共10亿字节）:<br/>
	 * 1:47271 <br/>
	 * 2:12590 <br/>
	 * 3:49011 <br/>
	 * 4:18789 <br/>
	 * <br/>
	 * 对100个长度为100万字节的字符串进行解码，每个字符串解码10次（共10亿字节）:<br/>
	 * 1:47812 <br/>
	 * 2:14250 <br/>
	 * 3:51666 <br/>
	 * 4:20167 <br/>
	 * <br/>
	 * 总结果：<br/>
	 * 1:17MB/s(10字节)--19MB/s(100字节)--19MB/s(1000字节)--20MB/s(1万字节)--19MB/s(
	 * 100万字节)<br/>
	 * 2:42MB/s(10字节)--67MB/s(100字节)--74MB/s(1000字节)--75MB/s(1万字节)--66MB/s(
	 * 100万字节)<br/>
	 * 3:17MB/s(10字节)--19MB/s(100字节)--19MB/s(1000字节)--19MB/s(1万字节)--18MB/s(
	 * 100万字节)<br/>
	 * 4:38MB/s(10字节)--46MB/s(100字节)--49MB/s(1000字节)--50MB/s(1万字节)--47MB/s(
	 * 100万字节)<br/>
	 * <br/>
	 * 结论：<br/>
	 * 2效率最高，但并不安全；<br/>
	 * 1安全但效率较低；<br/>
	 * 3、4只能处理带字节间空格的字符串；<br/>
	 */
	@Test
	public void testBinDecoder() {
		// 计时起始结束标志
		long start, end;

		// 测试配置
		print("对长度为100万字节的字符串解码:");
		print("共进行5轮；");
		print("每轮对100个字符串进行解码；");
		print("每个字符串解码10次；");
		print("开始:");
		// 字符串字节数
		int num0 = 1000000;

		// 解码字符串个数
		int num1 = 100;

		// 每同一字符串解码次数
		int num2 = 10;

		// 对应下方耗时，用于计算平均耗时
		long time0Sum = 0;
		long time1Sum = 0;
		long time2Sum = 0;
		long time3Sum = 0;

		// 测试5轮
		for (int i = 0; i < 5; i++) {

			// cn.kamij.hashdic.utils.EncodeUtils.binToBytes(String)耗时
			long time0 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.binToBytesFast(String)耗时
			long time1 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.binWithSpaceToBytes(String)耗时
			long time2 = 0;

			// cn.kamij.hashdic.utils.EncodeUtils.binWithSpaceToBytesFast(String)耗时
			long time3 = 0;

			for (int j = 0; j < num1; j++) {
				// 生成随机字节数组的字符串
				byte[] bs = RandomUtils.nextBytes(num0);
				String str = EncodeUtils.bytesToBin(bs);
				String strWithSpace = EncodeUtils.bytesToBinWithSpace(bs);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.binToBytes(str);
				}
				end = System.currentTimeMillis();
				time0 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.binToBytesFast(str);
				}
				end = System.currentTimeMillis();
				time1 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.binWithSpaceToBytes(strWithSpace);
				}
				end = System.currentTimeMillis();
				time2 += (end - start);

				start = System.currentTimeMillis();
				for (int k = 0; k < num2; k++) {
					EncodeUtils.binWithSpaceToBytesFast(strWithSpace);
				}
				end = System.currentTimeMillis();
				time3 += (end - start);

			}
			print("第" + (i + 1) + "轮:");
			print("cn.kamij.hashdic.utils.EncodeUtils.binToBytes(String):" + time0 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.binToBytesFast(String):" + time1 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.binWithSpaceToBytes(String):" + time2 + "ms");
			print("cn.kamij.hashdic.utils.EncodeUtils.binWithSpaceToBytesFast(String):" + time3 + "ms");

			time0Sum += time0;
			time1Sum += time1;
			time2Sum += time2;
			time3Sum += time3;
		}
		print("平均:");
		print("cn.kamij.hashdic.utils.EncodeUtils.binToBytes(String):" + time0Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time0Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.binToBytesFast(String):" + time1Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time1Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.binWithSpaceToBytes(String):" + time2Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time2Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
		print("cn.kamij.hashdic.utils.EncodeUtils.binWithSpaceToBytesFast(String):" + time3Sum / 5 + "ms--"
				+ (num0 * num1 * num2 / (time3Sum / 5) * 1000 / 1024 / 1024) + "MB/s");
	}

}
