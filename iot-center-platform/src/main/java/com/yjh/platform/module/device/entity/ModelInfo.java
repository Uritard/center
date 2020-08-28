package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import  java.util.List;

/**
 * @author czh
 * @since 2020-08-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ModelInfo", description = "模板信息表")
public class ModelInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模板基本信息")
    private TStdMeteModel model;


    @ApiModelProperty(value = "测点模板名称")
    private  List<MeteInfo> meteInfo;
}
