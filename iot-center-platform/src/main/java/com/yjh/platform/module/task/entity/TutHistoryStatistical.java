package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/9/24 - 15:42
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TutHistoryStatistical对象", description = "历史统计")
public class TutHistoryStatistical implements Serializable {

    @ApiModelProperty(value = "时间节点")
    private String timeNode;
    @ApiModelProperty(value = "个数")
    private Number count;


}
