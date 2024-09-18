package com.yjh.accessmeter.protocol.impl;

import com.yjh.accessmeter.common.Constant;
import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.common.result.ResultCodeEnum;
import com.yjh.accessmeter.common.utils.ByteUtil;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDevicePoint;
import com.yjh.accessmeter.netty.DLT645Message;
import com.yjh.accessmeter.protocol.ISensorProtocol;
import com.yjh.accessmeter.protocol.ProtocolEnum;
import com.yjh.accessmeter.protocol.ProtocolListener;
import com.yjh.accessmeter.protocol.ProtocolType;
import com.yjh.accessmeter.protocol.entity.ResultMete;
import com.yjh.accessmeter.protocol.impl.transport.MeterTcpClientManager;
import io.netty.channel.Channel;

import java.util.*;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/9/12
 * @since [产品/模块版本] （可选）
 */
@ProtocolType({ProtocolEnum.DLT645_97, ProtocolEnum.DLT645_07})
public class MeterProtocolImpl implements ISensorProtocol {

    private volatile boolean inited = false;

    @Override
    public ISensorProtocol init(List<IotDevice> devices) {
        inited = true;
        LOGGER.info("电表初始化成功");
        return this;
    }

    @Override
    public boolean isInit() {
        return inited;
    }

    @Override
    public List<ResultMete> send(IotDevice device, List<IotDevicePoint> devicePoints) {
        List<ResultMete> resultMeteList = new ArrayList<>();
        try {
            MeterTcpClientManager meterManager = MeterTcpClientManager.INSTANSE;
            Channel channel = meterManager.connect(device).channel();
            devicePoints.forEach(point -> {
                DLT645Message message = new DLT645Message();
                message.setAddress(device.getAddress());
                byte ctrl;
                if (ProtocolEnum.getEnum(device.getProtocolModel()) == ProtocolEnum.DLT645_97) {
                    ctrl = Constant.CONTROLL_CODE_REQUEST;
                } else {
                    ctrl = Constant.CONTROLL_CODE_REQUEST_2007;
                }
                message.setControlCode(ctrl);
                message.setDataType(ByteUtil.HexString2Bytes(point.getChannelNum()));
                String value = meterManager.sendAndGet(message, channel);
                ResultMete mete = new ResultMete();
                mete.setIotDeviceId(point.getIotDeviceId())
                        .setAddress(device.getAddress())
                        .setChannle(point.getChannelNum())
                        .setValue(value);
                resultMeteList.add(mete);
            });
            channel.close().sync();
            channel.flush();
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return resultMeteList;
    }

    @Override
    public void sendAsync(IotDevice device, ProtocolListener listener) {

    }

    @Override
    public Result sendControl(IotDevice device, Map<String, Object> params) {
        return new Result(ResultCodeEnum.CODE10009.getCode(), "协议不支持");
    }

}
