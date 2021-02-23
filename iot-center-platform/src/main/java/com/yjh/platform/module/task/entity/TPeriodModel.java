package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

/**
 * @author tt
 * @since 2020-09-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TPeriodModel对象", description = "周期任务模版表")
public class TPeriodModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "周期ID")
    @TableId(value = "period_id", type = IdType.AUTO)
    @TableField(value = "period_id", updateStrategy = FieldStrategy.IGNORED)
    private Long periodId;

    @Length(max = 255, message = "cronExpression长度必须小于等于255")
    @ApiModelProperty(value = "表达式")
    @TableField(value = "cron_expression", updateStrategy = FieldStrategy.IGNORED)
    private String cronExpression;

    @Length(max = 512, message = "remark长度必须小于等于512")
    @ApiModelProperty(value = "备注")
    @TableField(value = "remark", updateStrategy = FieldStrategy.IGNORED)
    private String remark;


    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time", updateStrategy = FieldStrategy.IGNORED)
    private Date updateTime;


    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "create_time", updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
