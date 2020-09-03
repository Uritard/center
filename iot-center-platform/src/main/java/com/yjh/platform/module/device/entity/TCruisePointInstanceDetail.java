package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/9/3
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePointInstance对象扩充", description = "巡检点实例表的扩充")
public class TCruisePointInstanceDetail extends TCruisePointInstance{

    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "设备类型")
    private String deviceTypeName;
    @ApiModelProperty(value = "实物ID")
    private String realId;
    @ApiModelProperty(value ="设备部位")
    private String customName;
    @ApiModelProperty(value = "设备类型-ID")
    private Integer deviceType;
}
