package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

/**
 * @author tt
 * @since 2020-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePlanAttr对象", description = "巡检预案属性表")
public class TCruisePlanAttr implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "预案ID")
    @TableField(value = "plan_id",updateStrategy = FieldStrategy.IGNORED)
    private Long planId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "关联巡检点定义实例表id")
    @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

    @Max(value=999999999)
    @ApiModelProperty(value = "巡检方式 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer pointType;

    @Length(max = 32,message = "areaId长度必须小于等于32")
    @ApiModelProperty(value = "区域ID")
     @TableField(value = "area_id",updateStrategy = FieldStrategy.IGNORED)
    private String areaId;

    @Length(max = 256,message = "cruiseRegionIds长度必须小于等于256")
    @ApiModelProperty(value = "巡检区域id")
    @TableField(value = "cruise_region_ids",updateStrategy = FieldStrategy.IGNORED)
    private String cruiseRegionIds;

    @Max(value=999999999)
    @ApiModelProperty(value = "巡视异常类型:0无，1.外观缺陷异常，2.多源对比异常，3.数值越限异常")
    private Integer exceptionType;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人id")
     @TableField(value = "robot_id",updateStrategy = FieldStrategy.IGNORED)
    private Long robotId;

    @Length(max = 32,message = "position长度必须小于等于32")
    @ApiModelProperty(value = "机器人点位或预置位点位或红外预置位")
    @TableField(value = "position",updateStrategy = FieldStrategy.IGNORED)
    private String position;

    @Max(value=99999999999l)
    @ApiModelProperty(value = "算法实例ID")
     @TableField(value = "algorithm_id",updateStrategy = FieldStrategy.IGNORED)
    private Long algorithmId;

    @Length(max = 256,message = "inferadAnalyze长度必须小于等于256")
    @ApiModelProperty(value = "红外诊断公式id")
    @TableField(value = "inferad_analyze",updateStrategy = FieldStrategy.IGNORED)
    private String inferadAnalyze;

    @Length(max = 32,message = "irTempBox长度必须小于等于32")
    @ApiModelProperty(value = "红外预置位温度框")
     @TableField(value = "ir_temp_box",updateStrategy = FieldStrategy.IGNORED)
    private String irTempBox;

    @Past
    @ApiModelProperty(value = "创建时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
     @TableField(value = "create_time",updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;

    @Past
    @ApiModelProperty(value = "更新时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField(value = "update_time",updateStrategy = FieldStrategy.IGNORED)
    private Date updateTime;

    @Max(value=999999999)
    @ApiModelProperty(value = "任务子类型")
    private Integer subType;


}
