package com.yjh.platform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 枚举缓存
 * @author 丫C
 * @date 2023/6/12
 */
public class EnumCache {
    /**
     * 以枚举任意值构建的缓存结构
     */
    static final Map<Class<? extends Enum>, Map<Object, Enum>> CACHE_BY_VALUE = new ConcurrentHashMap<>();
    /**
     * 以枚举名称构建的缓存结构
     */
    static final Map<Class<? extends Enum>, Map<Object, Enum>> CACHE_BY_NAME = new ConcurrentHashMap<>();
    /**
     * 枚举静态块加载标识缓存结构
     */
    static final Map<Class<? extends Enum>, Boolean> LOADED = new ConcurrentHashMap<>();

    /**
     * 以枚举名称构建缓存，在枚举的静态块里面调用
     * @param clazz
     * @param es
     */
    public static <E extends Enum> void registerByName(Class<E> clazz, E[] es) {
        Map<Object, Enum> map = new ConcurrentHashMap<>();
        for (E e : es) {
            map.put(e.name(), e);
        }
        CACHE_BY_NAME.put(clazz, map);
    }

}
