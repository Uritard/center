package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/10/29 - 16:19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "EnvironmentResult对象", description = "巡检记录报表-环境监测")
public class EnvironmentResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "环境设备名称")
    private String envDeviceName;
    @ApiModelProperty(value = "设备类型名称")
    private String envDeviceTypeName;
    @ApiModelProperty(value = "检测值")
    private String envDeviceVal;
    @ApiModelProperty(value = "状态")
    private String statusName;
    @ApiModelProperty(value = "位置")
    private String outInName;
}
