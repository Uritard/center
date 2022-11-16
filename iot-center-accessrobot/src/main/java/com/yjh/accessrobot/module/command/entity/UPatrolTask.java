package com.yjh.accessrobot.module.command.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UPatrolTask对象", description = "巡检任务表")
public class UPatrolTask implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡检任务UUID")
    private String taskId;

    @ApiModelProperty(value = "任务编码")
    private String taskCode;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "所属预案id")
    private Long planId;

    @ApiModelProperty(value = "所属厂站")
    private String areaId;

    @ApiModelProperty(value = "任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer taskType;

    @ApiModelProperty(value = "执行类型（172.周期，173.立即，174.定期）")
    private Integer executeType;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "定时时间类型（1.周，2.日）")
    private String dateType;

    @ApiModelProperty(value = "任务来源（暂时没用）")
    private Integer taskSource;

    @ApiModelProperty(value = "任务等级(从高到低):4级,3级,2级,1级")
    private Integer taskLevel;

    @ApiModelProperty(value = "巡视时间")
    private Date startTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "结束时间")
    private Date endTime;

    @ApiModelProperty(value = "创建用户id")
    private Long createUserId;


}
