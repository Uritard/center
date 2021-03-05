package com.yjh.logs.module.log.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2021/3/4
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysLog对象扩展", description = "审计日志表")
public class LongAnalyseDetail {
    private String logType;
    private String count;
}
