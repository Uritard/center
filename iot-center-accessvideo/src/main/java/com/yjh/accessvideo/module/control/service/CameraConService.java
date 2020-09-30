package com.yjh.accessvideo.module.control.service;

import com.sun.jna.NativeLong;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.device.dao.FormatDao;
import com.yjh.accessvideo.module.device.entity.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;

/**
* @author tt
* @since 2020-08-20
*/
@Service
public class CameraConService {

    private Logger log = LoggerFactory.getLogger(CameraConService.class);

    @Autowired
    private RedisTemplate redisTemplate;
    @Value("${nvr.rtmp.video}")
    private String UrlTem;

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    private NativeLong m_lRealPalyHandle = new NativeLong(-1);
    private NativeLong lUserIDLong = new NativeLong(Constant.maps.get("lUserID"));
    private HCNetSDK.NET_DVR_CLIENTINFO m_sClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();	// play structure


    @Logs(title = "相机播放", code = "cameraPlay")
    @Transactional(rollbackFor = Exception.class)
    public String startRealPlay(String cameraIp, int cameraPort, String userName, String password, int iChanNum) {
        String line = null;
        StringBuilder dataBack = new StringBuilder();
        try {
            String transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum);
//            transUrl = "/usr/bin/ffmpeg -loglevel debug -rtsp_transport tcp -i rtsp://admin:hik12345@192.168.33.2:554/Streaming/Channels/101?transportmode=unicast -vcodec copy -an -f flv rtmp://192.168.9.40:1935/live/123";
            Process process=Runtime.getRuntime().exec(transUrl);
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            while ((line = reader.readLine()) != null) { dataBack.append(line).append('\n'); }

            String url = "ps -ef | grep ffmpeg | grep -v 'grep'";
            Process processForId=Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
            processForId.waitFor();
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), "UTF-8"));
            String lineForId = null;
            StringBuilder dataBackForId = new StringBuilder();
            while ((lineForId = readerForId.readLine()) != null) {
                dataBackForId.append(lineForId).append('\n');
            }
            Integer processId = Integer.parseInt(dataBackForId.substring(9,15).replace(" ",""));
            Constant.maps.put(String.valueOf(iChanNum), processId);
        } catch (Exception e) {e.getMessage();}
        return dataBack.toString();
    }

    @Logs(title = "相机停止播放", code = "cameraStopPlay")
    @Transactional(rollbackFor = Exception.class)
    public String stopRealPlay(int iChanNum) {
        String urlStop = null;
        try {
            Integer processId = Constant.maps.get(String.valueOf(iChanNum));
            urlStop = "kill -9 "+processId;
            Runtime.getRuntime().exec(urlStop);
        } catch (Exception e) {e.getMessage();}
        return urlStop;
    }

    @Logs(title = "云台控制", code = "cameraControl")
    @Transactional(rollbackFor = Exception.class)
    public Object pTZControl(int dwPTZCommand, int iChanNum) {
        m_lRealPalyHandle = realPlay(iChanNum);
        if (m_lRealPalyHandle.intValue() == -1) {
            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
            return "fail";
        }
        if (hCNetSDK.NET_DVR_PTZControl(m_lRealPalyHandle, dwPTZCommand, 0)) {
            hCNetSDK.NET_DVR_PTZControl(m_lRealPalyHandle, dwPTZCommand, 1);
            hCNetSDK.NET_DVR_StopRealPlay(m_lRealPalyHandle);
            return "success";
        } else { return "errorInfo: "+hCNetSDK.NET_DVR_GetLastError(); }
    }

    @Logs(title = "相机抓图", code = "capturePicture")
    @Transactional(rollbackFor = Exception.class)
    public boolean capturePicture(String filePath, int iChanNum) {
        NativeLong iChanNumLong = new NativeLong(iChanNum);
        HCNetSDK.NET_DVR_JPEGPARA lpJpegPara = new HCNetSDK.NET_DVR_JPEGPARA();
        lpJpegPara.wPicSize = 5;
        lpJpegPara.wPicQuality = 1;/* 图片质量系数 0-最好 1-较好 2-一般 */
        //TODO
        String sPicFileName = filePath;
        if (!hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserIDLong, iChanNumLong, lpJpegPara, sPicFileName)) {
            log.error("抓图失败");
            return false;
        }
        return true;
    }

    @Logs(title = "预置点调用", code = "presetAction")
    @Transactional(rollbackFor = Exception.class)
    public boolean presetAction(int iPreset, int iChanNum) {
        m_lRealPalyHandle = realPlay(iChanNum);
        if (m_lRealPalyHandle.intValue() == -1) {
            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        if (!hCNetSDK.NET_DVR_PTZPreset(m_lRealPalyHandle, HCNetSDK.GOTO_PRESET, iPreset)) {
            log.error("调用预置点失败，预置点：" + iPreset);
            return false;
        }
        return true;
    }

    private NativeLong realPlay(int lChannel) {
        m_sClientInfo.lChannel = new NativeLong(lChannel);
        return hCNetSDK.NET_DVR_RealPlay_V30(lUserIDLong,
                m_sClientInfo, null, null, true);
    }

//    NativeLong lRealHandle = new NativeLong(0);
//            if (hCNetSDK.NET_DVR_PTZControlWithSpeed(lRealHandle, dwPTZCommand, 0, 0)) {
//        result.setData("success");
//    } else {result.setData("errorInfo: "+hCNetSDK.NET_DVR_GetLastError());}
//        NativeLong iChanNumLong = new NativeLong(iChanNum);
//        NativeLong lUserIDLong = new NativeLong(Constant.maps.get("lUserID"));
//        if (hCNetSDK.NET_DVR_PTZControl_Other(lUserIDLong,iChanNumLong, 23, 0)) {
//            return "success";
//        }
}

