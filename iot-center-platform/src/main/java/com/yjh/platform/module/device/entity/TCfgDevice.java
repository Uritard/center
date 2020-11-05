package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgDevice对象", description = "设备表")
public class TCfgDevice implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备编号")
    private String deviceId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "设备编码+设备测点地址")
    private String deviceCode;


    @ApiModelProperty(value = "项目id")
    private String stationId;

    @ApiModelProperty(value = "关联设备编码")
    private String relationCode;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "描述")
    private String remark;


}
