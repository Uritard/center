package com.yjh.platform.common.utils;

/**
 * @author lqh
 * @since 2020/9/27
 */

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Object2Map {
    /**
     *
     * @Title: objectToMap
     * @Description: 将object转换为map，默认不保留空值
     * @param @param obj
     * @return Map<String,Object> 返回类型
     * @throws
     */
    //默认null值字段不显示
    public static Map objectToMap(Object obj) {

        Map<String, String> map  = objectToMap(obj, false);
        return map;
    }
    public static Map<String, String> objectToMap(Object obj, boolean keepNullVal) {
        if (obj == null) {
            return null;
        }

        Map<String, String> map = new HashMap();
        try {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                if (keepNullVal == true) {
                    map.put(field.getName(),
                            field.get(obj) == null || "".equals(field.get(obj).toString())?"null":field.get(obj).toString());
                } else {
                    if (field.get(obj) != null && !"".equals(field.get(obj).toString())) {
                        map.put(field.getName(), field.get(obj).toString());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 将map的数据格式统一为String
     */
    public static Map<String, String> toStringMap(Map<?, ?> map) {
        Map<String, String> stringMap = new HashMap<>((int)(map.size() * 1.5));
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            Object k = entry.getKey();
            Object v = entry.getValue();
            stringMap.put(String.valueOf(k), String.valueOf(v));
        }
        return stringMap;
    }

}
