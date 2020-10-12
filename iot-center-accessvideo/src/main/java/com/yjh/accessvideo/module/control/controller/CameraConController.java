package com.yjh.accessvideo.module.control.controller;

import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.control.service.CameraConService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

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

    @Value("${server.port}")
    private String serverPort;//服务端口

    @Value("${nvr.capture.picture}")
    private String sdkPicturePath;//图片路径

    @ApiOperation(value = "相机播放")
    @RequestMapping(value = "/startRealPlay", method = RequestMethod.GET)
    public Result startRealPlay(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
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

//    TILT_UP 21 云台上仰 TILT_DOWN 22 云台下俯 PAN_LEFT 23 云台左转 PAN_RIGHT 24 云台右转
//    11 焦距变大(倍率变大) 12 焦距变小(倍率变小) 25 云台上仰和左转 26 云台上仰和右转 27 云台下俯和左转 28 云台下俯和右转 29 云台左右自动扫描
    @ApiOperation(value = "云台控制")
    @RequestMapping(value = "/ptzControl", method = RequestMethod.GET)
    public Result ptzControl(@RequestParam(value = "dwPTZCommand") int dwPTZCommand,
                             @RequestParam(value = "cameraId") Long cameraId,
                             @RequestParam(value = "dStop") int dStop,
                             @RequestParam(value = "speed") int speed) {
        Result result = new Result();
        try {
            result.setData(cameraConService.pTZControl(dwPTZCommand, cameraId, dStop, speed));
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
    public Result capturePicture(@RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            int max=9999,min=1;
            int ran = (int) (Math.random()*(max-min)+min);
            SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyyHHmmssSSS");
            String filePathTem = "/" + formatter.format(new Date())+ ran + ".jpeg";
            String filePath = sdkPicturePath + filePathTem;
            log.info("filePath: "+filePath);
            String message = cameraConService.capturePicture(filePath, cameraId);
            String urlPath = "http://" +getLocalIp()+":"+serverPort+"/image"+filePathTem;
            resultMap.put("urlPath", urlPath);
            result.setData(resultMap);
            result.setMessage(message);
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机抓图失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "预置位抓图")
    @RequestMapping(value = "/capturePresetPicture", method = RequestMethod.GET)
    public Result capturePresetPicture(@RequestParam(value = "presetId") Long presetId,
                                 @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String filePathTem = "/" + presetId + ".jpeg";
            String filePath = sdkPicturePath + filePathTem;
            log.info("filePath: "+filePath);
            String message = cameraConService.capturePicture(filePath, cameraId);
            String urlPath = "http://" +getLocalIp()+":"+serverPort+"/image"+filePathTem;
            resultMap.put("urlPath", urlPath);
            result.setData(resultMap);
            result.setMessage(message);
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
            result.setData(cameraConService.PresetAction(presetId, cameraId, HCNetSDK.GOTO_PRESET));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("转到预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "设置预置点")
    @RequestMapping(value = "/setPreset", method = RequestMethod.GET)
    public Result setPreset(@RequestParam(value = "presetId") Long presetId,
                            @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.PresetAction(presetId, cameraId, HCNetSDK.SET_PRESET));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("设置预置点失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "清除预置点")
    @RequestMapping(value = "/cancelPreset", method = RequestMethod.GET)
    public Result cancelPreset(@RequestParam(value = "presetId") Long presetId,
                               @RequestParam(value = "cameraId") Long cameraId) {
        Result result = new Result();
        try {
            result.setData(cameraConService.PresetAction(presetId, cameraId, HCNetSDK.CLE_PRESET));
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("清除预置点失败:", e);
        }
        return result;
    }

    private static String getLocalIp() throws SocketException {
        String ip = "";
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if (!name.contains("docker") && !name.contains("lo")) {
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()) {
                            String ipaddress = inetAddress.getHostAddress();
                            if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                                ip = ipaddress;
                            }
                        }
                    }
                }
            }
        } catch (SocketException ex) {
            ip = "127.0.0.1";
            ex.getMessage();
        }
        return ip;
    }

}
