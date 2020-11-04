package com.yjh.accessudp.commons.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhangye
 * @date 2018/7/31
 */
public class CollectionUtil {
    /**
     * 把数据库查出来字典表，list格式的转成map,id列为key,name列为value,id是数字
     *
     * @param list
     * @return
     */
    public static Map<Integer, String> dictList2map(List<Map<String, Object>> list) {
        Map<Integer, String> resultMap = new HashMap<Integer, String>();
        for (Map<String, Object> tmp : list) {
            resultMap.put((Integer) tmp.get("id"), (String) tmp.get("name"));
        }
        return resultMap;
    }

    /**
     * 把数据库查出来字典表，list格式的转成map,id列为key,name列为value,id是string
     *
     * @param list
     * @return
     */
    public static Map<String, String> dictList2mapString(List<Map<String, Object>> list) {
        Map<String, String> resultMap = new HashMap<String, String>();
        for (Map<String, Object> tmp : list) {
            resultMap.put((String) tmp.get("id"), (String) tmp.get("name"));
        }
        return resultMap;
    }
}
