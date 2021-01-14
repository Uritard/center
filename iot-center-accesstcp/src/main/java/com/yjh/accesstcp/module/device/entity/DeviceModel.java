package com.yjh.accesstcp.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2021/1/14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "设备模型", description = "设备模型封装类")
public class DeviceModel {
    private String device_id;
    private String device_name;
    private String component_id;
    private String component_name;
    private String bay_id;
    private String bay_name;
    private String main_device_id;
    private String main_device_name;
    private String device_type;
    private String meter_type;
    private String appearance_type;
    private String save_type_lis;
    private String recognition_type_list;
    private String phase;
    private String device_info;
    private String data_type;
    private String lower_value;
    private String upper_value;
    private String video_pos;
}
