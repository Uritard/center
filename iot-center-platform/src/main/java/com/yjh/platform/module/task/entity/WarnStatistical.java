package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/10/27 - 11:00
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "WarnStatistical对象", description = "告警历史统计一月")
public class WarnStatistical implements Serializable {

    @ApiModelProperty(value = "时间")
    private String timeNode;
    @ApiModelProperty(value = "个数")
    private Integer count;
}
