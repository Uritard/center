package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视监控-机器人/摄像头信息表", description = "巡视设备信息表")
public class CruiseDeviceInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "摄像头信息")
    private CameraCruiseInfo cameraCruiseInfo;
    @ApiModelProperty(value = "机器人信息")
    private List<RobotCruiseInfo> robotCruiseInfos;
}
