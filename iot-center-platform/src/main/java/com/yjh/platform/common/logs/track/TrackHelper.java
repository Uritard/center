package com.yjh.platform.common.logs.track;

import com.alibaba.fastjson.JSON;
import com.alibaba.ttl.TransmittableThreadLocal;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lichensi
 * @date 2020/12/9 16:32
 */
public interface TrackHelper {

    Logger logger = LoggerFactory.getLogger(TrackHelper.class);

    ThreadLocal<Map<String, String>> TRACK_LOCAL_THREAD = TransmittableThreadLocal.withInitial(() -> new HashMap(8));

    default Map<String, String> getRouteTrackInfo() {
        return TRACK_LOCAL_THREAD.get();
    }

    default String getRouteTrackInfo(String key) {
        return TRACK_LOCAL_THREAD.get().get(key);
    }

    default void setRouteTrackInfo(String key, String value) {
        if (!StringUtils.isBlank(key) && !StringUtils.isBlank(value)) {
            setCurrentTrackInfo(key, value);
            if (logger.isDebugEnabled()) {
                logger.debug("[yjh-track] 设置当前链路 {}={}", key, value);
            }
        }
    }

    default void setRouteTrackInfo(Map<String, String> trackInfo) {
        if (MapUtils.isNotEmpty(trackInfo)) {
            TRACK_LOCAL_THREAD.get().putAll(trackInfo);
        }
    }

    default String removeRouteTrackInfo(String key) {
        String value = this.getRouteTrackInfo(key);
        TRACK_LOCAL_THREAD.get().remove(key);
        if (logger.isDebugEnabled()) {
            logger.debug("[yjh-track] 清除当前链路 {}={}", key, value);
        }
        return value;
    }

    default Map<String, String> removeRouteTrackInfo() {
        Map<String, String> attachment = null;
        try {
            attachment = this.getRouteTrackInfo();
            TRACK_LOCAL_THREAD.remove();
            if (logger.isDebugEnabled()) {
                logger.debug("[yjh-track] >>> 清除当前链路 {}.", JSON.toJSONString(attachment));
            }
        } catch (Exception e) {
            logger.error("[yjh-track] >>> 清除当前链路异常: ", e);
        }
        return attachment;
    }

    static void removeTrackInfo() {
        TRACK_LOCAL_THREAD.remove();
    }

    static String setCurrentTrackInfo(String key, String value) {
        if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
            TRACK_LOCAL_THREAD.get().put(key, value);
            return value;
        }
        return null;
    }

    static void setCurrentTrackInfo(Map<String, String> trackInfo) {
        if (MapUtils.isNotEmpty(trackInfo)) {
            TRACK_LOCAL_THREAD.get().putAll(trackInfo);
        }
    }

    static String getCurrentTrackInfo(String key) {
        return TRACK_LOCAL_THREAD.get().get(key);
    }

    static Map<String, String> getCurrentTrackInfo() {
        return TRACK_LOCAL_THREAD.get();
    }
}
