package com.yjh.platform.common.logs.track;

import org.slf4j.MDC;

/**
 * @author lichensi
 * @date 2020/12/9 16:32
 */
public interface MDCHelper {
    static void put(String key, String val) {
        MDC.put(key, val);
    }

    static void remove(String key) {
        MDC.remove(key);
    }
}
