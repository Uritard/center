package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czh
 * @since 2020-08-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TVideoAlgoResult对象", description = "视频轮训任务结果表")
public class TVideoAlgoResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "序号ID")
    private Long id;

    @ApiModelProperty(value = "巡检点ID")
    private Long pointId;

    @ApiModelProperty(value = "预案Id")
    private String taskId;

    @ApiModelProperty(value = "计划Id")
    private Long planId;

    @ApiModelProperty(value = "设备Id")
    private Long deviceId;

    @ApiModelProperty(value = "预置位Id")
    private Long presetId;

    @ApiModelProperty(value = "设备测点实例ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "图片地址")
    private String picUrl;

    @ApiModelProperty(value = "设备部位Id")
    private String cusId;

    @ApiModelProperty(value = "算法配置Id")
    private Long algorithmId;

    @ApiModelProperty(value = "状态：-2数据异常 0未完成 1正常 2异常 3算法超时 4抓图失败 5未识别")
    private String status;

    @ApiModelProperty(value = "算法结果分析")
    private String analyseResult;

    @ApiModelProperty(value = "算法分析原图")
    private String picOrignal;

    @ApiModelProperty(value = "评价状态 1误报 2漏报")
    private Integer evaluationState;

    @ApiModelProperty(value = "算法表记图片")
    private String signpic;

    @ApiModelProperty(value = "算法大类型")
    private String algorithmType;

    @ApiModelProperty(value = "算法小类型")
    private String algorithmSonType;

    @ApiModelProperty(value = "执行时间")
    private Date executeTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
