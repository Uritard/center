/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol;

import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDevicePoint;
import com.yjh.accessmeter.protocol.entity.ResultMete;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/27
 * @since [产品/模块版本] （可选）
 */
public interface ISensorProtocol {
    Logger LOGGER = LoggerFactory.getLogger(ISensorProtocol.class);

    ISensorProtocol init(List<IotDevice> devices);

    boolean isInit();

    /**
     * 同步数据采集
     */
    List<ResultMete> send(IotDevice device, List<IotDevicePoint> devicePoints);

    /**
     * 异步数据采集
     */
    void sendAsync(IotDevice device, ProtocolListener listener);

    /**
     * 控制指令下发
     */
    Result sendControl(IotDevice device, Map<String, Object> params);

    default String round(String numberStr, int scale) {
        BigDecimal decimal;
        if (StringUtils.isEmpty(numberStr)) {
            decimal = BigDecimal.ZERO;
        } else {
            decimal = new BigDecimal(numberStr);
        }
        return decimal.setScale(scale, RoundingMode.HALF_UP).toPlainString();
    }
}
