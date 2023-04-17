package com.yjh.accessrobot.module.command.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/3/22
 * @since [产品/模块版本] （可选）
 */
@Data
public class ExcelEntity implements Serializable {
    @ExcelProperty(value = "变电站名称")
    private String stationName;
    @ExcelProperty(value = "变电站编码")
    private String stationId;
    @ExcelProperty(value = "区域名称")
    private String areaName;
    @ExcelProperty(value = "区域ID")
    private String areaId;
    @ExcelProperty(value = "间隔名称")
    private String regionName;
    @ExcelProperty(value = "间隔ID")
    private String regionId;
    @ExcelProperty(value = "主设备编码")
    private String mainDeviceId;
    @ExcelProperty(value = "设备类型")
    private String deviceTypeId;
    private String deviceTypeName;
    @ExcelProperty(value = "设备名称")
    private String mainDeviceName;
    @ExcelProperty(value = "设备部件名称")
    private String componentName;
    @ExcelProperty(value = "设备部件ID")
    private String componentId;
    @ExcelProperty(value = "设备点位名称")
    private String inspectionName;
    @ExcelProperty(value = "设备点位ID")
    private String inspectionCode;
    @ExcelProperty(value = "采集文件类型")
    private String fileType;
    @ExcelProperty(value = "识别类型")
    private String meteTypeId;
    private String meteTypeName;
    @ExcelProperty(value = "实物ID")
    private String realCode;
    @ExcelProperty(value = "采集装置名称")
    private String patrolDeviceName;
    @ExcelProperty(value = "采集装置ID")
    private String patrolDeviceId;
}
