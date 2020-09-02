package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.List;
import java.io.Serializable;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "DeviceTypeTree", description = "设备类型-模型树")
public class DeviceTypeTree implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "层级标志")
    private int level=1;

//    @ApiModelProperty(value = "设备类型")
//    private Integer deviceType;
//
//    @ApiModelProperty(value = "设备类型名")
//    private String deviceTypeName;

    private Integer ID;
    private String Label;

    @ApiModelProperty(value = "模板子节点")
    private List<MeteModel> children;
}
