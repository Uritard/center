package com.yjh.platform.module.video.controller;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.video.service.CameraConService;
import com.yjh.video.api.entity.PresetCmd;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/camera/v1")
@Api(value = "/cameraControl", tags = {"相机接口 - video 服务迁移"})
public class CameraConController {

    private Logger log = LoggerFactory.getLogger(CameraConController.class);

    @Resource
    private CameraConService cameraConService;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "相机播放")
    @RequestMapping(value = "/startRealPlay", method = RequestMethod.GET)
//    @Logs(title = "相机播放",content = "根据用户传递的参数相机播放",logType = 5, authority = "1234,1235")
    public Result startRealPlay(@RequestParam(value = "cameraId") Long cameraId, @RequestParam(value = "presetId", required = false) Long presetId) {
        Result result = new Result();
        if (presetId != null && presetId > 0) {
            try {
                cameraConService.isCameraControlled(cameraId);
                cameraConService.presetAction(presetId, cameraId, PresetCmd.PRESET_ACTION);
                cameraConService.pushCtrlTime(cameraId);
            } catch (BusinessException b) {
                result.setMessage(b.getMessage());
            }
        }
        try {
            result.setData(cameraConService.startRealPlay(cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "相机停止播放")
    @RequestMapping(value = "/stopRealPlay", method = RequestMethod.GET)
//    @Logs(title = "相机停止播放",content = "根据用户传递的参数停止相机播放",logType = 5, authority = "1234,1235")
    public Result stopRealPlay(@RequestParam(value = "cameraId", required = false) Long cameraId,
                               @RequestParam(value = "rtmpUrl", required = false) String rtmpUrl) {
        Result result = new Result();
        try {
            result.setData(cameraConService.stopRealPlay(cameraId, rtmpUrl));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机停止播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "相机批量播放")
    @RequestMapping(value = "/batchStartRealPlay", method = RequestMethod.GET)
//    @Logs(title = "相机批量播放",content = "根据用户传递的参数控制相机批量播放",logType = 5, authority = "1234,1235")
    public Result batchStartRealPlay(@RequestParam(value = "cameraIds") String cameraIds) {
        Result result = new Result();
        try {
            result.setData(cameraConService.batchStartRealPlay(cameraIds));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机批量播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "相机批量停止播放")
    @RequestMapping(value = "/batchStopRealPlay", method = RequestMethod.POST)
    public Result batchStopRealPlay(@RequestBody List<Map<String, String>> list) {
        Result result = new Result();
        try {
            List<String> resultList = new ArrayList<>();
            for (Map<String, String> map:list) {
                String resultBack = cameraConService.stopRealPlay(Long.valueOf(map.get("cameraId")), map.get("rtmpUrl"));
                resultList.add(resultBack);
            }
            result.setData(resultList);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机批量停止播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "机器人相机播放")
    @RequestMapping(value = "/robotStartRealPlay", method = RequestMethod.GET)
//    @Logs(title = "机器人相机播放",content = "根据用户传递的参数控制机器人相机播放",logType = 5, authority = "1234,1235")
    public Result robotStartRealPlay(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.robotStartRealPlay(robotId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人相机播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "机器人相机停止播放")
    @RequestMapping(value = "/robotStopRealPlay", method = RequestMethod.GET)
    public Result robotStopRealPlay(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.robotStopRealPlay(robotId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人相机停止播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "结束推流")
    @RequestMapping(value = "/stopStream", method = RequestMethod.GET)
    public Result stopStream(@RequestParam(value = "cameraId") String cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.stopStream(cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机停止播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "视频回放")
    @RequestMapping(value = "/startPlayBack", method = RequestMethod.GET)
//    @Logs(title = "视频回放",content = "根据用户传递的参数控制视频回放",logType = 5, authority = "1234,1235")
    public Result startPlayBack(@RequestParam(value = "cameraId") Long cameraId,
                                @RequestParam(value = "startTime") String startTime,
                                @RequestParam(value = "stopTime") String stopTime) {
        Result result = new Result();
        try {
            result.setData(cameraConService.startPlayBack(cameraId, startTime, stopTime));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("视频回放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "视频回放")
    @RequestMapping(value = "/startVideoBack", method = RequestMethod.GET)
//    @Logs(title = "视频回放",content = "根据用户传递的参数控制视频回放",logType = 5, authority = "1234,1235")
    public Result startVideoBack(HttpServletRequest request,
                                 @RequestParam(value = "cameraId") Long cameraId,
                                 @RequestParam(value = "startTime") String startTime,
                                 @RequestParam(value = "stopTime") String stopTime) {
        Result result = new Result();
        try {
            String token = request.getHeader("token");
            log.info("token:{}",token);
            result.setData(cameraConService.startVideoBack(token, cameraId, startTime, stopTime));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("视频回放失败:", e);
        }
        return result;
    }

    //    TILT_UP 21 云台上仰 TILT_DOWN 22 云台下俯 PAN_LEFT 23 云台左转 PAN_RIGHT 24 云台右转
//    11 焦距变大(倍率变大) 12 焦距变小(倍率变小) 25 云台上仰和左转 26 云台上仰和右转 27 云台下俯和左转 28 云台下俯和右转 29 云台左右自动扫描
    @ApiOperation(value = "云台控制")
    @RequestMapping(value = "/ptzControl", method = RequestMethod.GET)
//    @Logs(title = "相机云台控制",content = "根据用户传递的参数控制相机云台",logType = 5, authority = "1234,1235")
    public Result ptzControl(@RequestParam(value = "dwPTZCommand") int dwPTZCommand,
                             @RequestParam(value = "cameraId") Long cameraId,
                             @RequestParam(value = "dStop") int dStop,
                             @RequestParam(value = "speed") int speed) {
        Result result = new Result();
        try {
            cameraConService.isCameraControlled(cameraId);
            result.setData(cameraConService.pTZControl(dwPTZCommand, cameraId, dStop, speed));
            cameraConService.pushCtrlTime(cameraId);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("云台控制错误:", e);
        }
        return result;
    }


    @ApiOperation(value = "相机抓图")
    @RequestMapping(value = "/capturePicture", method = RequestMethod.GET)
    //    @Logs(title = "相机抓图",content = "根据用户传递的参数控制相机抓图",logType = 5, authority = "1234,1235")
    public Result capturePicture(@RequestParam(value = "cameraId") Long cameraId,
        @RequestParam(value = "meteName", required = false) String meteName,
        @RequestParam(value = "parentPath", required = false) String parentPath) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            cameraConService.isCameraControlled(cameraId);
            int max=9999,min=1;
            int ran = (int) (Math.random()*(max-min)+min);
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
            String parent = StringUtils.isEmpty(parentPath) ? "" : parentPath + "/";
            String filePathTem = "/" + parent + formatter.format(new Date())+ ran + ".jpg";
            String captureResultPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"));
            String capturePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content"));
            String filePath = captureResultPath + filePathTem;
            log.info("filePath: "+filePath);
            String message = cameraConService.capturePicture(filePath, cameraId, meteName);
            String urlPath = capturePath + filePathTem;
            resultMap.put("urlPath", urlPath);
            resultMap.put("absPath", filePath);
            String url = "chmod 777 "+ filePath;
            Runtime.getRuntime().exec(url);
            result.setData(resultMap);
            result.setMessage(message);
            cameraConService.pushCtrlTime(cameraId);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机抓图失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "任务中相机抓图")
    @RequestMapping(value = "/capturePictureForTask", method = RequestMethod.GET)
    public Result capturePictureForTask(@RequestParam(value = "cameraId") Long cameraId,
        @RequestParam(value = "meteName", required = false) String meteName) {
        Result result = new Result();
        Map<String, String> resultMap = new HashMap<>();
        try {
            int max=9999,min=1;
            int ran = (int) (Math.random()*(max-min)+min);
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
            String filePathTem = "/" + formatter.format(new Date())+ ran + ".jpg";
            String captureResultPath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"));
            String capturePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content"));
            String filePath = captureResultPath + filePathTem;
            log.info("filePath: "+filePath);
            String message = cameraConService.capturePicture(filePath, cameraId, meteName);
            String urlPath = capturePath+filePathTem;
            resultMap.put("urlPath", urlPath);
            resultMap.put("absPath", filePath);
            resultMap.put("resultNum", "已拍照");
            String url = "chmod 777 "+ filePath;
            Runtime.getRuntime().exec(url);
            result.setData(resultMap);
            result.setMessage(message);
            cameraConService.pushCtrlTime(cameraId);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务中相机抓图失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "预置位抓图")
    @RequestMapping(value = "/capturePresetPicture", method = RequestMethod.GET)
    public Result capturePresetPicture(@RequestParam(value = "presetId") Long presetId,
        @RequestParam(value = "cameraId") Long cameraId,
        @RequestParam(value = "meteName", required = false) String meteName,
        @RequestParam(value = "edgeCode", required = false) String edgeCode) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String capturePresetPath = cameraConService.getPresetBasePath();
            String capturePathPreset = cameraConService.getPresetUrlPath();

            cameraConService.isCameraControlled(cameraId);
            String filePathTem = "/"+presetId+"/" + presetId + ".jpg";
            if (StringUtils.isNotEmpty(edgeCode)) {
                filePathTem = String.format("/%s%s", edgeCode, filePathTem);
            }

            String filePath = capturePresetPath + filePathTem;
            log.info("预置位抓图 filePathTem: {},  filePath: {}, edgeCode: {}", filePathTem, filePath, edgeCode);
            String mkdir = "mkdir "+capturePresetPath+"/"+presetId;
            if (StringUtils.isNotEmpty(edgeCode)) {
                mkdir = String.format("mkdir -p %s/%s/%s", capturePresetPath, edgeCode, presetId);
            }

            log.info("mkdir: "+mkdir);
            Runtime.getRuntime().exec(mkdir);
            Thread.sleep(2000);
            log.info("filePath: "+filePath);
            String message = cameraConService.capturePicture(filePath, cameraId, meteName);
            String urlPath = capturePathPreset+filePathTem;
            resultMap.put("urlPath", urlPath);
            String url = "chmod 777 "+ filePath;
            Runtime.getRuntime().exec(url);
            result.setData(resultMap);
            result.setMessage(message);
            cameraConService.pushCtrlTime(cameraId);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机抓图失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "转到预置点")
    @RequestMapping(value = "/moveToPreset", method = RequestMethod.GET)
    public Result moveToPreset(@RequestParam(value = "presetId") Long presetId,
                               @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.moveToPreset(presetId, cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("转到预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "任务中转到预置点")
    @RequestMapping(value = "/moveToPresetForTask", method = RequestMethod.GET)
    public Result moveToPresetForTask(@RequestParam(value = "presetId") Long presetId,
                                      @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.moveToPresetForTask(presetId, cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("任务转到预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "设置预置点")
    @RequestMapping(value = "/setPreset", method = RequestMethod.GET)
    public Result setPreset(@RequestParam(value = "presetId") Long presetId,
                            @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.setPreset(presetId, cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设置预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取相机预置点的PTZ值，并抓图")
    @RequestMapping(value = "/getPresetPtzAndPic", method = RequestMethod.GET)
    public Result getPresetPtzAndPic(@RequestParam(value = "presetId") Long presetId,
                                     @RequestParam(value = "cameraId") Long cameraId,
                                     @RequestParam(value = "presetName", required = false) String presetName) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getPresetPtzAndPic(presetId, cameraId, presetName));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取预置点的PTZ值失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "清除预置点")
    @RequestMapping(value = "/cancelPreset", method = RequestMethod.GET)
    public Result cancelPreset(@RequestParam(value = "presetId") Long presetId,
                               @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.cancelPreset(presetId, cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("清除预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "开启语音广播")
    @GetMapping(value = "/startVoiceTrans")
    public Result startVoiceTrans(@RequestParam(value = "deviceId") Long deviceId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.startVoiceTrans(deviceId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("开启语音对讲失败描述:", e);
        }
        return result;
    }

}
