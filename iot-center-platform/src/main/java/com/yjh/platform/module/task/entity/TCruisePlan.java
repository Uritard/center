package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

/**
 * @author tt
 * @since 2020-09-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePlan对象", description = "巡检预案属性表")
public class TCruisePlan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "预案ID")
    @TableId(value = "plan_id", type = IdType.AUTO)
    @TableField(value = "plan_id", updateStrategy = FieldStrategy.IGNORED)
    private Long planId;

    @Length(max = 50, message = "planCode长度必须小于等于50")
    @ApiModelProperty(value = "预案编码")
    @TableField(value = "plan_code", updateStrategy = FieldStrategy.IGNORED)
    private String planCode;

    @Length(max = 50, message = "deviceId长度必须小于等于50")
    @ApiModelProperty(value = "device_id")
    @TableField(value = "device_id", updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @ApiModelProperty(value = "上级区域id")
    @TableField(value = "upRegionId", updateStrategy = FieldStrategy.IGNORED)
    private Long upRegionId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人id")
    @TableField(value = "robot_id",updateStrategy = FieldStrategy.IGNORED)
    private Long robotId;

    @Length(max = 32, message = "planName长度必须小于等于32")
    @ApiModelProperty(value = "预案名称")
    @TableField(value = "plan_name", updateStrategy = FieldStrategy.IGNORED)
    private String planName;

    @Max(value = 99999999999L)
    @ApiModelProperty(value = "任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer type;

    @Max(value = 99999999999L)
    @ApiModelProperty(value = "任务子类型")
    private Integer subType;

    @Length(max = 32, message = "planPointTypes长度必须小于等于32")
    @ApiModelProperty(value = "表计读数，位置状态识别，外观缺陷识别，红外测温，声音检测")
    @TableField(value = "plan_point_types", updateStrategy = FieldStrategy.IGNORED)
    private String planPointTypes;


    @ApiModelProperty(value = "创建时间")
    @TableField(value = "create_time", updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;


    @ApiModelProperty(value = "更新时间")
    @TableField(value = "update_time", updateStrategy = FieldStrategy.IGNORED)
    private Date updateTime;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
