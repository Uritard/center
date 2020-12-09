package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/12/9
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdMete对象", description = "系统测点信息表")
public class TStdMeteDetail extends TStdMete {
    private String deviceTypeName;

    private String meteTypeName;

    private String meteKindName;

    private String alarmTypeName;

    private String alarmLevelName;
}
