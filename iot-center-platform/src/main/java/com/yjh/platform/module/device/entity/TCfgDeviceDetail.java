package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgDevice对象", description = "设备扩展")
public class TCfgDeviceDetail extends TCfgDevice {

    private static final long serialVersionUID = 1L;

    private String meteKind;

    private String meteName;
}
