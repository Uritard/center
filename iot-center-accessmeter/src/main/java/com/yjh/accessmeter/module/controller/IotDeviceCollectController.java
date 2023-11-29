/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.controller;

import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.common.result.ResultCodeEnum;
import com.yjh.accessmeter.logs.Logs;
import com.yjh.accessmeter.module.service.SensorCollectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/29
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/iotDeviceCollect/v1")
@Api(value = "/iotDeviceCollect", tags = "电表操作接口")
@Slf4j
public class IotDeviceCollectController {

    @Resource
    private SensorCollectService sensorCollectService;

    @ApiOperation(value = "新增传物联设备配置")
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @Logs(title = "新增传物联设备配置", content = "新增传物联设备配置", logType = 2, authority = "1234")
    public Result add(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            sensorCollectService.add(id);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("增加传物联设备失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "删除传物联设备")
    @RequestMapping(value = "/delete", method = RequestMethod.GET)
    @Logs(title = "删除传物联设备", content = "新增传物联设备配置", logType = 2, authority = "1234")
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            sensorCollectService.delete(id);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("增加传物联设备失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "采集设备数据")
    @RequestMapping(value = "/collect", method = RequestMethod.GET)
    @Logs(title = "采集设备数据", content = "采集设备数据", logType = 2, authority = "1234")
    public Result collectData(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            sensorCollectService.collect(id);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("采集设备数据失败", e);
        }
        return result;
    }
}
