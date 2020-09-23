package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视监控-机器人巡检点信息表", description = "机器人&巡检点信息表")
public class CameraCruiseInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String type="camera";

    @ApiModelProperty(value = "摄像头信息")
    private Integer cameraType;

    private String cameraTypeName;

    @ApiModelProperty(value = "摄像头状态数量")
    private Integer cameraNotFault; //未故障

    private Integer cameraFault; //故障

    @ApiModelProperty(value = "巡检任务完成度")
    private Float rate;

}
