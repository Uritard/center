package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author: lqh
 * @Date: 2023/02/07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SilentInfo对象", description = "静默监视数据")
public class SilentInfo {
    @ApiModelProperty(value = "巡视设备编码")
    private String patrolDeviceCode;
    @ApiModelProperty(value = "设备点位名称")
    private String deviceName;
    @ApiModelProperty(value = "设备点位 ID")
    private String deviceId;
    @ApiModelProperty(value = "时间")
    private String time;
    @ApiModelProperty(value = "图像框")
    private String rectangle;
    @ApiModelProperty(value = "采集文件类型")
    private String fileType;
    @ApiModelProperty(value = "文件路径")
    private String filePath;
    @ApiModelProperty(value = "静默监视类型")
    private String monitorType;
}
