package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wf
 * @since 2020-08-19
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TUnionTask", description = "联合巡视预案表")
public class TUnionTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡检任务")
    @TableId(value = "union_id", type = IdType.AUTO)
    private String UnionId;

    @ApiModelProperty(value = "所属预案id")
    private Long PlanId;

    @ApiModelProperty(value = "所属厂站")
    private String AreaId;

    @ApiModelProperty(value = "任务名称")
    private String Name;

    @ApiModelProperty(value = "任务类型1.全面2.例行3.熄灯4.特殊5.专项6.自定义")
    private Integer Type;

    @ApiModelProperty(value = "是否立即执行(1.周期，2.立即，3.间隔)")
    private Integer IfRun;

    @ApiModelProperty(value = "机器人id")
    private Long RobotId;

    @ApiModelProperty(value = "定时时间类型(1.周,2.日)")
    private Integer Datetype;

    @ApiModelProperty(value = "备注1")
    private Integer Remark1;

    @ApiModelProperty(value = "备注2")
    private Integer Remark2;

    @ApiModelProperty(value = "备注3")
    private Integer Remark3;

    @ApiModelProperty(value = "任务来源：1日常巡视 2红外普测 3地电波 4机器人监控 5机器人本体任务")
    private Integer TaskType;

    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String StartTime;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date CreateTime;

}
