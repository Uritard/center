/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.controller;

import com.yjh.accessmeter.common.result.BusinessException;
import com.yjh.accessmeter.common.result.Result;
import com.yjh.accessmeter.common.result.ResultCodeEnum;
import com.yjh.accessmeter.module.service.SensorCollectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @ApiOperation(value = "新增物联设备配置")
    @GetMapping(value = "/add")
    public Result add(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(sensorCollectService.add(id));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("增加物联设备失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "删除物联设备")
    @GetMapping(value = "/delete")
    public Result delete(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(sensorCollectService.delete(id));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("删除物联设备失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "更新物联设备")
    @GetMapping(value = "/update")
    public Result update(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            result.setData(sensorCollectService.update(id));
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("更新物联设备失败：", e);
        }
        return result;
    }

    @ApiOperation(value = "采集设备数据")
    @GetMapping(value = "/collect")
    public Result collectData(@RequestParam("id") Long id) {
        Result result = new Result();
        try {
            sensorCollectService.collect(id);
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("采集设备数据失败", e);
        }
        return result;
    }
}
