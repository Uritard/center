package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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

    @ApiModelProperty(value = "数据来源")
    private String meteType;
    @ApiModelProperty(value = "表计类型")
    private Integer meterType;
    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

}
