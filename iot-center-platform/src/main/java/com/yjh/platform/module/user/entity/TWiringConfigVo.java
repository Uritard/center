package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author 丫C
 * @since 2023-06-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TWiringConfigVo对象", description = "主接线图与设备关联关系")
public class TWiringConfigVo extends TWiringConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联设备类型名称")
    private String equipmentTypeName;

    @ApiModelProperty(value = "关联设备在线状态 1-在线 0-离线")
    private Integer onlineState;

    @ApiModelProperty(value = "关联设备是否还存在 1-存在 0-删除")
    private Integer isExist;

}
