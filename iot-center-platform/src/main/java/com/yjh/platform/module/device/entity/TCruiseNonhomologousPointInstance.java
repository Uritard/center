package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * @author sunjinyan
 * @since 2022-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseNonhomologousPointInstance对象", description = "非同源巡检点实例表")
public class TCruiseNonhomologousPointInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "非同源巡检点实例ID")
    @TableId(value = "instance_id", type = IdType.AUTO)
    @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "关联机器人巡视点位id")
    @TableField(value = "instance_id_one",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceIdOne;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "关联视频巡视点位id")
    @TableField(value = "instance_id_two",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceIdTwo;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "关联设备id")
    @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "关联部件id")
    @TableField(value = "custom_id",updateStrategy = FieldStrategy.IGNORED)
    private Long customId;

    @Max(value=32)
    @ApiModelProperty(value = "关联设备名称")
    @TableField(value = "device_name",updateStrategy = FieldStrategy.IGNORED)
    private String deviceName;

    @Length(max = 32,message = "关联部件名称")
    @ApiModelProperty(value = "关联部件名称")
    @TableField(value = "custom_name",updateStrategy = FieldStrategy.IGNORED)
    private String customName;

    @Max(value=999999999999999999L)
    @ApiModelProperty(value = "关联设备点位id")
    @TableField(value = "device_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @Max(value=32)
    @ApiModelProperty(value = "关联设备点位名称")
    @TableField(value = "mete_name",updateStrategy = FieldStrategy.IGNORED)
    private String deviceMeteName;

    @Max(value=999999999)
    @ApiModelProperty(value = "点位识别类型 1. 表计读数，2位置状态识别，3外观缺陷识别，4红外测温，5声音检测")
    private Integer identifyType;

    @Max(value=999999999)
    @ApiModelProperty(value = "点位识别子类型(若选取表计读数再细分)： 1.油位表、2.避雷器动作次数表、3.泄漏电流表、4.档位表、5.SF6压力表、6.油温表、7.开关动作次数表、8.气压表、9液压表")
    private Integer identifySonType;

    @Max(value=999999999)
    @ApiModelProperty(value = "点位算法识别子类型： 1.指针、3.数显")
    private Integer analyseType;

    @Max(value=999999999)
    @ApiModelProperty(value = "非同源巡视点类型 1：红外 2：位置 3：数显表计 4：指针表计 5：三相 6：区间 7：5次")
    @TableField(value = "cruise_type",updateStrategy = FieldStrategy.IGNORED)
    private Integer cruiseType;

    @Length(max = 10,message = "非同源告警名称长度必须小于等于10")
    @ApiModelProperty(value = "非同源告警名称")
    @TableField(value = "cruise_name",updateStrategy = FieldStrategy.IGNORED)
    private String cruiseName;

    @Length(max = 10,message = "告警阈值长度必须小于等于10")
    @ApiModelProperty(value = "告警阈值")
    @TableField(value = "warn_threshold",updateStrategy = FieldStrategy.IGNORED)
    private String warnThreshold;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警等级")
    @TableField(value = "warn_level",updateStrategy = FieldStrategy.IGNORED)
    private Integer warnLevel;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警时间区间")
    @TableField(value = "interval_type",updateStrategy = FieldStrategy.IGNORED)
    private Integer intervalType;

    @Max(value=999999999)
    @ApiModelProperty(value = "设备识别类型")
    @TableField(value = "mete_type",updateStrategy = FieldStrategy.IGNORED)
    private Integer meteType;

    private Integer pageNum=1;

    private Integer pageSize=0;

    private String oneCruiseDeviceName;
    private String twoCruiseDeviceName;

    private Long instanceIdThree;
    private String threeCruiseDeviceName;
}
