package com.yjh.accessvqd.module.diagnose.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


/**
 * @author czh
 * @since 2021-01-14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "监测点对象", description = "视频诊断-监测点")
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "监控点ID")
    private String id;
    @ApiModelProperty(value = "是否检测")
    private String checkFlag;
    @ApiModelProperty(value = "所属的设备IP")
    private String ip;
    @ApiModelProperty(value = "所属的设备端口号")
    private String port;
    @ApiModelProperty(value = "通道号")
    private String chanIndex;
    @ApiModelProperty(value = "登录用户名")
    private String userName="administrator";
    @ApiModelProperty(value = "登录密码")
    private String userPwd="BIhWBdVruB9R";
    @ApiModelProperty(value = "信号丢失阈值")
    private String signalPoint;
    @ApiModelProperty(value = "图像模糊阈值")
    private String blurPoint;
    @ApiModelProperty(value = "对比度阈值")
    private String contrastPoint;
    @ApiModelProperty(value = "图像过亮阈值")
    private String brightPoint;
    @ApiModelProperty(value = "图像过暗阈值")
    private String darkPoint;
    @ApiModelProperty(value = "图像偏色阈值")
    private String chromaPoint;
    @ApiModelProperty(value = "黑白图像阈值")
    private String monoPoint;
    @ApiModelProperty(value = "噪声干扰阈值")
    private String noisePoint;
    @ApiModelProperty(value = "条纹干扰阈值")
    private String streakPoint;
    @ApiModelProperty(value = "画面冻结阈值")
    private String freezePoint;
    @ApiModelProperty(value = "视频抖动阈值")
    private String shakePoint;
    @ApiModelProperty(value = "视频剧变阈值")
    private String flashPoint;
    @ApiModelProperty(value = "场景变换阈值")
    private String scenePoint;
    @ApiModelProperty(value = "视频遮挡阈值")
    private String coverPoint;
    @ApiModelProperty(value = "云台失控阈值")
    private String ptzPoint;
    @ApiModelProperty(value = "码流类型")
    private String streamType;
    @ApiModelProperty(value = "网络协议")
    private String protocol;
    @ApiModelProperty(value = "设备类型")
    private String devType;
    @ApiModelProperty(value = "品牌代号")
    private String devBrand;



}
