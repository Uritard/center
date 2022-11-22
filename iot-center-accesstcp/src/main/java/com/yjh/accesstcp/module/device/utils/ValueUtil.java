package com.yjh.accesstcp.module.device.utils;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    /**
     * 以分隔符连接的String转为List<T>
     *
     * @param string   以分隔符连接的String
     * @param splitter 分隔符，默认为','
     * @param apply    转型函数
     * @return List<T>
     */
    public static <T> List<T> stringToList(String string, String splitter, Function<String, T> apply) {
        if (StringUtils.isBlank(string)) {
            return Collections.emptyList();
        }
        return Lists.newArrayList(string.split(splitter)).stream().map(apply).collect(Collectors.toList());
    }
}
