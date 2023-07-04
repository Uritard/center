package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 巡视设备树查询条件
 *
 * @author 丫C
 * @date 2023/06/29
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "PatrolDevTreeCondition", description = "巡视设备树查询条件实体")
public class PatrolDevTreeCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "name")
    private String name;

    /**
     * 相机类型:红外,可见光
     */
    @ApiModelProperty(value = "type")
    private Integer type;

    /**
     * 在线状态 1-在线 0-离线 2-全部
     */
    @ApiModelProperty(value = "flag")
    private Integer flag;

    /**
     * 5-间隔，6-设备(巡视设备)
     */
    @ApiModelProperty(value = "level")
    private String level;

}
