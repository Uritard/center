/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessrobot.module.simple.service;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-18
 * @since [产品/模块版本] （可选）
 */
public interface ISimpleRobotService {

    /**
     * 简易机器人模型下发
     * @param robotCode 机器人编码
     * @param command 模型下发指令
     * @param path 模型文件路径
     * @return 成功
     */
    int modelSend(String robotCode, String command, String path);

    /**
     * 简易机器人远程升级
     * @param robotCode 机器人编码
     * @param userId 操作用户id
     * @param packageFullPath 版本升级包全路径
     */
    void upgradeSend(String robotCode, String userId, String packageFullPath);
}
