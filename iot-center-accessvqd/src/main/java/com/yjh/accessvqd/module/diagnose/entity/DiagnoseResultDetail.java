package com.yjh.accessvqd.module.diagnose.entity;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author czh
 * @since 2020-08-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "诊断结果详情对象", description = "视频诊断-诊断结果详情")
public class DiagnoseResultDetail extends ChanResult {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "监控点名称-cameraName")
    private String channelName;
    @ApiModelProperty(value = "IP")
    private String ip;
    @ApiModelProperty(value = "Port")
    private String port;
    @ApiModelProperty(value = "设备类型-isControl")
    private String devType;
    @ApiModelProperty(value = "厂商-vendor")
    private String devBrand;
    @ApiModelProperty(value = "区域-region")
    private String regionName;
    @ApiModelProperty(value = "分辨率")
    private String resolving;


}



