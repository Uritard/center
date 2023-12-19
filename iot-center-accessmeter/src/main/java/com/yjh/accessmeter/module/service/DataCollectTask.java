/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.service;

import com.yjh.accessmeter.module.dao.TIotDeviceDao;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDeviceData;
import com.yjh.accessmeter.module.device.entity.IotDevicePoint;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.SensorProtocolFactory;
import com.yjh.accessmeter.protocol.entity.ResultMete;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.*;
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

    final TIotDeviceDao iotDeviceDao;
    final ThreadPoolTaskExecutor asyncExecutor;
    final Set<IotDevice> devices;

    public DataCollectTask(List<IotDevice> devices, TIotDeviceDao iotDeviceDao, ThreadPoolTaskExecutor asyncExecutor) {
        this.devices = new HashSet<>(devices);
        this.iotDeviceDao = iotDeviceDao;
        this.asyncExecutor = asyncExecutor;
    }

    @Override
    public void run() {
        Map<String, List<IotDevice>> deviceGroup = devices.stream().collect(Collectors.groupingBy(d -> d.getIp() + ":" + d.getPort()));
        for (List<IotDevice> des : deviceGroup.values()) {
            asyncExecutor.execute(() -> collectMeter(des));
        }
    }

    public void collectMeter(List<IotDevice> devices) {
        List<IotDeviceData> deviceDataList = new ArrayList<>();
        for (IotDevice device : devices) {
            try {
                List<IotDevicePoint> devicePoints = iotDeviceDao.selectPointByDeviceId(device.getId());

                ProtocolEnum protocolEnum = ProtocolEnum.getEnum(device.getProtocolModel());
                if (protocolEnum.getType() == 0) {
                    log.warn("协议类型无需主动建立连接: {}, device: {}", protocolEnum, device);
                }

                ISensorProtocol sensorProtocol = SensorProtocolFactory.CREATE.createProtocol(protocolEnum);
                if (sensorProtocol == null) {
                    log.error("设备协议未实现，请检查协议是否正确: {}, device: {}", protocolEnum, device);
                    continue;
                }
                List<ResultMete> resultMetes = sensorProtocol.send(device, devicePoints);

                IotDeviceData baseData = new IotDeviceData();
                baseData.setIotDeviceId(device.getId()).setIotDeviceName(device.getDeviceName()).setUnit(device.getUnit()).setPointId(0L)
                    .setPointName(device.getDeviceName()).setUpRegionId(device.getUpRegionId()).setIotDeviceType(device.getIotDeviceType())
                    .setCreateTime(new Date());
                // 结果入库
                if (devicePoints.isEmpty()) {
                    baseData.setValue(Optional.ofNullable(resultMetes.get(0)).map(ResultMete::getValue).map(String::valueOf).orElse(""));
                    deviceDataList.add(baseData);
                } else {
                    devicePoints.forEach(d -> {
                        int chanNum = d.getChannelNum();
                        IotDeviceData data = new IotDeviceData();
                        BeanUtils.copyProperties(baseData, data);
                        data.setPointId(d.getId()).setPointName(d.getPointName()).setUnit(d.getUnit());
                        resultMetes.stream().filter(m -> chanNum == m.getChannle()).findFirst()
                            .ifPresent(m -> data.setValue(String.valueOf(m.getValue())));

                        deviceDataList.add(data);
                    });
                }
            } catch (Exception e) {
                log.error("采集设备数据失败: {}", device, e);
            }
        }
        if (!deviceDataList.isEmpty()) {
            iotDeviceDao.batchInsertData(deviceDataList);
        }
    }

    public void addDevice(IotDevice device) {
        devices.add(device);
        // 新增设备立即采集一次数据
        collectMeter(Collections.singletonList(device));
    }

    public void removeDevice(IotDevice device) {
        devices.remove(device);
    }
}
