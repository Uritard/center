package com.yjh.accessvideo.module.control.service;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.commons.logs.SpringBeanUtils;
import com.yjh.accessvideo.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessvideo.commons.result.Result;
import com.yjh.accessvideo.commons.utils.threadPool.ExecutorsUtil;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.CameraConInfo;
import com.yjh.accessvideo.module.device.dao.FormatDao;
import com.yjh.accessvideo.module.device.entity.Format;
import org.apache.http.entity.mime.content.FileBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private CameraConDao cameraConDao;
    @Value("${nvr.rtmp.video}")
    private String UrlTem;

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    private NativeLong m_lRealPalyHandle = new NativeLong(-1);
    private NativeLong lUserIDLong = new NativeLong(-1);
    private HCNetSDK.NET_DVR_CLIENTINFO m_sClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();	// play structure


    @Logs(title = "相机播放", code = "cameraPlay")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startRealPlay(Long cameraId) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
            String userName = cameraConInfo.getUserName();
            String password = cameraConInfo.getPwd();
            String cameraIp = cameraConInfo.getRecordIp();
            int cameraPort = cameraConInfo.getRtspPort();
            int iChanNum = cameraConInfo.getChannelNum();
            int livePath;
            if (Constant.maps.get("livePath") != null) {
                livePath = Constant.maps.get("livePath")+1;
                Constant.maps.put("livePath", Constant.maps.get("livePath")+1);
            } else {
                livePath = 123;
                Constant.maps.put("livePath", 123);
            }
            log.info("Constant.maps: "+Constant.maps);
            String transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum, livePath);
            Runtime.getRuntime().exec(transUrl);
            String[] rtmpUrls = transUrl.split("rtmp");
            String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
            returnMap.put("rtmpUrl", rtmpUrl);
            Constant.mapsForCamera.put(String.valueOf(cameraId), rtmpUrl);
            log.info("mapsForCamera: "+Constant.mapsForCamera);
            log.info("returnMap: "+returnMap);
        } catch (Exception e) {e.getMessage();}
        return returnMap;
    }

    @Logs(title = "相机停止播放", code = "cameraStopPlay")
    @Transactional(rollbackFor = Exception.class)
    public String stopRealPlay(Long cameraId, String rtmpUrl) {
        String urlStop = null;
        String livePath;
        try {
            if (Objects.equals(null, rtmpUrl) || rtmpUrl.equals("")) {
                String rtmpUrlCamera = Constant.mapsForCamera.get(String.valueOf(cameraId));
                String[] rtmpUrlCameras = rtmpUrlCamera.split("/");
                livePath = rtmpUrlCameras[rtmpUrlCameras.length-1];
                log.info("livePath: "+livePath);
            } else {
                String[] rtmpUrls = rtmpUrl.split("/");
                livePath = rtmpUrls[rtmpUrls.length-1];
                log.info("livePath: "+livePath);
            }

            String url = "ps -ef | grep ffmpeg | grep '"+ livePath +"' | grep -v 'grep'";
            Process processForId=Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
            log.info("4");
            processForId.waitFor();
            log.info("5");
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), "UTF-8"));
            String lineForId = null;
            StringBuilder dataBackForId = new StringBuilder();
            while ((lineForId = readerForId.readLine()) != null) {
                dataBackForId.append(lineForId).append('\n');
            }
            log.info("6");
            Integer processNum = Integer.parseInt(dataBackForId.substring(9,15).replace(" ",""));
            urlStop = "kill -9 "+processNum;
            Runtime.getRuntime().exec(urlStop);
            log.info("7");
        } catch (Exception e) {e.getMessage();}
        return urlStop;
    }

    @Logs(title = "云台控制", code = "cameraControl")
    @Transactional(rollbackFor = Exception.class)
    public Object pTZControl(int dwPTZCommand, Long cameraId, int dStop) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        m_lRealPalyHandle = realPlay(iChanNum);
        if (m_lRealPalyHandle.intValue() == -1) {
            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
            return "fail";
        }
        if (dwPTZCommand==29) {
            hCNetSDK.NET_DVR_PTZControl(m_lRealPalyHandle, dwPTZCommand, dStop);
            return "success";
        } else {
            if (hCNetSDK.NET_DVR_PTZControl(m_lRealPalyHandle, dwPTZCommand, 0)) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {e.getMessage();}
                hCNetSDK.NET_DVR_PTZControl(m_lRealPalyHandle, dwPTZCommand, 1);
                hCNetSDK.NET_DVR_StopRealPlay(m_lRealPalyHandle);
                return "success";
            } else { return "PTZ control fail, errorInfo: "+hCNetSDK.NET_DVR_GetLastError(); }
        }

    }

    @Logs(title = "相机抓图", code = "capturePicture")
    @Transactional(rollbackFor = Exception.class)
    public FileBody capturePicture(String filePath, Long cameraId) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        NativeLong iChanNumLong = new NativeLong(iChanNum);
        HCNetSDK.NET_DVR_JPEGPARA lpJpegPara = new HCNetSDK.NET_DVR_JPEGPARA();
        lpJpegPara.wPicSize = 0xff;
        lpJpegPara.wPicQuality = 1;/* 图片质量系数 0-最好 1-较好 2-一般 */
        if (Objects.nonNull(Constant.maps.get("lUserID"))) {
            lUserIDLong = new NativeLong(Constant.maps.get("lUserID"));
            log.info("lUserIDLong: "+lUserIDLong);
            if (!hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserIDLong, iChanNumLong, lpJpegPara, filePath)) {
                log.error("capture picture fail, error code: "+hCNetSDK.NET_DVR_GetLastError());
            }
        }
        return new FileBody(new File(String.valueOf(filePath)));
    }

    @Logs(title = "预置点调用", code = "presetAction")
    @Transactional(rollbackFor = Exception.class)
    public boolean PresetAction(Long presetId, Long cameraId, int presetCmd) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,presetId);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        int iPreset = cameraConInfo.getPresetNum();
        m_lRealPalyHandle = realPlay(iChanNum);
        if (m_lRealPalyHandle.intValue() == -1) {
            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        if (!hCNetSDK.NET_DVR_PTZPreset(m_lRealPalyHandle, presetCmd, iPreset)) {
            log.error("set presetPoint fail, presetId：" + iPreset+", error code: "+hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        return true;
    }

    private NativeLong realPlay(int lChannel) {
        m_sClientInfo.lChannel = new NativeLong(lChannel);
        if (Objects.nonNull(Constant.maps.get("lUserID"))) {
            lUserIDLong = new NativeLong(Constant.maps.get("lUserID"));
        return hCNetSDK.NET_DVR_RealPlay_V30(lUserIDLong,
                m_sClientInfo, null, null, true);
        } else { return lUserIDLong;}
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

