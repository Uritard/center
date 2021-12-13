package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YChen
 * @date 2021/12/11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotInspectionTmp对象", description = "机器人巡检点拓展信息表")
public class TRobotInspectionTmp extends TRobotInspection implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机器人主设备名称")
    private String mainDeviceName;
}
