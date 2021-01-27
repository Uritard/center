package com.yjh.accessvqd.module.diagnose.entity;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;


/**
 * @author czh
 * @since 2020-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "诊断结果对象", description = "视频诊断-诊断结果")
public class ChanResult implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "诊断结果ID")
    private Long diagnoseResultId;

    @ApiModelProperty(value = "监测点ID")
    private String channelId;

    @ApiModelProperty(value = "诊断任务ID")
    private String diagnosePlanId;

    @ApiModelProperty(value = "监测点IP")
    private String ip;

    @ApiModelProperty(value = "通道号")
    private String chanIndex;

    @ApiModelProperty(value = "检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkTime;

    @ApiModelProperty(value = "监测点结果(0-未检测 1-正常 2-异常 3-登录失败 4-取流异常 5-解码失败 6-取流延迟)")
    private Integer channelResult;

    @ApiModelProperty(value = "视频丢失")
    private String signalResult;

    @ApiModelProperty(value = "图像模糊")
    private String blurResult;

    @ApiModelProperty(value = "对比度")
    private String contrastResult;

    @ApiModelProperty(value = "图像过亮")
    private String brightResult;

    @ApiModelProperty(value = "图像过暗")
    private String darkResult;

    @ApiModelProperty(value = "图像偏色")
    private String chromaResult;

    @ApiModelProperty(value = "黑白图像")
    private String monoResult;

    @ApiModelProperty(value = "噪声干扰")
    private String noiseResult;

    @ApiModelProperty(value = "条纹干扰")
    private String streakResult;

    @ApiModelProperty(value = "画面冻结")
    private String freezeResult;

    @ApiModelProperty(value = "视频抖动")
    private String shakeResult;

    @ApiModelProperty(value = "视频剧变")
    private String flashResult;

    @ApiModelProperty(value = "场景变换")
    private String sceneResult;

    @ApiModelProperty(value = "视频遮挡")
    private String coverResult;

    @ApiModelProperty(value = "云台检测")
    private String ptzResult;

    @ApiModelProperty(value = "诊断抓图数据URL路径")
    private String snapshotUrl;

    @ApiModelProperty(value = "图像宽度")
    private String width;

    @ApiModelProperty(value = "图像高度")
    private String height;

    @ApiModelProperty(value = "诊断结果-内容")
    private String resultContent;

}
