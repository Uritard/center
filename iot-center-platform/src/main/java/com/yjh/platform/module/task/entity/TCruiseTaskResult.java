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
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTaskResult对象", description = "任务点状态表")
public class TCruiseTaskResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 50,message = "taskResultId长度必须小于等于50")
    @ApiModelProperty(value = "任务结果UUID")
    private String taskResultId;

    @Length(max = 50,message = "taskId长度必须小于等于50")
    @ApiModelProperty(value = "巡检任务ID")
    private String taskId;

    @Length(max = 50,message = "taskName长度必须小于等于50")
    @ApiModelProperty(value = "巡检任务名称")
    private String taskName;

    @Max(value=999999999)
    @ApiModelProperty(value = "异常数量")
    private Integer taskAbnormal;

    @Max(value=999999999)
    private Integer taskAlarm;

    @Length(max = 30,message = "runExecute长度必须小于等于30")
    @ApiModelProperty(value = "执行类型")
    private String runExecute;

    @Past
    @ApiModelProperty(value = "巡检时间")
    private Date cruiseTaskTime;

    @Max(value=999999999)
    @ApiModelProperty(value = "状态:0-进行中 1-已完成 2-未完成 3-任务中断")
    private Integer taskStatus;

    @Max(value=999999999)
    @ApiModelProperty(value = "巡视结果：0-正常 1-异常")
    private Integer cruiseResult;

    @Length(max = 255,message = "remark长度必须小于等于255")
    @ApiModelProperty(value = "备注")
    private String remark;


}
