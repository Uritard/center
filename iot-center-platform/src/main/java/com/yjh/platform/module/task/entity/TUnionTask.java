package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TUnionTask对象", description = "巡检任务表")
public class TUnionTask implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡检任务")
    private String unionId;

    @ApiModelProperty(value = "所属预案id")
    private Long planId;

    @ApiModelProperty(value = "所属厂站")
    private String areaId;

    @ApiModelProperty(value = "任务名称")
    private String name;

    @ApiModelProperty(value = "任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer type;

    @ApiModelProperty(value = "是否立即执行（1.周期，2.立即，3.间隔）")
    private Integer ifRun;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "定时时间类型（1.周，2.日）")
    private Integer dateType;

    @ApiModelProperty(value = "备注1")
    private Integer remark1;

    @ApiModelProperty(value = "备注2")
    private Integer remark2;

    @ApiModelProperty(value = "备注3")
    private String remark3;

    @ApiModelProperty(value = "任务来源：1 日常巡视 2红外普测 3地电波 4机器人监控 5机器人本体任务")
    private Integer taskType;

    @ApiModelProperty(value = "巡视时间")
    private Date startTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
