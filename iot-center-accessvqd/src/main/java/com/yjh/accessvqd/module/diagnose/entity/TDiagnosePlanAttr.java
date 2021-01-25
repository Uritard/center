package com.yjh.accessvqd.module.diagnose.entity;

import java.io.Serializable;
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


    private String diagnosePlanId;

    private String channelId;


}
