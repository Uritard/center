package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RobotAlarm对象", description = "机器人本体告警表")
public class RobotAlarm implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "机器人告警ID")
    @TableId(value = "robot_alarm_id", type = IdType.AUTO)
    @TableField(value = "robot_alarm_id",updateStrategy = FieldStrategy.IGNORED)
    private Long robotAlarmId;

    @ApiModelProperty(value = "站所id")
    @TableField(value = "station_id",updateStrategy = FieldStrategy.IGNORED)
    private String stationId;

    @ApiModelProperty(value = "告警类型 ")
    private Integer alarmType;

    @ApiModelProperty(value = "1普通告警，2严重告警，3紧急告警，4致命告警")
    private Integer alarmLevel;

    @ApiModelProperty(value = "告警具体信息")
     @TableField(value = "alarm_info",updateStrategy = FieldStrategy.IGNORED)
    private String alarmInfo;

    @ApiModelProperty(value = "告警时间")
    private Date alarmTime;

    @ApiModelProperty(value = "处理方式：0自动，1手动")
    private Integer dealType;

    @ApiModelProperty(value = "处理信息")
     @TableField(value = "deal_info",updateStrategy = FieldStrategy.IGNORED)
    private String dealInfo;

    @ApiModelProperty(value = "处理人ID")
     @TableField(value = "deal_person_id",updateStrategy = FieldStrategy.IGNORED)
    private String dealPersonId;

    @ApiModelProperty(value = "处理人姓名")
     @TableField(value = "deal_person_name",updateStrategy = FieldStrategy.IGNORED)
    private String dealPersonName;

    @ApiModelProperty(value = "机器人告警时坐标X")
     @TableField(value = "position_station_num",updateStrategy = FieldStrategy.IGNORED)
    private String positionStationNum;

    @ApiModelProperty(value = "机器人告警时坐标Y")
     @TableField(value = "position_offset",updateStrategy = FieldStrategy.IGNORED)
    private String positionOffset;

    @ApiModelProperty(value = "0 未处理  1 处理")
    private Integer alarmState;

    private Date createTime;

    private Date endTime;

//    private Integer pageNum;
//
//    private Integer pageSize;

}
