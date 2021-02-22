package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
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

/**
 * @author tt
 * @since 2020-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePointInstance对象", description = "巡检点实例表")
public class TCruisePointInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "巡检点实例ID")
    @TableId(value = "instance_id", type = IdType.AUTO)
    @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "测点实例ID")
    @TableField(value = "device_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @Length(max = 32,message = "stationId长度必须小于等于32")
    @ApiModelProperty(value = "变电站id")
    @TableField(value = "station_id",updateStrategy = FieldStrategy.IGNORED)
    private String stationId;

    @Length(max = 64,message = "stationName长度必须小于等于64")
    @ApiModelProperty(value = "变电站名称")
    @TableField(value = "station_name",updateStrategy = FieldStrategy.IGNORED)
    private String stationName;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "关联设备id")
     @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @Length(max = 32,message = "customId长度必须小于等于32")
    @ApiModelProperty(value = "关联部位表id")
     @TableField(value = "custom_id",updateStrategy = FieldStrategy.IGNORED)
    private String customId;

    @Length(max = 32,message = "dataFormat长度必须小于等于32")
    @ApiModelProperty(value = "数据格式 1：数值结果，2：可见光图片，3：红外图谱，4：音频")
    @TableField(value = "data_format",updateStrategy = FieldStrategy.IGNORED)
    private String dataFormat;

    @Max(value=999999999)
    @ApiModelProperty(value = "点位识别类型 1. 表计读数，2位置状态识别，3外观缺陷识别，4红外测温，5声音检测")
    private Integer identifyType;

    @Max(value=999999999)
    @ApiModelProperty(value = "点位识别子类型(若选取表计读数再细分)： 1.油位表、2.避雷器动作次数表、3.泄漏电流表、4.档位表、5.SF6压力表、6.油温表、7.开关动作次数表、8.气压表、9液压表")
    private Integer identifySonType;

    @Max(value=999999999)
    @ApiModelProperty(value = "巡检点类型 4001：摄像头 4002：机器人")
    private Integer cruiseType;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "巡检点ID")
    @TableField(value = "cruise_id",updateStrategy = FieldStrategy.IGNORED)
    private Long cruiseId;

    @Length(max = 64,message = "cruiseName长度必须小于等于64")
    @ApiModelProperty(value = "巡检点名称")
    @TableField(value = "cruise_name",updateStrategy = FieldStrategy.IGNORED)
    private String cruiseName;

    @Length(max = 256,message = "cruiseContent长度必须小于等于256")
    @ApiModelProperty(value = "巡检内容")
     @TableField(value = "cruise_content",updateStrategy = FieldStrategy.IGNORED)
    private String cruiseContent;

    @Length(max = 32,message = "positionType长度必须小于等于32")
    @ApiModelProperty(value = "波动值")
     @TableField(value = "position_type",updateStrategy = FieldStrategy.IGNORED)
    private String positionType;

    @Length(max = 32,message = "unit长度必须小于等于32")
    @ApiModelProperty(value = "单位字典值")
    @TableField(value = "unit",updateStrategy = FieldStrategy.IGNORED)
    private String unit;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否四遥ID：0-是，1-否")
    private Integer ifSy;

    @Max(value=999999999)
    @ApiModelProperty(value = "四遥类型 1：遥测 2：摇信 3：遥控 4：遥调")
    private Integer syType;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否手动录像 0否1是")
    private Integer ifVideotape;

    @Length(max = 32,message = "videotapeTime长度必须小于等于32")
    @ApiModelProperty(value = "录像时长 单位ms")
    @TableField(value = "videotape_time",updateStrategy = FieldStrategy.IGNORED)
    private String videotapeTime;

    @Length(max = 128,message = "textDesc长度必须小于等于128")
    @ApiModelProperty(value = "文本描述")
    private String textDesc;

    @Length(max = 32,message = "sort长度必须小于等于32")
    @ApiModelProperty(value = "排序序号")
    @TableField(value = "sort",updateStrategy = FieldStrategy.IGNORED)
    private String sort;

//    private Integer pageNum;
//
//    private Integer pageSize;

}