package com.yjh.platform.common.utils;

import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lqh
 * @since 2020/12/8
 */
@SuppressWarnings("all")
public class ResultHandleUtils<K,V> implements ResultHandler<Map<K,V>> {
    private final Map<K,V> mappedResults = new HashMap<>();

    @Override
    public void handleResult(ResultContext context) {
        Map map = (Map) context.getResultObject();
        mappedResults.put((K)map.get("key"), (V)map.get("value"));
    }

    public Map<K,V> getMappedResults() {
        return mappedResults;
    }
}
