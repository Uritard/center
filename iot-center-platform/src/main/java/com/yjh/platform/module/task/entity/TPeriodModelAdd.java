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
 * @author tt
 * @since 2020-09-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TPeriodModel对象", description = "周期任务模版表")
public class TPeriodModelAdd implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "周期ID")
    @TableId(value = "period_id", type = IdType.AUTO)
    private Long periodId;

    @ApiModelProperty(value = "表达式")
    private String cronExpression;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "定时时间类型（分）")
    private String min;

    @ApiModelProperty(value = "定时时间类型（时）")
    private String hour;

    @ApiModelProperty(value = "定时时间类型（月的天数）")
    private String dayOfMonth;

    @ApiModelProperty(value = "定时时间类型（月份）")
    private String month;

    @ApiModelProperty(value = "定时时间类型（周的天数）")
    private String dayOfWeek;

    @ApiModelProperty(value = "定时时间类型（年）")
    private String year;

}
