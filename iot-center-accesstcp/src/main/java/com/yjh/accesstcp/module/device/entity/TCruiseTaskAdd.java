package com.yjh.accesstcp.module.device.entity;

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
@ApiModel(value = "TCruiseTask对象", description = "巡检任务表")
public class TCruiseTaskAdd implements Serializable {

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
    private Integer type;

    @ApiModelProperty(value = "是否立即执行（172.周期，173.立即，174.定期）")
    private Integer ifRun;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "定时时间类型（1.周，2.日）")
    private String dateType;

    @ApiModelProperty(value = "任务来源：1 日常巡视 2红外普测 3地电波 4机器人监控 5机器人本体任务")
    private Integer taskType;

    @ApiModelProperty(value = "任务等级(从高到低):4级,3级,2级,1级")
    private Integer taskLevel;

    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "定时时间类型（分）")
    private String min;

    @ApiModelProperty(value = "定时时间类型（时）")
    private String hour;

    @ApiModelProperty(value = "定时时间类型（月的天数）")
    private String dayOfMonth;

    @ApiModelProperty(value = "定时时间类型（月份）")
    private String month;

    @ApiModelProperty(value = "定时时间类型（周的天数）")
    private String dayOfWeek;

    @ApiModelProperty(value = "定时时间类型（年）")
    private String year;

    @ApiModelProperty(value = "定时时间类型（1.周，2.日）")
    private Long periodId;

    private String pCode;

    private String deviceList;

    private Long instanceId;

    @ApiModelProperty(value = "是否OCR识别 (0:是 1:否)")
    private String isOcr;

    private Long createUserId;

    @ApiModelProperty(value = "预案编码")
    private String planCode;

    private String identifier;

    private String endTime;

    @ApiModelProperty(value = "是否为联动任务（1表达联动任务，正常任务为空）")
    private String unionTaskStatus;

    @ApiModelProperty(value = "周期（月） ")
    private String cycleMonth;

    @ApiModelProperty(value = "周期（周） ")
    private String cycleWeek;

    @ApiModelProperty(value = "周期（执行时间） ")
    private String cycleExecuteTime;

    @ApiModelProperty(value = "周期（开始时间） ")
    private String cycleStartTime;

    @ApiModelProperty(value = "周期（结束时间） ")
    private String cycleEndTime;

    @ApiModelProperty(value = "间隔（数量） ")
    private String intervalNumber;

    @ApiModelProperty(value = "间隔（类型） ")
    private String intervalType;

    @ApiModelProperty(value = "间隔（执行时间） ")
    private String intervalExecuteTime;

    @ApiModelProperty(value = "间隔（开始时间） ")
    private String intervalStartTime;

    @ApiModelProperty(value = "间隔（结束时间） ")
    private String intervalEndTime;

}
