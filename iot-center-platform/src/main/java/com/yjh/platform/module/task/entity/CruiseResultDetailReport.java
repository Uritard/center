package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@ApiModel(value = "巡视结果数据详情", description = "巡视结果分析报表-组成元素")
public class CruiseResultDetailReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡视点名称")
    private String cruiseName;
    @ApiModelProperty(value = "巡视任务类型")
    private String cruiseTypeName;
    @ApiModelProperty(value = "采集数据")
    private String resultNum;
    @ApiModelProperty(value = "识别时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;
    @ApiModelProperty(value = "识别结果")
    private String identifyResultName;
    @ApiModelProperty(value = "数据来源")
    private String cTypeName;
    @ApiModelProperty(value = "识别图片")
    private String picPath;
    @ApiModelProperty(value = "序号")
    private int px;
}
