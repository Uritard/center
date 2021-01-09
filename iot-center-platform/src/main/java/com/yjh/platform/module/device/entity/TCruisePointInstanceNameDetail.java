package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/12/25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePointInstance对象", description = "巡检点实例表")
public class TCruisePointInstanceNameDetail extends TCruisePointInstance{
    private String deviceName;
    private String instanceName;
    private String cruiseName;
    private String analyseType;


}
