package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-10-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotAlarm对象", description = "机器人本体告警表")
public class TRobotAlarm implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "机器人告警ID")
    @TableId(value = "robot_alarm_id", type = IdType.AUTO)
    private Long robotAlarmId;

    @ApiModelProperty(value = "机器人本体告警名称")
    private String alarmName;

    private Long robotId;

    @ApiModelProperty(value = "站所id")
    private String stationId;

    @ApiModelProperty(value = "告警类型 ")
    private Integer alarmType;

    @ApiModelProperty(value = "1普通告警，2严重告警，3紧急告警，4致命告警")
    private Integer alarmLevel;

    @ApiModelProperty(value = "告警具体信息")
    private String alarmInfo;

    @ApiModelProperty(value = "告警时间")
    private Date alarmTime;

    @ApiModelProperty(value = "处理方式：0自动，1手动")
    private Integer dealType;

    @ApiModelProperty(value = "处理信息")
    private String dealInfo;

    @ApiModelProperty(value = "处理人ID")
    private String dealPersonId;

    @ApiModelProperty(value = "处理时间")
    private Date dealTime;

    @ApiModelProperty(value = "机器人告警时坐标X")
    private String positionStationNum;

    @ApiModelProperty(value = "机器人告警时坐标Y")
    private String positionOffset;

    @ApiModelProperty(value = "0 未处理  1 处理")
    private Integer alarmState;

    private Date createTime;

    private Date endTime;


}
