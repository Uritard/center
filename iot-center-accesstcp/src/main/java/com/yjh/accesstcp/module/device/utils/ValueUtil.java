package com.yjh.accesstcp.module.device.utils;

/**
 * @author lqh
 * @since 2022/4/8
 */
public class ValueUtil {

    public static String Object2String(Object obj, String def) {
        if (obj == null)
            return def;

        return obj.toString();
    }
}
