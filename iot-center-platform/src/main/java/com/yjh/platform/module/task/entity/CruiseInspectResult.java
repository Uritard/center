package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视监控-机器人巡检点信息表", description = "机器人&巡检点信息表")
public class CruiseInspectResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;
    @ApiModelProperty(value = "保存类型")
    private String saveTypeList;
    @ApiModelProperty(value = "巡检点名称")
    private String instanceName;
    @ApiModelProperty(value = "巡检结果名称")
    private String cruiseResultName;
    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    @ApiModelProperty(value = "数据来源")
    private Integer cruiseType;
    private  String cruiseTypeName;
    @ApiModelProperty(value = "巡检状态")
    private String cruiseStatus;
    @ApiModelProperty(value = "是否告警")
    private String isWarn;
    @ApiModelProperty(value = "单位")
    private String unit;
    @ApiModelProperty(value = "图片路径")
    private String imagePath;
    @ApiModelProperty(value = "视频与流地址信息列表")
    private Map<String,String> videoInfo;


}

