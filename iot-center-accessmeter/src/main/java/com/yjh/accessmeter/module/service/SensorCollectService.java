/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.service;

import com.yjh.accessmeter.common.result.BusinessException;
import com.yjh.accessmeter.common.result.ResultCodeEnum;
import com.yjh.accessmeter.module.dao.TIotDeviceDao;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.feign.PlatformProxy;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.SensorProtocolFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    @Resource
    private PlatformProxy platformProxy;

    private static final Map<Integer, DataCollectTask> COLLECT_TASK_MAP = new HashMap<>(8);

    /**
     * 初始化连接
     */
    @PostConstruct
    public void initAllMeter() {
        try {
            List<IotDevice> iotDeviceList = iotDeviceDao.selectAll();
            Map<ProtocolEnum, List<IotDevice>> protocolSet =
                iotDeviceList.stream().collect(Collectors.groupingBy(i -> ProtocolEnum.getEnum(i.getProtocolModel())));

            protocolSet.forEach((k, v) -> {
                if (k.getType() == 0) {
                    log.info("协议类型无需主动建立连接: {}", k);
                } else {
                    ISensorProtocol sensorProtocol = SensorProtocolFactory.CREATE.createProtocol(k);
                    if (!sensorProtocol.isInit()) {
                        sensorProtocol.init(v);
                    }
                }
            });

            Map<Integer, List<IotDevice>> freSet = iotDeviceList.stream().filter(
                    i -> i.getCollectionFrequency() != null && i.getCollectionFrequency() > 0
                        && Optional.ofNullable(ProtocolEnum.getEnum(i.getProtocolModel())).map(ProtocolEnum::getType).orElse(0) != 0)
                .collect(Collectors.groupingBy(IotDevice::getCollectionFrequency));

            freSet.forEach((k, v) -> taskScheduler.scheduleAtFixedRate(
                COLLECT_TASK_MAP.compute(k, (k1, v1) -> new DataCollectTask(v, iotDeviceDao, asyncExecutor, platformProxy)),
                Instant.ofEpochMilli(System.currentTimeMillis() + 15000), Duration.ofMinutes(k)));
        } catch (Exception e) {
            log.error("初始化设备连接出错", e);
        }

    }

    /**
     * 新增设备接入，创建后只需接入一次
     */
    public boolean add(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.info("没有查到设备配置 id:{}", id);
            throw new BusinessException(ResultCodeEnum.CODE10005.getCode(), "没有查到设备配置");
        }
        return add(device, true);
    }

    public boolean add(IotDevice device, boolean collectImmediate) {
        DataCollectTask dataCollectTask = COLLECT_TASK_MAP.get(device.getCollectionFrequency());
        if (dataCollectTask == null) {
            synchronized (this) {
                ProtocolEnum protocolEnum = ProtocolEnum.getEnum(device.getProtocolModel());
                if (protocolEnum.getType() == 0) {
                    log.info("协议类型无需主动建立连接: {}", protocolEnum);
                    return true;
                }
                ISensorProtocol sensorProtocol =
                    SensorProtocolFactory.CREATE.createProtocol(ProtocolEnum.getEnum(device.getProtocolModel()));
                if (!sensorProtocol.isInit()) {
                    sensorProtocol.init(Collections.singletonList(device));
                }

                COLLECT_TASK_MAP.compute(device.getCollectionFrequency(), (k1, v1) -> {
                    if (v1 == null) {
                        return new DataCollectTask(Collections.singletonList(device), iotDeviceDao, asyncExecutor, platformProxy);
                    } else {
                        v1.addDevice(device, collectImmediate);
                        return v1;
                    }
                });
            }
        } else {
            dataCollectTask.addDevice(device, collectImmediate);
        }
        log.info("新增数据采集 device:{}", device);

        return true;
    }

    public boolean delete(Long id) {
        IotDevice device = new IotDevice().setId(id);
        return delete(device);
    }

    public boolean delete(IotDevice device) {
        AtomicBoolean deleted = new AtomicBoolean(false);
        COLLECT_TASK_MAP.forEach((k, v) -> {
            if (v.containsDevice(device)) {
                v.removeDevice(device);
                deleted.set(true);
                log.info("删除了数据采集: {}, {}", k, device);
            }
        });
        log.info("已删除数据采集 device:{}", device);
        return deleted.get();
    }

    public boolean update(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.info("没有查到设备配置 id:{}", id);
            throw new BusinessException(ResultCodeEnum.CODE10005.getCode(), "没有查到设备配置");
        }
        log.info("更新数据采集 device:{}", device);
        delete(device);
        return add(device, false);
    }

    public void collect(Long id) {
        IotDevice device = iotDeviceDao.selectByPrimaryKey(id);
        if (device == null) {
            log.info("没有查到设备配置 id:{}", id);
            throw new BusinessException(ResultCodeEnum.CODE10005.getCode(), "没有查到设备配置");
        }

        DataCollectTask dataCollectTask = COLLECT_TASK_MAP.get(device.getCollectionFrequency());
        if (dataCollectTask != null) {
            dataCollectTask.collectMeter(Collections.singletonList(device));
        }
    }

}
