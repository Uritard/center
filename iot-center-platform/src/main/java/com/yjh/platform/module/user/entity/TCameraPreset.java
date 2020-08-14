package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YC
 * @date 2020/8/12 - 21:39
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraPreset对象", description = "摄像机预置位表")
public class TCameraPreset implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预置位id")
    private Long presetId;

    @ApiModelProperty(value = "摄像头id")
    private Long cameraId ;

    @ApiModelProperty(value = "预置位号")
    private int presetNum ;

    @ApiModelProperty(value = "预置位名称")
    private String presetName ;

    @ApiModelProperty(value = "啥也不是")
    private String creatorUser ;

    @ApiModelProperty(value = "创建时间")
    private Date creatorTime ;

    @ApiModelProperty(value = "是否使用")
    private int isUse ;

    @ApiModelProperty(value = "啥也不是啊")
    private String presetImg ;

    @ApiModelProperty(value = "检测点位置，0-室外 1-室内")
    private int inspectionPostion ;

    @ApiModelProperty(value = "采集状态，0-未采集 1-已采集")
    private int collectStatus ;

    @ApiModelProperty(value = "标定状态，0-未标定 1-已标定")
    private int calibrationStatus ;
}
