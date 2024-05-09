package com.yjh.demo.controller;

import com.yjh.demo.entity.MessageParam;
import com.yjh.demo.entity.ResultBean;
import com.yjh.demo.service.ServerService;
import com.yjh.demo.task.AutomationTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author zilong
 * @since 2022/1/29
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */
@RestController
@RequestMapping("/demo-server")
@RequiredArgsConstructor
@Slf4j
public class DemoServerController {
    private final ServerService serverService;
    private final AutomationTask automationTask;

    @PostMapping(value = "/create")
    public ResultBean createServer(@RequestParam String station, @RequestParam String platform,
        @RequestParam(defaultValue = "10011") Integer port, @RequestParam(required = false) String sendCode) {
        try {
            String rootTag = automationTask.startProtocolTask(station, platform, true);
            return serverService.createServer(port, sendCode, rootTag);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResultBean(500, "error");
        }
    }

    @PostMapping(value = "/destroy")
    public ResultBean destroyServer() {
        serverService.destroy();
        return new ResultBean(200, "success");
    }

    @PostMapping(value = "/out-msg")
    public ResultBean sendOutMessage(@RequestBody MessageParam xml) {
        try {
            serverService.sendMessage(xml.getXml(), xml.getMsgId());
            return new ResultBean(200, "success");
        } catch (Exception e) {
            log.error("xml格式有误", e);
            return new ResultBean(500, "error");
        }
    }

}
