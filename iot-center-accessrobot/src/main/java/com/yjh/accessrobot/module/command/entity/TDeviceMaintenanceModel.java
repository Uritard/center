package com.yjh.accessrobot.module.command.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/1/5
 * @since [产品/模块版本] （可选）
 */
@Data
public class TDeviceMaintenanceModel extends TDeviceMaintenance{
    private String stationName;
    private String startTime;
    private String deviceList;
    private String enable;
    private String endTime;
    private String configCode;
    private String deviceLevel;
    private String stationCode;
    private String coordinatePixel;
}
