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
 * @since 2020-09-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TPeriodModel对象", description = "周期任务模版表")
public class TPeriodModel implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "周期ID")
    @TableId(value = "period_id", type = IdType.AUTO)
    private Long periodId;

    @ApiModelProperty(value = "分")
    private String cronExpression;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
