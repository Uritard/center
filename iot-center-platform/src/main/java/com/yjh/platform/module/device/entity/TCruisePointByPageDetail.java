package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2021/5/26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDevicemete对象扩充", description = "标准设备测点关联巡检")
//public class TCruisePointByPageDetail extends TStdDeviceMeteForPointDetail {
public class TCruisePointByPageDetail {

    private Long deviceMeteId;

    private Long deviceId;

    private String customId;

    private String meteName;

    private String deviceName;

    private String customName;

    private String cruiseType;

    private String cruiseId;

    private String cruiseTypeName;

    //private List<Long> meteIds;

    private CruisePointType robotType = new CruisePointType();//机器人
    private CruisePointType cameraType = new CruisePointType();//视频
    //private CruisePointType infraredType = new CruisePointType();//红外
    private CruisePointType voiceType = new CruisePointType();//声纹

//    private CruisePointType robotType ;
//    private CruisePointType cameraType ;
//    private CruisePointType infraredType ;
//    private CruisePointType voiceType ;
}
