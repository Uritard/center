package com.yjh.accessvideo.module.control.controller;

import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.examples.win32.W32API;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.result.BusinessException;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.result.ResultCodeEnum;
import com.yjh.accessvideo.hik.HCNetSDK;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.omg.SendingContext.RunTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.*;
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
    private RedisTemplate redisTemplate;

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    private NativeLong m_lRealPalyHandle = new NativeLong(-1);

    private W32API.HWND m_hWnd;
    private HCNetSDK.NET_DVR_CLIENTINFO m_sClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();	// play structure

    @ApiOperation(value = "获取预览信息")
    @RequestMapping(value = "/getPlayerInfo", method = RequestMethod.GET)
    public Result getPlayerInfo(@RequestParam(value = "iChanNum") int iChanNum) {
        Result result = new Result();
        try {
            long lRealPalyHandle = m_lRealPalyHandle.longValue();
            if (lRealPalyHandle > 0) { hCNetSDK.NET_DVR_StopRealPlay(m_lRealPalyHandle); }
            int lUserID = Constant.maps.get("lUserID");
            NativeLong m_lUserID = new NativeLong(lUserID);
            final Panel m_panelPlay = new Panel();
            m_panelPlay.setBackground(Color.DARK_GRAY);
            m_panelPlay.setBounds(10, 10, 373, 313);
            m_hWnd = new W32API.HWND(Native.getComponentPointer(m_panelPlay));
            m_sClientInfo.hPlayWnd = m_hWnd;
            m_sClientInfo.lChannel = new NativeLong(iChanNum);
            m_lRealPalyHandle = hCNetSDK.NET_DVR_RealPlay_V30(m_lUserID, m_sClientInfo, null, null, true);
            if (hCNetSDK.NET_DVR_PTZControl(m_lRealPalyHandle, 23, 0)) {
                result.setData("success");
            } else {result.setData("errorInfo: "+hCNetSDK.NET_DVR_GetLastError());}
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("获取预览信息:", e);
        }
        return result;
    }

    @ApiOperation(value = "相机播放")
    @RequestMapping(value = "/startRealPlay", method = RequestMethod.GET)
    public Result startRealPlay(@RequestParam(value = "cameraId") int cameraId,
                         @RequestParam(value = "cameraIp") int cameraIp,
                         @RequestParam(value = "cameraPort") int cameraPort,
                         @RequestParam(value = "userName") int userName,
                         @RequestParam(value = "password") int password) {
        Result result = new Result();
        try {
            String url = "/usr/bin/ffmpeg -loglevel debug -rtsp_transport tcp -i rtsp://admin:hik12345@192.168.33.2:554/Streaming/Channels/101?transportmode=unicast -vcodec copy -an -f flv rtmp://192.168.33.170:1935/live/123";
            Process process=Runtime.getRuntime().exec(url);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line = null;
            StringBuilder dataBack = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                dataBack.append(line).append('\n');
            }
            result.setData(dataBack.toString());
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
    public Result stopRealPlay(@RequestParam(value = "cameraId") int cameraId,
                                @RequestParam(value = "cameraIp") int cameraIp,
                                @RequestParam(value = "cameraPort") int cameraPort,
                                @RequestParam(value = "userName") int userName,
                                @RequestParam(value = "password") int password) {
        Result result = new Result();
        try {
            String url = "ps -ef | grep ffmpeg";
            Process process=Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            String line = null;
            StringBuilder dataBack = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                dataBack.append(line).append('\n');
            }
            result.setData(dataBack.toString());
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("相机播放失败:", e);
        }
        return result;
    }

    @ApiOperation(value = "云台控制")
    @RequestMapping(value = "/ptzControl", method = RequestMethod.GET)
    public Result ptzControl(
//            @RequestParam(value = "cameraId") int cameraId,
//                         @RequestParam(value = "cameraIp") int cameraIp,
//                         @RequestParam(value = "cameraPort") int cameraPort,
//                         @RequestParam(value = "userName") int userName,
//                         @RequestParam(value = "password") int password,
                         @RequestParam(value = "dwPTZCommand") int dwPTZCommand) {
//    TILT_UP 21 云台上仰
//    TILT_DOWN 22 云台下俯
//    PAN_LEFT 23 云台左转
//    PAN_RIGHT 24 云台右转
        Result result = new Result();
        try {
//            Map<String, Object> cameraList = redisTemplate.opsForHash().entries("camera_info");
//            int lChannel = Integer.parseInt(String.valueOf(cameraList.get("cameraId")));
//            if (hCNetSDK.NET_DVR_PTZControl_Other(lUserID, 1, dwPTZCommand, 0)) {
//                result.setData("success");
//            } else {result.setData("fail");}
            NativeLong lRealHandle = new NativeLong(0);
            if (hCNetSDK.NET_DVR_PTZControlWithSpeed(lRealHandle, dwPTZCommand, 0, 0)) {
                result.setData("success");
            } else {result.setData("errorInfo: "+hCNetSDK.NET_DVR_GetLastError());}
        } catch (BusinessException b) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), b.getMessage());
        } catch (Exception e) {
            result.setCode(ResultCodeEnum.SYSTEMERROR.getCode(), ResultCodeEnum.SYSTEMERROR.getName());
            log.error("云台控制错误:", e);
        }
        return result;
    }

}
