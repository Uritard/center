package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "规则编号")
    @TableId(value = "rule_id", type = IdType.AUTO)
    @TableField(value = "rule_id",updateStrategy = FieldStrategy.IGNORED)
    private Long ruleId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "预案id")
    @TableField(value = "plan_id",updateStrategy = FieldStrategy.IGNORED)
    private Long planId;

    @Length(max = 256,message = "password长度必须小于等于256")
    @ApiModelProperty(value = "规则名称")
    @TableField(value = "rule_name",updateStrategy = FieldStrategy.IGNORED)
    private String ruleName;

    @Length(max = 256,message = "password长度必须小于等于256")
    @ApiModelProperty(value = "是否生成联动监控弹窗0.不生成   1.生成")
    @TableField(value = "rule_type",updateStrategy = FieldStrategy.IGNORED)
    private String ruleType;

    @Length(max = 256,message = "password长度必须小于等于256")
    @ApiModelProperty(value = "具体治理规则")
    @TableField(value = "rule_content",updateStrategy = FieldStrategy.IGNORED)
    private String ruleContent;

    @Max(value=999999999)
    @ApiModelProperty(value = "延时发送时间")
    private Integer ruleDelay;

    @Length(max = 500,message = "description长度必须小于等于500")
    @ApiModelProperty(value = "描述")
      @TableField(value = "description",updateStrategy = FieldStrategy.IGNORED)
    private String description;

    @ApiModelProperty(value = "入参数据,meteId,meteName")
     @TableField(value = "input_param",updateStrategy = FieldStrategy.IGNORED)
    private String inputParam;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "联动监控全景摄像机Id")
    private Long cameraId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "联动监控全景摄像机对应预置位Id")
    private Long presetId;


}
