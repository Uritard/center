package com.yjh.accessrobot.module.command.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author Zyy
 * @date 2025/5/30
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "充电区域信息", description = "充电区域信息")
public class ChargingAreaInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 机器人id
     */
    private Long robotId;

    /**
     * 充电区域坐标
     * 顺时针坐标
     * P1（靠墙）、P2（靠墙）、P3、P4，
     * 4 点坐标格式为：
     * x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4
     *
     */
    private String coordinates;
}
