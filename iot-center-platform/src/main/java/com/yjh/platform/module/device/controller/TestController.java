package com.yjh.platform.module.device.controller;

import com.yjh.platform.audiodevice.AudioDevice;
import com.yjh.platform.audiodevice.AudioDeviceManager;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/test/audio")
@Api(value = "/audioTest", description = "声纹测试")
public class TestController {
    private Logger log = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private AudioDeviceManager audioDeviceManager;

    @ApiOperation(value = "声纹开始录入")
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    public Result start(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            AudioDevice device = audioDeviceManager.getAudioDevice(deviceId);
            if (null == device){
                result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), "设备未注册");
                return result;
            }
            device.startRecording();
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

    @ApiOperation(value = "声纹结束录入")
    @RequestMapping(value = "/stop", method = RequestMethod.POST)
    public Result stop(@RequestParam(value = "deviceId", required = true) String deviceId) {
        Result result = new Result();
        try {
            AudioDevice device = audioDeviceManager.getAudioDevice(deviceId);
            if (null == device){
                result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), "设备未注册");
                return result;
            }
            device.stopRecording();;
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("添加错误:", e);
        }
        return result;
    }

}
