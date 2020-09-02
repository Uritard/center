package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "MeteInfo", description = "部位信息表")

public class CustomInfo implements Serializable {
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "部位类型")
    private int customType;


    @ApiModelProperty(value = "部位类型名称")
    private String customTypeName;


}