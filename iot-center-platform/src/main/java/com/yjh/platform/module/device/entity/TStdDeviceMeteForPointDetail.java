package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.LinkedList;
import java.util.List;

/**
 * @author lqh
 * @since 2020/9/8
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDevicemete对象扩充", description = "标准设备测点关联巡检")
public class TStdDeviceMeteForPointDetail extends TStdDeviceMete{

    private String deviceName;

    private String customName;

    private String cruiseType;

    private String cruiseId;

    private String cruiseTypeName;

    //private List<Long> meteIds;

    private CruisePointType robotType = new CruisePointType();
    private CruisePointType cameraType = new CruisePointType();
    //private CruisePointType infraredType = new CruisePointType();
    private CruisePointType voiceType = new CruisePointType();

//    private CruisePointType robotType ;
//    private CruisePointType cameraType ;
//    private CruisePointType infraredType ;
//    private CruisePointType voiceType ;

    


}
