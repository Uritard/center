package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/7/5
 * @since [产品/模块版本] （可选）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TRobotMapNodeDeviceInfo extends TRobotDeviceConfig{

    @ApiModelProperty(value = "地图点Id")
    private String nodeId;

    @ApiModelProperty(value = "地图点名称")
    private String nodeName;
}
