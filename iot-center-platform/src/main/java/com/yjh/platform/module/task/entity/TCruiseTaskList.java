package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-08-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTaskList", description = "任务巡检点状态信息")
public class TCruiseTaskList implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡检任务UUID")
    private String taskId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "所属预案id")
    private Long planId;

    @ApiModelProperty(value = "所属厂站")
    private String areaId;

    @ApiModelProperty(value = "任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer type;

    @ApiModelProperty(value = "是否立即执行（172.周期，173.立即，174.定期）")
    private Integer ifRun;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "定时时间类型（1.周，2.日）")
    private String dateType;

    @ApiModelProperty(value = "任务来源：1 日常巡视 2红外普测 3地电波 4机器人监控 5机器人本体任务")
    private Integer taskType;

    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "巡检点实例ID")
    private Long instanceId;

    @ApiModelProperty(value = "测点实例ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "关联设备id")
    private Long deviceId;

    @ApiModelProperty(value = "关联设备名称")
    private Long deviceName;

    @ApiModelProperty(value = "关联部位表id")
    private String customId;

    @ApiModelProperty(value = "关联部位表名称")
    private String customName;

    @ApiModelProperty(value = "关联巡视点表id")
    private String pointTaskId;

    @ApiModelProperty(value = "关联巡视点表名称")
    private String instanceName;

    @ApiModelProperty(value = "是否支持机器人巡视")
    private Integer ifRobot;

    @ApiModelProperty(value = "是否支持视频巡视")
    private Integer ifVideo;

    @ApiModelProperty(value = "是否支持红外巡视")
    private Integer ifInferad;

    @ApiModelProperty(value = "是否支持人工巡视")
    private Integer ifArtificial;

    @ApiModelProperty(value = "结果分析")
    private String personCheck;

    @ApiModelProperty(value = "巡视结果")
    private String identifyState;

}
