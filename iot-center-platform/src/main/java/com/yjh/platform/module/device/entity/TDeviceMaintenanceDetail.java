package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author lqh
 * @since 2021/1/11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDeviceMaintenance对象", description = "设备区域检修表扩展")
public class TDeviceMaintenanceDetail extends TDeviceMaintenance{
    private List<IdAndNameDetail> deviceInfo;
    private List<Long> upRegionList;
}
