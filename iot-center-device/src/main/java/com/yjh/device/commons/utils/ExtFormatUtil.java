package com.yjh.device.commons.utils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtFormatUtil {

    /**
     * 将一个map数组转换成key-value对应的map.
     * 这个数组中存在重复的键值, 所以值是一个list.
     *
     * @param mapArray 需要转换的map数组
     * @param keyName  map中作为键值的key
     * @param groupKey 数组中的键值是否聚合在一起的
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Map array2Map(Map[] mapArray, String keyName, boolean groupKey) {
        Map result = new HashMap();
        if (groupKey) {
            // 设置一个不会和现有key重复的值
            Object preKey = Object.class;
            List values = new ArrayList();
            for (int i = 0; i < mapArray.length; i++) {
                Map item = mapArray[i];
                Object key = item.get(keyName);
                if (!objectEquals(preKey, key)) {
                    preKey = key;
                    values = new ArrayList(8);
                    result.put(key, values);
                }
                values.add(item);
            }
        } else {
            for (int i = 0; i < mapArray.length; i++) {
                Map item = mapArray[i];
                Object key = item.get(keyName);
                List values = (List) result.get(key);
                if (values == null) {
                    values = new ArrayList(8);
                    result.put(key, values);
                }
                values.add(item);
            }
        }
        return result;
    }

    public static Map array2Map(List<Map> mapList, String keyName, boolean groupKey) {
        Map result = new HashMap();
        if (groupKey) {
            // 设置一个不会和现有key重复的值
            Object preKey = Object.class;
            List values = new ArrayList();
            for (int i = 0; i < mapList.size(); i++) {
                Map item = mapList.get(i);
                Object key = String.valueOf(item.get(keyName));
                if (!objectEquals(preKey, key)) {
                    preKey = key;
                    values = new ArrayList(8);
                    result.put(key, values);
                }
                values.add(item);
            }
        } else {
            for (int i = 0; i < mapList.size(); i++) {
                Map item = mapList.get(i);
                Object key = item.get(keyName);
                List values = (List) result.get(key);
                if (values == null) {
                    values = new ArrayList(8);
                    result.put(key, values);
                }
                values.add(item);
            }
        }
        return result;
    }

    /**
     * @param maps    目标数组
     * @param key     目标的数组字段
     * @param mapName
     * @return
     */
    @Deprecated
    public static Map[] format(Map[] maps, String key, String mapName, Map formatValues) {
        if (null != maps && maps.length > 0 && StringUtils.isNotBlank(key)) {
//            String[] keys = new String[maps.length];
//            for (int i = 0; i < maps.length; i++) {
//                keys[i] = "" + maps[i].get(key);
//            }
            Map[] resultMaps = new Map[maps.length];
            if (null == formatValues) {//空白的value值
                for (int i = 0; i < maps.length; i++) {
                    resultMaps[i] = new HashMap(maps[i]);
                    resultMaps[i].put(mapName, null);
                }
            } else {
                for (int i = 0; i < maps.length; i++) {
                    resultMaps[i] = new HashMap(maps[i]);
                    resultMaps[i].put(mapName, formatValues.get("" + maps[i].get(key)));
                }
            }
            return resultMaps;
        }
        return null;
    }

    /**
     * @param data
     * @param key
     * @param mapName
     * @param formatValues
     * @return
     */
    public static List<Map> format(List<Map> data, String key, String mapName, Map formatValues) {
        if (null == data || data.size() == 0) {
            return new ArrayList<>();
        }
        if (StringUtils.isNotBlank(key)) {
            List<Map> resultList = new ArrayList<Map>();
            if (null == formatValues) {//空白的value值
                for (int i = 0; i < data.size(); i++) {
                    Map temp = new HashMap(data.get(i));
                    temp.put(mapName, null);
                    resultList.add(temp);
                }
            } else {
                for (int i = 0; i < data.size(); i++) {
                    Map temp = new HashMap(data.get(i));
                    temp.put(mapName, formatValues.get("" + data.get(i).get(key)));
                    resultList.add(temp);
                }
            }
            return resultList;
        }
        return new ArrayList<>();
    }

    /**
     * 比较两个对象
     *
     * @param obj1
     * @param obj2
     * @return
     */
    private static boolean objectEquals(Object obj1, Object obj2) {
        return obj1 == obj2 || obj1 != null && obj2 != null && obj1.equals(obj2);
    }
}
