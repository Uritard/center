package com.yjh.accesstcp.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author lqh
 * @since 2022/4/8
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "检修区域模型", description = "检修区域")
public class MaintenanceModel {
    private String configCode;//检修区域配置编码
    private String enable;//是否有效
    private String startTime;//开始时间
    private String endTime;//结束时间
    private String deviceLevel;//设备层级
    private List<String> deviceIds;
    private String deviceList;//检修设备列表
    private String coordinatePixel;//坐标框
}
