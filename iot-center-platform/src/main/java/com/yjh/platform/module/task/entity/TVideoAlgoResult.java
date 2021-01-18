package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "序号ID")
    private Long id;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "巡检点ID")
    private Long pointId;

    @Length(max = 50,message = "taskId长度必须小于等于50")
    @ApiModelProperty(value = "预案Id")
    private String taskId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "计划Id")
    private Long planId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备Id")
    private Long deviceId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "预置位Id")
    private Long presetId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备测点实例ID")
    private Long deviceMeteId;

    @Length(max = 255,message = "picUrl长度必须小于等于255")
    @ApiModelProperty(value = "图片地址")
    private String picUrl;

    @Length(max = 32,message = "cusId长度必须小于等于32")
    @ApiModelProperty(value = "设备部位Id")
    private String cusId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "算法配置Id")
    private Long algorithmId;

    @Length(max = 2,message = "status长度必须小于等于2")
    @ApiModelProperty(value = "状态：-2数据异常 0未完成 1正常 2异常 3算法超时 4抓图失败 5未识别")
    private String status;

    @Length(max = 255,message = "analyseResult长度必须小于等于255")
    @ApiModelProperty(value = "算法结果分析")
    private String analyseResult;

    @Length(max = 255,message = "picOrignal长度必须小于等于255")
    @ApiModelProperty(value = "算法分析原图")
    private String picOrignal;

    @Max(value=999999999)
    @ApiModelProperty(value = "评价状态 1误报 2漏报")
    private Integer evaluationState;

    @Length(max = 255,message = "signpic长度必须小于等于255")
    @ApiModelProperty(value = "算法表记图片")
    private String signpic;

    @Length(max = 32,message = "algorithmType长度必须小于等于32")
    @ApiModelProperty(value = "算法大类型")
    private String algorithmType;

    @Length(max = 32,message = "algorithmSonType长度必须小于等于32")
    @ApiModelProperty(value = "算法小类型")
    private String algorithmSonType;

    @Past
    @ApiModelProperty(value = "执行时间")
    private Date executeTime;

    @Past
    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
