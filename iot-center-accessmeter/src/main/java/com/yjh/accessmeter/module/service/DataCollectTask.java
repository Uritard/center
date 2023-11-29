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
import com.yjh.accessmeter.protocol.ResultMete;
import com.yjh.accessmeter.protocol.SensorProtocolFactory;
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
            ISensorProtocol sensorProtocol = SensorProtocolFactory.CREATE.createProtocol(ProtocolEnum.getEnum(device.getIotDeviceType()));
            List<ResultMete> resultMetes = sensorProtocol.send(device);

            List<IotDevicePoint> devicePoints = iotDeviceDao.selectPointByDeviceId(device.getDeviceId());
            IotDeviceData baseData = new IotDeviceData();
            baseData.setIotDeviceId(device.getDeviceId()).setIotDeviceName(device.getDeviceName()).setUnit(device.getUnit()).setPointId(0L)
                .setPointName(device.getDeviceName()).setUpRegionId(device.getUpRegionId())
                .setMagnificationCoefficient(device.getMagnificationCoefficient()).setCreateTime(new Date());
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
        }
        iotDeviceDao.batchInsertData(deviceDataList);
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
