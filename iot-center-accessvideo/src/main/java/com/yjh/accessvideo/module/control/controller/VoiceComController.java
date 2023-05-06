/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessvideo.module.control.controller;

import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.commons.utils.DateTimeUtil;
import com.yjh.accessvideo.hik.transmit.HikUtilsApp;
import com.yjh.accessvideo.module.control.service.VoiceComService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * <功能描述>
 *
 * @author 丫C
 * @date 2023/4/19
 * @since [产品/模块版本] （可选）
 */
@RestController
@RequestMapping("/voiceCom/v1")
@Api(value = "/VoiceComController", tags = "语音对讲相关接口")
public class VoiceComController {

    private final Logger log = LoggerFactory.getLogger(VoiceComController.class);

    private final VoiceComService voiceComService;

    public VoiceComController(VoiceComService voiceComService){
        this.voiceComService = voiceComService;
    }

    @ApiOperation(value = "初始化")
    @GetMapping(value = "/initSdk")
    public Result initSdk() {
        Result result = new Result();
        try {
            HikUtilsApp hikUtilsApp = new HikUtilsApp();
            hikUtilsApp.initSdk();
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("初始化失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "设备登录")
    @GetMapping(value = "/deviceLogin")
    public Result deviceLogin(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(voiceComService.deviceLogin(cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设备登录失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "开启语音对讲")
    @GetMapping(value = "/startVoiceCom")
    public Result startVoiceCom(@RequestParam(value = "cameraId") Long cameraId,
                                @RequestParam(value = "dwVoiceChan") Integer dwVoiceChan) {
        Result result = new Result();
        try {
            result.setData(voiceComService.startVoiceCom(cameraId, dwVoiceChan));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("开启语音对讲失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "关闭语音对讲")
    @GetMapping(value = "/stopVoiceCom")
    public Result stopVoiceCom(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(voiceComService.stopVoiceCom(cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("关闭语音对讲失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取设备语音编码格式")
    @GetMapping(value = "/getAudioCompress")
    public Result getAudioCompress(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
            HikUtilsApp hikUtilsApp = new HikUtilsApp();
            hikUtilsApp.getAudioCompress(lUserId);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取设备语音编码格式失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "开启语音转发测试")
    @GetMapping(value = "/startVoiceTransTest")
    public Result startVoiceTransTest(@RequestParam(value = "cameraId") Long cameraId,
                                      @RequestParam(value = "timeItem") long timeItem,
                                      @RequestParam(value = "fileName") String fileName) {
        Result result = new Result();
        try {
            result.setData(voiceComService.startVoiceTransTest(cameraId, timeItem, fileName));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("开启语音转发测试失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "发送语音数据测试")
    @GetMapping(value = "/voiceComSendDataTest")
    public Result voiceComSendDataTest(@RequestParam(value = "fileName") String fileName,
                                       @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            log.info("hikDeviceUserIdMaps:{}", Constant.hikDeviceUserIdMaps);
            Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
            log.info("hikDeviceVoiceTransHandleMaps:{}", Constant.hikDeviceVoiceTransHandleMaps);
            Integer lVoiceTranHandle = Constant.hikDeviceVoiceTransHandleMaps.get(lUserId);
            String format = DateTimeUtil.formatThreadLocal(new Date());
            voiceComService.voiceComSendDataTest(lVoiceTranHandle, fileName, format);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("发送语音数据测试失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "接收语音数据测试")
    @PostMapping(value = "/receivedVoiceData")
    public Result receivedVoiceData(@RequestParam(value = "cameraId") Long cameraId,
                                    HttpServletRequest request) {
        Result result = new Result();
        try {
            log.info("hikDeviceUserIdMaps:{}", Constant.hikDeviceUserIdMaps);
            Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
            log.info("hikDeviceVoiceTransHandleMaps:{}", Constant.hikDeviceVoiceTransHandleMaps);
            Integer lVoiceTranHandle = Constant.hikDeviceVoiceTransHandleMaps.get(lUserId);
            voiceComService.receivedVoiceData(request, lVoiceTranHandle);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("接收语音数据测试失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "关闭语音转发测试")
    @GetMapping(value = "/stopVoiceTransTest")
    public Result stopVoiceTransTest(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(voiceComService.stopVoiceTransTest(cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("关闭语音转发测试失败描述:", e);
        }
        return result;
    }

    @ApiOperation(value = "设备注销")
    @GetMapping(value = "/deviceLogout")
    public Result deviceLogout(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            log.info("hikDeviceUserIdMaps:{}", Constant.hikDeviceUserIdMaps);
            Integer lUserId = Constant.hikDeviceUserIdMaps.get(String.valueOf(cameraId));
            HikUtilsApp hikUtilsApp = new HikUtilsApp();
            result.setData(hikUtilsApp.deviceLogout(lUserId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设备注销失败描述:", e);
        }
        return result;
    }

}
