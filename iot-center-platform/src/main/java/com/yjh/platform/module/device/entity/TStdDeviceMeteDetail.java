package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author lqh
 * @since 2020/9/3
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDevicemete对象扩充", description = "标准设备测点表扩充")
public class TStdDeviceMeteDetail extends TStdDeviceMete{


    
    private String meteKindName;

    private String unitName;

    private String customType;

    private String customTypeName;

    private String deviceName;

    private String alarmTypeName;

    private String meteTypeName;



    private String alarmLevelName;

    private Long upRegionId;
    private List<Long> ids;

    private Long algorithmId;
    private String algorithmName;

}

