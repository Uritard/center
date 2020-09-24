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
@ApiModel(value = "HistoryStatistical对象", description = "历史统计")
public class QuarterEntity implements Serializable {

    @ApiModelProperty(value = "前1月")
    private Integer monthOne;
    @ApiModelProperty(value = "前2月")
    private Integer monthTwo;
    @ApiModelProperty(value = "前3月")
    private Integer monthThree;


}
