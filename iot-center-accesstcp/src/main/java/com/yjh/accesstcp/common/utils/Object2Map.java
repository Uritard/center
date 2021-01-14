package com.yjh.accesstcp.common.utils;

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

        Map<String, Object> map  = objectToMap(obj, false);
        return map;
    }
    public static Map objectToMap(Object obj, boolean keepNullVal) {
        if (obj == null) {
            return null;
        }

        Map<String, Object> map = new HashMap();
        try {
            Field[] declaredFields = obj.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                if (keepNullVal == true) {
                    map.put(field.getName(), field.get(obj));
                } else {
                    if (field.get(obj) != null && !"".equals(field.get(obj).toString())) {
                        map.put(field.getName(), field.get(obj));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
    //将map的数据格式统一为String
    public static Map toStringMap(Map map){
        Map<String,Object> stringObjectMap = map;
        Map<String,Object> map2 = new HashMap<>();
        for (String key : stringObjectMap.keySet()) {
            Object s = stringObjectMap.get(key);
            String s2 = String.valueOf(s);
            map2.put(key,s2);
        }
        return map2;
    }
}
