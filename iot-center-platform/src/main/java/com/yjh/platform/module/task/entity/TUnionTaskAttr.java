package com.yjh.platform.module.task.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

/**
 * @author tt
 * @since 2020-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TUnionTaskAttr对象", description = "联合巡视预案属性表")
public class TUnionTaskAttr implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 50,message = "unionId长度必须小于等于50")
    @ApiModelProperty(value = "关联任务表UUID")
    @TableField(value = "union_id",updateStrategy = FieldStrategy.IGNORED)
    private String unionId;

    @Max(value=999999999999999999l)
    @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "测点实例ID")
    @TableField(value = "device_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @Length(max = 32,message = "deviceCustomId长度必须小于等于32")
    @ApiModelProperty(value = "关联巡视设备部位表id")
    @TableField(value = "device_custom_id",updateStrategy = FieldStrategy.IGNORED)
    private String deviceCustomId;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否支持机器人巡视")
     @TableField(value = "if_robot",updateStrategy = FieldStrategy.IGNORED)
    private Integer ifRobot;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否支持视频巡视")
     @TableField(value = "if_video",updateStrategy = FieldStrategy.IGNORED)
    private Integer ifVideo;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否支持红外巡视")
    @TableField(value = "if_inferad",updateStrategy = FieldStrategy.IGNORED)
    private Integer ifInferad;



}
