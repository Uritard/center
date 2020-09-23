package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视监控-机器人巡检点信息表", description = "机器人&巡检点信息表")
public class RobotCruiseInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    private String type="robot";

    @ApiModelProperty(value ="机器人ID")
    private Long robotId;
    @ApiModelProperty(value = "机器人名称")
    private String robotName;
    @ApiModelProperty(value = "电量")
    private String elePower;
    @ApiModelProperty(value = "连接信号")
    private String tranSignal;
    @ApiModelProperty(value = "机器人类型")
    private Integer robotPosition;
    @ApiModelProperty(value = "机器人类型名称")
    private String robotPositionName;
    @ApiModelProperty(value = "巡检任务完成度")
    private Float rate;
}

