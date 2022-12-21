package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author 丫C
 * @since 2022-11-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RobotPatrolTaskAlarm对象", description = "机器人/无人机巡视告警")
public class RobotPatrolTaskAlarm implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "所属机器人/无人机")
    private String robotCode;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务编码")
    private String taskCode;

    @ApiModelProperty(value = "设备点位名称")
    private String deviceName;

    @ApiModelProperty(value = "设备点位ID")
    private String deviceId;

    @ApiModelProperty(value = "告警等级")
    private String alarmLevel;

    @ApiModelProperty(value = "告警类型")
    private String alarmType;

    @ApiModelProperty(value = "识别类型(1:表计读取 2:位置状态识别 3:设备外观查看 4:红外测温 5:声音检测 6:闪烁检测 " +
            "11:局放超声波检测 12:局放地电压检测 13:局放特高频检测 101:环境温度检测 102:环境湿度检测 103:氧气浓度检测 104:SF6浓度检测)")
    private String recognitionType;

    @ApiModelProperty(value = "值")
    private String value;

    @ApiModelProperty(value = "值带单位")
    private String valueUnit;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "巡视任务执行ID")
    private String taskPatrolledId;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "缺陷类型")
    private String defectType;

}
