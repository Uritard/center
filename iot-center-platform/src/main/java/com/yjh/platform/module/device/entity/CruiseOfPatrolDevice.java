package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2024/2/19
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "获取当前任务下各种巡视设备的巡检点", description = "不同巡视设备下的巡检点")
public class CruiseOfPatrolDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡检方式ID")
    private String instanceId;
    @ApiModelProperty(value = "巡检方式名称")
    private Integer cruiseType;
    @ApiModelProperty(value = "巡检点数量")
    private String robotName;
}
