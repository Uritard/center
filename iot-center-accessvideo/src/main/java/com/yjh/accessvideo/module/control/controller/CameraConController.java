package com.yjh.accessvideo.module.control.controller;

import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.common.utils.FtpsUtil;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.configuration.PlatFromFtpsConfig;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.control.entity.TemperatureInfo;
import com.yjh.accessvideo.module.control.service.CameraConService;
import com.yjh.accessvideo.module.control.service.DroneCameraConService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tt
 * @since 2020-08-20
 */
@RestController
@RequestMapping("/camera/v1")
@Api(value = "/cameraControl", description = "相机接口")
public class CameraConController {

    private Logger log = LoggerFactory.getLogger(CameraConController.class);

    @Autowired
    private CameraConService cameraConService;

    @Autowired
    private DroneCameraConService droneCameraConService;

    @Resource(name = "redisTemplate")
    private RedisTemplate redisTemplate;

    @Autowired
    private PlatFromFtpsConfig platFromFtpsConfig;

    @ApiOperation(value = "相机播放")
    @RequestMapping(value = "/startRealPlay", method = RequestMethod.GET)
//    @Logs(title = "相机播放",content = "根据用户传递的参数相机播放",logType = 5, authority = "1234,1235")
    public Result startRealPlay(@RequestParam(value = "cameraId") Long cameraId, @RequestParam(value = "presetId", required = false) Long presetId) {
        Result result = new Result();
        if (presetId != null && presetId > 0) {
            try {
                cameraConService.isCameraControlled(cameraId);
                cameraConService.presetAction(presetId, cameraId, HCNetSDK.GOTO_PRESET);
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

    @ApiOperation(value = "结束推流")
    @RequestMapping(value = "/stopStream", method = RequestMethod.GET)
    public Result stopStream(@RequestParam(value = "cameraId") Long cameraId) {
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

    @ApiOperation(value = "关闭所有流")
    @RequestMapping(value = "/stopAllStream", method = RequestMethod.GET)
    public Result stopAllStream() {
        Result result = new Result();
        try {
            result.setData(cameraConService.stopAllStream());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机停止播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "相机停止播放")
    @RequestMapping(value = "/stopRealPlay", method = RequestMethod.GET)
//    @Logs(title = "相机停止播放",content = "根据用户传递的参数停止相机播放",logType = 5, authority = "1234,1235")
    public Result stopRealPlay(@RequestParam(value = "cameraId") Long cameraId,
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
//    @Logs(title = "机器人相机停止播放",content = "根据用户传递的参数控制机器人相机停止播放",logType = 5, authority = "1234,1235")
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

    @ApiOperation(value = "机器人视频回放")
    @RequestMapping(value = "/startRobotPlayBack",method = RequestMethod.GET)
    public Result startRobotPlayBack(@RequestParam(value = "robotId")Long robotId,
                                     @RequestParam(value = "startTime")String startTime,
                                     @RequestParam(value = "stopTime")String stopTime){
        Result result=new Result();
        try{
            result.setData(cameraConService.startRobotPlayBack(robotId,startTime,stopTime));
        }catch (BusinessException b){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(),b.getMessage());
        }catch (Exception e){
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(),ResultCodeEnum.SYSTEMERROR.getName());
            log.error("机器人视频回放失败",e);
        }
        return  result;

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
            copyFile(filePathTem,filePath);
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
            copyFile(filePathTem,filePath);
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
            copyFile(filePathTem,filePath);
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
//    @Logs(title = "转到预置点",content = "根据用户传递的参数转到预置点",logType = 5, authority = "1234,1235")
    public Result moveToPreset(@RequestParam(value = "presetId") Long presetId,
                               @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            cameraConService.isCameraControlled(cameraId);
            result.setData(cameraConService.presetAction(presetId, cameraId, HCNetSDK.GOTO_PRESET));
            cameraConService.pushCtrlTime(cameraId);
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
            result.setData(cameraConService.presetAction(presetId, cameraId, HCNetSDK.GOTO_PRESET));
            cameraConService.pushCtrlTime(cameraId);
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
                            @RequestParam(value = "cameraId") Long cameraId,
                            @RequestParam(value = "presetName", required = false) String presetName) {
        Result result = new Result();
        try {
            cameraConService.isCameraControlled(cameraId);
            cameraConService.presetAction(presetId, cameraId, HCNetSDK.SET_PRESET, presetName);
            result.setData(cameraConService.getCameraPTZ(presetId, cameraId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设置预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取相机预置点的PTZ值，并抓图")
    @RequestMapping(value = "/getPresetPTZAndPic", method = RequestMethod.GET)
    public Result getPresetPTZAndPic(@RequestParam(value = "presetId") Long presetId,
                                     @RequestParam(value = "cameraId") Long cameraId,
                                     @RequestParam(value = "presetName", required = false) String presetName) {
        Result result = new Result();
        try {
            Map<String, Object> resultMap = new HashMap<>();
            // 确认相机可控
            cameraConService.isCameraControlled(cameraId);
            //相机移动到预置位，并更新相机的操作时间
            cameraConService.presetAction(presetId, cameraId, HCNetSDK.GOTO_PRESET);
            // 更新相机操作时间
            cameraConService.pushCtrlTime(cameraId);
            Thread.sleep(10000); // 等10秒钟，确保相机镜头调整到位

            // 再次确认相机可控
            cameraConService.isCameraControlled(cameraId);
            // 获取预置位的PTZ数据
            String ptzStr = cameraConService.getCameraPTZ(presetId, cameraId);
            resultMap.put("cameraPtz", ptzStr);

            // 抓图
            Result resultPic = capturePicture(cameraId, presetName, "presetCheckImg");
            if (result != null && resultPic.getData() != null) {
                Map<String, Object> picMap = (Map<String, Object>) resultPic.getData();
                resultMap.putAll(picMap);
            }

            result.setData(resultMap);
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
            String capturePresetPath = cameraConService.getPresetBasePath();
            cameraConService.isCameraControlled(cameraId);
//            String filePathTem = "/"+presetId+"/" + presetId + ".jpg";
//            String filePath = capturePresetPath + filePathTem;
            String cmd = "rm -rf "+capturePresetPath+"/"+presetId;
            log.info("删除语句"+cmd);
            Runtime.getRuntime().exec(cmd);
            result.setData(cameraConService.presetAction(presetId, cameraId, HCNetSDK.CLE_PRESET));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("清除预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取相机状态")
    @RequestMapping(value = "/getCameraStatus", method = RequestMethod.GET)
    public Result getCameraStatus(@RequestParam(value = "recordId") Long recordId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getCameraStatus(recordId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取相机状态失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取NVR下挂相机树状态")
    @RequestMapping(value = "/getCameraTreeStatus", method = RequestMethod.GET)
    public Result getCameraTreeStatus(@RequestParam(value = "cameraName",required = false) String cameraName,
                                      @RequestParam(value = "flag",required = false) Integer flag) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getCameraStatusTree(cameraName,flag));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取NVR下挂相机树失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取NVR存储状态")
    @RequestMapping(value = "/getNVRStoreInfo", method = RequestMethod.GET)
    public Result getNVRStoreInfo(@RequestParam(value = "recordId") Long recordId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.getNVRStoreInfo(recordId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取NVR存储状态失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "NVR注册接口")
    @RequestMapping(value = "/registerNVR", method = RequestMethod.GET)
    public Result registerNVR(@RequestParam(value = "recordId") Long recordId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.registerNVR(recordId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("NVR注册失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "获取到视频存入指定文件中 保存为Mp4格式文件")
    @RequestMapping(value = "/getDVRToPlace", method = RequestMethod.GET)
    public Result getDVRToPlace(@RequestParam(value = "cameraId") Long cameraId,
                                @RequestParam(value = "startTime") String startTime,
                                @RequestParam(value = "endTime") String endTime)  {
        Result result = new Result();
        try {
            String  url= cameraConService.getDVRToPlace(cameraId,startTime,endTime);
            log.info("service返回值："+url);
            if(null!=url) {
                result.setData(url);
            } else { result.setData("下载失败请重新下载"); }
        } catch (Exception e) {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.error("视频上传服务器失败",e);
        }
        return result;
    }

    @ApiOperation(value = "开始录制视频")
    @RequestMapping(value = "/startDvrToPlace", method = RequestMethod.GET)
    public Result startDvrToPlace(@RequestParam(value = "cameraId") Long cameraId)  {
        Result result = new Result();
        try {
            String fileName= cameraConService.startDvrToPlace(cameraId);
            log.info("service返回值："+fileName);
            if(null!=fileName) {
                result.setData(fileName);
            } else { result.setData("开始录制失败"); }
        } catch (Exception e) {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.error("视频上传服务器失败",e);
        }
        return result;
    }

    @ApiOperation(value = "结束录制视频")
    @RequestMapping(value = "/stopDvrToPlace", method = RequestMethod.GET)
    public Result stopDvrToPlace(HttpServletRequest request, @RequestParam(value = "fileName") String fileName)  {
        Result result = new Result();
        try {
            String userId = request.getHeader("userId");
            String path = cameraConService.stopDvrToPlace(fileName, userId);
            log.info("service返回值："+path + ";" + userId);
            if(null!=path) {
                result.setData(path);
            } else { result.setData("结束录制失败"); }
        } catch (Exception e) {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.error("视频上传服务器失败",e);
        }
        return result;
    }

    @ApiOperation(value = "获取到视频文件列表")
    @RequestMapping(value = "/getFileList", method = RequestMethod.GET)
    public Result getFileList(@RequestParam(value = "cameraId") Long cameraId, @RequestParam(value = "startTime") String startTime,
        @RequestParam(value = "endTime") String endTime) {
        Result result = new Result();
        try {
            List<Map<String, String>> list = cameraConService.getFile(cameraId, startTime, endTime);
            result.setData(list);
        } catch (Exception e) {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.info(e.getMessage());
        }
        return result;

    }

    @RequestMapping(value = "/playBackByTime",method = RequestMethod.GET)
    public Result playBackByTime(@RequestParam("cameraId")Long cameraId, @RequestParam(value = "startTime") String startTime,
        @RequestParam(value = "endTime") String endTime) {
        Result result = new Result();
        try {
            Map<String, String> resultMap = cameraConService.playBackByTime(cameraId,startTime,endTime);
            result.setData(resultMap);
        } catch (Exception e) {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.info(e.getMessage());
        }
        return result;
    }
//    @ApiOperation(value = "获取全屏最大温度值")
//    @RequestMapping(value = "/getTemperature", method = RequestMethod.GET)
//    @Deprecated
//    public Result getTemperature(@RequestParam(value = "cameraId",required = false) Long cameraId)
//    {
//        Result result = new Result();
//        List<String> list=cameraConService.getTemperature(cameraId);
//        if (list.size()>0)
//        {
//            result.setData(list);
//        }else {
//            result.setData("获取温度失败");
//        }
//
//        return result;
//    }

    @ApiOperation(value = "获取红外文件_dlt664")
    @RequestMapping(value = "/givePicFir", method = RequestMethod.GET)
    public Result givePicFir(@RequestParam(value = "presetId",required = false) Long presetId,
                             @RequestParam(value = "cameraId",required = false) Long cameraId,
                             @RequestParam(value = "meteName",required = false) String meteName) {
        Result result = new Result();
        Map<String,String> map;
        String givePic = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:givePic", "content"));
        if ("1".equals(givePic)){
            map = cameraConService.givePicFir(cameraId,presetId,meteName);
        }else {
            map = cameraConService.givePicFir2(cameraId,presetId,meteName);
        }
        if (map.size()>0)
        {
            result.setData(map);
            result.setMessage("success");
        }else
            {
                result.setData("获取文件失败");
            }

        return result;
    }

//    @ApiOperation(value = "开启可视对讲")
//    @RequestMapping(value = "/startVoiceTalk", method = RequestMethod.GET)
//    public Result startVoiceTalk( @RequestParam(value = "videoIntercomId",required = false) Long videoIntercomId)
//    {
//        Result result = new Result();
//        int re=cameraConService.startVoiceTalk(videoIntercomId);
//        if (re>0)
//        {
//            result.setData("开启对讲");
//        }else
//            {
//                result.setData("无法开启对讲");
//            }
//
//        return result;
//    }
//    @ApiOperation(value = "关闭可视对讲")
//    @RequestMapping(value = "/stopVoiceTalk", method = RequestMethod.GET)
//    public Result stopVoiceTalk()
//    {
//        Result result = new Result();
//        try {
//            cameraConService.stopVoiceTalk();
//            result.setData("关闭对讲");
//
//        }catch (Exception e){
//            result.setData("系统异常");
//        }
//        return result;
//    }
//
//    @ApiOperation(value = "可视对讲播放")
//    @RequestMapping(value = "/startVideoRealPlay", method = RequestMethod.GET)
//    public Result startVideoRealPlay( @RequestParam(value = "videoIntercomId",required = false) Long videoIntercomId)
//    {
//        Result result = new Result();
//       try {
//           Map<String, Object> map=cameraConService.startVideoRealPlay(videoIntercomId);
//           if (map.size()>0)
//           {
//               result.setData(map);
//           }else
//               {
//                   result.setData("地址不存在");
//               }
//       }catch (Exception e)
//       {
//           result.setData("系统异常");
//           log.info(e.getMessage());
//       }
//
//        return result;
//    }

//    /**
//     * \获取可视对讲在  状态
//     * @param videoIntercomId
//     * @return
//     */
//    @RequestMapping(value = "/getVidemoIntercomStatus", method = RequestMethod.GET)
//    public Result  getVidemoIntercomStatus ( @RequestParam(value = "videoIntercomId",required = false) Long videoIntercomId)
//    {
//        Result result = new Result();
//        result.setData(cameraConService.getVidemoIntercomStatus(videoIntercomId));
//        return result;
//    }

    /**
     *
     * @param
     * @return
     */

    @ApiOperation(value = "根据坐标获取温度")
    @RequestMapping(value = "/PointTemperature", method = RequestMethod.POST)
//    @Logs(title = "根据坐标获取温度",content = "根据用户传递的参数根据坐标获取温度",logType = 1, authority = "1234,1235")
    public Result  PointTemperature ( @RequestBody TemperatureInfo temperatureInfo)
    {
        log.info(temperatureInfo.toString());
        Result result = new Result();
       if (temperatureInfo.getPicPath()!=null&&temperatureInfo.getPicPath()!="") {
           result.setData(cameraConService.lineTemperature(temperatureInfo.getPicPath(),temperatureInfo.getPoints()));
       } else {
           result.setData(cameraConService.getlineTemperature(temperatureInfo.getCameraId(),temperatureInfo.getPoints()));
       }
        return result;
    }

    @ApiOperation(value = "开始录制视频-视频简易处理")
    @RequestMapping(value = "/startRecordVideo", method = RequestMethod.GET)
    public Result startRecordVideo(@RequestParam(value = "cameraId") Long cameraId)  {
        Result result = new Result();
        try {
            String fileName = cameraConService.startRecordVideo(cameraId);
            log.info("startRecordVideo service返回值：{}", fileName);
            if(null!=fileName) {
                result.setData(fileName);
            } else { result.setData("开始录制失败"); }
        } catch (Exception e) {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.error("视频上传服务器失败",e);
        }
        return result;
    }

    @ApiOperation(value = "结束录制视频-视频简易处理")
    @GetMapping(value = "/endRecordVideo")
    public Result endRecordVideo(@RequestParam(value = "fileName") String fileName)  {
        Result result = new Result();
        try {
            cameraConService.endRecordVideo(fileName);
        } catch (Exception e) {
            result.setData(ResultCodeEnum.SYSTEMERROR);
            log.error("视频上传服务器失败",e);
        }
        return result;
    }

    private void copyFile(String ftpsPath, String localPath){
        // 如果 ftpsTurbo 为 true，则表示设置了文件盘共享，或者没有单独部署，不使用 ftps 对文件进行传输拷贝
        if(Constant.ftpsTurbo()){
            return;
        }
        try {
            if(StringUtils.isEmpty(ftpsPath) || StringUtils.isEmpty(localPath)) {return;}
            FtpsUtil.putFile(localPath, "video"+ftpsPath, platFromFtpsConfig.getIp(), platFromFtpsConfig.getPort(),
                    platFromFtpsConfig.getKeypw(), platFromFtpsConfig.getUsername(), platFromFtpsConfig.getPassword());
        } catch (Exception e) {
            log.error("将文件上传至 platform ftp 服务器错误:", e);
        }
        HashMap<String,String> param = new HashMap<>();
        param.put("ftpsPath","video"+ftpsPath);
        param.put("localPath",localPath);
        Constant.otherServerMap(param,Constant.COPY_FILE_URL);
    }

    @ApiOperation(value = "无人机相机播放")
    @RequestMapping(value = "/droneStartRealPlay", method = RequestMethod.GET)
    public Result droneStartRealPlay(@RequestParam(value = "robotId") Long robotId) {
        Result result = new Result();
        try {
            result.setData(droneCameraConService.droneStartRealPlay(robotId));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("无人机相机播放失败:", e);
        }

        return result;
    }

}
