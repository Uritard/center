package com.yjh.accessvideo.module.device.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-10-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTaskResultDetail对象", description = "任务点状态详细表")
public class TCruiseTaskResultDetail implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡检点结果task_result_id+cruise_id")
    private String cruiseResultId;

    @ApiModelProperty(value = "巡检任务结果ID")
    private String taskResultId;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;

    @ApiModelProperty(value = "巡检时间")
    private Date cruiseTime;

    @ApiModelProperty(value = "巡检结束时间")
    private Date endTime;

    @ApiModelProperty(value = "状态:0-已执行 1-未执行 2-执行失败 3-未知")
    private Integer cruiseStatus;

    private String remark;


}
