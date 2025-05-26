package com.yjh.accessrobot.netty.entiy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpgradeInfo: 机器人版本更新结果
 *
 * @author shaobinfen
 * @date 2025/05/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeResult {

    /**
     * 更新状态：
     * Sucess-成功
     * fail-失败
     */
    private String upgradeState;

    /**
     * 版本号
     */
    private String programVersion;

    @Override
    public String toString() {
        return "机器人升级结果{" +
                "升级状态='" + (upgradeState != null ? upgradeState : "null") + '\'' +
                ", 程序版本='" + (programVersion != null ? programVersion : "null") + '\'' +
                '}';
    }
}