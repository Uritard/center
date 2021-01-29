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
 * @date 2020/10/29 - 16:56
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TaskVO对象", description = "巡检记录报表-总体情况")
public class TaskVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "站所名称")
    private String stationName;
    @ApiModelProperty(value = "巡检任务名称")
    private String taskName;
    @ApiModelProperty(value = "测点数")
    private Integer meteNum;
    @ApiModelProperty(value = "关联测点数")
    private Integer meteRelationNum;
    /*@ApiModelProperty(value = "未处理数")
    private Integer abnormalNum;*/
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "巡检时间")
    private Date cruiseDate;

}
