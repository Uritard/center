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

    @ApiModelProperty(value = "机器人信息")
    private Map<String,Object> robotInfo;

    @ApiModelProperty(value = "巡检任务完成度")
    private Float rate;
}

