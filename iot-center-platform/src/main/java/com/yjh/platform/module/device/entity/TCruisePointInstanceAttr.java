package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author tt
 * @since 2020-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePointInstanceAttr对象", description = "巡检点实例全属性")
public class TCruisePointInstanceAttr implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡检点实例ID")
    @TableId(value = "instance_id", type = IdType.AUTO)
    private Long instanceId;

    @ApiModelProperty(value = "测点实例ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "变电站id")
    private String stationId;

    @ApiModelProperty(value = "变电站名称")
    private String stationName;

    @ApiModelProperty(value = "关联设备id")
    private Long deviceId;

    @ApiModelProperty(value = "关联部位表id")
    private String customId;

    @ApiModelProperty(value = "数据格式 1：数值结果，2：可见光图片，3：红外图谱，4：音频")
    private String dataFormat;

    @ApiModelProperty(value = "点位识别类型 1. 表计读数，2位置状态识别，3外观缺陷识别，4红外测温，5声音检测")
    private Integer identifyType;

    @ApiModelProperty(value = "点位识别子类型(若选取表计读数再细分)： 1.油位表、2.避雷器动作次数表、3.泄漏电流表、4.档位表、5.SF6压力表、6.油温表、7.开关动作次数表、8.气压表、9液压表")
    private Integer identifySonType;

    @ApiModelProperty(value = "巡检点类型 4001：摄像头 4002：机器人")
    private Integer cruiseType;

    @ApiModelProperty(value = "巡检点ID")
    private Long cruiseId;

    @ApiModelProperty(value = "巡检点名称")
    private String cruiseName;

    @ApiModelProperty(value = "巡检内容")
    private String cruiseContent;

    @ApiModelProperty(value = "波动值")
    private String positionType;

    @ApiModelProperty(value = "单位字典值")
    private String unit;

    @ApiModelProperty(value = "是否四遥ID：0-是，1-否")
    private Integer ifSy;

    @ApiModelProperty(value = "四遥类型 1：遥测 2：摇信 3：遥控 4：遥调")
    private Integer syType;

    @ApiModelProperty(value = "是否手动录像 0否1是")
    private Integer ifVideotape;

    @ApiModelProperty(value = "录像时长 单位ms")
    private String videotapeTime;

    @ApiModelProperty(value = "文本描述")
    private String textDesc;

    @ApiModelProperty(value = "排序序号")
    private String sort;

    @ApiModelProperty(value = "巡检点实例名称")
    private String instanceName;

    @ApiModelProperty(value = "属性名")
    private String attrName;

    @ApiModelProperty(value = "属性值")
    private String attrValue;

    private String deviceName;

    private Long robotId;

    private String deviceMeteName;
}