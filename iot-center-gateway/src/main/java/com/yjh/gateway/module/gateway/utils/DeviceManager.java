package com.yjh.gateway.module.gateway.utils;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

public class DeviceManager {

    private Map<String, Map<Object, Object>> deviceInfoMap = new HashMap<>();
    private static DeviceManager instance = null;


    public static synchronized DeviceManager getInstance() {
        if (null == instance) {
            instance = new DeviceManager();
        }
        return instance;
    }

    public Map<Object, Object> getDeviceInfo(String deviceID, RedisTemplate redisTemplate) {
        /*Map<Object, Object> deviceInfo = deviceInfoMap.get(deviceID);
        if (null == deviceInfo || deviceInfo.size() == 0) {
            deviceInfo = redisTemplate.opsForHash().entries(String.format("dmp_device_base:%s", deviceID));
            deviceInfoMap.put(deviceID, deviceInfo);
        }*/
        Map<Object, Object> deviceInfo = redisTemplate.opsForHash().entries(String.format("dmp_device_base:%s", deviceID));
        return deviceInfo;
    }
}
