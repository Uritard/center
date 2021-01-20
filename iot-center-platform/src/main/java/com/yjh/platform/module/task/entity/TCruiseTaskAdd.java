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

    private String password;

    private String deviceList;

}
