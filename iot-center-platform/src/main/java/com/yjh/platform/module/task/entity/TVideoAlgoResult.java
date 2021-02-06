package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField(value = "id",updateStrategy = FieldStrategy.IGNORED)
    private Long id;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "巡检点ID")
    @TableField(value = "point_id",updateStrategy = FieldStrategy.IGNORED)
    private Long pointId;

    @Length(max = 50,message = "taskId长度必须小于等于50")
    @ApiModelProperty(value = "预案Id")
     @TableField(value = "task_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "计划Id")
    @TableField(value = "plan_id",updateStrategy = FieldStrategy.IGNORED)
    private Long planId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备Id")
    @TableField(value = "preset_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "预置位Id")
    private Long presetId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备测点实例ID")
    @TableField(value = "device_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @Length(max = 255,message = "picUrl长度必须小于等于255")
    @ApiModelProperty(value = "图片地址")
    @TableField(value = "pic_url",updateStrategy = FieldStrategy.IGNORED)
    private String picUrl;

    @Length(max = 32,message = "cusId长度必须小于等于32")
    @ApiModelProperty(value = "设备部位Id")
    @TableField(value = "cus_id",updateStrategy = FieldStrategy.IGNORED)
    private String cusId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "算法配置Id")
     @TableField(value = "algorithm_id",updateStrategy = FieldStrategy.IGNORED)
    private Long algorithmId;

    @Length(max = 2,message = "status长度必须小于等于2")
    @ApiModelProperty(value = "状态：-2数据异常 0未完成 1正常 2异常 3算法超时 4抓图失败 5未识别")
    @TableField(value = "status",updateStrategy = FieldStrategy.IGNORED)
    private String status;

    @Length(max = 255,message = "analyseResult长度必须小于等于255")
    @ApiModelProperty(value = "算法结果分析")
    @TableField(value = "analyse_result",updateStrategy = FieldStrategy.IGNORED)
    private String analyseResult;

    @Length(max = 255,message = "picOrignal长度必须小于等于255")
    @ApiModelProperty(value = "算法分析原图")
    @TableField(value = "pic_orignal",updateStrategy = FieldStrategy.IGNORED)
    private String picOrignal;

    @Max(value=999999999)
    @ApiModelProperty(value = "评价状态 1误报 2漏报")
    private Integer evaluationState;

    @Length(max = 255,message = "signpic长度必须小于等于255")
    @ApiModelProperty(value = "算法表记图片")
    @TableField(value = "signpic",updateStrategy = FieldStrategy.IGNORED)
    private String signpic;

    @Length(max = 32,message = "algorithmType长度必须小于等于32")
    @ApiModelProperty(value = "算法大类型")
     @TableField(value = "algorithm_type",updateStrategy = FieldStrategy.IGNORED)
    private String algorithmType;

    @Length(max = 32,message = "algorithmSonType长度必须小于等于32")
    @ApiModelProperty(value = "算法小类型")
     @TableField(value = "algorithm_son_type",updateStrategy = FieldStrategy.IGNORED)
    private String algorithmSonType;

    @Past
    @ApiModelProperty(value = "执行时间")
    private Date executeTime;

    @Past
    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
