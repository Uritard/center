package com.yjh.platform.module.simple.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * <功能描述>
 * @author YIJIAHE
 * @date 2023/5/25
 * @since [产品/模块版本] （可选）
 */
@Data
public class SimpleDeviceExcel {

    @ExcelProperty("变电站名称")
    private String stationName;

    @ExcelProperty("电压等级")
    private String voltageLevel;

    @ExcelProperty("区域名称")
    private String areaName;

    @ExcelProperty("间隔名称")
    private String bayName;

    @ExcelProperty("设备名称")
    private String deviceName;

    @ExcelProperty("X1")
    private Double x1;

    @ExcelProperty("Y1")
    private Double y1;

    @ExcelProperty("Z1")
    private Double z1;

    @ExcelProperty("X2")
    private Double x2;

    @ExcelProperty("Y2")
    private Double y2;

    @ExcelProperty("Z2")
    private Double z2;

    @ExcelProperty("X3")
    private Double x3;

    @ExcelProperty("Y3")
    private Double y3;

    @ExcelProperty("Z3")
    private Double z3;

    @ExcelProperty("X4")
    private Double x4;

    @ExcelProperty("Y4")
    private Double y4;

    @ExcelProperty("Z4")
    private Double z4;

}
