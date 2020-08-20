package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-08-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePlan对象", description = "巡检预案表")
public class TCruisePlan implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "预案ID")
    @TableId(value = "plan_id", type = IdType.AUTO)
    private Long planId;

    @ApiModelProperty(value = "关联巡检点定义实例表id")
    private Long instanceId;

    @ApiModelProperty(value = "巡检方式 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer pointType;

    @ApiModelProperty(value = "巡检区域id")
    private String cruiseRegionIds;

    @ApiModelProperty(value = "巡视异常类型:0无，1.外观缺陷异常，2.多源对比异常，3.数值越限异常")
    private Integer exceptionType;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "机器人点位或预置位点位或红外预置位")
    private String position;

    @ApiModelProperty(value = "算法实例ID")
    private Integer algorithmId;

    @ApiModelProperty(value = "算法实例名称")
    private String algorithmName;

    @ApiModelProperty(value = "红外诊断公式id")
    private String inferadAnalyze;

    @ApiModelProperty(value = "红外预置位温度框")
    private String irTempBox;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
