package com.yjh.accessvideo.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author tt
 * @since 2020-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "Analysis对象", description = "算法分析类")
public class Analysis implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "任务ID")
    private String taskId;

    @ApiModelProperty(value = "巡检点实例ID")
    private Long instanceId;

    @ApiModelProperty(value = "算法类型")
    private String analyseType;

    @ApiModelProperty(value = "巡检图片路径")
    private String picPath;

    @ApiModelProperty(value = "模版图片路径")
    private String picModelPath;

    @ApiModelProperty(value = "CSV文件路径")
    private String csvPath;

    @ApiModelProperty(value = "红外图谱辅助Data文件路径")
    private String dataPath;

    @ApiModelProperty(value = "是否AI算法 0是1否")
    private int isAi;


}
