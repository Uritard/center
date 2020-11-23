package com.yjh.accessvideo.module.control.service;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.commons.logs.Logs;
import com.yjh.accessvideo.hik.HCNetSDK;
import com.yjh.accessvideo.module.control.dao.CameraConDao;
import com.yjh.accessvideo.module.control.entity.CameraConInfo;
import com.yjh.accessvideo.module.control.entity.CameraStatusInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

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

    @Value("${nvr.rtmp.back}")
    private String UrlBackTem;

    private static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    private NativeLong m_lRealPlayHandle = new NativeLong(-1);// playhandle
    private NativeLong m_minsOne = new NativeLong(-1);
    private NativeLong lUserIDLong = new NativeLong(-1);
    private HCNetSDK.NET_DVR_CLIENTINFO m_sClientInfo = new HCNetSDK.NET_DVR_CLIENTINFO();	// play structure
    private HCNetSDK.NET_DVR_PREVIEWINFO dvr_previewinfo = new HCNetSDK.NET_DVR_PREVIEWINFO();


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
            int cameraType = cameraConInfo.getCameraType();
            int livePath;
            if (Constant.maps.get("livePath") != null) {
                livePath = Constant.maps.get("livePath")+1;
                Constant.maps.put("livePath", Constant.maps.get("livePath")+1);
            } else {
                livePath = 123;
                Constant.maps.put("livePath", 123);
            }
            log.info("Constant.maps: "+Constant.maps);
            log.info(userName+" "+password+" "+cameraIp+" "+cameraPort+" "+iChanNum+" "+cameraType+" "+livePath);
            String transUrl = "";
            if (cameraType==205) {
                transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,2, livePath);
            } else if (cameraType==206) {transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,1, livePath);}
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
            } else {
                String[] rtmpUrls = rtmpUrl.split("/");
                livePath = rtmpUrls[rtmpUrls.length-1];
            }
            log.info("livePath: "+livePath);
            String url = "ps -ef | grep ffmpeg | grep '"+ livePath +"' | grep -v 'grep'";
            Process processForId=Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
            processForId.waitFor();
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), "UTF-8"));
            String lineForId = null;
            StringBuilder dataBackForId = new StringBuilder();
            while ((lineForId = readerForId.readLine()) != null) {
                dataBackForId.append(lineForId).append('\n');
            }
            Integer processNum = Integer.parseInt(dataBackForId.substring(9,15).replace(" ",""));
            urlStop = "kill -9 "+processNum;
            Runtime.getRuntime().exec(urlStop);
        } catch (Exception e) {e.getMessage();}
        return "stop " + cameraId + " preview success!";
    }

    @Logs(title = "相机批量播放", code = "cameraPlay")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> batchStartRealPlay(String cameraIds) {
        List<Long> list = new ArrayList<>();
        String[] cameraIdArry = cameraIds.split(",");
        for (String aCameraIdArry : cameraIdArry) list.add(Long.valueOf(aCameraIdArry));
        List<Map<String, Object>> returnMapList = new ArrayList<>();
        try {
            List<CameraConInfo> cameraConInfoList = cameraConDao.batchSelectConInfo(list);
            for (CameraConInfo cameraConInfo: cameraConInfoList) {
                String userName = cameraConInfo.getUserName();
                String password = cameraConInfo.getPwd();
                String cameraIp = cameraConInfo.getRecordIp();
                int cameraPort = cameraConInfo.getRtspPort();
                int iChanNum = cameraConInfo.getChannelNum();
                int cameraType = cameraConInfo.getCameraType();
                int livePath;
                if (Objects.nonNull(Constant.maps.get("livePath"))) {
                    livePath = Constant.maps.get("livePath")+1;
                    Constant.maps.put("livePath", Constant.maps.get("livePath")+1);
                } else {
                    livePath = 123;
                    Constant.maps.put("livePath", 123);
                }
                log.info("Constant.maps: "+Constant.maps);
                log.info(userName+" "+password+" "+cameraIp+" "+cameraPort+" "+iChanNum+" "+cameraType+" "+livePath);
                String transUrl = "";
                if (cameraType==205) {
                    transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,2, livePath);
                } else if (cameraType==206) {transUrl = String.format(UrlTem, userName, password, cameraIp, cameraPort, iChanNum,1, livePath);}
                Runtime.getRuntime().exec(transUrl);
                String[] rtmpUrls = transUrl.split("rtmp");
                String rtmpUrl = "rtmp"+rtmpUrls[rtmpUrls.length-1];
                Map<String, Object> returnMap = new HashMap<>();
                returnMap.put("cameraId", String.valueOf(cameraConInfo.getCameraId()));
                returnMap.put("rtmpUrl", rtmpUrl);
                returnMapList.add(returnMap);
                Constant.mapsForCamera.put(String.valueOf(cameraConInfo.getCameraId()), rtmpUrl);
                log.info("mapsForCamera: "+Constant.mapsForCamera);
            }
        } catch (Exception e) {e.getMessage();}
        log.info("returnMapList: "+returnMapList);
        return returnMapList;
    }

    @Logs(title = "视频回放", code = "cameraPlayBack")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> startPlayBack(Long cameraId, Date startTime, Date stopTime) {
        Map<String, Object> returnMap = new HashMap<>();
        try {
            CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
            String userName = cameraConInfo.getUserName();
            String cameraIp = cameraConInfo.getRecordIp();
            String password = cameraConInfo.getPwd();
            int cameraPort = cameraConInfo.getRtspPort();
            int iChanNum = cameraConInfo.getChannelNum();
            int historyPath;
            if (Objects.nonNull(Constant.maps.get("historyPath"))) {
                historyPath = Constant.maps.get("historyPath")+1;
                Constant.maps.put("historyPath", Constant.maps.get("historyPath")+1);
            } else {
                historyPath = 123;
                Constant.maps.put("historyPath", 123);
            }
            log.info("Constant.maps: "+Constant.maps);
            int yearStart = startTime.getYear()+1900;
            int monthStart = startTime.getMonth()+1;
            int dateStart = startTime.getDate();
            int hourStart = startTime.getHours();
            int minuteStart = startTime.getMinutes();
            int secondStart = startTime.getSeconds();
            String starttime = yearStart+monthStart+dateStart+"T"+hourStart+minuteStart+secondStart+"Z";

            int yearStop = stopTime.getYear()+1900;
            int monthStop = stopTime.getMonth()+1;
            int dateStop = stopTime.getDate();
            int hourStop = stopTime.getHours();
            int minuteStop = stopTime.getMinutes();
            int secondStop = stopTime.getSeconds();
            String endtime = yearStop+monthStop+dateStop+"T"+hourStop+minuteStop+secondStop+"Z";
            ///usr/bin/ffmpeg -loglevel error -rtsp_transport tcp -i rtsp://%s:%s@%s:%s/Streaming/tracks/%s0%s?starttime=%s&endtime=%s -vcodec copy -an -f flv rtmp://192.168.9.40:1935/live/%s
            //rtsp://admin:hik12345@192.168.33.2:554/Streaming/tracks/101?starttime=20201111T095500Z&endtime=20201111T100005Z
            log.info("userName: "+userName+",password: "+password+",cameraIp: "+cameraIp+",cameraPort: " +cameraPort
                    +",iChanNum: "+iChanNum +",starttime: "+starttime+",endtime: "+endtime+",historyPath: "+historyPath);
            String transUrl = String.format(UrlBackTem, userName, password, cameraIp, cameraPort, iChanNum,1, starttime, endtime, historyPath);
            log.info("transUrl: "+transUrl);
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

    @Logs(title = "云台控制", code = "cameraControl")
    @Transactional(rollbackFor = Exception.class)
    public Object pTZControl(int dwPTZCommand, Long cameraId, int dStop, int speed) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        m_lRealPlayHandle = realPlay(iChanNum);
        if (m_lRealPlayHandle.intValue() == -1) {
            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
            return "fail";
        }
        if (dwPTZCommand==29) {
            return hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, dStop, speed);
        } else {
            if (hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, 0, speed)) {
                try {
                    Thread.sleep(200);
                } catch (Exception e) {e.getMessage();}
                hCNetSDK.NET_DVR_PTZControlWithSpeed(m_lRealPlayHandle, dwPTZCommand, 1, speed);
                return hCNetSDK.NET_DVR_StopRealPlay(m_lRealPlayHandle);
            } else { return "PTZ control fail, errorInfo: "+hCNetSDK.NET_DVR_GetLastError(); }
        }

    }

    @Logs(title = "相机抓图", code = "capturePicture")
    @Transactional(rollbackFor = Exception.class)
    public String capturePicture(String filePath, Long cameraId) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,null);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        NativeLong iChanNumLong = new NativeLong(iChanNum);
        HCNetSDK.NET_DVR_JPEGPARA lpJpegPara = new HCNetSDK.NET_DVR_JPEGPARA();
        lpJpegPara.wPicSize = 0xff;
        lpJpegPara.wPicQuality = 1;/* 图片质量系数 0-最好 1-较好 2-一般 */
        if (Objects.nonNull(Constant.maps.get("lUserID"))) {
            boolean capPictureResult = false;
            lUserIDLong = new NativeLong(Constant.maps.get("lUserID"));
            log.info("lUserIDLong: "+lUserIDLong);
            if (cameraConInfo.getCameraType()==206) {
                log.info("红外相机，特殊拍照");
                m_lRealPlayHandle = hCNetSDK.NET_DVR_RealPlay_V40(lUserIDLong, dvr_previewinfo, null, null);
                if (Objects.equals(m_lRealPlayHandle, m_minsOne)) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("capture picture fail, error code: "+iErr);
                    return "capture picture fail, error code: "+iErr;
                }
                int m_lRealPlayHandleIndex = hCNetSDK.NET_DVR_GetRealPlayerIndex(m_lRealPlayHandle);
                if (Objects.equals(m_lRealPlayHandleIndex, -1)) {
                    int iErr = hCNetSDK.NET_DVR_GetLastError();
                    log.error("capture picture fail, error code: "+iErr);
                    return "capture picture fail, error code: "+iErr;
                }
                //打开测温信息
                hCNetSDK.PlayM4_RenderPrivateData(iChanNum, 0x20, 1);
                hCNetSDK.PlayM4_RenderPrivateDataEx(iChanNum, 0x20, 7, 1);
                hCNetSDK.PlayM4_SetOverlayPriInfoFlag(iChanNum, 0x20, true);
                hCNetSDK.PlayM4_GetJPEG(iChanNum, lpJpegPara.getPointer(), lpJpegPara.wPicSize, lpJpegPara.wPicQuality);
                //关闭测温信息
                hCNetSDK.PlayM4_RenderPrivateData(iChanNum, 0x20, 0);
                hCNetSDK.PlayM4_RenderPrivateDataEx(iChanNum, 0x20, 7, 0);
            } else { capPictureResult = hCNetSDK.NET_DVR_CaptureJPEGPicture(lUserIDLong, iChanNumLong, lpJpegPara, filePath); }
            log.info("capPictureResult: "+capPictureResult);
            if (!capPictureResult) {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("capture picture fail, error code: "+iErr);
                return "capture picture fail, error code: "+iErr;
            } else { return "success"; }
        } else { return "userID is null"; }
    }

    @Logs(title = "预置点调用", code = "presetAction")
    @Transactional(rollbackFor = Exception.class)
    public boolean PresetAction(Long presetId, Long cameraId, int presetCmd) {
        CameraConInfo cameraConInfo = cameraConDao.selectConInfo(cameraId,presetId);
        int iChanNum = cameraConInfo.getChannelNum()+32;
        int iPreset = cameraConInfo.getPresetNum();
        m_lRealPlayHandle = realPlay(iChanNum);
        if (m_lRealPlayHandle.intValue() == -1) {
            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        if (!hCNetSDK.NET_DVR_PTZPreset(m_lRealPlayHandle, presetCmd, iPreset)) {
            log.error("set presetPoint fail, presetId：" + iPreset+", error code: "+hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        return true;
    }

    @Logs(title = "获取相机状态", code = "getCameraStatus")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> getCameraStatus(Long recordId) {
        List<CameraStatusInfo> cameraConInfoMap = cameraConDao.cameraInfoByNVR(recordId);
        log.info("cameraConInfoMap: "+cameraConInfoMap);
        NativeLong iChanNumTem = new NativeLong(0);
        Map<String, String> channleStatusMap = new HashMap<>();
        if (Objects.nonNull(Constant.maps.get("lUserID"))) {
            lUserIDLong = new NativeLong(Constant.maps.get("lUserID"));
            IntByReference intByReference = new IntByReference(0);
            HCNetSDK.NET_DVR_IPPARACFG m_strIpparaCfg = new HCNetSDK.NET_DVR_IPPARACFG();
            m_strIpparaCfg.write();
            Pointer m_strIpparaCfgPointer = m_strIpparaCfg.getPointer();
            if (!hCNetSDK.NET_DVR_GetDVRConfig(lUserIDLong, HCNetSDK.NET_DVR_GET_IPPARACFG, iChanNumTem, m_strIpparaCfgPointer, m_strIpparaCfg.size(), intByReference)) {
                int iErr = hCNetSDK.NET_DVR_GetLastError();
                log.error("get camera status fail, error code: "+iErr);
                channleStatusMap.put("get camera status fail, error code: ", String.valueOf(iErr));
                return channleStatusMap;
            }
            m_strIpparaCfg.read();
            //设备支持IP通道
            for(int iChannum =1; iChannum < HCNetSDK.MAX_IP_CHANNEL+1; iChannum++) {
                if (m_strIpparaCfg.struIPChanInfo[iChannum-1].byEnable == 1){
                    for (CameraStatusInfo cameraStatusInfo:cameraConInfoMap) {
                        if (Objects.equals(cameraStatusInfo.getChannelNum(), iChannum)) channleStatusMap.put(String.valueOf(cameraStatusInfo.getCameraId()), "1");
                    }
                }
                if (m_strIpparaCfg.struIPChanInfo[iChannum-1].byEnable == 0) {
                    for (CameraStatusInfo cameraStatusInfo:cameraConInfoMap) {
                        if (Objects.equals(cameraStatusInfo.getChannelNum(), iChannum)) channleStatusMap.put(String.valueOf(cameraStatusInfo.getCameraId()), "0");
                    }
                }

            }
            return channleStatusMap;
        }
        channleStatusMap.put("errorMessage: " ,"userID is null");
        return channleStatusMap;
    }

    private NativeLong realPlay(int lChannel) {
        m_sClientInfo.lChannel = new NativeLong(lChannel);
        if (Objects.nonNull(Constant.maps.get("lUserID"))) {
            lUserIDLong = new NativeLong(Constant.maps.get("lUserID"));
        return hCNetSDK.NET_DVR_RealPlay_V30(lUserIDLong,
                m_sClientInfo, null, null, true);
        } else { return lUserIDLong;}
    }

//    private boolean playBack() {
//        NET_DVR_VOD_PARA  struVoidParam = new NET_DVR_VOD_PARA();
//        struVoidParam.dwSize = struVoidParam.size();
//        struVoidParam.struBeginTime = struStartTime;
//        struVoidParam.struEndTime = struStopTime;
//        struVoidParam.hWnd = hwnd;
//        NET_DVR_STREAM_INFO struInfo = new NET_DVR_STREAM_INFO ();
//        struInfo.dwSize = struInfo.size();
//        struInfo.dwChannel = m_iChanShowNum;
//        struVoidParam.struIDInfo = struInfo;
//        if(RadioForward.isSelected()) {
//            m_lPlayHandle = hCNetSDK.NET_DVR_PlayBackByTime_V40(lUserIDLong, struVoidParam);
//        } else {
//            NET_DVR_PLAYCOND struPlayCond = new NET_DVR_PLAYCOND();
//            struPlayCond.dwChannel = m_iChanShowNum;
//            struPlayCond.struStartTime = struStartTime;
//            struPlayCond.struStopTime = struStopTime;
//            m_lPlayHandle = hCNetSDK.NET_DVR_PlayBackReverseByTime_V40(lUserIDLong,hwnd,struPlayCond);
//        }
//        if (m_lPlayHandle.intValue() == -1) {
//            log.error("preview fail,error code:" + hCNetSDK.NET_DVR_GetLastError());
//            return false;
//        } else { return true; }
//    }

}

