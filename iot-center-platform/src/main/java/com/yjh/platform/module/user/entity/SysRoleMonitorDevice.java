package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysRoleMonitorDevice", description = "角色-监视设备关系实体")
public class SysRoleMonitorDevice {

    private Long id;

    private Long roleId;

    private Long monitorDeviceId;
}
