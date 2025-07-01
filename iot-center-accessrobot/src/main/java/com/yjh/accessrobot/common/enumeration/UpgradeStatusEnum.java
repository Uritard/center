package com.yjh.accessrobot.common.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <功能描述> 远程升级状态
 *
 * @author shaobinfen
 * @date 2025/6/27
 * @since [产品/模块版本](可选)
 */
@Getter
@AllArgsConstructor
public enum UpgradeStatusEnum {

    START_UPGRADE(1, "开始升级"),

    PACKAGE_UPLOADED(2, "版本包上传完成"),

    COMMAND_ISSUED(3, "远程升级指令已下发"),

    UPGRADE_COMPLETE(4, "升级完成");

    private final Integer value;

    private final String desc;
}
