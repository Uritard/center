package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lqh
 * @since 2020-09-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgUnionRule对象", description = "联动规则表")
public class TCfgUnionRule implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "规则编号")
    @TableId(value = "rule_id", type = IdType.AUTO)
    private Long ruleId;

    @ApiModelProperty(value = "预案id")
    private Long planId;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @ApiModelProperty(value = "是否生成联动监控弹窗0.不生成   1.生成")
    private String ruleType;

    @ApiModelProperty(value = "具体治理规则")
    private String ruleContent;

    @ApiModelProperty(value = "延时发送时间")
    private Integer ruleDelay;

    @ApiModelProperty(value = "描述")
    private String description;

    @ApiModelProperty(value = "入参数据,meteId,meteName")
    private String inputParam;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "联动监控全景摄像机Id")
    private Long cameraId;

    @ApiModelProperty(value = "联动监控全景摄像机对应预置位Id")
    private Long presetId;


}
