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
import com.serotonin.modbus4j.exception.ErrorResponseException;
import com.serotonin.modbus4j.exception.ModbusInitException;
import com.serotonin.modbus4j.exception.ModbusTransportException;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.protocol.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/27
 * @since [产品/模块版本] （可选）
 */
@ProtocolType(ProtocolEnum.MODBUS_RTU)
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
    public List<ResultMete> send(IotDevice device) {
        List<ResultMete> metes = new ArrayList<>();
        try {
            ModbusMaster master = master(device);
            BatchRead<Integer> batch = new BatchRead<>();

            batch.addLocator(0, BaseLocator.holdingRegister(1, 0x43, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(1, BaseLocator.holdingRegister(1, 0x44, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(2, BaseLocator.holdingRegister(1, 0x45, DataType.TWO_BYTE_INT_SIGNED));
            batch.addLocator(3, BaseLocator.holdingRegister(1, 0x46, DataType.TWO_BYTE_INT_SIGNED));
            BatchResults<Integer> results = master.send(batch);
            LOGGER.info("BatchResults: {}", JSON.toJSONString(results));
            ResultMete mete1 = new ResultMete();
            mete1.setChannle(1).setValue(Double.valueOf(results.getIntValue(0)));
            metes.add(mete1);

            ResultMete mete2 = new ResultMete();
            mete2.setChannle(2).setValue(Double.valueOf(results.getIntValue(1)));
            metes.add(mete2);

            ResultMete mete3 = new ResultMete();
            mete3.setChannle(3).setValue(Double.valueOf(results.getIntValue(2)));
            metes.add(mete3);

            ResultMete mete4 = new ResultMete();
            mete4.setChannle(4).setValue(Double.valueOf(results.getIntValue(3)));
            metes.add(mete4);
        } catch (ModbusTransportException | ErrorResponseException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return metes;
    }

    @Override
    public void sendAsync(IotDevice device, ProtocolListener listener) {

    }

    private ModbusMaster master(IotDevice device) {
        String key = device.getIp() + ":" + device.getPort();
        return MASTER_MAP.computeIfAbsent(key, k -> {
            IpParameters params = new IpParameters();
            params.setHost(device.getIp());
            params.setPort(device.getPort());
            ModbusMaster master = factory.createTcpMaster(params, true);
            master.setTimeout(4000);
            master.setRetries(1);
            try {
                master.init();
                return master;
            } catch (ModbusInitException e) {
                LOGGER.error("modbus init error", e);
            }
            return null;
        });
    }
}
