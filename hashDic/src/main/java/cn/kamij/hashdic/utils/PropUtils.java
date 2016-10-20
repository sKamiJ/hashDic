package cn.kamij.hashdic.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;

import org.springframework.web.util.UriUtils;

/**
 * 提供资源文件相关功能
 * 
 * @author KamiJ
 *
 */
public class PropUtils {

	/**
	 * 根目录路径
	 */
	private static final String ROOT;
	static {
		String tmp = null;
		try {
			// 获取根目录（URL被转译过了，所以这里需要反转译一下）
			tmp = UriUtils.decode(PropUtils.class.getResource("/").getFile(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		ROOT = tmp;
	}

	/**
	 * resources目录路径
	 */
	private static final String RESOURCES = "src/main/resources/";

	/**
	 * 获取properties文件中的某个资源
	 * 
	 * @param propPath
	 *            properties文件路径
	 * @param resourceName
	 *            资源名称
	 * @return 想要获取的资源，路径或资源名称错误时返回null
	 */
	public static String getProp(String propPath, String resourceName) {
		String result = null;
		try {
			Properties prop = new Properties();
			// 读取资源
			Reader reader = new FileReader(new File(propPath));
			prop.load(reader);
			// 获取资源
			String resource = prop.getProperty(resourceName);
			if (resource != null) {
				// 不为空时获取
				result = resource.trim();
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取根目录下properties文件中的某个资源
	 * 
	 * @param propName
	 *            properties文件名，无需.properties后缀
	 * @param resourceName
	 *            资源名称
	 * @return 想要获取的资源，文件名或资源名称错误时返回null
	 */
	public static String getPropInRoot(String propName, String resourceName) {
		return getProp(ROOT + propName + ".properties", resourceName);
	}

	/**
	 * 获取resources下properties文件中的某个资源
	 * 
	 * @param propName
	 *            properties文件名，无需.properties后缀
	 * @param resourceName
	 *            资源名称
	 * @return 想要获取的资源，文件名或资源名称错误时返回null
	 */
	public static String getPropInResources(String propName, String resourceName) {
		return getProp(RESOURCES + propName + ".properties", resourceName);
	}

	/**
	 * 更改properties文件中的某个资源
	 * 
	 * @param propPath
	 *            properties文件路径
	 * @param resourceName
	 *            资源名称
	 * @param newValue
	 *            更新的值
	 * @param ifSetNew
	 *            若无该资源，是否新建该资源
	 * @return 是否成功更改该资源，不新建且无该资源时返回false
	 */
	public static boolean setProp(String propPath, String resourceName, String newValue, boolean ifSetNew) {
		boolean result = true;
		try {
			Properties prop = new Properties();
			// 读取资源
			File file = new File(propPath);
			Reader reader = new FileReader(file);
			prop.load(reader);
			// 获取资源
			String resource = prop.getProperty(resourceName);
			if (resource != null || ifSetNew == true) {
				// 新建或更改该资源
				prop.setProperty(resourceName, newValue.trim());
				Writer writer = new FileWriter(file);
				prop.store(writer, null);
				writer.close();
			} else {
				result = false;
			}
			reader.close();
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 更改根目录下properties文件中的某个资源
	 * 
	 * @param propName
	 *            properties文件名，无需.properties后缀
	 * @param resourceName
	 *            资源名称
	 * @param newValue
	 *            更新的值
	 * @param ifSetNew
	 *            若无该资源，是否新建该资源
	 * @return 是否成功更改该资源，不新建且无该资源时返回false
	 */
	public static boolean setPropInRoot(String propName, String resourceName, String newValue, boolean ifSetNew) {
		return setProp(ROOT + propName + ".properties", resourceName, newValue, ifSetNew);
	}

	/**
	 * 更改resources下properties文件中的某个资源
	 * 
	 * @param propName
	 *            properties文件名，无需.properties后缀
	 * @param resourceName
	 *            资源名称
	 * @param newValue
	 *            更新的值
	 * @param ifSetNew
	 *            若无该资源，是否新建该资源
	 * @return 是否成功更改该资源，不新建且无该资源时返回false
	 */
	public static boolean setPropInResources(String propName, String resourceName, String newValue, boolean ifSetNew) {
		return setProp(RESOURCES + propName + ".properties", resourceName, newValue, ifSetNew);
	}
	
	/**
	 * 更新properties文件中的某个资源，无法新建
	 * 
	 * @param propPath
	 *            properties文件路径
	 * @param resourceName
	 *            资源名称
	 * @param newValue
	 *            更新的值
	 * @return 是否成功更新该资源
	 */
	public static boolean updateProp(String propPath, String resourceName, String newValue) {
		return setProp(propPath, resourceName, newValue, false);
	}

	/**
	 * 更新根目录下properties文件中的某个资源，无法新建
	 * 
	 * @param propName
	 *            properties文件名，无需.properties后缀
	 * @param resourceName
	 *            资源名称
	 * @param newValue
	 *            更新的值
	 * @return 是否成功更新该资源
	 */
	public static boolean updatePropInRoot(String propName, String resourceName, String newValue) {
		return updateProp(ROOT + propName + ".properties", resourceName, newValue);
	}

	/**
	 * 更新resources下properties文件中的某个资源，无法新建
	 * 
	 * @param propName
	 *            properties文件名，无需.properties后缀
	 * @param resourceName
	 *            资源名称
	 * @param newValue
	 *            更新的值
	 * @return 是否成功更新该资源
	 */
	public static boolean updatePropInResources(String propName, String resourceName, String newValue) {
		return updateProp(RESOURCES + propName + ".properties", resourceName, newValue);
	}
	
}
