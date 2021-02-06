package com.yjh.accessvqd.module.diagnose.entity;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField(value = "diagnose_result_id",updateStrategy = FieldStrategy.IGNORED)
    private Long diagnoseResultId;

    @ApiModelProperty(value = "监测点ID")
     @TableField(value = "channel_id",updateStrategy = FieldStrategy.IGNORED)
    private String channelId;


    @ApiModelProperty(value = "诊断任务ID")
    @TableField(value = "diagnose_plan_id",updateStrategy = FieldStrategy.IGNORED)
    private String diagnosePlanId;

    @ApiModelProperty(value = "监测点IP")
    @TableField(value = "ip",updateStrategy = FieldStrategy.IGNORED)
    private String ip;

    @ApiModelProperty(value = "通道号")
    @TableField(value = "chan_index",updateStrategy = FieldStrategy.IGNORED)
    private String chanIndex;

    @ApiModelProperty(value = "检测时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "check_time",updateStrategy = FieldStrategy.IGNORED)
    private Date checkTime;

    @ApiModelProperty(value = "监测点结果(0-未检测 1-正常 2-异常 3-登录失败 4-取流异常 5-解码失败 6-取流延迟)")
     @TableField(value = "channel_result",updateStrategy = FieldStrategy.IGNORED)
    private Integer channelResult;

    @ApiModelProperty(value = "视频丢失")
    @TableField(value = "signal_result",updateStrategy = FieldStrategy.IGNORED)
    private String signalResult;

    @ApiModelProperty(value = "图像模糊")
    @TableField(value = "blur_result",updateStrategy = FieldStrategy.IGNORED)
    private String blurResult;

    @ApiModelProperty(value = "对比度")
     @TableField(value = "contrast_result",updateStrategy = FieldStrategy.IGNORED)
    private String contrastResult;

    @ApiModelProperty(value = "图像过亮")
    @TableField(value = "bright_result",updateStrategy = FieldStrategy.IGNORED)
    private String brightResult;

    @ApiModelProperty(value = "图像过暗")
    @TableField(value = "dark_result",updateStrategy = FieldStrategy.IGNORED)
    private String darkResult;

    @ApiModelProperty(value = "图像偏色")
    @TableField(value = "chroma_result",updateStrategy = FieldStrategy.IGNORED)
    private String chromaResult;

    @ApiModelProperty(value = "黑白图像")
    @TableField(value = "mono_result",updateStrategy = FieldStrategy.IGNORED)
    private String monoResult;

    @ApiModelProperty(value = "噪声干扰")
     @TableField(value = "noise_result",updateStrategy = FieldStrategy.IGNORED)
    private String noiseResult;

    @ApiModelProperty(value = "条纹干扰")
    @TableField(value = "streak_result",updateStrategy = FieldStrategy.IGNORED)
    private String streakResult;

    @ApiModelProperty(value = "画面冻结")
     @TableField(value = "freeze_result",updateStrategy = FieldStrategy.IGNORED)
    private String freezeResult;

    @ApiModelProperty(value = "视频抖动")
     @TableField(value = "shake_result",updateStrategy = FieldStrategy.IGNORED)
    private String shakeResult;

    @ApiModelProperty(value = "视频剧变")
    @TableField(value = "flash_result",updateStrategy = FieldStrategy.IGNORED)
    private String flashResult;

    @ApiModelProperty(value = "场景变换")
     @TableField(value = "scene_result",updateStrategy = FieldStrategy.IGNORED)
    private String sceneResult;

    @ApiModelProperty(value = "视频遮挡")
     @TableField(value = "cover_result",updateStrategy = FieldStrategy.IGNORED)
    private String coverResult;

    @ApiModelProperty(value = "云台检测")
     @TableField(value = "ptz_result",updateStrategy = FieldStrategy.IGNORED)
    private String ptzResult;

    @ApiModelProperty(value = "诊断抓图数据URL路径")
    @TableField(value = "snapshot_url",updateStrategy = FieldStrategy.IGNORED)
    private String snapshotUrl;

    @ApiModelProperty(value = "图像宽度")
    @TableField(value = "width",updateStrategy = FieldStrategy.IGNORED)
    private String width;

    @ApiModelProperty(value = "图像高度")
    @TableField(value = "height",updateStrategy = FieldStrategy.IGNORED)
    private String height;

    @ApiModelProperty(value = "诊断结果-内容")
     @TableField(value = "result_content",updateStrategy = FieldStrategy.IGNORED)
    private String resultContent;

    @ApiModelProperty(value = "诊断状态")
    @TableField(value = "status",updateStrategy = FieldStrategy.IGNORED)
    private String status;

}
