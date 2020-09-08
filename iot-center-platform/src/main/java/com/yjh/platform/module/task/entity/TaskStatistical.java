package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/9/7 - 11:14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TaskStatistical对象", description = "巡视任务结果统计")
public class TaskStatistical implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "全面巡视")
    private Integer planType1;
    @ApiModelProperty(value = "例行巡视")
    private Integer planType2;
    @ApiModelProperty(value = "熄灯巡视")
    private Integer planType3;
    @ApiModelProperty(value = "特殊巡视")
    private Integer planType4;
    @ApiModelProperty(value = "专项巡视")
    private Integer planType5;
    @ApiModelProperty(value = "自定义巡视")
    private Integer planType6;

}
