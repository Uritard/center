/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.controller;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.simple.service.impl.SimpleDeviceServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

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
    public Result importModel(@RequestPart("file") MultipartFile file, HttpServletRequest request) {
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

}
