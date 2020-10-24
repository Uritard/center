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
 * @author YC
 * @date 2020/10/23 - 11:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "ReportForms", description = "报表临时存储")
public class ReportForms implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "报表名称")
    private String reportName;
    @ApiModelProperty(value = "报表类型")
    private String reportType;
    @ApiModelProperty(value = "发起时间")
    private String startTime;
}
