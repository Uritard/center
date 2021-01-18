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
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人告警ID")
    @TableId(value = "robot_alarm_id", type = IdType.AUTO)
    private Long robotAlarmId;

    @Length(max = 125,message = "alarmName长度必须小于等于125")
    @ApiModelProperty(value = "机器人本体告警名称")
    private String alarmName;

    @Max(value=999999999999999999l)
    private Long robotId;

    @Length(max = 32,message = "stationId长度必须小于等于32")
    @ApiModelProperty(value = "站所id")
    private String stationId;

    @Max(value=99)
    @ApiModelProperty(value = "告警类型 ")
    private Integer alarmType;

    @Max(value=9)
    @ApiModelProperty(value = "1普通告警，2严重告警，3紧急告警，4致命告警")
    private Integer alarmLevel;

    @Length(max = 256,message = "alarmInfo长度必须小于等于256")
    @ApiModelProperty(value = "告警具体信息")
    private String alarmInfo;

    @Past
    @ApiModelProperty(value = "告警时间")
    private Date alarmTime;

    @Max(value=9)
    @ApiModelProperty(value = "处理方式：0自动，1手动")
    private Integer dealType;

    @Length(max = 256,message = "dealInfo长度必须小于等于256")
    @ApiModelProperty(value = "处理信息")
    private String dealInfo;

    @Length(max = 32,message = "dealPersonId长度必须小于等于32")
    @ApiModelProperty(value = "处理人ID")
    private String dealPersonId;

    @Past
    @ApiModelProperty(value = "处理时间")
    private Date dealTime;

    @Length(max = 100,message = "positionStationNum长度必须小于等于100")
    @ApiModelProperty(value = "机器人告警时坐标X")
    private String positionStationNum;

    @Length(max = 100,message = "positionOffset长度必须小于等于100")
    @ApiModelProperty(value = "机器人告警时坐标Y")
    private String positionOffset;

    @Max(value=9)
    @ApiModelProperty(value = "0 未处理  1 处理")
    private Integer alarmState;

    @Past
    private Date createTime;

    @Past
    private Date endTime;


}
