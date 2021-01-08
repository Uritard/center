package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "设备基本类型", description = "巡视结果分析报表-Element")
public class DeviceBaseReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备区域")
    private String deviceRegion;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "设备编码")
    private String deviceCode;
    @ApiModelProperty(value = "设备类型")
    private String deviceTypeName;
    @ApiModelProperty(value = "设备部位")
    private String customName;
    @ApiModelProperty(value = "测点数量")
    private Integer meteCounts;
}
