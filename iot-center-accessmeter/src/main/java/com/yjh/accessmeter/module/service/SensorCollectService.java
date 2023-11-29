/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.service;

import com.yjh.accessmeter.module.dao.TIotDeviceDao;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.SensorProtocolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/27
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
public class SensorCollectService {
    @Resource
    private ThreadPoolTaskScheduler taskScheduler;
    @Resource
    private ThreadPoolTaskExecutor asyncExecutor;
    @Resource
    private TIotDeviceDao iotDeviceDao;

    private static final Map<Integer, DataCollectTask> COLLECT_TASK_MAP = new HashMap<>(8);

    @PostConstruct
    public void initAllMeter() {
        List<IotDevice> iotDeviceList = iotDeviceDao.selectAll();
        Map<ProtocolEnum, List<IotDevice>> protocolSet =
            iotDeviceList.stream().collect(Collectors.groupingBy(i -> ProtocolEnum.getEnum(i.getIotDeviceType())));

        protocolSet.forEach((k, v) -> {
            ISensorProtocol sensorProtocol = SensorProtocolFactory.CREATE.createProtocol(k);
            sensorProtocol.init(v);
        });

        Map<Integer, List<IotDevice>> freSet = iotDeviceList.stream().collect(Collectors.groupingBy(IotDevice::getCollectionFrequency));

        freSet.forEach((k, v) -> taskScheduler.scheduleAtFixedRate(
            COLLECT_TASK_MAP.compute(k, (k1, v1) -> new DataCollectTask(v, iotDeviceDao, asyncExecutor)),
            Instant.ofEpochMilli(System.currentTimeMillis() + 15000), Duration.ofMinutes(k)));

    }

    @Async
    public void add(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.info("没有查到设备配置 id:{}", id);
            return;
        }
        DataCollectTask dataCollectTask = COLLECT_TASK_MAP.get(device.getCollectionFrequency());
        if (dataCollectTask == null) {
            ISensorProtocol sensorProtocol = SensorProtocolFactory.CREATE.createProtocol(ProtocolEnum.getEnum(device.getIotDeviceType()));
            sensorProtocol.init(Collections.singletonList(device));

            dataCollectTask = COLLECT_TASK_MAP.compute(device.getCollectionFrequency(),
                (k1, v1) -> new DataCollectTask(Collections.singletonList(device), iotDeviceDao, asyncExecutor));
            dataCollectTask.addDevice(device);
        }
    }

    public void delete(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.info("没有查到设备配置 id:{}", id);
            return;
        }
        DataCollectTask dataCollectTask = COLLECT_TASK_MAP.get(device.getCollectionFrequency());
        if (dataCollectTask != null) {
            dataCollectTask.removeDevice(device);
        }
        log.info("已删除数据采集 device:{}", device);
    }

    public void collect(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.info("没有查到设备配置 id:{}", id);
            return;
        }
        DataCollectTask dataCollectTask = COLLECT_TASK_MAP.get(device.getCollectionFrequency());
        if (dataCollectTask != null) {
            dataCollectTask.collectMeter(Collections.singletonList(device));
        }
    }

}
