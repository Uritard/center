package com.yjh.accesstcp.module.device.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/9
 * @since [产品/模块版本] （可选）
 */
@Data
public class CameraModel extends TCameraInfo {
    private String patroldeviceName;
    private String patroldeviceCode;
    private String stationName;
    private String stationCode;
    private String deviceModel;
    private String manufacturer;
    private String useUnit;
    private String deviceSource;
    private String productionDate;
    private String productionCode;
    private String istransport;
    private String useMode;
    private String videoMode;
    private String place;
    private String type;
    private String patroldeviceInfo;
    private String robotsCode;
}
