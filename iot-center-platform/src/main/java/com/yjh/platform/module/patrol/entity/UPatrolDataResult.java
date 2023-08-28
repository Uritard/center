package com.yjh.platform.module.patrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UPatrolDataResult对象", description = "巡检点数据表")
public class UPatrolDataResult implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡视点数据id")
    @TableId(value = "cruise_data_id", type = IdType.AUTO)
    private Long cruiseDataId;

    @ApiModelProperty(value = "巡视任务id")
    private String taskId;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "测点ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "测点名称")
    private String deviceMeteName;

    @ApiModelProperty(value = "部位ID")
    private String customId;

    @ApiModelProperty(value = "部位名称")
    private String customName;

    @ApiModelProperty(value = "设备点位id")
    private String devicePointId;

    @ApiModelProperty(value = "巡检点实例ID")
    private Long instanceId;

    @ApiModelProperty(value = "巡检点实例名称")
    private String instanceName;

    @ApiModelProperty(value = "巡检点ID")
    private Long cruiseId;

    @ApiModelProperty(value = "巡检点名称")
    private String cruiseName;

    @ApiModelProperty(value = "巡检时间")
    private Date cruiseTime;

    @ApiModelProperty(value = "状态:0-已执行 1-未执行 2-执行失败 3-未知")
    private Integer cruiseStatus;

    @ApiModelProperty(value = "巡检点类型 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer cruiseType;

    @ApiModelProperty(value = "巡视设备ID（相机或机器人ID）")
    private String cruiseDeviceId;

    @ApiModelProperty(value = "巡视设备名称（相机或机器人名称）")
    private String cruiseDeviceName;

    @ApiModelProperty(value = "巡检结果文字描述")
    private String resultDesc;

    @ApiModelProperty(value = "巡检结果数值")
    private String resultNum;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "机器人巡检图片分析值（暂时没用）")
    private String modifyNum;

    @ApiModelProperty(value = "巡检分析图片，相对路径")
    private String picpath;

    @ApiModelProperty(value = "操作前结果图片,相对")
    private String confirmPicPath;

    @ApiModelProperty(value = "机器人巡检图片,相对（暂时没用）")
    private String picPathAnl;

    @ApiModelProperty(value = "人工校核结果")
    private String personCheck;

    @ApiModelProperty(value = "算法原始图片/红外可见光，绝对路径")
    private String origpic;

    @ApiModelProperty(value = "操作前结果图片,绝对")
    private String origConfirmPicPath;

    @ApiModelProperty(value = "机器人巡检图片,绝对（暂时没用）")
    private String origPicAnl;

    @ApiModelProperty(value = "巡视异常原因 -抓图失败、数据异常、异常告警、算法超时")
    private Integer cruiseAbnormal;

    @ApiModelProperty(value = "审核状态1-审核0-未审核")
    private Integer evaluationState;

    @ApiModelProperty(value = "识别状态 1识别正常 2识别异常")
    private Integer identifyState;

    @ApiModelProperty(value = "实际结果 1正常 2异常")
    private Integer identifyResult;

    private Date createtime;

    @ApiModelProperty(value = "备用字段3")
    private String remark;

    @ApiModelProperty(value = "审核人")
    private String checkUser;

    @ApiModelProperty(value = "审核时间")
    private Date checkDate;

    @ApiModelProperty(value = "是否产生告警1.是0.否")
    private Integer isWarn;

    @ApiModelProperty(value = "巡视执行结果-正常、异常")
    private Integer cruiseResult;

    @ApiModelProperty(value = "红外FIR文件名称")
    private String firName;

    @ApiModelProperty(value = "红外FIR文件生成时间")
    private Date firDate;

    @ApiModelProperty(value = "FIR文件存储路径")
    private String resultPic;

    @ApiModelProperty(value = "图片坐标点")
    private String points;

    @ApiModelProperty(value = "声纹文件地址")
    private String voicePath;


}
