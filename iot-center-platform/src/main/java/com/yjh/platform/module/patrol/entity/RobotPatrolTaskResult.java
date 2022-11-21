package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RobotPatrolTaskResult对象", description = "机器人/无人机巡视结果")
public class RobotPatrolTaskResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "下级唯一标识")
    private String sendCode;

    @ApiModelProperty(value = "巡视设备名称")
    private String patrolDeviceName;

    @ApiModelProperty(value = "巡视设备编码")
    private String patrolDeviceCode;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务编码")
    private String taskCode;

    @ApiModelProperty(value = "设备点位名称")
    private String deviceName;

    @ApiModelProperty(value = "设备点位ID")
    private String deviceId;

    @ApiModelProperty(value = "值类型(0:默认值类型 11:局放放电频次 12:局放信号峰值 13:局放信号均值)")
    private String valueType;

    @ApiModelProperty(value = "值")
    private String value;

    @ApiModelProperty(value = "值带单位")
    private String valueUnit;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "时间")
    private String time;

    @ApiModelProperty(value = "识别类型(1:表计读取 2:位置状态识别 3:设备外观查看 4:红外测温 5:声音检测 6:闪烁检测 " +
            "11:局放超声波检测 12:局放地电压检测 13:局放特高频检测 101:环境温度检测 102:环境湿度检测 103:氧气浓度检测 104:SF6浓度检测)")
    private String recognitionType;

    @ApiModelProperty(value = "采集文件类型(1:红外图谱 2:可见光照片 3:音频 4:视频)")
    private String fileType;

    @ApiModelProperty(value = "图像框(格式：x1,y1;x2,y2;x3,y3;x4,y4等为图片文件的像素点)")
    private String rectangle;

    @ApiModelProperty(value = "文件名称")
    private String filePath;

    @ApiModelProperty(value = "巡视任务执行ID")
    private String taskPatrolledId;

    @ApiModelProperty(value = "结论(0为失败，1为成功，2为判别异常)")
    private String valid;

    /**
     * 自家机器人拓展字段
     */
    @ApiModelProperty(value = "红外原图")
    private String originFilePath;

    /**
     * 自家机器人拓展字段
     */
    @ApiModelProperty(value = "可见光原图、红外结果")
    private String originFileResultPath;
}
