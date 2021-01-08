package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseDataResultDetail对象", description = "巡检记录报表-明细")
public class ResultAnalyseReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "设备基本信息")
    private DeviceBaseReport deviceBaseR;
    @ApiModelProperty(value = "测点信息")
    private DeviceMeteBaseReport deivceMeteBaseR;
    @ApiModelProperty(value = "巡视结果详情")
    private List<CruiseResultDetailReport> CRDR;
}
