/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.NamingCase;
import com.yjh.protocol_a.Message;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/6/5
 * @since [产品/模块版本] （可选）
 */
public class StringVariableUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(StringVariableUtil.class);

    private static final Map<String, String> VARIABLE_PARAMS = new ConcurrentHashMap<>();
    private static final String[] PARAM_CODE = new String[] {"task_code", "task_patrolled_id"};

    private static final String CAN_EMPTY = "empty";
    private static final String UUID_S = "uuid";
    private static final String NOW = "now";
    private static final String SIMPLE_NOW = "simpleNow";
    private static final String NOW_DATE = "nowDate";

    private StringVariableUtil() {

    }

    public static String variableParse(String value) {
        if (!StringUtils.contains(value, "{{")) {
            return value;
        }
        StringBuffer outBuffer = new StringBuffer();
        // 最小匹配 {{}}
        String regex = "\\{\\{(.*?)}}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        //自旋进行最小匹配，直到无法匹配
        while (matcher.find()) {
            //替换匹配内容
            String key = matcher.group(1);
            String replacement = getFieldValue(VARIABLE_PARAMS, key);
            if (replacement != null) {
                matcher.appendReplacement(outBuffer, replacement);
            }
        }
        matcher.appendTail(outBuffer);

        return outBuffer.toString();
    }

    /**
     * 解析字符串中 ${} 形式传参，并替换成对象中指定值
     */
    public static String variableParse(String value, Object src) {
        StringBuffer outBuffer = new StringBuffer();
        // 最小匹配 {{}}
        String regex = "\\{\\{(.*?)}}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(value);
        //自旋进行最小匹配，直到无法匹配
        while (matcher.find()) {
            //替换匹配内容
            String key = matcher.group(1);
            String replacement = getFieldValue(src, key);

            matcher.appendReplacement(outBuffer, replacement);
        }
        matcher.appendTail(outBuffer);

        return outBuffer.toString();
    }

    public static String getFieldValue(Object obj, String field) {
        if (CAN_EMPTY.equalsIgnoreCase(field)) {
            return "";
        }
        if (UUID_S.equalsIgnoreCase(field)) {
            return UUID.fastUUID().toString(true);
        }
        if (NOW.equalsIgnoreCase(field)) {
            return DateUtil.now();
        }
        if (SIMPLE_NOW.equalsIgnoreCase(field)) {
            return DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN);
        }
        if (NOW_DATE.equalsIgnoreCase(field)) {
            return DateUtil.formatDate(new Date());
        }

        if (obj == null) {
            return "";
        }
        Object value;
        // 判断对象是否是Map类型
        if (obj instanceof Map) {
            Map<Object, Object> objMap = (Map)obj;
            value = objMap.get(field);
        } else {
            // 反射函数处理对象结果
            try {
                Class<?> cls = obj.getClass();
                // 使用get方法获取值，注意不规范的驼峰设值
                Method methodGet = cls.getMethod("get" + StringUtils.capitalize(field));
                value = methodGet.invoke(obj);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                LOGGER.error(e.getMessage(), e);
                value = null;
            }

        }

        return value == null ? null : String.valueOf(value);
    }

    public static void updateParams(Message message) {
        List<Map<String, Object>> items = message.getItems();
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        for (Map<String, Object> item : items) {
            for (Map.Entry<String, Object> entry : item.entrySet()) {
                String key = entry.getKey();
                String value = (String)entry.getValue();
                if (StringUtils.equalsAny(key, PARAM_CODE)) {
                    VARIABLE_PARAMS.put(NamingCase.toCamelCase(key), value);
                }
            }
        }
    }

    public static void updateParams(Map<String, String> params) {
        if (MapUtils.isEmpty(params)) {
            return;
        }

        VARIABLE_PARAMS.putAll(params);
    }

    public static Map<String, String> getVariableParams() {
        return Collections.unmodifiableMap(VARIABLE_PARAMS);
    }
}
