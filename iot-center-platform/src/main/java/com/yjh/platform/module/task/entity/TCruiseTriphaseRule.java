package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author lqh
 * @since 2023-03-18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTriphaseRule对象", description = "三相告警配置实例表")
public class TCruiseTriphaseRule implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "三相告警规则ID")
    @TableId(value = "triphase_id", type = IdType.AUTO)
    private Long triphaseId;

    @ApiModelProperty(value = "三相告警规则名称")
    private String triphaseName;

    @ApiModelProperty(value = "测点实例ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "关联设备id")
    private Long deviceId;

    @ApiModelProperty(value = "关联部位表id")
    private String customId;

    @ApiModelProperty(value = "巡视点id 1")
    private Long instanceOneId;

    @ApiModelProperty(value = "巡视点id 1名称")
    private String instanceOneName;

    @ApiModelProperty(value = "巡视点1巡视设备名称")
    private String oneCruiseDeviceName;

    @ApiModelProperty(value = "巡视点id 2")
    private Long instanceTwoId;

    @ApiModelProperty(value = "巡视点id 2名称")
    private String instanceTwoName;

    @ApiModelProperty(value = "巡视点2巡视设备名称")
    private String twoCruiseDeviceName;

    @ApiModelProperty(value = "巡视点id 3")
    private Long instanceTriId;

    @ApiModelProperty(value = "巡视点id 3名称")
    private String instanceTriName;

    @ApiModelProperty(value = "巡视点3巡视设备名称")
    private String triCruiseDeviceName;

    @ApiModelProperty(value = "点位识别类型 1. 表计读数，2红外测温")
    private Integer identifyType;

    @ApiModelProperty(value = "点位识别子类型(若选取表计读数再细分)： 1.油位表、2.避雷器动作次数表、3.泄漏电流表、4.档位表、5.SF6压力表、6.油温表、7.开关动作次数表、8.气压表、9液压表")
    private Integer identifySonType;

    @ApiModelProperty(value = "三相告警类型 1：三相不平衡 2：三相温差")
    private Integer triphaseType;

    @ApiModelProperty(value = "告警阈值")
    private String warnThreshold;

    @ApiModelProperty(value = "告警等级：1-预警，2-一般告警，3-严重告警，4-危急告警，")
    private Integer warnLevel;


}
