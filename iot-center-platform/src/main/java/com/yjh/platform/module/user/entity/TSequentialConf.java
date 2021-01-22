package com.yjh.platform.module.user.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2021-01-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TSequentialConf对象", description = "顺控配置表")
public class TSequentialConf implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备编号")
    private String cfgDeviceId;

    private String cfgDeviceName;

    @ApiModelProperty(value = "监控量编号")
    private String cfgMeteId;

    private String cfgMeteName;

    @ApiModelProperty(value = "预置位id")
    private Long presetId;

    private String presetName;

    @ApiModelProperty(value = "识别结果")
    private String identifyResult;


}
