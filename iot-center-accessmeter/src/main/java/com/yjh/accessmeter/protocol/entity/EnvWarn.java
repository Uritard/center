/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/20
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EnvWarn extends EnvBase {
    /**
     * 设备位号
     */
    String pIndex;

    /**
     * 告警类型
     * 1：SF6报警
     * 2：防盗报警
     * 3：烟雾报警
     * 4：温度报警
     * 5：O3告警
     * 6：空调告警
     * 7：液位传感器告警
     * 8：水泵告警
     * 9：门禁告警
     * 10：风机告警
     * 11：湿度告警
     * 12：氧气告警
     * 13：CO告警
     * 14：CO2告警
     * 15：SO2告警
     * 16：CH4告警
     * 17：H2S告警
     * 18：H2告警
     * 19：风速告警
     * 20：液位告警
     */
    String warningType;

    /**
     * 告警级别
     * 1 2 3, 默认 2
     */
    String waringLevel;

    /**
     * 32位随机UUID大写，去横杠
     */
    String msgId;

}
