package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/9/9
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgMete对象扩充", description = "四遥标准设备测点关联巡检")
public class TCfgMeteForPointDetail  extends TCfgMete{

    private String deviceId;
    private String deviceName;

    private String cruiseType;

    private String cruiseId;

    private String cruiseTypeName;

    private String meteKindName;

    private CruisePointType robotType = new CruisePointType();
    private CruisePointType cameraType = new CruisePointType();
    private CruisePointType infraredType = new CruisePointType();
    private CruisePointType voiceType = new CruisePointType();
}
