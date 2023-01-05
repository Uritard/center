package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/3
 * @since [产品/模块版本] （可选）
 */
@Data
@TableName("sys_user_device_permission")
public class SysUserDevicePermissionDO {
    @TableId
    private Long id;

    @TableField
    private Long userId;

    @TableField
    private Long monitorDeviceId;
}
