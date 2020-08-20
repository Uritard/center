package com.yjh.platform.module.user.entity;

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
 * @author yc
 * @since 2020-08-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraPreset对象", description = "摄像机预位置表")
public class TCameraPreset implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "预置位id")
    @TableId(value = "preset_id", type = IdType.AUTO)
    private Long presetId;

    @ApiModelProperty(value = "摄像头id")
    private Long cameraId;

    @ApiModelProperty(value = "预置位号")
    private Integer presetNum;

    @ApiModelProperty(value = "预置位名称")
    private String presetName;

    private String creatorUser;

    @ApiModelProperty(value = "创建时间")
    private Date creatorTime;

    @ApiModelProperty(value = "是否使用")
    private Integer isUse;

    private String presetImg;

    @ApiModelProperty(value = "检测点位置，0-室外 1-室内")
    private Integer inspectionPostion;

    @ApiModelProperty(value = "采集状态，0-未采集 1-已采集")
    private Integer collectStatus;

    @ApiModelProperty(value = "标定状态，0-未标定 1-已标定")
    private Integer calibrationStatus;


}
