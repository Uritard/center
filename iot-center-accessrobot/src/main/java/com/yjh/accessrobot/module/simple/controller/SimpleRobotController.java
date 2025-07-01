/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessrobot.module.simple.controller;

import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.result.ResultCodeEnum;
import com.yjh.accessrobot.module.simple.service.ISimpleRobotService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-18
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/simpleRobot/v1")
@Api(value = "/simpleRobot", tags = "简易机器人接口")
public class SimpleRobotController {

    private final ISimpleRobotService simpleRobotService;

    private final RedisTemplate redisTemplate;

    @ApiOperation(value = "模型同步")
    @GetMapping(value = "/modelSend")
    public Result modelSend(@RequestParam(value = "robotCode") String robotCode, @RequestParam(value = "command") String command,
        @RequestParam(value = "path") String path) {
        Result result = new Result();
        try {
            result.setData(simpleRobotService.modelSend(robotCode, command, path));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("失败查询描述：", e);
        }
        return result;
    }

    @ApiOperation(value = "远程升级")
    @GetMapping(value = "/upgradeSend")
    public Result upgradeSend(@RequestParam(value = "robotCode") String robotCode,
                              @RequestParam(value = "userId") String userId,
                              @RequestParam(value = "packageFullPath") String packageFullPath) {
        Result result = new Result();
        try {
            log.info("开始下发远程升级指令, 机器人编码: {}, 版本升级包地址: {}", robotCode, packageFullPath);
            simpleRobotService.upgradeSend(robotCode, userId, packageFullPath);
        } catch (BusinessException b) {
            //重置机器人升级状态
            redisTemplate.delete("UpgradeStatus:" + robotCode);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("简易机器人远程升级失败:", e);
        }
        return result;
    }
}
