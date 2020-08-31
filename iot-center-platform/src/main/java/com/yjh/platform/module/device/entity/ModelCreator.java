package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
/**
 * @author czh
 * @since 2020-08-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ModelInfo", description = "模板信息表")
public class ModelCreator implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "模板ID")
    private Long modelId;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "模板名")
    private String model_name;

    @ApiModelProperty(value = "测点ID")
    List<Long> meteIds;

}
