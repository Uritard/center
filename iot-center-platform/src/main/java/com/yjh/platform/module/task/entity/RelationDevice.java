package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/10/29 - 16:28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RelationDevice对象", description = "巡检记录报表-关联设备")
public class RelationDevice implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "类别")
    private String rcpName;
    @ApiModelProperty(value = "检测值")
    private String analysisResult;
    @ApiModelProperty(value = "状态")
    private String judgment;
}
