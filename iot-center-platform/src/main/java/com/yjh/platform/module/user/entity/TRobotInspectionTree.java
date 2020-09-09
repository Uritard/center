package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-08-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotInspectionTree", description = "机器人巡检点树")
public class TRobotInspectionTree implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "机器人名字")
    private String robotName;

    @ApiModelProperty(value = "机器人类型")
    private String robotPosition;

    @ApiModelProperty(value = "巡检点ID")
    private Long inspectionId;

    @ApiModelProperty(value = "巡检点名称")
    private String inspectionName;

}
