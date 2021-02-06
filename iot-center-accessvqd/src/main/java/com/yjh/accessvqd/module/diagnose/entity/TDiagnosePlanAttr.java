package com.yjh.accessvqd.module.diagnose.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czh
 * @since 2021-01-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDiagnosePlanAttr对象", description = "")
public class TDiagnosePlanAttr implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField(value = "diagnose_plan_id",updateStrategy = FieldStrategy.IGNORED)
    private String diagnosePlanId;
    @TableField(value = "channel_id",updateStrategy = FieldStrategy.IGNORED)
    private String channelId;


}
