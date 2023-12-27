/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.service;

import com.alibaba.fastjson.JSON;
import com.yjh.accessmeter.module.dao.TIotDeviceDao;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDeviceDataEx;
import com.yjh.accessmeter.module.device.entity.IotDevicePoint;
import com.yjh.accessmeter.module.feign.PlatformProxy;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.SensorProtocolFactory;
import com.yjh.accessmeter.protocol.entity.ResultMete;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/29
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class DataCollectTask implements Runnable {

    private final TIotDeviceDao iotDeviceDao;
    private final ThreadPoolTaskExecutor asyncExecutor;
    private final PlatformProxy platformProxy;
    private final Map<Long, IotDevice> devices;

    public DataCollectTask(List<IotDevice> devices, TIotDeviceDao iotDeviceDao, ThreadPoolTaskExecutor asyncExecutor,
        PlatformProxy platformProxy) {
        this.devices = devices.stream().collect(Collectors.toMap(IotDevice::getId, Function.identity(), (e1, e2) -> e2));
        this.iotDeviceDao = iotDeviceDao;
        this.asyncExecutor = asyncExecutor;
        this.platformProxy = platformProxy;
    }

    @Override
    public void run() {
        try {
            log.info("开始数据采集，devices: {}", JSON.toJSONString(devices.keySet()));
            Map<String, List<IotDevice>> deviceGroup =
                devices.values().stream().collect(Collectors.groupingBy(d -> d.getIp() + ":" + d.getPort()));
            for (List<IotDevice> des : deviceGroup.values()) {
                asyncExecutor.execute(() -> collectMeter(des));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void collectMeter(List<IotDevice> devices) {
        List<IotDeviceDataEx> deviceDataList = new ArrayList<>();
        for (IotDevice device : devices) {
            try {
                List<IotDevicePoint> devicePoints = iotDeviceDao.selectPointByDeviceId(device.getId());

                ProtocolEnum protocolEnum = ProtocolEnum.getEnum(device.getProtocolModel());
                ISensorProtocol sensorProtocol = SensorProtocolFactory.CREATE.createProtocol(protocolEnum);
                if (sensorProtocol == null) {
                    log.warn("协议类型无需主动建立连接或未实现，请检查协议是否正确: {}, device: {}", protocolEnum, device);
                    continue;
                }
                List<ResultMete> resultMetes = sensorProtocol.send(device, devicePoints);

                // 结果处理预备入库
                resultParseToInsert(deviceDataList, device, devicePoints, resultMetes);
            } catch (Exception e) {
                log.error("采集设备数据失败: {}", device, e);
            }
        }
        if (!deviceDataList.isEmpty()) {
            try {
                iotDeviceDao.batchInsertData(deviceDataList);

                platformProxy.uploadToRedis(deviceDataList);
            } catch (Exception e) {
                log.error("设备采集数据入库失败: {}", JSON.toJSONString(deviceDataList), e);
            }

        }
    }

    /**
     * 采集结果处理
     */
    private void resultParseToInsert(List<IotDeviceDataEx> deviceDataList, IotDevice device, List<IotDevicePoint> devicePoints,
        List<ResultMete> resultMetes) {
        IotDeviceDataEx baseData = new IotDeviceDataEx();
        baseData.setIp(device.getIp()).setPort(device.getPort()).setDeviceId(device.getDeviceId()).setAddress(device.getAddress())
            .setControllable(device.getControllable()).setUpRegionName(device.getUpRegionName()).setIotDeviceId(device.getId())
            .setIotDeviceName(device.getDeviceName()).setUnit(device.getUnit()).setPointId(0L).setPointName(device.getDeviceName())
            .setUpRegionId(device.getUpRegionId()).setIotDeviceType(device.getIotDeviceType()).setCreateTime(new Date());

        if (devicePoints.isEmpty()) {
            baseData.setValue(Optional.ofNullable(resultMetes.get(0)).map(ResultMete::getValue).map(String::valueOf).orElse(""));
            deviceDataList.add(baseData);
        } else {
            Map<String, IotDevicePoint> pointMap =
                devicePoints.stream().collect(Collectors.toMap(IotDevicePoint::getChannelNum, Function.identity(), (e1, e2) -> e1));
            resultMetes.forEach(m -> {
                String chanNum = m.getChannle();
                IotDeviceDataEx data = new IotDeviceDataEx();
                BeanUtils.copyProperties(baseData, data);
                IotDevicePoint point = pointMap.get(chanNum);
                data.setValue(m.getValue());
                data.setChannelNum(chanNum);
                if (point != null) {
                    data.setPointId(point.getId()).setPointName(point.getPointName()).setUnit(point.getUnit());
                    deviceDataList.add(data);
                } else {
                    log.error("查询到数据 Channle 不匹配: {}, {}", m, device);
                }
            });
        }
    }

    public void addDevice(IotDevice device) {
        addDevice(device, true);
    }

    public void addDevice(IotDevice device, boolean collectImmediate) {
        devices.put(device.getId(), device);
        if (collectImmediate) {
            // 新增设备立即采集一次数据
            collectMeter(Collections.singletonList(device));
        }
    }

    public boolean removeDevice(IotDevice device) {
        IotDevice d = devices.remove(device.getId());
        return d != null;
    }

    public boolean containsDevice(IotDevice device) {
        return devices.containsKey(device.getId());
    }
}
