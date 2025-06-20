/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.simple.entity.ModelCommand;
import com.yjh.platform.module.simple.service.impl.SimpleDeviceServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-05-30
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/simpleDevice/v1")
@Api(value = "/simpleDevice", tags = "简易机器人设备接口")
public class SimpleDeviceController {
    private final SimpleDeviceServiceImpl simpleDeviceService;

    @ApiOperation(value = "简易机器人模型导入")
    @Logs(title = "简易机器人模型导入", content = "导入简易机器人模型文件", logType = 8, authority = "1234")
    @PostMapping(value = "/import")
    public Result importModel(@RequestPart("file") MultipartFile file) {
        Result result = new Result();
        try {
            simpleDeviceService.importModel(file);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("导入简易机器人模型文件失败：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

    @ApiOperation(value = "简易机器人模型下发")
    @Logs(title = "简易机器人模型下发", content = "下发简易机器人模型文件", logType = 10, authority = "1234")
    @PostMapping(value = "/modelSend")
    public Result simpleModelSend(@RequestBody @Valid ModelCommand modelSend) {
        Result result = new Result();
        try {
            simpleDeviceService.modelSend(modelSend);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("简易机器人模型下发失败：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }


    @ApiOperation(value = "简易机器人模型导出")
    @Logs(title = "简易机器人模型导出", content = "导出简易机器人模型文件", logType = 9, authority = "1234")
    @PostMapping(value = "/modelExport")
    public Result simpleModelExport(@RequestBody @Valid ModelCommand modelSend) {
        Result result = new Result();
        try {
            simpleDeviceService.modelExport(modelSend);
        } catch (BusinessException b) {
            result.setCode(b.getCode(), b.getMessage());
        } catch (Exception e) {
            log.info("简易机器人模型导出失败：", e);
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
        }
        return result;
    }

}
