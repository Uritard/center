/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.controller;

import com.yjh.accesstcp.commons.result.BusinessException;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.service.uphandler.tek.TekUpTransforServer;
import com.yjh.accesstcp.module.device.service.uphandler.tek.entity.TaskPlan;
import com.yjh.accesstcp.module.device.service.uphandler.tek.entity.TekResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 非标准的上级系统接口处理
 * @author Chenfei
 * @date 2024/12/6
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/task")
@Api(value = "/task", tags = "非标准的上级系统接口处理")
public class UpNonStandardController {
    private final TekUpTransforServer tekUpTransforServer;

    @ApiOperation(value = "任务下发")
    @PostMapping(value = "/ioms/download")
    public Result sendTask(@RequestBody TaskPlan taskPlan) {
        TekResult result = new TekResult();
        try {
            tekUpTransforServer.sendTask(taskPlan);
            result.success();
        } catch (BusinessException e) {
            result.error(e.getMessage());
            log.error("下发任务错误: {}", e.getMessage(), e);
        } catch (Exception e) {
            result.error("任务下发失败");
            log.error("失败查询描述：", e);
        }
        return result;
    }
}
