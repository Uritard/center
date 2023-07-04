package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 被巡视设备树查询条件
 *
 * @author 丫C
 * @date 2023/06/29
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "InspectedDevTreeCondition", description = "被巡视设备树查询条件实体")
public class InspectedDevTreeCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "name")
    private String name;

    /**
     * 过滤类型:region,dev,ins
     */
    @ApiModelProperty(value = "type")
    private String type;

    /**
     * 5-间隔，6-设备，7-部位，8-测点，9巡视点
     */
    @ApiModelProperty(value = "level")
    private String level;

    /**
     * all-所有，dev-设备，camera-摄像头，robot-机器人
     */
    @ApiModelProperty(value = "deviceShow")
    private String deviceShow;

    /**
     * 识别类型
     */
    @ApiModelProperty(value = "meteType")
    private String meteType;

    @ApiModelProperty(value = "analyseType")
    private String analyseType;

}
