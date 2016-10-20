package cn.kamij.hashdic.utils;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * 提供加解密相关的功能；<br/>
 * <br/>
 * 加密算法说明：<br/>
 * 对称加密：利用一把同样的密钥加密解密，如AES、DES算法；<br/>
 * 非对称加密：利用一对密钥（公钥和私钥）加密解密，公钥加密的数据只有用私钥解密，私钥加密的数据只有用公钥解密，如RSA算法；<br/>
 * Hash算法：将信息映射为一定长的文本，因为是多对一的映射，所以具有不可逆的特性，如MD5，SHA256算法；<br/>
 * 
 * @author KamiJ @2016/9/2
 * @version 1.0
 */
public class CryptoUtils {
	// 所支持的加密方式
	// Hash算法
	/**
	 * MD5(Message Digest Algorithm 5，信息摘要算法第五版)，目前很流行，但通过字典可以破解。<br/>
	 * 其摘要长度为128位比特。<br/>
	 */
	public static final String MD5 = "MD5";
	/**
	 * SHA(Secure Hash Algorithm，安全散列算法)家族成员之一。<br/>
	 * SHA由美国国家安全局 (NSA)设计，目前规定了SHA-1,SHA-224,SHA-256,SHA-384和SHA-512这几种单向散列算法。
	 * <br/>
	 * 其散列值为160位比特，适用于长度不超过2^64位比特的消息。<br/>
	 */
	public static final String SHA1 = "SHA-1";
	/**
	 * SHA(Secure Hash Algorithm，安全散列算法)家族成员之一。<br/>
	 * SHA由美国国家安全局 (NSA)设计，目前规定了SHA-1,SHA-224,SHA-256,SHA-384和SHA-512这几种单向散列算法。
	 * <br/>
	 * 其散列值为224位比特，适用于长度不超过2^64位比特的消息。<br/>
	 */
	public static final String SHA224 = "SHA-224";
	/**
	 * SHA(Secure Hash Algorithm，安全散列算法)家族成员之一。<br/>
	 * SHA由美国国家安全局 (NSA)设计，目前规定了SHA-1,SHA-224,SHA-256,SHA-384和SHA-512这几种单向散列算法。
	 * <br/>
	 * 其散列值为256位比特，适用于长度不超过2^64位比特的消息。<br/>
	 */
	public static final String SHA256 = "SHA-256";
	/**
	 * SHA(Secure Hash Algorithm，安全散列算法)家族成员之一。<br/>
	 * SHA由美国国家安全局 (NSA)设计，目前规定了SHA-1,SHA-224,SHA-256,SHA-384和SHA-512这几种单向散列算法。
	 * <br/>
	 * 其散列值为384位比特，适用于长度不超过2^128位比特的消息。<br/>
	 */
	public static final String SHA384 = "SHA-384";
	/**
	 * SHA(Secure Hash Algorithm，安全散列算法)家族成员之一。<br/>
	 * SHA由美国国家安全局 (NSA)设计，目前规定了SHA-1,SHA-224,SHA-256,SHA-384和SHA-512这几种单向散列算法。
	 * <br/>
	 * 其散列值为512位比特，适用于长度不超过2^128位比特的消息。<br/>
	 */
	public static final String SHA512 = "SHA-512";
	// 对称加密算法
	/**
	 * AES(Advanced Encryption Standard，高级加密标准)，是对称加密中最流行的算法之一。<br/>
	 * 密钥为128、192或256位比特。
	 */
	public static final String AES = "AES";
	// 非对称加密算法
	/**
	 * RSA算法是目前最有影响力的非对称加密算法。<br/>
	 * 其名称由三位发明者姓氏开头字母组成。<br/>
	 * 它基于一个十分简单的数论事实：将两个大质数相乘十分容易，但是想要对其乘积进行因式分解却极其困难，因此可以将乘积公开作为加密密钥。<br/>
	 */
	public static final String RSA = "RSA";

	/**
	 * 对数据使用Hash算法
	 * 
	 * @param data
	 *            数据
	 * @param algorithm
	 *            Hash算法种类
	 * @return 加密后的字节数组，输入错误时返回null
	 */
	public static byte[] hash(final byte[] data, final String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm).digest(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 对数据进行MD5加密
	 * 
	 * @param data
	 *            数据
	 * @return 加密后的字节数组，长度为16，输入错误时返回null
	 */
	public static byte[] md5(final byte[] data) {
		return hash(data, MD5);
	}

	/**
	 * 对数据进行SHA1加密
	 * 
	 * @param data
	 *            数据
	 * @return 加密后的字节数组，长度为20，输入错误时返回null
	 */
	public static byte[] sha1(final byte[] data) {
		return hash(data, SHA1);
	}

	/**
	 * 对数据进行SHA224加密
	 * 
	 * @param data
	 *            数据
	 * @return 加密后的字节数组，长度为28，输入错误时返回null
	 */
	public static byte[] sha224(final byte[] data) {
		return hash(data, SHA224);
	}

	/**
	 * 对数据进行SHA256加密
	 * 
	 * @param data
	 *            数据
	 * @return 加密后的字节数组，长度为32，输入错误时返回null
	 */
	public static byte[] sha256(final byte[] data) {
		return hash(data, SHA256);
	}

	/**
	 * 对数据进行SHA384加密
	 * 
	 * @param data
	 *            数据
	 * @return 加密后的字节数组，长度为48，输入错误时返回null
	 */
	public static byte[] sha384(final byte[] data) {
		return hash(data, SHA384);
	}

	/**
	 * 对数据进行SHA512加密
	 * 
	 * @param data
	 *            数据
	 * @return 加密后的字节数组，长度为64，输入错误时返回null
	 */
	public static byte[] sha512(final byte[] data) {
		return hash(data, SHA512);
	}

	/**
	 * 对数据进行AES加密，方式为AES/ECB/PKCS5Padding
	 * 
	 * @param data
	 *            数据
	 * @param key
	 *            密钥，这里对其使用一次md5算法确保其长度为128位比特
	 * @return 加密后的字节数组，长度为(data.length/16+16)，输入错误时返回null
	 */
	public static byte[] enAES(final byte[] data, final byte[] key) {
		try {
			// 设置AES加密
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			// 对密钥使用一次md5算法
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(md5(key), "AES"));
			// 进行加密
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 对加密数据进行AES解密，方式为AES/ECB/PKCS5Padding
	 * 
	 * @param encryptData
	 *            加密数据
	 * @param key
	 *            密钥，这里对其使用一次md5算法确保其长度为128位比特
	 * @return 解密后的字节数组，长度为其原先长度，输入错误时返回null
	 */
	public static byte[] deAES(final byte[] encryptData, final byte[] key) {
		try {
			// 设置AES解密
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			// 对密钥使用一次md5算法
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(md5(key), "AES"));
			// 进行解密
			return cipher.doFinal(encryptData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 生成密钥对
	 */
	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
			// 设置RSA密钥长度
			keyPairGen.initialize(1024);
			// 生成密钥对
			return keyPairGen.generateKeyPair();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 从密钥对取得私钥
	 * 
	 * @param keyPair
	 *            密钥对
	 * @return 私钥字节数组
	 */
	public static byte[] getPrivateKey(final KeyPair keyPair) {
		return keyPair.getPrivate().getEncoded();
	}

	/**
	 * 从密钥对取得公钥
	 * 
	 * @param keyPair
	 *            密钥对
	 * @return 公钥字节数组
	 */
	public static byte[] getPublicKey(final KeyPair keyPair) {
		return keyPair.getPublic().getEncoded();
	}

	/**
	 * 使用私钥对数据进行RSA加密
	 * 
	 * @param data
	 *            数据
	 * @param privateKey
	 *            私钥字节数组
	 * @return 加密后的字节数组，结果会变化，输入错误时返回null
	 */
	public static byte[] enRSAPri(final byte[] data, final byte[] privateKey) {
		try {
			// 设置RSA加密
			Cipher cipher = Cipher.getInstance(RSA);
			// 设置私钥
			cipher.init(Cipher.ENCRYPT_MODE,
					KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(privateKey)));
			// 进行加密
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 使用公钥对数据进行RSA加密
	 * 
	 * @param data
	 *            数据
	 * @param publicKey
	 *            公钥字节数组
	 * @return 加密后的字节数组，结果会变化，输入错误时返回null
	 */
	public static byte[] enRSAPub(final byte[] data, final byte[] publicKey) {
		try {
			// 设置RSA加密
			Cipher cipher = Cipher.getInstance(RSA);
			// 设置公钥
			cipher.init(Cipher.ENCRYPT_MODE,
					KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(publicKey)));
			// 进行加密
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 使用私钥对数据进行RSA解密
	 * 
	 * @param encryptData
	 *            加密数据
	 * @param privateKey
	 *            私钥字节数组
	 * @return 解密后的字节数组，长度为其原先长度，输入错误时返回null
	 */
	public static byte[] deRSAPri(final byte[] encryptData, final byte[] privateKey) {
		try {
			// 设置RSA解密
			Cipher cipher = Cipher.getInstance(RSA);
			// 设置私钥
			cipher.init(Cipher.DECRYPT_MODE,
					KeyFactory.getInstance(RSA).generatePrivate(new PKCS8EncodedKeySpec(privateKey)));
			// 进行解密
			return cipher.doFinal(encryptData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 使用公钥对数据进行RSA解密
	 * 
	 * @param encryptData
	 *            加密数据
	 * @param publicKey
	 *            公钥字节数组
	 * @return 解密后的字节数组，长度为其原先长度，输入错误时返回null
	 */
	public static byte[] deRSAPub(final byte[] encryptData, final byte[] publicKey) {
		try {
			// 设置RSA解密
			Cipher cipher = Cipher.getInstance(RSA);
			// 设置公钥
			cipher.init(Cipher.DECRYPT_MODE,
					KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(publicKey)));
			// 进行解密
			return cipher.doFinal(encryptData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
