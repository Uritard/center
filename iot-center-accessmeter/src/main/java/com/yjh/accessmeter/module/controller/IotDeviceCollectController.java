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
import org.apache.commons.collections4.MapUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

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

    @ApiOperation(value = "采集所有电表电量数据")
    @GetMapping(value = "/collectMeterDataTask")
    public Result collectMeterDataTask() {
        Result result = new Result();
        try {
            sensorCollectService.collectMeterDataTask();
            result.setCode(ResultCodeEnum.NORMAL.getCode());
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("采集设备数据失败", e);
        }
        return result;
    }

    @ApiOperation(value = "智能环境设备控制")
    @PostMapping(value = "/envDeviceControl")
    public Result envDeviceControl(@RequestBody Map<String, Object> map) {
        log.info("智能环境设备控制");
        Result result = new Result();
        try {
            if (MapUtils.isNotEmpty(map)) {
                return sensorCollectService.envDeviceControl(map);
            } else {
                result.setCode(ResultCodeEnum.PARAMERROR.getCode(), ResultCodeEnum.PARAMERROR.getName());
                return result;
            }
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("智能环境设备控制异常", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }
}
