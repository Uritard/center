package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author: lqh
 * @Date: 2023/04/10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "非同源告警信息对象")
public class NonhomologousInfo {

    private String warnId;

    private Long one;

    private Long two;

    private Long three;
}
