package com.yjh.platform.common.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/11
 * @since [产品/模块版本] （可选）
 */
public class Object2List {

    public static <K, V> List<Map<K, V>> castListMap(Object obj, Class<K> kCalzz, Class<V> vCalzz) {
        List<Map<K, V>> result = new ArrayList<>();
        if (obj instanceof List<?>) {
            for (Object mapObj : (List<?>) obj) {
                if (mapObj instanceof Map<?, ?>) {
                    Map<K, V> map = new HashMap<>(16);
                    for (Map.Entry<?, ?> entry : ((Map<?, ?>) mapObj).entrySet()) {
                        map.put(kCalzz.cast(entry.getKey()), vCalzz.cast(entry.getValue()));
                    }
                    result.add(map);
                }
            }
            return result;
        }
        return null;
    }
}
