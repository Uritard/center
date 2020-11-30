package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;


/**
 * @author lqh
 * @since 2020/11/30
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "Robot对象", description = "机器人")
public class Robot implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "机器人编号")
    private String robotCode;

    @ApiModelProperty(value = "机器人名字")
    private String robotName;

    @ApiModelProperty(value = "机器人状态 ")
    private String robotStatus;

    @ApiModelProperty(value = "机器人型号")
    private Integer robotType;

    private String robotTypeName;

    @ApiModelProperty(value = "机器人类型")
    private String robotPosition;

    private String robotPositionName;

    private String photePath;

}
