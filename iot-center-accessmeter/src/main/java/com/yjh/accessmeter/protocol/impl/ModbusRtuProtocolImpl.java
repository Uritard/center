/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.impl;

import com.alibaba.fastjson.JSON;
import com.serotonin.modbus4j.BatchRead;
import com.serotonin.modbus4j.BatchResults;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.code.DataType;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDevicePoint;
import com.yjh.accessmeter.protocol.*;
import com.yjh.accessmeter.protocol.entity.ModbusExtend;
import com.yjh.accessmeter.protocol.entity.ResultMete;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/27
 * @since [产品/模块版本] （可选）
 */
@ProtocolType({ProtocolEnum.MODBUS_RTU, ProtocolEnum.MODBUS_TCP})
public class ModbusRtuProtocolImpl implements ISensorProtocol {
    private static final Map<String, ModbusMaster> MASTER_MAP = new ConcurrentHashMap<>(16);
    private ModbusFactory factory = new ModbusFactory();

    @Override
    public ISensorProtocol init(List<IotDevice> devices) {
        if (CollectionUtils.isEmpty(devices)) {
            LOGGER.warn("devices is null, not append!");
        }

        for (IotDevice device : devices) {
            master(device);
        }

        return null;
    }

    @Override
    public List<ResultMete> send(IotDevice device, List<IotDevicePoint> devicePoints) {
        List<ResultMete> metes = new ArrayList<>();
        try {
            ModbusMaster master = master(device);
            BatchRead<Number> batch = new BatchRead<>();

            for (IotDevicePoint point : devicePoints) {
                ModbusExtend ext = JSON.parseObject(point.getExtend(), ModbusExtend.class);
                int dataType = ext.getDataType() == null || ext.getDataType() == 0 ? DataType.TWO_BYTE_INT_SIGNED : ext.getDataType();
                batch.addLocator(point.getChannelNum(), BaseLocator.holdingRegister(ext.getSlaveId(), ext.getStart(), dataType));
            }

            BatchResults<Number> results = master.send(batch);
            LOGGER.info("BatchResults: {}", results);

            for (IotDevicePoint point : devicePoints) {
                ResultMete mete = new ResultMete();
                int channel = point.getChannelNum();

                Number ret = (Number)results.getValue(channel);
                double value = ret.doubleValue();
                if (device.getMagnificationCoefficient() != null) {
                    value = device.getMagnificationCoefficient() * value;
                }
                mete.setChannle(channel).setValue(round(String.valueOf(value), 2));
                metes.add(mete);
            }
        } catch (Exception e) {
            LOGGER.error("请求数据失败：{}", device, e);
        }

        return metes;
    }

    @Override
    public void sendAsync(IotDevice device, ProtocolListener listener) {

    }

    private ModbusMaster master(IotDevice device) {
        String key = device.getIp() + ":" + device.getPort() + ":" + device.getProtocolModel();
        return MASTER_MAP.computeIfAbsent(key, k -> {
            IpParameters params = new IpParameters();
            params.setHost(device.getIp());
            params.setPort(device.getPort());
            // MODBUS_RTU 协议需要校验码，TCP 不需要
            if (ProtocolEnum.getEnum(device.getProtocolModel()) == ProtocolEnum.MODBUS_RTU) {
                params.setEncapsulated(true);
            }
            ModbusMaster master = factory.createTcpMaster(params, false);
            master.setTimeout(4000);
            try {
                master.init();
                return master;
            } catch (ModbusInitException e) {
                LOGGER.error("modbus init error", e);
            }
            return null;
        });
    }

    public static void main(String[] args) {
        ModbusRtuProtocolImpl modbusRtuProtocol = new ModbusRtuProtocolImpl();
        IotDevice device = new IotDevice();
        device.setIp("172.24.49.234");
        device.setPort(502);

        modbusRtuProtocol.init(Collections.singletonList(device));

        IotDevicePoint point = new IotDevicePoint();
        point.setChannelNum(1).setExtend("{\"slaveId\": 1,\"start\": \"67\"}");

        IotDevicePoint point2 = new IotDevicePoint().setChannelNum(2).setExtend("{\"slaveId\": 1,\"start\": \"68\"}");
        IotDevicePoint point3 = new IotDevicePoint().setChannelNum(3).setExtend("{\"slaveId\": 1,\"start\": \"69\"}");
        IotDevicePoint point4 = new IotDevicePoint().setChannelNum(4).setExtend("{\"slaveId\": 1,\"start\": \"70\"}");

        List<ResultMete> metes = modbusRtuProtocol.send(device, Arrays.asList(point, point2, point3, point4));
        System.out.println(JSON.toJSONString(metes));
    }
}
