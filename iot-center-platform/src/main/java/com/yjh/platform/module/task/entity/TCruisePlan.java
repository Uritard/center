package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "预案ID")
    @TableId(value = "plan_id", type = IdType.AUTO)
    private Long planId;

    @Length(max = 32,message = "planName长度必须小于等于32")
    @ApiModelProperty(value = "预案名称")
    private String planName;

    @Max(value=99999999999l)
    @ApiModelProperty(value = "任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer type;

    @Length(max = 32,message = "planPointTypes长度必须小于等于32")
    @ApiModelProperty(value = "表计读数，位置状态识别，外观缺陷识别，红外测温，声音检测")
    private String planPointTypes;

    @Past
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Past
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
