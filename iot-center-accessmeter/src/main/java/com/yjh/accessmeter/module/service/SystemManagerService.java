/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.service;

import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.module.dao.TIotDeviceDao;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.SensorProtocolFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/20
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class SystemManagerService {

    @Resource
    private TIotDeviceDao iotDeviceDao;

    private static Map<String, String> meterWarnTypeMap = new HashMap<>(32);

    @PostConstruct
    public void updateSystemConfig() {
        try {
            String meterWarnAddr = iotDeviceDao.getSystemConfig("otherConfig", "meterWarnAddr");
            if (StringUtils.isNotEmpty(meterWarnAddr)) {
                Constant.meterWarnAddr = meterWarnAddr;
            }

            String meterWarnType = iotDeviceDao.getSystemConfig("otherConfig", "meterWarnType");
            if (StringUtils.isNotEmpty(meterWarnType)) {
                String[] warnTypes = StringUtils.split(meterWarnType, ",");
                Map<String, String> m = Arrays.stream(warnTypes)
                                              .filter(e -> StringUtils.contains(e, ":"))
                                              .map(e -> e.split(":"))
                                              .collect(Collectors.toMap(e -> e[0], e -> e[1] + "告警", (e1, e2) -> e1));
                meterWarnTypeMap.putAll(m);
            }
        } catch (Exception e) {
            log.error("初始化设备连接出错", e);
        }

    }

    public String getWarnName(String type) {
        return meterWarnTypeMap.getOrDefault(type, "设备告警");
    }
}
