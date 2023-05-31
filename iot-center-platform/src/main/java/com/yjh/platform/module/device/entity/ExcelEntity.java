package com.yjh.platform.module.device.entity;

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
    @ExcelProperty(value = "变电站编码")
    private String stationId;
    @ExcelProperty(value = "变电站名称")
    private String stationName;
    @ExcelProperty(value = "区域ID")
    private String areaId;
    @ExcelProperty(value = "区域名称")
    private String areaName;
    @ExcelProperty(value = "间隔ID")
    private String regionId;
    @ExcelProperty(value = "间隔名称")
    private String regionName;
    @ExcelProperty(value = "主设备编码")
    private String mainDeviceId;
    @ExcelProperty(value = "设备名称")
    private String mainDeviceName;
    @ExcelProperty(value = "设备类型")
    private String deviceTypeName;
    @ExcelProperty(value = "测点ID")
    private String meteId;
    @ExcelProperty(value = "测点名称")
    private String meteName;
    @ExcelProperty(value = "位置类型")
    private String positionType;
    @ExcelProperty(value = "识别类型")
    private String meteTypeName;
    @ExcelProperty(value = "测点级别")
    private String redundantType;
    @ExcelProperty(value = "设备部位")
    private String customName;
    @ExcelProperty(value = "表计类型")
    private String meterTypeName;
    @ExcelProperty(value = "识别算法")
    private String analyseTypeName;
    @ExcelProperty(value = "AI缺陷")
    private String isAi;
    @ExcelProperty(value = "AI判别")
    private String isJudge;
    @ExcelProperty(value = "测点类型")
    private String meteKindName;
    @ExcelProperty(value = "是否告警弹框")
    private String alarmNote;
    @ExcelProperty(value = "告警上限1")
    private String highLimit1;
    @ExcelProperty(value = "告警上限2")
    private String highLimit2;
    @ExcelProperty(value = "告警上限3")
    private String highLimit3;
    @ExcelProperty(value = "告警上限4")
    private String highLimit4;
    @ExcelProperty(value = "告警下限1")
    private String lowLimit1;
    @ExcelProperty(value = "告警下限2")
    private String lowLimit2;
    @ExcelProperty(value = "告警下限3")
    private String lowLimit3;
    @ExcelProperty(value = "告警下限4")
    private String lowLimit4;
    @ExcelProperty(value = "单位")
    private String unit;
    @ExcelProperty(value = "告警级别")
    private String alarmLevelName;
    @ExcelProperty(value = "测点属性")
    private String inspectionType;
    private Integer alarmLevel;
    private Integer meteKindId;
    private String deviceTypeId;
    private String meteTypeId;
    private String customId;
    private Integer meterTypeId;
    private Integer analyseTypeId;

    private Long deviceId;

    private String errorInfo;
}
