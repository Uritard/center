package com.yjh.platform.common.websocket;

import java.util.Objects;
import java.util.UUID;

/**
 * 
 * @author Ja
 * @time 2017年2月8日 上午9:38:55
 * @description UUID生成工具类
 *
 */
public class UUIDUtils {

	public UUIDUtils() {
	}

	/**
	 * 32位UUID生成方法
	 * 
	 * @return 32位UUID生成方法
	 */
	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		String uuidStr = uuid.toString();
		if (!Objects.equals("",uuidStr)) {
			uuidStr = uuidStr.toUpperCase();
			uuidStr = uuidStr.replaceAll("-", "");
		} else {
			uuidStr = "";
		}
		return uuidStr;
	}

}
