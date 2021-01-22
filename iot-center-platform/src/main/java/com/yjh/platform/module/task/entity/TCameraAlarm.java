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
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

/**
 * @author tt
 * @since 2020-10-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraAlarm对象", description = "可视设备本体告警表")
public class TCameraAlarm implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "可视设备本体告警告警ID")
    @TableId(value = "camera_alarm_id", type = IdType.AUTO)
    @TableField(value = "camera_alarm_id",updateStrategy = FieldStrategy.IGNORED)
    private Long cameraAlarmId;

    @Length(max = 125,message = "alarmName长度必须小于等于125")
    @ApiModelProperty(value = "可视设备本体告警名称")
    @TableField(value = "alarm_name",updateStrategy = FieldStrategy.IGNORED)
    private String alarmName;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "可视设备ID")
     @TableField(value = "camera_id",updateStrategy = FieldStrategy.IGNORED)
    private Long cameraId;

    @Length(max = 32,message = "stationId长度必须小于等于32")
    @ApiModelProperty(value = "站所id")
     @TableField(value = "station_id",updateStrategy = FieldStrategy.IGNORED)
    private String stationId;

    @Max(value=99)
    @ApiModelProperty(value = "告警类型")
    private Integer alarmType;

    @Max(value=9)
    @ApiModelProperty(value = "1普通告警，2严重告警，3紧急告警，4致命告警")
    private Integer alarmLevel;

    @Length(max = 256,message = "alarmInfo长度必须小于等于256")
    @ApiModelProperty(value = "告警具体信息")
     @TableField(value = "alarm_info",updateStrategy = FieldStrategy.IGNORED)
    private String alarmInfo;

    @Past
    @ApiModelProperty(value = "告警时间")
     @TableField(value = "alarm_time",updateStrategy = FieldStrategy.IGNORED)
    private Date alarmTime;

    @Max(value=9)
    @ApiModelProperty(value = "是否告警")
    private Integer isAlarm;

    @Max(value=9)
    @ApiModelProperty(value = "处理方式：0自动，1手动")
    private Integer dealType;

    @Length(max = 256,message = "dealInfo长度必须小于等于256")
    @ApiModelProperty(value = "处理信息")
     @TableField(value = "deal_info",updateStrategy = FieldStrategy.IGNORED)
    private String dealInfo;

    @Length(max = 32,message = "dealPersonId长度必须小于等于256")
    @ApiModelProperty(value = "处理人ID")
     @TableField(value = "deal_person_id",updateStrategy = FieldStrategy.IGNORED)
    private String dealPersonId;

    @Past
    @ApiModelProperty(value = "处理时间")
    private Date dealTime;

    @Max(value=9)
    @ApiModelProperty(value = "0 未处理  1 处理")
    private Integer alarmState;

    @Past
    @TableField(value = "create_time",updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;

    @Past
    @TableField(value = "end_time",updateStrategy = FieldStrategy.IGNORED)
    private Date endTime;


}
