package com.yjh.platform.module.device.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/17
 * @since [产品/模块版本] （可选）
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceCountBean {

    public int count;

    private String deviceType;

    private String deviceTypeName;

}
