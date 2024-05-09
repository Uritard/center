/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.controller;

import com.yjh.demo.entity.BaseTree;
import com.yjh.demo.entity.ResultBean;
import com.yjh.demo.task.AutomationTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/7
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/automation")
@RequiredArgsConstructor
@Slf4j
public class AutomationController {

    private final AutomationTask automationTask;

    @GetMapping(value = "/protocolTree")
    public ResultBean protocolTree(@RequestParam(required = false) String station, @RequestParam(required = false) String platform) {
        try {
            List<BaseTree> tree = automationTask.protocolTree(station, platform);
            return new ResultBean(200, "success").setData(tree);
        } catch (Exception e) {
            log.error("获取协议树失败", e);
            return new ResultBean(500, "error");
        }
    }

    @GetMapping(value = "/messageContent")
    public ResultBean messageContent(@RequestParam String station, @RequestParam String platform, @RequestParam String protocol,
        @RequestParam String message, @RequestParam boolean isServer) {
        try {
            String messageXml = automationTask.messageContent(station, platform, protocol, message, isServer);
            return new ResultBean(200, "success").setData(messageXml);
        } catch (Exception e) {
            log.error("获取协议内容失败", e);
            return new ResultBean(500, "error");
        }
    }

    @PostMapping(value = "/startAutotest")
    public ResultBean startAutotest(@RequestParam boolean server) {
        try {
            automationTask.protocolTaskRunning(server);
            return new ResultBean(200, "success");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResultBean(500, e.getMessage());
        }
    }

    @PostMapping(value = "/autoReply")
    public ResultBean autoReply(@RequestParam boolean autoReply) {
        try {
            automationTask.autoReply(autoReply);
            return new ResultBean(200, "success");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResultBean(500, e.getMessage());
        }
    }
}
