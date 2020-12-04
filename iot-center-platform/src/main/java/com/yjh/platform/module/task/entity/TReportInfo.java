package com.yjh.platform.module.task.entity;

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
@ApiModel(value = "TReportInfo对象 ", description = "报表信息表")
public class TReportInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "报表id")
    private String reportId;

    @ApiModelProperty(value = "报表名称")
    private String reportName;

    @ApiModelProperty(value = "报表类型")
    private String reportType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "生成日期")
    private Date generateDate;

    @ApiModelProperty(value = "报表在服务器上的唯一标识")
    private String reportEnvId;

    @ApiModelProperty(value = "备注")
    private String remarks;
}
