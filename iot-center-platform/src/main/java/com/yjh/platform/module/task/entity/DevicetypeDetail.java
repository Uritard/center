package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/10/13 - 19:33
 */@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TWarnInfoDetail对象", description = "告警信息表扩展")
public class DevicetypeDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备类型")
    private String deviceTypeName;
    @ApiModelProperty(value = "告警个数")
    private Number count;
}
