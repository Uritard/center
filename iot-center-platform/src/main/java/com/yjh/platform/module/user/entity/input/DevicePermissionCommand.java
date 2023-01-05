package com.yjh.platform.module.user.entity.input;

import lombok.Data;

import java.util.List;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/3
 * @since [产品/模块版本] （可选）
 */
@Data
public class DevicePermissionCommand {

    private List<Long> deviceIds;

    private Long userId;

}
