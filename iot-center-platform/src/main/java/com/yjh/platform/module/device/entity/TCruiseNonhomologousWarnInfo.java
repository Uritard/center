package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.Date;

/**
 * @author sunjinyan
 * @since 2022-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseNonhomologousWarnInfo对象", description = "非同源巡检点告警表")
public class TCruiseNonhomologousWarnInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=60)
    @ApiModelProperty(value = "非同源巡检点实例ID")
    @TableField(value = "warn_id",updateStrategy = FieldStrategy.IGNORED)
    private String warnId;

//    @Length(max = 32,message = "stationId长度必须小于等于32")
//    @ApiModelProperty(value = "变电站id")
//    @TableField(value = "station_id",updateStrategy = FieldStrategy.IGNORED)
//    private String stationId;
//
//    @Length(max = 64,message = "stationName长度必须小于等于64")
//    @ApiModelProperty(value = "变电站名称")
//    @TableField(value = "station_name",updateStrategy = FieldStrategy.IGNORED)
//    private String stationName;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "关联告警规则id")
     @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

//    @Length(max = 256,message = "关联结果id")
//    @ApiModelProperty(value = "关联结果id")
//     @TableField(value = "result_ids",updateStrategy = FieldStrategy.IGNORED)
//    private String resultIds;

    @Max(value=32)
    @ApiModelProperty(value = "告警内容")
    @TableField(value = "warn_content",updateStrategy = FieldStrategy.IGNORED)
    private String warnContent;

    @Length(max = 32,message = "告警时间")
    @ApiModelProperty(value = "告警时间")
    @TableField(value = "warn_time",updateStrategy = FieldStrategy.IGNORED)
    private String warnTime;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "关联设备id")
    @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @Max(value=999999999999999999l)
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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "关联设备点位id")
    @TableField(value = "device_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @Max(value=32)
    @ApiModelProperty(value = "关联设备点位名称")
    @TableField(value = "mete_name",updateStrategy = FieldStrategy.IGNORED)
    private String deviceMeteName;

    @Max(value=32)
    @ApiModelProperty(value = "机器人名称")
    @TableField(value = "robot_name",updateStrategy = FieldStrategy.IGNORED)

    private String robotName;
    @Max(value=32)
    @ApiModelProperty(value = "相机名称")
    @TableField(value = "camera_name",updateStrategy = FieldStrategy.IGNORED)
    private String cameraName;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警类型")
    @TableField(value = "warn_type",updateStrategy = FieldStrategy.IGNORED)
    private Integer warnType;

    @Length(max = 999999999)
    @ApiModelProperty(value = "告警等级")
     @TableField(value = "warn_level",updateStrategy = FieldStrategy.IGNORED)
    private Integer warnLevel;

    @Length(max = 999999999)
    @ApiModelProperty(value = "设备类型")
    @TableField(value = "device_type",updateStrategy = FieldStrategy.IGNORED)
    private Integer deviceType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    private Integer pageNum=1;

    private Integer pageSize=0;

}