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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    @ApiOperation(value = "模型同步")
    @RequestMapping(value = "/modelSend", method = RequestMethod.GET)
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
}
