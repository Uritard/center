package com.yjh.platform.module.task.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "实时告警数据", description = "执行任务告警信息表")
public class RealTimeWarn {

    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "巡视点名称")
    private String instanceName;
    @ApiModelProperty(value = "数据来源")
    private String cruiseTypeName;
    @ApiModelProperty(value = "告警等级")
    private String warnLevelName;
    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseTime;
    @ApiModelProperty(value = "巡视点ID")
    private Long instanceId;
}
