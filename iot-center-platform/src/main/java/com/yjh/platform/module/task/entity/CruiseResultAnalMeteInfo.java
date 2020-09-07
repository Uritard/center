package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视结果分析-实例测点信息", description = "实例测点与相关巡检点")
public class CruiseResultAnalMeteInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "测点ID")
    private Long deviceMeteId;
    @ApiModelProperty(value = "巡检点Id")
    private Long instanceId;
    @ApiModelProperty(value = "测点点位名称")
    private String instanceName;
    @ApiModelProperty(value = "识别状态")
    private Integer identifyState;
    @ApiModelProperty(value = "识别状态名")
    private String identifyStateName;
    @ApiModelProperty(value = "图片路径")
    private String picPath;
    @ApiModelProperty(value = "识别时间")
    private Date endTime;
    @ApiModelProperty(value = "设备ID")
    private Long deviceId;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
}

