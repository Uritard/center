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


    @ApiModelProperty(value = "预案ID")
    @TableId(value = "plan_id", type = IdType.AUTO)
    private Long planId;

    @ApiModelProperty(value = "预案名称")
    private String planName;

    @ApiModelProperty(value = "任务类型1. 全面2. 例行3. 熄灯4. 特殊5. 专项 6.自定义")
    private Integer type;

    @ApiModelProperty(value = "表计读数，位置状态识别，外观缺陷识别，红外测温，声音检测")
    private String planPointTypes;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;


}
