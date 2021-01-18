package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备检测点编码")
    @TableId(value = "inspection_id", type = IdType.AUTO)
    private Long inspectionId;

    @Length(max = 68,message = "inspectionCode长度必须小于等于68")
    @ApiModelProperty(value = "机器人检测点编码")
    private String inspectionCode;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人ID")
    private Long robotId;

    @Length(max = 60,message = "inspectionName长度必须小于等于60")
    @ApiModelProperty(value = "巡检点名称")
    private String inspectionName;

    @Max(value=999999999)
    @ApiModelProperty(value = "检测点类型")
    private Integer inspectionType;

    @Length(max = 20,message = "alarmTop长度必须小于等于20")
    @ApiModelProperty(value = "检测点告警上限")
    private String alarmTop;

    @Length(max = 20,message = "alarmBottom长度必须小于等于20")
    @ApiModelProperty(value = "检测点告警下线")
    private String alarmBottom;

    @Length(max = 60,message = "defaultValue长度必须小于等于60")
    @ApiModelProperty(value = "检测点默认值")
    private String defaultValue;

    @Max(value=999999999)
    @ApiModelProperty(value = "检测点位置，0-室外 1-室内")
    private Integer inspectionPosition;

    @Max(value=999999999)
    @ApiModelProperty(value = "采集状态，0-未采集 1-已采集")
    private Integer collectStatus;

    @Max(value=999999999)
    @ApiModelProperty(value = "标定状态，0-未标定 1-已标定")
    private Integer calibrationStatus;

    @Length(max = 20,message = "unit长度必须小于等于20")
    @ApiModelProperty(value = "巡检结果单位")
    private String unit;


}
