package com.yjh.platform.module.device.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "NVR-监测点树", description = "NVR-监测点树")
public class NVRChannelTree {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "节点ID")
    private Long id;
    @ApiModelProperty(value = "节点标签")
    private String label;
    @ApiModelProperty(value = "上级ID")
    private Long upId;
    @ApiModelProperty(value = "层级")
    private String level;
    @ApiModelProperty(value = "子节点")
    private List<NVRChannelTree> children;
}
