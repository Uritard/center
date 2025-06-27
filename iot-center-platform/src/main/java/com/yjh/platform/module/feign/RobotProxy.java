/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.feign;

import com.yjh.platform.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/5/16
 * @since [产品/模块版本] （可选）
 */
@FeignClient(name = "iot-center-accessrobot")
public interface RobotProxy {
    @PostMapping(value = "robot/v1/deleteTransfer")
    Result deleteTransfer(@RequestParam(value = "edgeCode") List<String> edgeCode, @RequestParam(value = "taskId") String taskId,
        @RequestParam(value = "startTime") String startTime, @RequestParam(value = "source") String source);

    /**
     * 简易机器人模型下发
     */
    @GetMapping(value = "/simpleRobot/v1/modelSend")
    Result modelSend(@RequestParam(value = "robotCode") String robotCode, @RequestParam(value = "command") String command,
        @RequestParam(value = "path") String path);

    @GetMapping(value = "/simpleRobot/v1/upgradeSend")
    Result upgradeSend(@RequestParam(value = "robotCode") String robotCode,
                       @RequestParam(value = "userId") String userId,
                       @RequestParam(value = "packageFullPath") String packageFullPath);
}
