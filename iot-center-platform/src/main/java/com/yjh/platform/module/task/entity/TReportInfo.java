package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Past;
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

    @Length(max = 50,message = "reportId长度必须小于等于50")
    @ApiModelProperty(value = "报表id")
    private String reportId;

    @Length(max = 64,message = "reportName长度必须小于等于64")
    @ApiModelProperty(value = "报表名称")
    private String reportName;

    @Length(max = 32,message = "reportType长度必须小于等于32")
    @ApiModelProperty(value = "报表类型")
    private String reportType;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "生成日期")
    private Date generateDate;

    @Length(max = 64,message = "reportEnvId长度必须小于等于64")
    @ApiModelProperty(value = "报表在服务器上的唯一标识")
    private String reportEnvId;

    @Length(max = 64,message = "remarks长度必须小于等于64")
    @ApiModelProperty(value = "备注")
    private String remarks;
}
