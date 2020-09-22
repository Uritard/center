package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/9/18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgUnionRule对象扩展", description = "联动规则表扩展")
public class TCfgUnionRuleDetail extends TCfgUnionRule {

    private String planName;

    private String ruleForShow;
}
