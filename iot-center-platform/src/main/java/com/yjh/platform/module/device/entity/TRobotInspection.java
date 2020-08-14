package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TRobotInspection对象", description = "机器人巡检点信息表")
public class TRobotInspection implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备检测点编码")
    @TableId(value = "inspection_id", type = IdType.AUTO)
    private Long inspectionId;

    @ApiModelProperty(value = "机器人ID")
    private Long robotId;

    @ApiModelProperty(value = "巡检点名称")
    private String inspectionName;

    @ApiModelProperty(value = "检测点类型")
    private Integer inspectionType;

    @ApiModelProperty(value = "检测点告警上限")
    private String alarmTop;

    @ApiModelProperty(value = "检测点告警下线")
    private String alarmBottom;

    @ApiModelProperty(value = "检测点默认值")
    private String defaultValue;

    @ApiModelProperty(value = "检测点位置，0-室外 1-室内")
    private Integer inspectionPostion;

    @ApiModelProperty(value = "采集状态，0-未采集 1-已采集")
    private Integer collectStatus;

    @ApiModelProperty(value = "标定状态，0-未标定 1-已标定")
    private Integer calibrationStatus;

    @ApiModelProperty(value = "巡检结果单位")
    private String unit;


}
