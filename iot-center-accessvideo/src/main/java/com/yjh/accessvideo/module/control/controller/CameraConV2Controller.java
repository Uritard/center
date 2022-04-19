/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessvideo.module.control.controller;

import com.sun.jna.NativeLong;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.control.service.CameraConService;
import com.yjh.accessvideo.service.ftpsservice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/4/10
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/camera/v2")
@Api(value = "/cameraControl", description = "相机接口")
public class CameraConV2Controller {

    private final Logger log = LoggerFactory.getLogger(CameraConController.class);

    @Autowired
    private CameraConService cameraConService;


    @ApiOperation(value = "获取NVR存储状态和通道信息")
    @RequestMapping(value = "/getNVRStoreInfo", method = RequestMethod.GET)
    public Result getNVRStoreInfo(@RequestParam(value = "recordId") Long recordId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getNVRStoreAndChanle(recordId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取NVR存储状态和通道信息失败:", e);
        }
        return result;
    }

    @RequestMapping(value = "/getNVRChannelInfo", method = RequestMethod.GET)
    public Result getNVRChannelInfo(@RequestParam(value = "recordId") Long recordId,
        @RequestParam(value = "startTime", required = false) String startTime,
        @RequestParam(value = "endTime", required = false) String endTime) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getNVRIpparaCfg(recordId, startTime, endTime));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取NVR存储状态和通道信息失败:", e);
        }
        return result;
    }
}
