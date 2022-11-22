package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author YC
 * @date 2021/1/13 13:57
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视结果分析-实例测点信息", description = "实例测点与相关巡检点")
public class CruiseResultAnalyzeMeteInfo extends CruiseResultAnalMeteInfo{
    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "测点ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "巡检点Id")
    private Long instanceId;

    @ApiModelProperty(value = "测点点位名称")
    private String cruiseName;

    @ApiModelProperty(value = "数据状态")
    private Integer cruiseResult;

    @ApiModelProperty(value = "数据状态名")
    private String cruiseResultName;

    @ApiModelProperty(value = "实际结果")
    private Integer identifyResult;

    @ApiModelProperty(value = "实际结果名称")
    private String identifyResultName;

    @ApiModelProperty(value = "最终状态:0-异常 1-正常")
    private int finalState;

    @ApiModelProperty(value = "图片路径")
    private String picPath;

    @ApiModelProperty(value = "识别时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "测点名称")
    private String meteName;

    @ApiModelProperty(value = "数据来源")
    private String meteType;

    @ApiModelProperty(value = "表计类型")
    private Integer meterType;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "巡视时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseTime;

}
