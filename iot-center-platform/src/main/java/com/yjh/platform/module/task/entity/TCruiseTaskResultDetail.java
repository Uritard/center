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
@ApiModel(value = "TCruiseTaskResultDetail对象", description = "任务点状态详细表")
public class TCruiseTaskResultDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 82,message = "cruiseResultId长度必须小于等于82")
    @ApiModelProperty(value = "巡检点结果task_result_id+cruise_id")
    private String cruiseResultId;

    @Length(max = 50,message = "taskResultId长度必须小于等于50")
    @ApiModelProperty(value = "巡检任务结果ID")
    private String taskResultId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @Length(max = 50,message = "deviceName长度必须小于等于50")
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;

    @Length(max = 50,message = "instanceName长度必须小于等于50")
    @ApiModelProperty(value = "巡检点名称")
    private String instanceName;

    @Past
    @ApiModelProperty(value = "巡检时间")
    private Date cruiseTime;

    @Past
    @ApiModelProperty(value = "巡检结束时间")
    private Date endTime;

    @Max(value=999999999)
    @ApiModelProperty(value = "状态:0-已执行 1-未执行 2-执行失败 3-未知")
    private Integer cruiseStatus;

    @Length(max = 255,message = "remark长度必须小于等于255")
    private String remark;


}
