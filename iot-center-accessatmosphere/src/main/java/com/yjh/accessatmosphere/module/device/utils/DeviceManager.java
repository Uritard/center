package com.yjh.accessatmosphere.module.device.utils;

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
}
