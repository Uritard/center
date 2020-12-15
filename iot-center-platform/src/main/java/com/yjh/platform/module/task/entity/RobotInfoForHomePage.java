package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/12/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "机器人信息", description = "机器人信息")
public class RobotInfoForHomePage {

    private String  robotFactoryName;

    private String robotCode;

    private String typeName;

    private String position;

    private String mileage;

    private String onlineState;

    private String batteryLevel;

    private String controlModel;

    private String commissionDate;
}
